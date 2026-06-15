package com.review.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User ID
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * User city
     */
    private String city;

    /**
     * User Bio
     */
    private String introduce;

    /**
     * Number of fans
     */
    private Integer fans;

    /**
     * Gender(0: male, 1: female)
     */
    private Integer gender;

    /**
     * Birthday of the user
     */
    private LocalDate birthday;

    /**
     * Number of credits
     */
    private Integer credits;

    /**
     * VIP level (0-9, 0 means no VIP, 9 means highest VIP)
     */
    private Integer level;

    /**
     * Creation time
     */
    private LocalDateTime createTime;

    /**
     * Last update time
     */
    private LocalDateTime updateTime;

}
