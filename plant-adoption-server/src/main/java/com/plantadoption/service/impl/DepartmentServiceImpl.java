package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantadoption.entity.Department;
import com.plantadoption.mapper.DepartmentMapper;
import com.plantadoption.service.DepartmentService;
import org.springframework.stereotype.Service;

/**
 * 部门服务实现类
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {
}