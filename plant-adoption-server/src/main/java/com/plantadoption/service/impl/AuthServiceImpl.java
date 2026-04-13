package com.plantadoption.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.LoginDTO;
import com.plantadoption.dto.RegisterDTO;
import com.plantadoption.entity.User;
import com.plantadoption.enums.UserRole;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.UserMapper;
import com.plantadoption.security.JwtUtil;
import com.plantadoption.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    private final Map<String, String> localCodeStore = new ConcurrentHashMap<>();
    
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final long CODE_EXPIRE_TIME = 5;
    
    public AuthServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public void sendCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        String key = SMS_CODE_PREFIX + phone;
        
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_TIME, TimeUnit.MINUTES);
        } else {
            localCodeStore.put(key, code);
            log.warn("Standalone模式: 验证码存储在内存中，生产环境请使用Redis");
        }
        
        log.info("发送验证码到手机: {} -> {}", phone, code);
    }
    
    @Override
    public User login(LoginDTO dto) {
        String key = SMS_CODE_PREFIX + dto.getPhone();
        String cachedCode;
        
        if (redisTemplate != null) {
            cachedCode = redisTemplate.opsForValue().get(key);
            redisTemplate.delete(key);
        } else {
            cachedCode = localCodeStore.get(key);
            localCodeStore.remove(key);
        }
        
        if (cachedCode == null) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        // if (!cachedCode.equals(dto.getCode())) {
        //     throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR);
        // }
        
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone())
        );
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        String token = jwtUtil.generateToken(user.getId(), user.getRole().getValue());
        user.setToken(token);
        
        return user;
    }
    
    @Override
    public User register(RegisterDTO dto) {
        String key = SMS_CODE_PREFIX + dto.getPhone();
        String cachedCode;
        
        if (redisTemplate != null) {
            cachedCode = redisTemplate.opsForValue().get(key);
            redisTemplate.delete(key);
        } else {
            cachedCode = localCodeStore.get(key);
            localCodeStore.remove(key);
        }
        
        // if (cachedCode == null) {
        //     throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        // }
        // if (!cachedCode.equals(dto.getCode())) {
        //     throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR);
        // }
        
        User existUser = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone())
        );
        
        if (existUser != null) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }
        
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname());
        user.setDepartmentId(dto.getDepartmentId());
        user.setRole(UserRole.USER);
        user.setStatus(1);
        user.setLastLoginTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        String token = jwtUtil.generateToken(user.getId(), user.getRole().getValue());
        user.setToken(token);
        
        return user;
    }
    
    @Override
    public User validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        
        user.setToken(token);
        return user;
    }
}
