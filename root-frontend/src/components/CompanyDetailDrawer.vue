<template>
  <el-drawer
    :model-value="visible"
    title="公司详情"
    direction="rtl"
    size="40%"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div v-if="company" class="detail-container">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="UUID">{{ company.uuid }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ company.name }}</el-descriptions-item>
        <el-descriptions-item label="公司代码">{{ company.company_code }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ company.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ company.phone_number }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="company.status === 'ACTIVE' ? 'success' : 'danger'">{{ company.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址">{{ company.address || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ company.description || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ company.created_at }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ company.updated_at }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div v-else>
      <p>没有可显示的公司信息。</p>
    </div>
  </el-drawer>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import { ElDrawer, ElDescriptions, ElDescriptionsItem, ElTag } from 'element-plus';

defineProps({
  company: {
    type: Object,
    default: null,
  },
  visible: {
    type: Boolean,
    default: false,
  },
});

defineEmits(['update:visible']);
</script>

<style scoped>
.detail-container {
  padding: 20px;
}
</style>
