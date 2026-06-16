package com.review.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class Voucher implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * VoucherService ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Shop ID
     */
    private Long shopId;

    /**
     * VoucherService title
     */
    private String title;

    /**
     * VoucherService subtitle
     */
    private String subTitle;

    /**
     * VoucherService usage rules
     */
    private Long rules;

    /**
     * Payment amount
     */
    private Long payValue;

    /**
     * Discount amount
     */
    private Long actualValue;

    /**
     * VoucherService type
     */
    private Integer type;

    /**
     * VoucherService status
     */
    private Integer status;

    /**
     * VoucherService stock
     */
    @TableField(exist = false)
    private Integer stock;

    /**
     * Effective time
     */
    @TableField(exist = false)
    private LocalDateTime beginTime;

    /**
     * Expiration time
     */
    @TableField(exist = false)
    private LocalDateTime endTime;

    /**
     * Creation time
     */
    private LocalDateTime createTime;

    /**
     * Last update time
     */
    private LocalDateTime updateTime;
}
