package com.review.model.dto;

import lombok.Data;

@Data
public class LoginFormDTO {

    /**
     * Phone number
     */
    private String phone;

    /**
     * Verification code
     */
    private String code;

    /**
     * Login password
     */
    private String password;
}
