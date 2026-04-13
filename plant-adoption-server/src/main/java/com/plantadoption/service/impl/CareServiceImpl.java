package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.CareLogDTO;
import com.plantadoption.entity.CareLog;
import com.plantadoption.entity.Plant;
import com.plantadoption.entity.User;
import com.plantadoption.enums.CareType;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.CareLogMapper;
import com.plantadoption.mapper.PlantMapper;
import com.plantadoption.mapper.UserMapper;
import com.plantadoption.service.CareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 养护服务实现类
 */
@Service
@RequiredArgsConstructor
public class CareServiceImpl extends ServiceImpl<CareLogMapper, CareLog> implements CareService {
    
    private final PlantMapper plantMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public CareLog recordCare(Long userId, CareLogDTO dto) {
        Plant plant = plantMapper.selectById(dto.getPlantId());
        if (plant == null) {
            throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
        }
        
        User user = userMapper.selectById(userId);
        
        CareLog careLog = new CareLog();
        careLog.setPlantId(dto.getPlantId());
        careLog.setPlantName(plant.getName());
        careLog.setUserId(userId);
        careLog.setUserName(user.getNickname());
        careLog.setCareType(CareType.valueOf(dto.getCareType()));
        careLog.setCareTime(LocalDateTime.now());
        careLog.setDescription(dto.getDescription());
        careLog.setImages(dto.getImages());
        
        baseMapper.insert(careLog);
        
        plantMapper.incrementCareCount(dto.getPlantId());
        
        return careLog;
    }
    
    @Override
    public IPage<CareLog> pageCareLogs(IPage<CareLog> page, Long plantId, Long userId, String careType) {
        LambdaQueryWrapper<CareLog> wrapper = new LambdaQueryWrapper<>();
        
        if (plantId != null) {
            wrapper.eq(CareLog::getPlantId, plantId);
        }
        if (userId != null) {
            wrapper.eq(CareLog::getUserId, userId);
        }
        if (careType != null && !careType.isEmpty()) {
            wrapper.eq(CareLog::getCareType, CareType.valueOf(careType));
        }
        
        wrapper.orderByDesc(CareLog::getCareTime);
        
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public CareLog getCareLogDetail(Long id) {
        CareLog careLog = baseMapper.selectLogWithDetails(id);
        if (careLog == null) {
            throw new BusinessException(ErrorCode.CARE_LOG_NOT_FOUND);
        }
        return careLog;
    }
    
    @Override
    public void deleteCareLog(Long userId, Long id) {
        CareLog careLog = baseMapper.selectById(id);
        if (careLog == null) {
            throw new BusinessException(ErrorCode.CARE_LOG_NOT_FOUND);
        }
        
        if (!careLog.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        baseMapper.deleteById(id);
    }
}
