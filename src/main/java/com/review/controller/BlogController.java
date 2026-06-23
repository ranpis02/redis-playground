package com.review.controller;

import com.review.service.BlogService;
import com.review.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Autowired
    private BlogService blogService;

    @GetMapping("/{id}")
    public R queryBlog(@PathVariable Long id) {
        return blogService.queryBlogById(id);
    }

    @GetMapping("/hot")
    public R queryHotBlog(@RequestParam(defaultValue = "1") Integer pageNo) {
        return blogService.queryHotBlog(pageNo);
    }

    @PostMapping("/{id}/like")
    public R likeBlog(@PathVariable Long id) {
        return blogService.likeBlog(id);
    }

    @GetMapping("/{id}/likes/top5")
    public R queryTop5(@PathVariable Long id) {
        return blogService.queryBlogLikesTop5(id);
    }
}
