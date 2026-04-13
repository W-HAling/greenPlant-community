<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">系统配置</span>
    </div>
    
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="configKey" label="配置键" />
      <el-table-column prop="configValue" label="配置值">
        <template #default="{ row }">
          <el-input
            v-model="row.configValue"
            @blur="handleUpdate(row)"
            style="width: 200px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="configType" label="类型" width="100" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigList, setConfigValue } from '@/api/config'
import type { SysConfig } from '@/api/types'

const loading = ref(false)
const tableData = ref<SysConfig[]>([])

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getConfigList()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleUpdate = async (row: SysConfig) => {
  try {
    await setConfigValue(row.configKey, row.configValue)
    ElMessage.success('更新成功')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>
