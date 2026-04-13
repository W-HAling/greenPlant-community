<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">社区管理</span>
    </div>
    
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userName" label="发布者" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="postType" label="类型">
        <template #default="{ row }">
          <el-tag>{{ getPostTypeText(row.postType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column prop="commentCount" label="评论" width="80" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(val) => handleStatusChange(row, Boolean(val))"
          />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, del } from '@/utils/request'
import type { CommunityPost, PageResult } from '@/api/types'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<CommunityPost[]>([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getPostTypeText = (type: string) => {
  const map: Record<string, string> = {
    SHARE: '分享',
    QUESTION: '问答',
    EXPERIENCE: '经验'
  }
  return map[type] || type
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await get<PageResult<CommunityPost>>('/community/post/list', {
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

const handleStatusChange = async (row: CommunityPost, status: boolean) => {
  try {
    await get(`/community/post/${row.id}/status`, { status: status ? 1 : 0 })
    ElMessage.success('操作成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (row: CommunityPost) => {
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？', '提示', { type: 'warning' })
    await del(`/community/post/${row.id}`)
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
