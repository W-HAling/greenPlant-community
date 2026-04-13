package com.plantadoption.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.Result;
import com.plantadoption.entity.Notification;
import com.plantadoption.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@Tag(name = "通知管理", description = "系统通知相关接口")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @Operation(summary = "分页查询通知列表")
    @GetMapping("/list")
    public Result<IPage<Notification>> pageNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long userId = (Long) request.getAttribute("currentUserId");
        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> result = notificationService.pageNotifications(page, userId);
        return Result.success(result);
    }
    
    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        notificationService.markAsRead(userId, id);
        return Result.success();
    }
    
    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
    
    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        int count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        notificationService.deleteNotification(userId, id);
        return Result.success();
    }
}
