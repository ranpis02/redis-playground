package com.review.model.dto;

import lombok.Data;

@Data
public class UserDTO {
    /**
     * User ID
     */
    private Long id;

    /**
     * User nickname
     */
    private String nickname;

    /**
     * User avatar URL
     */
    private String icon;
}
