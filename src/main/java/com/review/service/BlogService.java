package com.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.review.model.entity.Blog;
import com.review.utils.R;

public interface BlogService extends IService<Blog> {
    /**
     * Query the blog by id
     *
     * @param id the blog id
     * @return the result of the query
     */
    R queryBlogById(Long id);
}
