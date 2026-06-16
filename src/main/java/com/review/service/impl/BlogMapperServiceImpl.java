package com.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.review.mapper.BlogMapper;
import com.review.model.entity.Blog;
import com.review.service.BlogMapperService;
import org.springframework.stereotype.Service;

@Service
public class BlogMapperServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogMapperService{
}
