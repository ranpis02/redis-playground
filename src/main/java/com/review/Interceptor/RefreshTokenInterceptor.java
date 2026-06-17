package com.review.Interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.review.model.dto.UserDTO;
import com.review.utils.RedisConstants;
import com.review.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.review.utils.RedisConstants.*;
import static com.review.utils.SystemConstants.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String auth = request.getHeader("Authorization");
        if(auth == null || !auth.startsWith(AUTHORIZATION_BEARER_PREFIX)) {
            return true;
        }

        String userKey = RedisConstants.LOGIN_USER_KEY + StrUtil.subAfter(auth, AUTHORIZATION_BEARER_PREFIX, false);
        // Get the User object
        Map<Object, Object> userObj = stringRedisTemplate.opsForHash().entries(userKey);

        if(userObj.isEmpty()) return true;

        // Convert the query object to the userDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userObj, new UserDTO(), false);

        // Store the user info into ThreadLocal for later use in this request
        UserHolder.set(userDTO);
        // Flush the expiration time of the token in the redis
        stringRedisTemplate.expire(userKey, LOGIN_USER_TTL, TimeUnit.SECONDS);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
                                @Nullable Exception ex) {
        // Free the ThreadLocal memory to prevent memory leaks
        UserHolder.remove();
    }
}
