package com.review.Interceptor;

import com.review.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import static com.review.utils.StatusCodeConstants.*;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // If user is not logged in, reject with 401
        if(UserHolder.get() == null) {
            response.setStatus(UNAUTHORIZED);

            return false;
        }

        // If user is logged in, then proceed
        return true;


        // HttpSession session = request.getSession();

        // // Get the user in the session
        // Object user = session.getAttribute("user");
        //
        // // If User does not exist, then intercepts
        // if(user == null) {
        //     response.setStatus(401); // Unauthorized
        //     return false;
        // }
        //
        // // If user exists, then put the user into ThreadLocal for later use
        // UserHolder.set((UserDTO) user);
        // return true;
    }

}
