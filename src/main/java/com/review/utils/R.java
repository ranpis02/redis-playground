package com.review.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R {

    /**
     * Whether the request is successful or not
     */
    private Boolean success;

    /**
     * Error message when the request fails
     */
    private String errorMsg;

    /**
     * The actual business data returned to the client
     */
    private Object data;

    /**
     * Total number of records
     */
    private Long total;

    public static R ok(){
        return new R(true, null, null, null);
    }

    public static R ok(Object data){
        return new R(true, null, data, null);
    }

    public static R ok(List<?> data, Long total){
        return new R(true, null, data, total);
    }

    public static R fail(String errorMsg){
        return new R(false, errorMsg, null, null);
    }
}
