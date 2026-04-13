<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">用户管理</span>
    </div>
    
    <div class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable>
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="avatar" label="头像" width="80">
        <template #default="{ row }">
          <el-avatar :src="row.avatar" :size="40">
            {{ row.nickname?.charAt(0) }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="departmentName" label="部门" />
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(val) => handleStatusChange(row, Boolean(val))"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '@/api/user'
import type { UserInfo } from '@/api/types'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<UserInfo[]>([])

const searchForm = reactive({
  nickname: '',
  phone: '',
  role: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getUserList({
      current: pagination.current,
      size: pagination.size,
      nickname: searchForm.nickname || undefined,
      phone: searchForm.phone || undefined,
      role: searchForm.role || undefined
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.nickname = ''
  searchForm.phone = ''
  searchForm.role = ''
  handleSearch()
}

const handleStatusChange = async (row: UserInfo, status: boolean) => {
  try {
    await ElMessageBox.confirm(
      `确定要${status ? '启用' : '禁用'}该用户吗？`,
      '提示',
      { type: 'warning' }
    )
    await updateUserStatus(row.id, status ? 1 : 0)
    ElMessage.success('操作成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>
