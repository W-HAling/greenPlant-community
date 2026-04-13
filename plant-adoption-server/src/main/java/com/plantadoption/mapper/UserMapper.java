package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantadoption.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT u.*, d.name as department_name FROM sys_user u " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "WHERE u.id = #{id} AND u.deleted = 0")
    User selectUserWithDepartment(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM plant WHERE adopter_id = #{userId} AND status = 'ADOPTED' AND deleted = 0")
    int countAdoptedPlantsByUserId(@Param("userId") Long userId);
}
