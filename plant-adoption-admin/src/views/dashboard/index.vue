<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon size="28"><Cherry /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">绿植总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.available }}</div>
              <div class="stat-label">可领养</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.adopted }}</div>
              <div class="stat-label">已领养</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ pendingCount }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>最近领养申请</span>
          </template>
          <el-table :data="recentAdoptions" stripe>
            <el-table-column prop="plantName" label="绿植名称" />
            <el-table-column prop="userName" label="申请人" />
            <el-table-column prop="applyTime" label="申请时间">
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
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PENDING'"
                  type="primary"
                  size="small"
                  @click="handleApprove(row)"
                >
                  审批
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>最近养护动态</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="log in recentCareLogs"
              :key="log.id"
              :timestamp="formatDate(log.careTime)"
              placement="top"
            >
              <div class="care-log-item">
                <div class="care-user">{{ log.userName }}</div>
                <div class="care-action">
                  对【{{ log.plantName }}】进行了{{ getCareTypeText(log.careType) }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPlantStats } from '@/api/plant'
import { getAdoptionList, approveAdoption } from '@/api/adoption'
import type { AdoptionRecord } from '@/api/types'
import dayjs from 'dayjs'

const stats = ref({ total: 0, available: 0, adopted: 0, maintenance: 0 })
const pendingCount = ref(0)
const recentAdoptions = ref<AdoptionRecord[]>([])
const recentCareLogs = ref<any[]>([])

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

const getCareTypeText = (type: string) => {
  const map: Record<string, string> = {
    WATER: '浇水',
    FERTILIZE: '施肥',
    PRUNE: '修剪',
    PEST_CONTROL: '病虫害防治',
    OTHER: '养护'
  }
  return map[type] || '养护'
}

const loadStats = async () => {
  try {
    stats.value = await getPlantStats()
  } catch (error) {
    console.error(error)
  }
}

const loadRecentAdoptions = async () => {
  try {
    const data = await getAdoptionList({ current: 1, size: 10 })
    recentAdoptions.value = data.records
    
    const pending = await getAdoptionList({ current: 1, size: 1, status: 'PENDING' })
    pendingCount.value = pending.total
  } catch (error) {
    console.error(error)
  }
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
    loadRecentAdoptions()
    loadStats()
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
        loadRecentAdoptions()
      } catch (error) {
        console.error(error)
      }
    }
  }
}

onMounted(() => {
  loadStats()
  loadRecentAdoptions()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
    }
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }
    
    .stat-info {
      margin-left: 16px;
      
      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #303133;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
  
  .care-log-item {
    .care-user {
      font-weight: 600;
      color: #303133;
    }
    
    .care-action {
      font-size: 13px;
      color: #606266;
      margin-top: 4px;
    }
  }
}
</style>
