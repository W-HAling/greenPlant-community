package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantadoption.entity.DriftBottle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DriftBottleMapper extends BaseMapper<DriftBottle> {

    @Select("""
            SELECT COUNT(*)
            FROM drift_bottle
            WHERE sender_id = #{senderId}
              AND deleted = 0
              AND DATE(create_time) = CURRENT_DATE
            """)
    int countDailySentBySender(Long senderId);
}
