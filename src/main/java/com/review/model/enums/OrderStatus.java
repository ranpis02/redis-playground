package com.review.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    UNPAID(1),
    PAID(2),
    REDEEMED(3),
    CANCELED(4),
    REFUNDING(5),
    REFUNDED(6);

    @EnumValue
    private final Integer code;
}
