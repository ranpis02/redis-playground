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
import com.review.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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
        if (!Validator.isMobile(phone)) {
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

        if (cacheCode == null || !cacheCode.equals(requestCode)) {
            return R.fail("Verification failed!");
        }

        // Query or create user
        User user = query().eq("phone", phone).one();

        if (user == null) {
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

    @Override
    public R sign() {
        Long userId = UserHolder.get().getId();

        LocalDateTime now = LocalDateTime.now();

        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY_PREFIX + userId + keySuffix;
        int dayOfMonth = now.getDayOfMonth();

        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return R.ok();
    }

    @Override
    public R signCount() {
        Long userId = UserHolder.get().getId();

        LocalDateTime now = LocalDateTime.now();

        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY_PREFIX + userId + keySuffix;
        int dayOfMonth = now.getDayOfMonth();

        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );

        if (result == null || result.isEmpty()) {
            return R.ok(0);
        }

        Long num = result.get(0);
        if (num == null || num == 0) {
            return R.ok(0);
        }

        int count = 0;
        while ((num & 1) != 0) {
            count++;
            num >>>= 1;
        }
        return R.ok(count);
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
