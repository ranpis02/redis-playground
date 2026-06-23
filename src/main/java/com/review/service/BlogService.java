package com.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.review.model.entity.Blog;
import com.review.utils.R;

public interface BlogService extends IService<Blog> {
    /**
     * Query the blog by id
     *
     * @param id blog id
     * @return result of the query
     */
    R queryBlogById(Long id);

    /**
     * Query the hot blogs
     * @param current current page number
     * @return result of the query
     */
    R queryHotBlog(Integer current);

    /**
     * Like the blog
     *
     * @param id the blog id
     * @return result of the operation
     */
    R likeBlog(Long id);

    R queryBlogLikesTop5(Long id);
}
