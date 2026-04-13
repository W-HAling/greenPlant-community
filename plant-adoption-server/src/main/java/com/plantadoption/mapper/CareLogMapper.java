package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantadoption.entity.CareLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 养护日志Mapper接口
 */
@Mapper
public interface CareLogMapper extends BaseMapper<CareLog> {
    
    @Select("SELECT cl.*, u.avatar as user_avatar, p.image_url as plant_image_url, p.location as plant_location " +
            "FROM care_log cl " +
            "LEFT JOIN sys_user u ON cl.user_id = u.id " +
            "LEFT JOIN plant p ON cl.plant_id = p.id " +
            "WHERE cl.id = #{id} AND cl.deleted = 0")
    CareLog selectLogWithDetails(@Param("id") Long id);
}
