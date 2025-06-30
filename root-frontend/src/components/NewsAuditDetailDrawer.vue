<template>
  <el-drawer
    :model-value="visible"
    title="新闻审核详情"
    direction="rtl"
    size="50%"
    @update:model-value="$emit('update:visible', $event)"
    @open="fetchDetail"
  >
    <div v-loading="loading" class="detail-container">
      <div v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="新闻标题">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="提交人">{{ detail.publisherName }}</el-descriptions-item>
          <el-descriptions-item label="所属公司">{{ detail.companyName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="摘要">{{ detail.summary }}</el-descriptions-item>
        </el-descriptions>
        <div class="content-preview">
          <h3>内容预览</h3>
          <div v-html="detail.content" class="html-content"></div>
        </div>
      </div>
      <div v-else-if="!loading">
        <p>无法加载新闻详情。</p>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import { ElDrawer, ElDescriptions, ElDescriptionsItem, ElMessage } from 'element-plus';
import newsService from '../services/newsService';

const props = defineProps({
  visible: Boolean,
  newsUuid: String,
});

defineEmits(['update:visible']);

const loading = ref(false);
const detail = ref(null);

const fetchDetail = async () => {
  if (!props.newsUuid) return;
  loading.value = true;
  detail.value = null;
  try {
    const response = await newsService.getNewsAuditDetail(props.newsUuid);
    detail.value = response.data.data;
  } catch (error) {
    ElMessage.error('获取新闻详情失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.detail-container {
  padding: 20px;
}
.content-preview {
  margin-top: 20px;
}
.html-content {
  border: 1px solid #ebeef5;
  padding: 16px;
  border-radius: 4px;
  margin-top: 10px;
  background-color: #fafafa;
}
</style>
