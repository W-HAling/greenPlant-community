package com.plantadoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.plantadoption.entity.SysConfig;

/**
 * 系统配置服务接口
 */
public interface SysConfigService extends IService<SysConfig> {
    
    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值，带默认值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 获取整数配置值
     */
    int getIntConfigValue(String configKey, int defaultValue);
    
    /**
     * 获取布尔配置值
     */
    boolean getBooleanConfigValue(String configKey, boolean defaultValue);
    
    /**
     * 设置配置值
     */
    void setConfigValue(String configKey, String configValue);
}
