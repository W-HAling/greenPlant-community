package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.entity.Plant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 绿植Mapper接口
 */
@Mapper
public interface PlantMapper extends BaseMapper<Plant> {
    
    @Select("SELECT p.*, u.nickname as adopter_name, u.phone as adopter_phone, u.avatar as adopter_avatar " +
            "FROM plant p " +
            "LEFT JOIN sys_user u ON p.adopter_id = u.id " +
            "WHERE p.id = #{id} AND p.deleted = 0")
    Plant selectPlantWithAdopter(@Param("id") Long id);
    
    @Update("UPDATE plant SET care_count = care_count + 1, last_care_time = NOW() WHERE id = #{plantId}")
    int incrementCareCount(@Param("plantId") Long plantId);
    
    @Select("SELECT COUNT(*) FROM plant WHERE status = #{status} AND deleted = 0")
    int countByStatus(@Param("status") String status);
}
