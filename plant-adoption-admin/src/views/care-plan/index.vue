<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">养护模板</span>
      <el-button type="primary" @click="openDialog">
        <el-icon><Plus /></el-icon>
        新建模板
      </el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="templateName" label="模板名称" min-width="180" />
      <el-table-column prop="plantCategory" label="适配类别" width="140" />
      <el-table-column prop="plantSpecies" label="适配品种" min-width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计划项" width="90">
        <template #default="{ row }">
          {{ row.items?.length || 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
    </el-table>

    <el-dialog v-model="dialogVisible" title="新建养护模板" width="760px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="96px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="formData.templateName" placeholder="例如：绿萝标准模板" />
        </el-form-item>
        <el-form-item label="适配类别" prop="plantCategory">
          <el-select v-model="formData.plantCategory" placeholder="请选择类别" clearable>
            <el-option label="观叶" value="foliage" />
            <el-option label="多肉" value="succulent" />
            <el-option label="花卉" value="flower" />
            <el-option label="蕨类" value="fern" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="适配品种" prop="plantSpecies">
          <el-input v-model="formData.plantSpecies" placeholder="可选，不填则按类别通用" />
        </el-form-item>
        <el-form-item label="模板说明" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入模板说明" />
        </el-form-item>

        <div class="plan-items-header">
          <span>养护计划项</span>
          <el-button link type="primary" @click="addItem">新增计划项</el-button>
        </div>

        <div v-for="(item, index) in formData.items" :key="index" class="plan-item-card">
          <div class="plan-item-actions">
            <span>计划项 {{ index + 1 }}</span>
            <el-button link type="danger" @click="removeItem(index)">删除</el-button>
          </div>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item :label="`类型${index + 1}`" :prop="`items.${index}.careType`">
                <el-select v-model="item.careType" placeholder="养护类型">
                  <el-option label="浇水" value="water" />
                  <el-option label="施肥" value="fertilize" />
                  <el-option label="修剪" value="prune" />
                  <el-option label="清洁" value="clean" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="`周期${index + 1}`" :prop="`items.${index}.cycleType`">
                <el-select v-model="item.cycleType" placeholder="周期类型">
                  <el-option label="每日" value="daily" />
                  <el-option label="每周" value="weekly" />
                  <el-option label="每月" value="monthly" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="`频次${index + 1}`" :prop="`items.${index}.cycleValue`">
                <el-input-number v-model="item.cycleValue" :min="1" :max="30" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item :label="`细节${index + 1}`" :prop="`items.${index}.careDetail`">
            <el-input v-model="item.careDetail" placeholder="例如：沿盆边浇水 50ml" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createCarePlanTemplate, getCarePlanTemplateList } from '@/api/care-plan'
import type { CarePlanTemplate } from '@/api/types'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const tableData = ref<CarePlanTemplate[]>([])

const createDefaultItem = () => ({
  careType: 'water',
  cycleType: 'weekly',
  cycleValue: 1,
  careDetail: '',
  careNote: '',
  remindAdvance: 1,
  sort: 0
})

const formData = reactive({
  templateName: '',
  plantCategory: '',
  plantSpecies: '',
  description: '',
  items: [createDefaultItem()]
})

const formRules: FormRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  items: [{ required: true, message: '请至少保留一个计划项', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getCarePlanTemplateList()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openDialog = () => {
  dialogVisible.value = true
}

const addItem = () => {
  formData.items.push(createDefaultItem())
}

const removeItem = (index: number) => {
  if (formData.items.length === 1) return
  formData.items.splice(index, 1)
}

const resetForm = () => {
  formRef.value?.resetFields()
  formData.templateName = ''
  formData.plantCategory = ''
  formData.plantSpecies = ''
  formData.description = ''
  formData.items = [createDefaultItem()]
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitting.value = true
  try {
    await createCarePlanTemplate({
      templateName: formData.templateName,
      plantCategory: formData.plantCategory || undefined,
      plantSpecies: formData.plantSpecies || undefined,
      description: formData.description || undefined,
      items: formData.items.map((item, index) => ({
        ...item,
        sort: index
      }))
    })
    ElMessage.success('模板创建成功')
    dialogVisible.value = false
    resetForm()
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.plan-items-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

.plan-item-card {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}

.plan-item-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
