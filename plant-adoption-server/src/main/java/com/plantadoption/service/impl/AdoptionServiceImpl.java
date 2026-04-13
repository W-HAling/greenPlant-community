package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.AdoptionApplyDTO;
import com.plantadoption.dto.AdoptionApproveDTO;
import com.plantadoption.entity.AdoptionRecord;
import com.plantadoption.entity.Plant;
import com.plantadoption.entity.User;
import com.plantadoption.enums.AdoptionStatus;
import com.plantadoption.enums.PlantStatus;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.AdoptionRecordMapper;
import com.plantadoption.mapper.PlantMapper;
import com.plantadoption.mapper.UserMapper;
import com.plantadoption.service.AdoptionService;
import com.plantadoption.service.CarePlanService;
import com.plantadoption.service.NotificationService;
import com.plantadoption.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 领养服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl extends ServiceImpl<AdoptionRecordMapper, AdoptionRecord> implements AdoptionService {
    
    private final PlantMapper plantMapper;
    private final UserMapper userMapper;
    private final SysConfigService sysConfigService;
    private final NotificationService notificationService;
    private final CarePlanService carePlanService;
    
    @Autowired(required = false)
    private RedissonClient redissonClient;
    
    private final ConcurrentHashMap<Long, ReentrantLock> localLocks = new ConcurrentHashMap<>();
    
    @Override
    @Transactional
    public AdoptionRecord apply(Long userId, AdoptionApplyDTO dto) {
        if (redissonClient != null) {
            return applyWithDistributedLock(userId, dto);
        } else {
            return applyWithLocalLock(userId, dto);
        }
    }
    
    private AdoptionRecord applyWithDistributedLock(Long userId, AdoptionApplyDTO dto) {
        String lockKey = "lock:plant:adopt:" + dto.getPlantId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            
            return doApply(userId, dto);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    private AdoptionRecord applyWithLocalLock(Long userId, AdoptionApplyDTO dto) {
        ReentrantLock lock = localLocks.computeIfAbsent(dto.getPlantId(), k -> new ReentrantLock());
        
        try {
            boolean acquired = lock.tryLock(10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            
            return doApply(userId, dto);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    private AdoptionRecord doApply(Long userId, AdoptionApplyDTO dto) {
        Plant plant = plantMapper.selectById(dto.getPlantId());
        if (plant == null) {
            throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
        }
        
        if (plant.getStatus() != PlantStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.PLANT_NOT_AVAILABLE);
        }
        
        if (!canAdopt(userId)) {
            throw new BusinessException(ErrorCode.ADOPTION_LIMIT_EXCEEDED);
        }
        
        int pendingCount = baseMapper.countByUserIdAndStatus(userId, AdoptionStatus.PENDING.getValue());
        if (pendingCount > 0) {
            throw new BusinessException(ErrorCode.ADOPTION_ALREADY_EXISTS);
        }
        
        User user = userMapper.selectById(userId);
        
        AdoptionRecord record = new AdoptionRecord();
        record.setPlantId(dto.getPlantId());
        record.setPlantName(plant.getName());
        record.setUserId(userId);
        record.setUserName(user.getNickname());
        record.setStatus(AdoptionStatus.PENDING);
        record.setApplyTime(LocalDateTime.now());
        
        baseMapper.insert(record);
        
        notificationService.sendNotification(
            userId,
            "领养申请已提交",
            "您已成功提交绿植【" + plant.getName() + "】的领养申请，请等待审批。",
            "ADOPTION",
            record.getId(),
            "AdoptionRecord"
        );
        
        return record;
    }
    
    @Override
    @Transactional
    public AdoptionRecord approve(Long approverId, AdoptionApproveDTO dto) {
        AdoptionRecord record = baseMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException(ErrorCode.ADOPTION_RECORD_NOT_FOUND);
        }
        
        if (record.getStatus() != AdoptionStatus.PENDING) {
            throw new BusinessException(ErrorCode.ADOPTION_STATUS_ERROR);
        }
        
        User approver = userMapper.selectById(approverId);
        
        record.setApproverId(approverId);
        record.setApproveTime(LocalDateTime.now());
        record.setApproveRemark(dto.getRemark());
        
        if (dto.getApproved()) {
            record.setStatus(AdoptionStatus.APPROVED);
            
            Plant plant = plantMapper.selectById(record.getPlantId());
            plant.setStatus(PlantStatus.ADOPTED);
            plant.setAdopterId(record.getUserId());
            plant.setAdoptionTime(LocalDateTime.now());
            plantMapper.updateById(plant);

            if (plant.getCarePlanTemplateId() != null) {
                carePlanService.bindPlantPlan(plant.getId(), plant.getCarePlanTemplateId(), "领养审批通过自动绑定");
            }
            
            notificationService.sendNotification(
                record.getUserId(),
                "领养申请已通过",
                "您对绿植【" + record.getPlantName() + "】的领养申请已通过审批。",
                "ADOPTION",
                record.getId(),
                "AdoptionRecord"
            );
        } else {
            record.setStatus(AdoptionStatus.REJECTED);
            
            notificationService.sendNotification(
                record.getUserId(),
                "领养申请已拒绝",
                "您对绿植【" + record.getPlantName() + "】的领养申请已被拒绝。" + 
                    (dto.getRemark() != null ? "原因：" + dto.getRemark() : ""),
                "ADOPTION",
                record.getId(),
                "AdoptionRecord"
            );
        }
        
        baseMapper.updateById(record);
        return record;
    }
    
    @Override
    @Transactional
    public void cancel(Long userId, Long recordId) {
        AdoptionRecord record = baseMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.ADOPTION_RECORD_NOT_FOUND);
        }
        
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        if (record.getStatus() != AdoptionStatus.PENDING) {
            throw new BusinessException(ErrorCode.ADOPTION_STATUS_ERROR);
        }
        
        record.setStatus(AdoptionStatus.CANCELLED);
        baseMapper.updateById(record);
    }
    
    @Override
    @Transactional
    public void returnPlant(Long userId, Long recordId, String reason) {
        AdoptionRecord record = baseMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.ADOPTION_RECORD_NOT_FOUND);
        }
        
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        if (record.getStatus() != AdoptionStatus.APPROVED) {
            throw new BusinessException(ErrorCode.ADOPTION_STATUS_ERROR);
        }
        
        record.setStatus(AdoptionStatus.RETURNED);
        record.setReturnTime(LocalDateTime.now());
        record.setReturnReason(reason);
        baseMapper.updateById(record);
        
        Plant plant = plantMapper.selectById(record.getPlantId());
        plant.setStatus(PlantStatus.AVAILABLE);
        plant.setAdopterId(null);
        plant.setAdoptionTime(null);
        plantMapper.updateById(plant);
    }
    
    @Override
    public IPage<AdoptionRecord> pageRecords(IPage<AdoptionRecord> page, Long userId, String status) {
        LambdaQueryWrapper<AdoptionRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(AdoptionRecord::getUserId, userId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AdoptionRecord::getStatus, AdoptionStatus.valueOf(status));
        }
        
        wrapper.orderByDesc(AdoptionRecord::getCreateTime);
        
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public AdoptionRecord getRecordDetail(Long id) {
        AdoptionRecord record = baseMapper.selectRecordWithDetails(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.ADOPTION_RECORD_NOT_FOUND);
        }
        return record;
    }
    
    @Override
    public boolean canAdopt(Long userId) {
        int maxPlants = sysConfigService.getIntConfigValue("max_plants_per_user", 3);
        int currentCount = userMapper.countAdoptedPlantsByUserId(userId);
        return currentCount < maxPlants;
    }
}
