package com.plantadoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.Result;
import com.plantadoption.dto.UserUpdateDTO;
import com.plantadoption.entity.User;
import com.plantadoption.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        User user = userService.getUserDetail(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String role) {
        
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.like(StringUtils.hasText(nickname), User::getNickname, nickname);
        wrapper.like(StringUtils.hasText(phone), User::getPhone, phone);
        wrapper.eq(departmentId != null, User::getDepartmentId, departmentId);
        wrapper.eq(StringUtils.hasText(role), User::getRole, role);
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> result = userService.page(page, wrapper);
        
        // 擦除敏感信息
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }
    
    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result<User> updateUserInfo(HttpServletRequest request, @Valid @RequestBody UserUpdateDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        User user = userService.updateUserInfo(userId, dto);
        user.setPassword(null);
        return Result.success("更新成功", user);
    }
    
    @Operation(summary = "获取用户领养统计")
    @GetMapping("/adoption/stats")
    public Result<Integer> getAdoptionStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        int count = userService.getAdoptedCount(userId);
        return Result.success(count);
    }
    
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserDetail(id);
        user.setPassword(null);
        user.setPhone(user.getPhone() != null ? user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : null);
        return Result.success(user);
    }
}
