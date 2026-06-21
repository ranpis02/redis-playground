package com.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.review.model.entity.VoucherOrder;
import com.review.utils.R;

public interface VoucherOrderService extends IService<VoucherOrder> {

    /**
     * Naive seckill implementation without any concurrency optimization.
     * Serves as a baseline for comparing with optimized versions.
     *
     * @param id voucher ID
     * @return operation result
     */
    R grabSeckillVoucherNaive(Long id);

    /**
     * Grab the seckill voucher with one-per-user restriction.
     *
     * @param id voucher ID
     * @return operation result
     */
    R grabSeckillVoucherOneRestriction(Long id);

}
