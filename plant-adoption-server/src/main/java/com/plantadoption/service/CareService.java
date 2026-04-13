package com.plantadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plantadoption.dto.CareLogDTO;
import com.plantadoption.entity.CareLog;

/**
 * 养护服务接口
 */
public interface CareService extends IService<CareLog> {
    
    /**
     * 记录养护日志
     */
    CareLog recordCare(Long userId, CareLogDTO dto);
    
    /**
     * 分页查询养护日志
     */
    IPage<CareLog> pageCareLogs(IPage<CareLog> page, Long plantId, Long userId, String careType);
    
    /**
     * 获取养护日志详情
     */
    CareLog getCareLogDetail(Long id);
    
    /**
     * 删除养护日志
     */
    void deleteCareLog(Long userId, Long id);
}
