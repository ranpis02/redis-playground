package com.review.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisIdWorkerTest {

    @Autowired
    private RedisIdWorker redisIdWorker;

    private static final long BEGIN_TIMESTAMP = 1781952107L;
    private static final int COUNT_BITS = 32;

    @Test
    void testNextIdReturnsPositive() {
        long id = redisIdWorker.nextId("order");
        assertTrue(id > 0, "Generated ID should be positive");
    }

    @Test
    void testNextIdIsMonotonicallyIncreasing() {
        int n = 1000;
        long prev = redisIdWorker.nextId("order");
        for (int i = 0; i < n; i++) {
            long cur = redisIdWorker.nextId("order");
            assertTrue(cur > prev,
                    "ID should be monotonically increasing: prev=" + prev + ", cur=" + cur);
            prev = cur;
        }
    }

    // @Test
    // void testTimestampPartIsCorrect() {
    //     long id = redisIdWorker.nextId("order");
    //     long timestamp = id >> COUNT_BITS;
    //     long nowSecond = System.currentTimeMillis() / 1000;
    //     long expectedTimestamp = nowSecond - BEGIN_TIMESTAMP;
    //     // Allow 5 seconds of tolerance (test execution time)
    //     assertTrue(Math.abs(timestamp - expectedTimestamp) <= 5,
    //             "Timestamp part should be close to current time, expected ~"
    //                     + expectedTimestamp + " but got " + timestamp);
    // }

    @Test
    void testDifferentPrefixesGenerateIndependentIds() {
        long id1 = redisIdWorker.nextId("order");
        long id2 = redisIdWorker.nextId("payment");
        long id3 = redisIdWorker.nextId("user");
        assertTrue(id1 > 0 && id2 > 0 && id3 > 0, "All IDs should be positive");
    }

    @Test
    void testNoDuplicateIdsInBatch() {
        int n = 5000;
        Set<Long> ids = new HashSet<>(n);
        for (int i = 0; i < n; i++) {
            long id = redisIdWorker.nextId("order");
            assertTrue(ids.add(id), "Duplicate ID found: " + id);
        }
        assertEquals(n, ids.size(), "Should have " + n + " unique IDs");
    }

    @Test
    void testConcurrentIdGeneration() throws InterruptedException {
        int threadCount = 10;
        int idsPerThread = 500;
        int totalIds = threadCount * idsPerThread;

        Set<Long> ids = new HashSet<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < idsPerThread; i++) {
                        synchronized (ids) {
                            ids.add(redisIdWorker.nextId("order"));
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        assertEquals(totalIds, ids.size(),
                "Should generate " + totalIds + " unique IDs under concurrent access");
    }

    @Test
    void testSequenceIncrementsWithinSameSecond() {
        long id1 = redisIdWorker.nextId("order");
        long id2 = redisIdWorker.nextId("order");
        long id3 = redisIdWorker.nextId("order");

        long seq1 = id1 & ((1L << COUNT_BITS) - 1);
        long seq2 = id2 & ((1L << COUNT_BITS) - 1);
        long seq3 = id3 & ((1L << COUNT_BITS) - 1);

        assertTrue(seq2 == seq1 + 1 || seq2 > seq1,
                "Sequence should increment: " + seq1 + " -> " + seq2);
        assertTrue(seq3 == seq2 + 1 || seq3 > seq2,
                "Sequence should increment: " + seq2 + " -> " + seq3);
    }
}
