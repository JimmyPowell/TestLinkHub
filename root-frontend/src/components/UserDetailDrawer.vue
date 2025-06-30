<template>
  <el-drawer
    :model-value="visible"
    title="用户详情"
    direction="rtl"
    size="40%"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div v-if="user" class="detail-container">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="UUID">{{ user.uuid }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user.name }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ user.phone_number }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ user.company_name || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag>{{ user.role }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="user.status === 'ACTIVE' ? 'success' : 'danger'">{{ user.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址">{{ user.address || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="个人描述">{{ user.description || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ user.updated_at }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div v-else>
      <p>没有可显示的用户信息。</p>
    </div>
  </el-drawer>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import { ElDrawer, ElDescriptions, ElDescriptionsItem, ElTag } from 'element-plus';

defineProps({
  user: {
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
