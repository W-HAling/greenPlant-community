package com.plantadoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.plantadoption.dto.UserUpdateDTO;
import com.plantadoption.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据手机号查询用户
     */
    User getByPhone(String phone);
    
    /**
     * 更新用户信息
     */
    User updateUserInfo(Long userId, UserUpdateDTO dto);
    
    /**
     * 获取用户详情（含部门信息）
     */
    User getUserDetail(Long userId);
    
    /**
     * 获取用户领养统计
     */
    int getAdoptedCount(Long userId);
}
