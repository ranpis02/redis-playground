package com.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.review.mapper.BlogMapper;
import com.review.model.dto.UserDTO;
import com.review.model.entity.Blog;
import com.review.model.entity.User;
import com.review.service.BlogService;
import com.review.service.UserService;
import com.review.utils.R;
import com.review.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.review.utils.SystemConstants.*;
import static com.review.utils.RedisConstants.*;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R queryBlogById(Long id) {
        // Query the blog
        Blog blog = getById(id);

        if(blog == null) {
            return R.fail("The blog does not exist.");
        }

        // Check if the user liked this blog
        queryUserByBlog(blog);
        isBlogLiked(blog);

        return R.ok(blog);
    }

    @Override
    public R queryHotBlog(Integer current) {
        Page<Blog> page = this.lambdaQuery()
                .orderByDesc(Blog::getLiked)
                .page(new Page<>(current, MAX_PAGE_SIZE));

        // Get the current page of blogs
        List<Blog> records = page.getRecords();

        records.forEach(
                blog -> {
                    this.queryUserByBlog(blog);
                    this.isBlogLiked(blog);
                }
        );

        return R.ok(records);
    }

    @Override
    public R likeBlog(Long id) {
        Long userId = UserHolder.get().getId();
        String blogKey = BLOG_LIKED_KEY_PREFIX + id;

        Double score = stringRedisTemplate.opsForZSet().score(blogKey, userId.toString());

        if(Objects.isNull(score)) {
            // If user has not liked the blog, increment the like count and add the user to Redis set
            boolean update = this.update(
                    new LambdaUpdateWrapper<Blog>()
                            .eq(Blog::getId, id)
                            .setSql("liked = liked + 1")
            );

            if(update) {
                // stringRedisTemplate.opsForSet().add(blogKey, userId.toString());
                stringRedisTemplate.opsForZSet().add(blogKey, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // If the user has already liked the blog, decrement the like count and remove the user from Redis set
            boolean update = this.update(
                    new LambdaUpdateWrapper<Blog>()
                            .eq(Blog::getId, id)
                            .setSql("liked = liked - 1")
            );
            if(update) {
                // stringRedisTemplate.opsForSet().remove(blogKey, userId.toString());
                stringRedisTemplate.opsForZSet().remove(blogKey, userId.toString());
            }
        }

        return R.ok();
    }

    /**
     * Get all users liked this blog
     *
     * @param id blog id
     * @return list of users liked this blog
     */
    @Override
    public R queryBlogLikesTop5(Long id) {
        String blogKey = BLOG_LIKED_KEY_PREFIX + id;

        Set<String> top5 = stringRedisTemplate.opsForZSet().reverseRange(blogKey, 0, 4);
        if(top5 == null || top5.isEmpty()) {
            return R.ok(Collections.emptyList());
        }

        // Ignore the sequence
        // List<Long> ids = top5.stream().map(Long::valueOf).toList();
        // List<UserDTO> userDTOList = userService.listByIds(ids).stream()
        //         .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
        //         .toList();
        // =================================================================
        List<Long> ids = top5.stream().map(Long::valueOf).toList();
        Map<Long, UserDTO> userMap = userService.listByIds(ids).stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toMap(UserDTO::getId, Function.identity()));

        List<UserDTO> userDTOList = ids.stream()
                .map(userMap::get)
                .toList();

        return R.ok(userDTOList);
    }

    /**
     * Query the user whose liked the blog and then merge the user's name and icon to the blog object
     */
    private void queryUserByBlog(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);

        blog.setName(user.getNickname());
        blog.setIcon(user.getIcon());
    }

    /**
     * Check if the current user liked the blog and set the isLike field of the blog object
     *
     * @param blog blog to be checked
     */
    private void isBlogLiked(Blog blog) {
        Long userId = UserHolder.get().getId();
        Long blogId = blog.getId();

        String blogKey = BLOG_LIKED_KEY_PREFIX + blogId;
        // Boolean isMember = stringRedisTemplate.opsForSet().isMember(blogKey, id.toString());
        // blog.setIsLike(Boolean.TRUE.equals(isMember));
        Double score = stringRedisTemplate.opsForZSet().score(blogKey, userId.toString());
        blog.setIsLike(Objects.nonNull(score));
    }
}
