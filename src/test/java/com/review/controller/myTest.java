package com.review.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.review.model.dto.UserDTO;
import com.review.utils.R;
import com.review.model.dto.LoginFormDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.review.utils.RedisConstants.LOGIN_CODE_PREFIX;

/**
 * 自由测试模板 —— 可在此随意编写测试代码
 */
@SpringBootTest
public class myTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // ========== Redis 操作示例 ==========

    @Test
    public void redisTest() {
        // 写入 Redis
        stringRedisTemplate.opsForValue().set("test:key", "hello-redis");

        // 读取 Redis
        String value = stringRedisTemplate.opsForValue().get("test:key");
        System.out.println("Redis value = " + value);
    }

    @Test
    public void loginCodeTest() {
        // 模拟：先存一个验证码到 Redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_PREFIX + "13800138000", "666666");

        // 模拟：从 Redis 取出的验证码
        String storedCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_PREFIX + "13800138000");
        System.out.println("storedCode = " + storedCode);

        // 模拟：用户传入的验证码
        LoginFormDTO dto = new LoginFormDTO();
        dto.setPhone("13800138000");
        dto.setCode("666666");
        String inputCode = dto.getCode();
        System.out.println("inputCode = " + inputCode);

        // 比对
        boolean match = storedCode != null && storedCode.equals(inputCode);
        System.out.println("match = " + match);
    }

    @Test
    public void uuidTest() {
        UUID uuid = UUID.randomUUID();
        System.out.println("");

    }

    /**
     * hutool 工具包的未知调用问题
     */
    @Test
    public void huToolTest() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(12L);
        // userDTO.setNickname("TestUser");

        // CopyOptions options = CopyOptions.create();
        // options.setIgnoreNullValue(true);
        // options.setFieldValueEditor((fieldName, fieldValue) -> {
        //     System.out.println(fieldName + " = " + fieldValue);
        //     return fieldValue.toString();
        // });
        //
        // Map<String, Object> map = BeanUtil.beanToMap(userDTO, new HashMap<>(), options);
    }

    @Test
    public void localDateTest() {
        LocalDateTime now = LocalDateTime.now();

        System.out.println(now.plusSeconds(60));
    }

}
