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
public class Shop implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Shop name
     */
    private String name;

    /**
     * Shop images
     */
    private String images;

    /**
     * Business district
     */
    private String area;

    /**
     * Shop address
     */
    private String address;

    /**
     * Shop location - longitude
     */
    private Double x;

    /**
     * Shop location - latitude
     */
    private Double y;

    /**
     * Average spending per customer (Long)
     */
    private Long avgPrice;

    /**
     * Sales volume
     */
    private Long sold;

    /**
     * Number of comments
     */
    private Long comments;

    /**
     * Shop rating score (1-5, stored as score * 10 to avoid decimals)
     */
    private Integer score;

    /**
     * Shop business hours
     */
    private String openHours;

    /**
     * Record creation time
     */
    private LocalDateTime createTime;

    /**
     * Record last update time
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Double distance;
}
