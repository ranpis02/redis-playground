package com.review.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.review.model.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class VoucherOrder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Order user ID
     */
    private Long userId;

    /**
     * VoucherService ID
     */
    private Long voucherId;

    /**
     * Payment method: 1 - Balance pay, 2 - AliPay, 3 - WeChat Pay
     */
    private Integer payType;

    /**
     * Order status:
     * 1 - unpaid
     * 2 - paid
     * 3 - Redeemed
     * 4 - Canceled
     * 5 - Refunding
     * 6 - Refunded
     */
    private OrderStatus status;

    /**
     * Order time
     */
    private LocalDateTime createTime;

    /**
     * Pay time
     */
    private LocalDateTime payTime;

    /**
     * Redeemed time
     */
    private LocalDateTime useTime;

    /**
     * Refunded time
     */
    private LocalDateTime refundTime;

    /**
     * Last update time
     */
    private LocalDateTime updateTime;
}
