package com.plantadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plantadoption.entity.Notification;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 发送通知
     */
    void sendNotification(Long userId, String title, String content, String type, Long relatedId, String relatedType);
    
    /**
     * 分页查询通知列表
     */
    IPage<Notification> pageNotifications(IPage<Notification> page, Long userId);
    
    /**
     * 标记通知为已读
     */
    void markAsRead(Long userId, Long notificationId);
    
    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 获取未读通知数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 删除通知
     */
    void deleteNotification(Long userId, Long notificationId);
}
