package com.plantadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plantadoption.dto.AdoptionApplyDTO;
import com.plantadoption.dto.AdoptionApproveDTO;
import com.plantadoption.entity.AdoptionRecord;

/**
 * 领养服务接口
 */
public interface AdoptionService extends IService<AdoptionRecord> {
    
    /**
     * 申请领养
     */
    AdoptionRecord apply(Long userId, AdoptionApplyDTO dto);
    
    /**
     * 审批领养申请
     */
    AdoptionRecord approve(Long approverId, AdoptionApproveDTO dto);
    
    /**
     * 取消领养申请
     */
    void cancel(Long userId, Long recordId);
    
    /**
     * 归还绿植
     */
    void returnPlant(Long userId, Long recordId, String reason);
    
    /**
     * 分页查询领养记录
     */
    IPage<AdoptionRecord> pageRecords(IPage<AdoptionRecord> page, Long userId, String status);
    
    /**
     * 获取领养记录详情
     */
    AdoptionRecord getRecordDetail(Long id);
    
    /**
     * 检查用户是否可以领养
     */
    boolean canAdopt(Long userId);
}
