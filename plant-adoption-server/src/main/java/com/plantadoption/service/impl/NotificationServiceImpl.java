package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.entity.Notification;
import com.plantadoption.enums.NotificationType;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.NotificationMapper;
import com.plantadoption.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 通知服务实现类
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    @Override
    public void sendNotification(Long userId, String title, String content, String type, Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(NotificationType.valueOf(type));
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(0);
        
        baseMapper.insert(notification);
    }
    
    @Override
    public IPage<Notification> pageNotifications(IPage<Notification> page, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.orderByDesc(Notification::getCreateTime);
        
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = baseMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        baseMapper.updateById(notification);
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        baseMapper.markAllAsRead(userId);
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.countUnreadByUserId(userId);
    }
    
    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = baseMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        baseMapper.deleteById(notificationId);
    }
}
