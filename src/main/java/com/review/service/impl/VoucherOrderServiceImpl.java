package com.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.review.mapper.VoucherOrderMapper;
import com.review.model.entity.SeckillVoucher;
import com.review.model.entity.VoucherOrder;
import com.review.service.SeckillVoucherService;
import com.review.service.VoucherOrderService;
import com.review.utils.R;
import com.review.utils.RedisIdWorker;
import com.review.utils.UserHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.review.utils.RedisConstants.*;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Autowired
    private SeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // @Autowired
    // @Qualifier("unlockScript")
    // private DefaultRedisScript<Long> unlockScript;

    @Autowired
    @Qualifier("seckillScript")
    private DefaultRedisScript<Long> seckillScript;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    @Lazy
    private VoucherOrderServiceImpl proxy;

    private static final String QUEUE_NAME = "stream.orders";

    private static final String GROUP_NAME = "order_group";

    private static final String CONSUMER_NAME = "c1";

    private static final long MAX_RETRY = 3;

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private volatile boolean running = true;

    @PostConstruct
    private void init() {
        try {
            stringRedisTemplate.opsForStream().createGroup(
                    QUEUE_NAME,
                    ReadOffset.from("0"),  // consumer from the head
                    GROUP_NAME
            );
        } catch (Exception e) {
            if (isBusyGroupError(e)) {
                log.info("Consumer group already exists: {}", GROUP_NAME);
            } else {
                log.error("Failed to create Redis Stream consumer group", e);
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startConsumer() {
        SECKILL_ORDER_EXECUTOR.execute(new VoucherOrderHandler());
        log.info("Voucher order consumer thread started successfully after application is ready.");
    }

    @PreDestroy
    private void destroy() {
        running = false;
        SECKILL_ORDER_EXECUTOR.shutdownNow();
        try {
            if (!SECKILL_ORDER_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("SECKILL_ORDER_EXECUTOR did not terminate in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @Transactional
    public R grabSeckillVoucherNaive(Long id) {
        // Query the seckill voucher
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);

        // Check if the voucher is valid and has stock
        if (seckillVoucher == null) return R.fail("The voucher does not exist.");

        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return R.fail("The seckill event has not started yet.");
        }

        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return R.fail("The seckill event has ended.");
        }

        if (seckillVoucher.getStock() < 1) {
            return R.fail("The voucher is out of stock.");
        }

        // Add one-per-user restriction (Invalid in the concurrent scenario, but kept for demonstration)
        // long count = this.count(
        //         new LambdaQueryWrapper<VoucherOrder>().eq(VoucherOrder::getUserId, UserHolder.get().getId())
        // );
        // if(count > 0) {
        //     return R.fail("You have reached the purchase limit.");
        // }

        boolean isSuccess = seckillVoucherService.update(
                new LambdaUpdateWrapper<SeckillVoucher>()
                        .eq(SeckillVoucher::getVoucherId, id)
                        .gt(SeckillVoucher::getStock, 0)
                        .setSql("stock = stock - 1")
        );

        if (!isSuccess) {
            return R.fail("Failed to grab the voucher.");
        }

        // If grab successfully, then create a new voucher order
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIdWorker.nextId(SECKILL_VOUCHER_ORDER_PREFIX);
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(UserHolder.get().getId());
        voucherOrder.setVoucherId(id);

        boolean isCreated = save(voucherOrder);

        if (!isCreated) throw new RuntimeException("Failed to create voucher order.");

        return R.ok(orderId);
    }

    @Override
    public R grabSeckillVoucherOneRestriction(Long id) {
        Long userId = UserHolder.get().getId();

        // Per-user distributed lock to serialize order creation for the same user
        // SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate, unlockScript);
        // boolean isLocked = lock.tryLock(LOCK_TTL, TimeUnit.SECONDS);
        // if (!isLocked) {
        //     return R.fail("Too many requests, please try again.");
        // }
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
        if (seckillVoucher == null) return R.fail("The voucher does not exist.");

        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return R.fail("The seckill event has not started yet.");
        }

        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return R.fail("The seckill event has ended.");
        }

        if (seckillVoucher.getStock() < 1) {
            return R.fail("The voucher is out of stock.");
        }

        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + userId + ":" + id);

        try {
            if (!lock.tryLock(0, LOCK_TTL, TimeUnit.SECONDS)) {
                return R.fail("Too many requests, please try again.");
            }

            VoucherOrderServiceImpl currentProxy = (VoucherOrderServiceImpl) AopContext.currentProxy();
            return currentProxy.createOrder(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * Associate with the grabSeckillVoucherOneRestriction
     */
    @Transactional
    public R createOrder(Long voucherId) {
        Long userId = UserHolder.get().getId();

        long count = this.count(
                new LambdaQueryWrapper<VoucherOrder>()
                        .eq(VoucherOrder::getUserId, userId)
                        .eq(VoucherOrder::getVoucherId, voucherId)
        );
        if (count > 0) {
            return R.fail("You have reached the purchase limit.");
        }

        boolean isSuccess = seckillVoucherService.update(
                new LambdaUpdateWrapper<SeckillVoucher>()
                        .eq(SeckillVoucher::getVoucherId, voucherId)
                        .gt(SeckillVoucher::getStock, 0)
                        .setSql("stock = stock - 1")
        );
        if (!isSuccess) {
            return R.fail("Failed to grab the voucher.");
        }

        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIdWorker.nextId(SECKILL_VOUCHER_ORDER_PREFIX);
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        save(voucherOrder);

        return R.ok("Create order successfully. The order ID is: " + orderId);
    }

    @Override
    public R grabSeckillVoucherAsyncExecute(Long id) {
        Long userId = UserHolder.get().getId();
        long orderId = redisIdWorker.nextId(SECKILL_VOUCHER_ORDER_PREFIX);

        String stockKey = SECKILL_VOUCHER_STOCK_PREFIX + id;
        String orderKey = SECKILL_VOUCHER_ORDER_PREFIX + id + ":" + userId;

        long result = stringRedisTemplate.execute(
                seckillScript,
                List.of(QUEUE_NAME, stockKey, orderKey),
                userId.toString(),
                id.toString(),
                String.valueOf(orderId)
        );

        if (result == 1) {
            return R.fail("The voucher is out of stock.");
        }

        if (result == 2) {
            return R.fail("You have reached the purchase limit.");
        }

        return R.ok(orderId);
    }

    /**
     * Handle the order from the Redis Stream (include the ready messages and the pending messages), and then acknowledge them
     *
     * @param record the message to be processed
     */
    private void executeBusiness(MapRecord<String, Object, Object> record) {
        VoucherOrder order = BeanUtil.fillBeanWithMap(record.getValue(), new VoucherOrder(), true);

        handleVoucherOrder(order);

        stringRedisTemplate.opsForStream()
                .acknowledge(QUEUE_NAME, GROUP_NAME, record.getId());
    }

    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    List<MapRecord<String, Object, Object>> message = stringRedisTemplate.opsForStream().read(
                            Consumer.from(GROUP_NAME, CONSUMER_NAME),
                            StreamReadOptions.empty()
                                    .count(1)
                                    .block(Duration.ofSeconds(1)),
                            StreamOffset.create(QUEUE_NAME, ReadOffset.lastConsumed())
                    );

                    if (message == null || message.isEmpty()) {
                        continue;
                    }

                    // Take the oldest message from the list
                    MapRecord<String, Object, Object> record = message.get(0);
                    executeBusiness(record);
                } catch (Exception e) {
                    if (!running) break;
                    log.error("Error processing order, and then will be enrolled in the PEL compensation mechanism subsequently", e);
                    handlePendingList();
                }
            }
        }
    }

    /**
     * Handle the voucher order with Redisson distributed lock to prevent the message redelivery and the
     *
     * @param order Order to be processed
     */
    private void handleVoucherOrder(VoucherOrder order) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + order.getUserId() + ":" + order.getVoucherId());

        if (!lock.tryLock()) {
            throw new RuntimeException("Failed to acquire Redisson lock for user: " + order.getUserId());
        }

        try {
            // VoucherOrderServiceImpl proxy = (VoucherOrderServiceImpl) AopContext.currentProxy();
            proxy.createVoucherOrder(order);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Handle voucher order creation with Redisson distributed lock.
     * Core purpose of this lock:
     * 1. Ensure idempotency of order creation.
     *    - createVoucherOrder contains multiple non-atomic operations (e.g. stock validation, DB query, insert)
     *    - Without a lock, concurrent execution may lead to duplicate orders or inconsistent state.
     * 2. Prevent duplicate processing caused by message re-delivery in Redis Stream / MQ scenarios.
     *    - The same message may be consumed multiple times under retry or consumer failure conditions.
     * 3. Serialize per-user + per-voucher operations to avoid race conditions.
     *    - Ensures that only one order creation task for the same user and voucher can proceed at a time.
     * Lock granularity: userId + voucherId (fine-grained to reduce contention)
     *
     * @param order Order to be processed
     */
    @Transactional
    public void createVoucherOrder(VoucherOrder order) {
        // Database fallback check
        long count = this.count(
                new LambdaQueryWrapper<VoucherOrder>()
                        .eq(VoucherOrder::getUserId, order.getUserId())
                        .eq(VoucherOrder::getVoucherId, order.getVoucherId())
        );

        if (count > 0) return;

        // Update the stock in the database
        boolean isUpdate = seckillVoucherService.update(
                new LambdaUpdateWrapper<SeckillVoucher>()
                        .eq(SeckillVoucher::getVoucherId, order.getVoucherId())
                        .gt(SeckillVoucher::getStock, 0)
                        .setSql("stock = stock - 1")

        );

        if (!isUpdate) throw new RuntimeException("Failed to update stock, order creation failed.");

        save(order);
    }

    /**
     * Handle the pending list of the consumer's PEL (Pending Entries List) to process unacknowledged messages
     */
    private void handlePendingList() {
        while (true) {
            try {
                // Read the oldest entry in the consumer's PEL
                List<MapRecord<String, Object, Object>> pendingMessages = stringRedisTemplate.opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(QUEUE_NAME, ReadOffset.from("0"))
                );

                // If there is no pending message, then break the loop
                if (pendingMessages == null || pendingMessages.isEmpty()) {
                    break;
                }

                MapRecord<String, Object, Object> record = pendingMessages.get(0);
                RecordId messageId = record.getId();

                if (isPoisonMessage(messageId)) {
                    log.error("Found a poison message, message id: {}", messageId);
                    stringRedisTemplate.opsForStream().acknowledge(QUEUE_NAME, GROUP_NAME, messageId);
                    continue;
                }

                executeBusiness(record);
            } catch (Exception e) {
                log.error("Error processing pending message, retry after a brief sleep", e);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // throw new RuntimeException(ex);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Check if the message is a poison message (i.e., has been retried more than MAX_RETRY times)
     *
     * @param messageId message id
     * @return true if the message is a poison message,false otherwise
     */
    private boolean isPoisonMessage(RecordId messageId) {
        try {
            PendingMessages pendingMessages = stringRedisTemplate.opsForStream().pending(
                    QUEUE_NAME,
                    Consumer.from(GROUP_NAME, CONSUMER_NAME),
                    Range.closed(messageId.getValue(), messageId.getValue()),
                    1
            );

            if (!pendingMessages.isEmpty()) {
                PendingMessage pendingMessage = pendingMessages.get(0);

                return pendingMessage.getTotalDeliveryCount() > MAX_RETRY;
            }
        } catch (Exception e) {
            log.error("Failed to check the dead letter, message id: {}", messageId, e);
        }

        return false;
    }


    private boolean isBusyGroupError(Throwable e) {
        while (e != null) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }
}
