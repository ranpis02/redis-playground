package com.review.service.impl;

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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.review.utils.RedisConstants.*;

@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Autowired
    private SeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DefaultRedisScript<Long> unlockScript;

    @Autowired
    private RedissonClient redissonClient;

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
}
