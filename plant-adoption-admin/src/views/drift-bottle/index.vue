<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">漂流瓶管理</span>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索内容关键词"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filters.status" clearable placeholder="状态" style="width: 180px">
        <el-option label="待拾取" value="FLOATING" />
        <el-option label="已拾取" value="PICKED" />
        <el-option label="已回复" value="REPLIED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="senderId" label="发送人" width="100" />
      <el-table-column prop="receiverId" label="拾取人" width="100">
        <template #default="{ row }">
          {{ row.receiverId || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column prop="imageUrls" label="图片" width="100">
        <template #default="{ row }">
          {{ parseImages(row.imageUrls).length }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagTypeMap[row.status] || 'info'">
            {{ statusTextMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="pickExpireTime" label="拾取截止" width="160">
        <template #default="{ row }">
          {{ formatDate(row.pickExpireTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="replyTime" label="回复时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.replyTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { TagProps } from 'element-plus'
import dayjs from 'dayjs'
import { deleteDriftBottle, getDriftBottleList } from '@/api/drift-bottle'
import type { DriftBottle } from '@/api/types'

type TagType = TagProps['type']

const loading = ref(false)
const tableData = ref<DriftBottle[]>([])

const filters = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const statusTextMap: Record<string, string> = {
  FLOATING: '待拾取',
  PICKED: '已拾取',
  REPLIED: '已回复'
}

const statusTagTypeMap: Record<string, TagType> = {
  FLOATING: 'info',
  PICKED: 'warning',
  REPLIED: 'success'
}

const formatDate = (value?: string) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

const parseImages = (images?: string) => {
  if (!images) return []
  return images.split(',').filter(Boolean)
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getDriftBottleList({
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

const handleDelete = async (row: DriftBottle) => {
  try {
    await ElMessageBox.confirm('确定要删除这条漂流瓶吗？', '提示', { type: 'warning' })
    await deleteDriftBottle(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}
</style>
