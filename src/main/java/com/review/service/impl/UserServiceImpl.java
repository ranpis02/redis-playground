package com.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.review.mapper.UserMapper;
import com.review.model.dto.LoginFormDTO;
import com.review.model.dto.UserDTO;
import com.review.model.entity.User;
import com.review.service.UserService;
import com.review.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.review.utils.RedisConstants.*;
import static com.review.utils.SystemConstants.*;// Static import all static field

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R sendCode(String phone) {
        if(!Validator.isMobile(phone)) {
            return R.fail("Invalid phone number format!");
        }

        String code = RandomUtil.randomNumbers(6);

        stringRedisTemplate.opsForValue().set(LOGIN_CODE_PREFIX + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        log.info("Verification code sent successfully! Code: {}", code);

        return R.ok();
    }

    @Override
    public R login(LoginFormDTO loginFormDTO) {
        String phone = loginFormDTO.getPhone();

        // Get the verification code from redis
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_PREFIX + phone);
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
                (fieldName, fieldValue) -> Objects.toString(fieldValue, "")
        ));

        // Store the user infotmatiom to the redis
        String tokenKey = LOGIN_USER_PREFIX + UUID.randomUUID();
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        // Set token expiration
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.HOURS);
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
        user.setNickname(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));

        save(user);
        return user;
    }
}
