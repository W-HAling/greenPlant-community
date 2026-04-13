package com.plantadoption.service;

import com.plantadoption.dto.LoginDTO;
import com.plantadoption.dto.RegisterDTO;
import com.plantadoption.entity.User;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 发送验证码
     */
    void sendCode(String phone);
    
    /**
     * 用户登录
     */
    User login(LoginDTO dto);
    
    /**
     * 用户注册
     */
    User register(RegisterDTO dto);
    
    /**
     * 验证Token
     */
    User validateToken(String token);
}
