<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">绿植管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加绿植
      </el-button>
    </div>
    
    <div class="search-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="名称/品种/位置"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="可领养" value="AVAILABLE" />
            <el-option label="已领养" value="ADOPTED" />
            <el-option label="维护中" value="MAINTENANCE" />
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
      <el-table-column prop="imageUrl" label="图片" width="100">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imageUrl.split(',')[0]"
            :preview-src-list="row.imageUrl.split(',').filter(Boolean)"
            preview-teleported
            :z-index="9999"
            fit="cover"
            style="width: 60px; height: 60px; border-radius: 4px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="variety" label="品种" />
      <el-table-column prop="location" label="位置" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adopterName" label="领养人" />
      <el-table-column prop="carePlanTemplateId" label="模板ID" width="100" />
      <el-table-column prop="careCount" label="养护次数" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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
    
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入绿植名称" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-input v-model="formData.variety" placeholder="请输入品种" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入放置位置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="可领养" value="AVAILABLE" />
            <el-option label="已领养" value="ADOPTED" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <el-upload
            action="/api/upload/image"
            :headers="uploadHeaders"
            list-type="picture-card"
            v-model:file-list="fileList"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            accept="image/*"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="养护提示" prop="careTips">
          <el-input
            v-model="formData.careTips"
            type="textarea"
            :rows="3"
            placeholder="请输入养护提示"
          />
        </el-form-item>
        <el-form-item label="养护模板" prop="carePlanTemplateId">
          <el-select
            v-model="formData.carePlanTemplateId"
            placeholder="请选择养护模板"
            clearable
            filterable
          >
            <el-option
              v-for="item in templateOptions"
              :key="item.id"
              :label="item.templateName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadUserFile, UploadProps } from 'element-plus'
import { getPlantList, createPlant, updatePlant, deletePlant } from '@/api/plant'
import { getCarePlanTemplateList } from '@/api/care-plan'
import type { CarePlanTemplate, Plant } from '@/api/types'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const fileList = ref<UploadUserFile[]>([])

const handleUploadSuccess: UploadProps['onSuccess'] = (res, file) => {
  if (res.code === 200) {
    file.url = res.data
    updateImageUrl()
  } else {
    ElMessage.error(res.message || '上传失败')
    fileList.value.pop()
  }
}

const handleUploadRemove: UploadProps['onRemove'] = () => {
  updateImageUrl()
}

const updateImageUrl = () => {
  const urls = fileList.value
    .map(f => f.url || (f.response as any)?.data)
    .filter(Boolean)
  formData.imageUrl = urls.join(',')
}

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Plant[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加绿植')
const formRef = ref<FormInstance>()
const templateOptions = ref<CarePlanTemplate[]>([])

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive<Partial<Plant>>({
  name: '',
  variety: '',
  location: '',
  status: 'AVAILABLE',
  description: '',
  careTips: '',
  carePlanTemplateId: undefined
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getStatusType = (status: string) => {
  const map: Record<string, TagType> = {
    AVAILABLE: 'success',
    ADOPTED: 'warning',
    MAINTENANCE: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    AVAILABLE: '可领养',
    ADOPTED: '已领养',
    MAINTENANCE: '维护中'
  }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getPlantList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
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

const loadTemplates = async () => {
  try {
    templateOptions.value = await getCarePlanTemplateList({ status: 1 })
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加绿植'
  Object.assign(formData, {
    name: '',
    variety: '',
    location: '',
    status: 'AVAILABLE',
    description: '',
    careTips: '',
    carePlanTemplateId: undefined,
    imageUrl: ''
  })
  fileList.value = []
  dialogVisible.value = true
}

const handleEdit = (row: Plant) => {
  dialogTitle.value = '编辑绿植'
  Object.assign(formData, row)
  if (row.imageUrl) {
    fileList.value = row.imageUrl.split(',').filter(Boolean).map((url, index) => ({
      name: `image-${index}`,
      url
    }))
  } else {
    fileList.value = []
  }
  dialogVisible.value = true
}

const handleDelete = async (row: Plant) => {
  try {
    await ElMessageBox.confirm('确定要删除该绿植吗？', '提示', {
      type: 'warning'
    })
    await deletePlant(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  submitting.value = true
  try {
    if (formData.id) {
      await updatePlant(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await createPlant(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadData()
  loadTemplates()
})
</script>
