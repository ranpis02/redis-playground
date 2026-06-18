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
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Password (stored as a hash)
     */
    private String password;

    /**
     * User nickname
     */
    private String nickname;

    /**
     * User avatar
     */
    private String icon;

    /**
     * Creation time
     */
    private LocalDateTime createTime;

    /**
     * Last update time
     */
    private LocalDateTime updateTime;
}
