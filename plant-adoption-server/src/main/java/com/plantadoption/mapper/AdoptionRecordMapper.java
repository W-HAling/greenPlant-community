package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantadoption.entity.AdoptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 领养记录Mapper接口
 */
@Mapper
public interface AdoptionRecordMapper extends BaseMapper<AdoptionRecord> {
    
    @Select("SELECT ar.*, p.image_url as plant_image_url, p.location as plant_location, " +
            "u.phone as user_phone, u.avatar as user_avatar, " +
            "approver.nickname as approver_name " +
            "FROM adoption_record ar " +
            "LEFT JOIN plant p ON ar.plant_id = p.id " +
            "LEFT JOIN sys_user u ON ar.user_id = u.id " +
            "LEFT JOIN sys_user approver ON ar.approver_id = approver.id " +
            "WHERE ar.id = #{id} AND ar.deleted = 0")
    AdoptionRecord selectRecordWithDetails(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM adoption_record WHERE user_id = #{userId} AND status = #{status} AND deleted = 0")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}
