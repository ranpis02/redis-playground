package com.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.review.mapper.BlogMapper;
import com.review.model.entity.Blog;
import com.review.model.entity.User;
import com.review.service.BlogService;
import com.review.service.UserService;
import com.review.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private UserService userService;

    @Override
    public R queryBlogById(Long id) {
        // Query the blog
        Blog blog = getById(id);

        if(blog == null) {
            return R.fail("The blog does not exist.");
        }



    }

    /**
     * Query the user by blog and then set the user's name and icon to the blog
     */
    private void queryUserByBlog(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);

        blog.setName(user.getNickname());
        blog.setIcon(user.getIcon());
    }
}
