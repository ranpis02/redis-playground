package com.review.Interceptor;

import com.review.model.dto.UserDTO;
import com.review.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        HttpSession session = request.getSession();

        // Get the user in the session
        Object user = session.getAttribute("user");

        // If User does not exist, then intercepts
        if(user == null) {
            response.setStatus(401); // Unauthorized
            return false;
        }

        // If user exists, then put the user into ThreadLocal for later use
        UserHolder.set((UserDTO) user);
        return true;
    }

}
