<template>
  <el-drawer
    :model-value="visible"
    title="课程预览"
    direction="rtl"
    size="60%"
    @update:model-value="$emit('update:visible', $event)"
    @open="fetchDetail"
  >
    <div v-loading="loading" class="detail-container">
      <div v-if="detail">
        <el-descriptions title="课程基本信息" :column="2" border>
          <el-descriptions-item label="课程名称">{{ detail.version.name }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ detail.version.authorName }}</el-descriptions-item>
          <el-descriptions-item label="课程状态">
            <el-tag :type="getStatusTagType(detail.lesson.status)">{{ getStatusText(detail.lesson.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前版本">{{ detail.version.version }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detail.version.description }}</el-descriptions-item>
        </el-descriptions>

        <div class="resources-section">
          <h3>课程资源</h3>
          <el-table :data="detail.resources" style="width: 100%">
            <el-table-column prop="name" label="资源名称" />
            <el-table-column prop="resourcesType" label="类型" width="120" />
            <el-table-column prop="resourcesUrl" label="链接">
              <template #default="scope">
                <a :href="scope.row.resourcesUrl" target="_blank" rel="noopener noreferrer">点击查看</a>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="80" />
          </el-table>
        </div>
      </div>
      <div v-else-if="!loading">
        <p>无法加载课程详情。</p>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { ElDrawer, ElDescriptions, ElDescriptionsItem, ElTable, ElTableColumn, ElTag, ElMessage } from 'element-plus';
import courseService from '../services/courseService';

const props = defineProps({
  visible: Boolean,
  courseUuid: String,
});

defineEmits(['update:visible']);

const loading = ref(false);
const detail = ref(null);

const fetchDetail = async () => {
  if (!props.courseUuid) return;
  loading.value = true;
  detail.value = null;
  try {
    const response = await courseService.getLessonDetail(props.courseUuid);
    detail.value = response.data.data;
  } catch (error) {
    ElMessage.error('获取课程详情失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

const getStatusTagType = (status) => {
  const types = {
    pending_review: 'warning',
    active: 'success',
    rejected: 'danger',
    archived: 'info',
  };
  return types[status] || 'info';
};

const getStatusText = (status) => {
  const texts = {
    pending_review: '待审核',
    active: '已发布',
    rejected: '已驳回',
    archived: '已归档',
  };
  return texts[status] || '未知';
};
</script>

<style scoped>
.detail-container {
  padding: 20px;
}
.resources-section {
  margin-top: 30px;
}
</style>
