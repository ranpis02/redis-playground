package com.review.controller;

import com.review.model.dto.LoginFormDTO;
import com.review.service.UserService;
import com.review.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/code")
    public R sendCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    @PostMapping("/login")
    public R login(@RequestBody LoginFormDTO loginForm) {
        return userService.login(loginForm);
    }

}
