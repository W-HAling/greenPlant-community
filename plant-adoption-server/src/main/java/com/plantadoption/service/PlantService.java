package com.plantadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plantadoption.dto.PlantDTO;
import com.plantadoption.entity.Plant;

/**
 * 绿植服务接口
 */
public interface PlantService extends IService<Plant> {
    
    /**
     * 分页查询绿植列表
     */
    IPage<Plant> pagePlants(IPage<Plant> page, String status, String keyword, String location);
    
    /**
     * 获取绿植详情（含领养人信息）
     */
    Plant getPlantDetail(Long id);
    
    /**
     * 创建绿植
     */
    Plant createPlant(PlantDTO dto);
    
    /**
     * 更新绿植
     */
    Plant updatePlant(Long id, PlantDTO dto);
    
    /**
     * 删除绿植
     */
    void deletePlant(Long id);
    
    /**
     * 生成绿植二维码
     */
    String generateQrCode(Long id);
    
    /**
     * 获取绿植统计数据
     */
    PlantStats getStats();
    
    /**
     * 绿植统计内部类
         */
    record PlantStats(int total, int available, int adopted, int maintenance) {}
}
