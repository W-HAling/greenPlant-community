package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.entity.CareTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CareTaskMapper extends BaseMapper<CareTask> {

    @Select("""
            SELECT ct.*, p.name AS plant_name, COALESCE(ct.care_detail_override, cpi.care_detail) AS care_detail, cpi.care_type
            FROM care_task ct
            LEFT JOIN plant p ON ct.plant_id = p.id
            LEFT JOIN care_plan_item cpi ON ct.plan_item_id = cpi.id
            WHERE ct.adopter_id = #{userId}
              AND (#{status} IS NULL OR ct.status = #{status})
            ORDER BY ct.due_date ASC, ct.id ASC
            """)
    List<CareTask> selectMyTasks(@Param("userId") Long userId, @Param("status") String status);

    @Select("""
            SELECT ct.*, p.name AS plant_name, u.nickname AS adopter_name, COALESCE(ct.care_detail_override, cpi.care_detail) AS care_detail, cpi.care_type
            FROM care_task ct
            LEFT JOIN plant p ON ct.plant_id = p.id
            LEFT JOIN sys_user u ON ct.adopter_id = u.id
            LEFT JOIN care_plan_item cpi ON ct.plan_item_id = cpi.id
            WHERE (#{status} IS NULL OR ct.status = #{status})
              AND (
                    #{keyword} IS NULL
                    OR p.name LIKE CONCAT('%', #{keyword}, '%')
                    OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
                    OR cpi.care_detail LIKE CONCAT('%', #{keyword}, '%')
                  )
            ORDER BY
              CASE ct.status
                WHEN 'OVERDUE' THEN 0
                WHEN 'PENDING' THEN 1
                ELSE 2
              END,
              ct.due_date ASC,
              ct.id DESC
            """)
    IPage<CareTask> selectAdminTasks(Page<CareTask> page,
                                     @Param("status") String status,
                                     @Param("keyword") String keyword);
}
