<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">领养管理</span>
    </div>
    
    <div class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已归还" value="RETURNED" />
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
      <el-table-column prop="plantName" label="绿植名称" />
      <el-table-column prop="userName" label="申请人" />
      <el-table-column prop="userPhone" label="手机号" />
      <el-table-column prop="applyTime" label="申请时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.applyTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="approveTime" label="审批时间" width="160">
        <template #default="{ row }">
          {{ row.approveTime ? formatDate(row.approveTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="approverName" label="审批人" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            type="primary"
            link
            @click="handleApprove(row)"
          >
            审批
          </el-button>
          <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
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
    
    <el-dialog v-model="detailVisible" title="领养详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="绿植名称">{{ currentRecord.plantName }}</el-descriptions-item>
        <el-descriptions-item label="绿植位置">{{ currentRecord.plantLocation }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRecord.userName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentRecord.userPhone }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDate(currentRecord.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord.status)">
            {{ getStatusText(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批时间" v-if="currentRecord.approveTime">
          {{ formatDate(currentRecord.approveTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审批人" v-if="currentRecord.approverName">
          {{ currentRecord.approverName }}
        </el-descriptions-item>
        <el-descriptions-item label="审批备注" :span="2" v-if="currentRecord.approveRemark">
          {{ currentRecord.approveRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="归还时间" v-if="currentRecord.returnTime">
          {{ formatDate(currentRecord.returnTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="归还原因" :span="2" v-if="currentRecord.returnReason">
          {{ currentRecord.returnReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdoptionList, getAdoptionDetail, approveAdoption } from '@/api/adoption'
import type { AdoptionRecord } from '@/api/types'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<AdoptionRecord[]>([])
const detailVisible = ref(false)
const currentRecord = ref<AdoptionRecord | null>(null)

const searchForm = reactive({
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getStatusType = (status: string) => {
  const map: Record<string, TagType> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info',
    RETURNED: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELLED: '已取消',
    RETURNED: '已归还'
  }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getAdoptionList({
      current: pagination.current,
      size: pagination.size,
      status: searchForm.status || undefined
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
  searchForm.status = ''
  handleSearch()
}

const handleApprove = async (row: AdoptionRecord) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入审批备注（可选）', '审批领养申请', {
      confirmButtonText: '通过',
      cancelButtonText: '拒绝',
      distinguishCancelAndClose: true,
      inputPlaceholder: '请输入备注'
    })
    
    await approveAdoption(row.id, true, value)
    ElMessage.success('审批通过')
    loadData()
  } catch (action: any) {
    if (action === 'cancel') {
      try {
        const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝领养申请', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入拒绝原因'
        })
        await approveAdoption(row.id, false, value)
        ElMessage.success('已拒绝')
        loadData()
      } catch (error) {
        console.error(error)
      }
    }
  }
}

const handleDetail = async (row: AdoptionRecord) => {
  try {
    currentRecord.value = await getAdoptionDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>
