package com.review.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SeckillVoucher implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * VoucherService ID
     */
    @TableId(type = IdType.INPUT)
    private Long voucherId;

    /**
     * Product stock
     */
    private Integer stock;

    /**
     * Create time of seckill event
     */
    private LocalDateTime createTime;

    /**
     * Start time of seckill event
     */
    private LocalDateTime beginTime;

    /**
     * End time of seckill event
     */
    private LocalDateTime endTime;

    /**
     * Update time of seckill event
     */
    private LocalDateTime updateTime;

}
