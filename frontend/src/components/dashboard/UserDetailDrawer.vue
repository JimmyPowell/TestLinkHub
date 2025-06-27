<template>
  <el-drawer
    :model-value="visible"
    title="用户详情"
    direction="rtl"
    size="40%"
    @update:modelValue="$emit('update:visible', $event)"
  >
    <div v-if="user" class="drawer-content">
      <div class="avatar-container">
        <el-avatar :size="100" :src="user.avatar_url">
          <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
        </el-avatar>
        <h3 class="username">{{ user.name }}</h3>
      </div>
      <el-descriptions :column="1" border class="user-details">
        <el-descriptions-item label="UUID">{{ user.uuid }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ user.phone_number }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ user.company_name }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag>{{ user.role }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="user.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ user.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="性别">{{ user.gender }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ user.address }}</el-descriptions-item>
        <el-descriptions-item label="个人描述">{{ user.description }}</el-descriptions-item>
        <el-descriptions-item label="帖子数">{{ user.post_count }}</el-descriptions-item>
        <el-descriptions-item label="课程数">{{ user.lesson_count }}</el-descriptions-item>
        <el-descriptions-item label="会议数">{{ user.meeting_count }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(user.created_at) }}</el-descriptions-item>
        <el-descriptions-item label="最后更新时间">{{ formatDateTime(user.updated_at) }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div v-else>
      <p>加载中...</p>
    </div>
  </el-drawer>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import { ElDrawer, ElDescriptions, ElDescriptionsItem, ElTag, ElAvatar } from 'element-plus';
import { formatDateTime } from '../../utils/format';

defineProps({
  user: {
    type: Object,
    default: null,
  },
  visible: {
    type: Boolean,
    required: true,
  },
});

defineEmits(['update:visible']);
</script>

<style scoped>
.drawer-content {
  padding: 20px;
}
.avatar-container {
  text-align: center;
  margin-bottom: 20px;
}
.username {
  margin-top: 10px;
  font-size: 1.2em;
}
.user-details {
  margin-top: 20px;
}
</style>
