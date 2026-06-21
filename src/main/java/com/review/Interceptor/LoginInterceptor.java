package com.review.Interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.utils.R;
import com.review.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

import static com.review.utils.StatusCodeConstants.*;

public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (UserHolder.get() == null) {
            response.setStatus(UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getOutputStream()
                    .write(MAPPER.writeValueAsString(R.fail("Unauthorized")).getBytes(StandardCharsets.UTF_8));
            return false;
        }

        return true;
    }

}
