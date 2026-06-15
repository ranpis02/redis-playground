package com.review.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ShopType implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Type of the shop
     */
    private String name;

    /**
     * Icon of the shop
     */
    private String icon;

    /**
     * Sort order
     */
    private Integer sort;

    /**
     * Creation time
     */
    @JsonIgnore
    private LocalDateTime createTime;

    /**
     * Last update time
     */
    @JsonIgnore
    private LocalDateTime updateTime;
}
