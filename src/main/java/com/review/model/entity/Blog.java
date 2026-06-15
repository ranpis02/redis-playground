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
public class Blog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Blog ID (primary key)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Shop ID
     */
    private Long shopId;

    /**
     * User ID
     */
    private Long userId;

    /**
     * User logo
     */
    @TableField(exist = false)
    private String icon;

    /**
     * Username of the blogger
     */
    @TableField(exist = false)
    private String name;

    /**
     * Whether like or not
     */
    @TableField(exist = false)
    private Boolean isLike;

    /**
     * Blog title
     */
    private String title;

    /**
     * Blog images (comma-separated URLs)
     */
    private String images;

    /**
     *  Blog content
     */
    private String content;

    /**
     * Number of likes
     */
    private Integer liked;

    /**
     * Number of comments
     */
    private Integer comments;

    /**
     * Creation time
     */
    private LocalDateTime createTime;

    /**
     * Last update time
     */
    private LocalDateTime updateTime;
}
