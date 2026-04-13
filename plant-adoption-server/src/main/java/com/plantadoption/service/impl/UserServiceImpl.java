package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.UserUpdateDTO;
import com.plantadoption.entity.User;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.UserMapper;
import com.plantadoption.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    public User getByPhone(String phone) {
        return baseMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
        );
    }
    
    @Override
    public User updateUserInfo(Long userId, UserUpdateDTO dto) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getDepartmentId() != null) {
            user.setDepartmentId(dto.getDepartmentId());
        }
        
        baseMapper.updateById(user);
        return baseMapper.selectUserWithDepartment(userId);
    }
    
    @Override
    public User getUserDetail(Long userId) {
        User user = baseMapper.selectUserWithDepartment(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
    
    @Override
    public int getAdoptedCount(Long userId) {
        return baseMapper.countAdoptedPlantsByUserId(userId);
    }
}
