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
     * Voucher ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Shop ID
     */
    private Long shopId;

    /**
     * Voucher title
     */
    private String title;

    /**
     * Voucher subtitle
     */
    private String subTitle;

    /**
     * Voucher usage rules
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
     * Voucher type
     */
    private Integer type;

    /**
     * Voucher status
     */
    private Integer status;

    /**
     * Voucher stock
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
