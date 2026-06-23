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

    @GetMapping("/query")
    public R queryBlog(@RequestParam Long id) {
        return blogService.queryBlogById(id);
    }

    @GetMapping("/query-page")
    public R queryHotBlog(@RequestParam Integer pageNo) {
        return blogService.queryHotBlog(pageNo);
    }

    @PostMapping("/like")
    public R likeBlog(@RequestParam Long id) {
        return blogService.likeBlog(id);
    }

    @GetMapping("/query-likes-top5")
    public R queryTop5(@RequestParam Long id) {
        return blogService.queryBlogLikesTop5(id);
    }
}
