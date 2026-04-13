<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">养护任务总览</span>
    </div>

    <div class="stats-grid">
      <div class="stats-card">
        <div class="stats-label">任务总数</div>
        <div class="stats-value">{{ stats.total }}</div>
      </div>
      <div class="stats-card warning">
        <div class="stats-label">待执行</div>
        <div class="stats-value">{{ stats.pending }}</div>
      </div>
      <div class="stats-card danger">
        <div class="stats-label">已逾期</div>
        <div class="stats-value">{{ stats.overdue }}</div>
      </div>
      <div class="stats-card success">
        <div class="stats-label">已完成</div>
        <div class="stats-value">{{ stats.done }}</div>
      </div>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索绿植、领养人或任务内容"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filters.status" clearable placeholder="任务状态" style="width: 180px">
        <el-option label="待执行" value="PENDING" />
        <el-option label="已逾期" value="OVERDUE" />
        <el-option label="已完成" value="DONE" />
        <el-option label="已取消" value="CANCELED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="plantName" label="绿植名称" min-width="140" />
      <el-table-column prop="adopterName" label="当前领养人" width="120" />
      <el-table-column prop="careType" label="养护类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ careTypeTextMap[row.careType || ''] || row.careType || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="careDetail" label="任务内容" min-width="220" show-overflow-tooltip />
      <el-table-column prop="dueDate" label="截止日期" width="120" />
      <el-table-column prop="status" label="任务状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagTypeMap[row.status] || 'info'">
            {{ statusTextMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastRemindTime" label="最近提醒" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.lastRemindTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="completedTime" label="完成时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.completedTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING' || row.status === 'OVERDUE'"
            type="primary"
            link
            @click="openAdjustDialog(row)"
          >
            调整
          </el-button>
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

    <el-dialog v-model="adjustDialogVisible" title="调整养护任务" width="520px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="截止日期">
          <el-date-picker v-model="adjustForm.dueDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="周期类型">
          <el-select v-model="adjustForm.cycleType" clearable placeholder="沿用模板">
            <el-option label="每日" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期数值">
          <el-input-number v-model="adjustForm.cycleValue" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="任务内容">
          <el-input v-model="adjustForm.careDetail" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="调整说明">
          <el-input v-model="adjustForm.adjustNote" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjusting" @click="submitAdjust">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { TagProps } from 'element-plus'
import dayjs from 'dayjs'
import { adjustCareTask, getCareTaskList, getCareTaskStats } from '@/api/care-plan'
import type { CareTask, CareTaskStats } from '@/api/types'

type TagType = TagProps['type']

const loading = ref(false)
const tableData = ref<CareTask[]>([])
const stats = reactive<CareTaskStats>({
  total: 0,
  pending: 0,
  overdue: 0,
  done: 0
})
const adjustDialogVisible = ref(false)
const adjusting = ref(false)
const currentTaskId = ref<number>()

const filters = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const adjustForm = reactive({
  dueDate: '',
  cycleType: '',
  cycleValue: undefined as number | undefined,
  careDetail: '',
  adjustNote: ''
})

const statusTextMap: Record<string, string> = {
  PENDING: '待执行',
  OVERDUE: '已逾期',
  DONE: '已完成',
  CANCELED: '已取消'
}

const statusTagTypeMap: Record<string, TagType> = {
  PENDING: 'warning',
  OVERDUE: 'danger',
  DONE: 'success',
  CANCELED: 'info'
}

const careTypeTextMap: Record<string, string> = {
  WATER: '浇水',
  FERTILIZE: '施肥',
  PRUNE: '修剪',
  CLEAN: '清洁',
  OTHER: '其他'
}

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getCareTaskList({
      current: pagination.current,
      size: pagination.size,
      status: filters.status || undefined,
      keyword: filters.keyword || undefined
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const data = await getCareTaskStats()
    stats.total = data.total
    stats.pending = data.pending
    stats.overdue = data.overdue
    stats.done = data.done
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  filters.keyword = ''
  filters.status = ''
  pagination.current = 1
  loadData()
}

const openAdjustDialog = (row: CareTask) => {
  currentTaskId.value = row.id
  adjustForm.dueDate = row.dueDate
  adjustForm.cycleType = row.cycleTypeOverride || ''
  adjustForm.cycleValue = row.cycleValueOverride
  adjustForm.careDetail = row.careDetailOverride || row.careDetail || ''
  adjustForm.adjustNote = ''
  adjustDialogVisible.value = true
}

const submitAdjust = async () => {
  if (!currentTaskId.value) return
  adjusting.value = true
  try {
    await adjustCareTask(currentTaskId.value, {
      dueDate: adjustForm.dueDate || undefined,
      cycleType: adjustForm.cycleType || undefined,
      cycleValue: adjustForm.cycleValue,
      careDetail: adjustForm.careDetail || undefined,
      adjustNote: adjustForm.adjustNote || undefined
    })
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    loadStats()
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    adjusting.value = false
  }
}

onMounted(() => {
  loadStats()
  loadData()
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stats-card {
  padding: 16px 18px;
  border-radius: 12px;
  background: #f4f8f4;
}

.stats-card.warning {
  background: #fff7e8;
}

.stats-card.danger {
  background: #fff1f0;
}

.stats-card.success {
  background: #f0f9eb;
}

.stats-label {
  font-size: 13px;
  color: #6b7280;
}

.stats-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
