package com.review.utils;

import com.review.model.dto.UserDTO;

/**
 * Manage the user login state
 */
public class UserHolder {

    /**
     * Caution:
     */
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void set(UserDTO userDTO) {
        tl.set(userDTO);
    }

    public static UserDTO get() {
        return tl.get();
    }

    public static void remove() {
        tl.remove();
    }

}
