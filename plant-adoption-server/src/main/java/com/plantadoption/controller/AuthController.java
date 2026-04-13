package com.plantadoption.controller;

import com.plantadoption.common.Result;
import com.plantadoption.dto.LoginDTO;
import com.plantadoption.dto.RegisterDTO;
import com.plantadoption.entity.User;
import com.plantadoption.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "登录、注册、验证码相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "发送验证码")
    @PostMapping("/code")
    public Result<Void> sendCode(@RequestParam String phone) {
        authService.sendCode(phone);
        return Result.success();
    }
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<User> login(@Valid @RequestBody LoginDTO dto) {
        User user = authService.login(dto);
        return Result.success("登录成功", user);
    }
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterDTO dto) {
        User user = authService.register(dto);
        return Result.success("注册成功", user);
    }
}
