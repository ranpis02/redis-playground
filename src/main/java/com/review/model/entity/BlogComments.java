package com.review.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Accessors(chain = true): This annotation allows for chainable setter methods.
 * For example, you can create a new BlogComments object by
 * BlogComments comment = new BlogComments().setUserId(123L).setBlogId(456L);
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BlogComments implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Comment ID
     */
    private Integer id;

    /**
     * Commenter ID
     */
    private Long userId;

    /**
     * Blog ID
     */
    private Long blogId;

    /**
     * ID of the associated top-level comment (If this comment is a top-level comment, then the value is set to 0)
     */
    private Long parentId;

    /**
     * ID of the reply comment
     */
    private Long answerId;

    /**
     * Comment content
     */
    private String content;

    /**
     * Number of likes for the comment
     */
    private Integer liked;

    /**
     * Comment status (0 for normal, 1 for reported, 2 for not visible)
     */
    private Boolean status;

    /**
     * Create time of the comment
     */
    private LocalDateTime createTime;

    /**
     * Update time of the comment
     */
    private LocalDateTime updateTime;
}
