package com.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.review.model.dto.LoginFormDTO;
import com.review.utils.R;
import com.review.model.entity.User;

public interface UserService extends IService<User> {

    /**
     * Verify the user's login information
     *
     * @param loginFormDTO The login information submitted by the user, include phone number and verification code
     * @return The login result
     */
    public R login(LoginFormDTO loginFormDTO);
}
