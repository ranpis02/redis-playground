package com.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.review.mapper.UserMapper;
import com.review.model.dto.LoginFormDTO;
import com.review.model.dto.UserDTO;
import com.review.model.entity.User;
import com.review.service.UserService;
import com.review.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.review.utils.RedisConstants.*;
import static com.review.utils.SystemConstants.*;// Static import all static field

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Verify the user's login information
     *
     * @param loginFormDTO The login information submitted by the user, include phone number and verification code
     * @return response content
     */
    @Override
    public R login(LoginFormDTO loginFormDTO) {
        String phone = loginFormDTO.getPhone();

        // Get the verification code from redis
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone
        );
        String requestCode = loginFormDTO.getCode();

        if(cacheCode == null || !cacheCode.equals(requestCode)) {
            return R.fail("Verification failed!");
        }

        // Query or create user
        User user = query().eq("phone", phone).one();

        if(user == null) {
            user = createUserWithPhone(phone);
        }

        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor(
                (FieldName, FieldValue) -> FieldValue.toString()
        ));

        // Store the user infotmatiom to the redis
        String tokenKey = LOGIN_USER_KEY + UUID.randomUUID();
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        // Set token expiration
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.SECONDS);
        return R.ok(tokenKey);
    }

    /**
     * When user pass the verification and are not in the member list, register the user automatically
     *
     * @param phone phone number of the unregistered user
     * @return the newly registered user
     */
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));

        save(user);
        return user;
    }
}
