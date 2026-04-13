<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">养护记录</span>
    </div>
    
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="plantName" label="绿植名称" />
      <el-table-column prop="userName" label="养护人" />
      <el-table-column prop="careType" label="养护类型">
        <template #default="{ row }">
          <el-tag>{{ getCareTypeText(row.careType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="careTime" label="养护时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.careTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="images" label="图片" width="120">
        <template #default="{ row }">
          <el-image
            v-if="row.images"
            :src="parseImages(row.images)[0]"
            :preview-src-list="parseImages(row.images)"
            fit="cover"
            style="width: 60px; height: 60px; border-radius: 4px"
          />
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
import { get } from '@/utils/request'
import type { CareLog, PageResult } from '@/api/types'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<CareLog[]>([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getCareTypeText = (type: string) => {
  const map: Record<string, string> = {
    WATER: '浇水',
    FERTILIZE: '施肥',
    PRUNE: '修剪',
    PEST_CONTROL: '病虫害防治',
    OTHER: '其他'
  }
  return map[type] || type
}

const parseImages = (images: string) => {
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await get<PageResult<CareLog>>('/care/list', {
      current: pagination.current,
      size: pagination.size
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>
