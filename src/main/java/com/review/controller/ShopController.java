package com.review.controller;

import com.review.service.ShopService;
import com.review.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping("/query")
    public R queryById(@RequestParam Long id) {
        return shopService.queryById(id);
    }
}
