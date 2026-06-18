package com.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.review.model.dto.LoginFormDTO;
import com.review.utils.R;
import com.review.model.entity.User;

public interface UserService extends IService<User> {

    /**
     * Send verification code to the given phone number
     *
     * @param phone target phone number
     * @return result indicating whether the code was sent
     */
    R sendCode(String phone);

    /**
     * Verify the login credentials and authenticate the user.
     * If the user does not exist, auto-register a new account.
     *
     * @param loginFormDTO login credentials containing phone number and verification code
     * @return login result containing the token on success
     */
    R login(LoginFormDTO loginFormDTO);

}
