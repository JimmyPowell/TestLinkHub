<template>
  <el-drawer
    :model-value="visible"
    title="会议详情"
    @update:model-value="$emit('update:visible', $event)"
    direction="rtl"
    size="50%"
  >
    <div v-if="meeting" class="drawer-content">
      <h2>{{ meeting.name }}</h2>
      <p><strong>发起者:</strong> {{ meeting.initiatorName }}</p>
      <p><strong>状态:</strong> {{ meeting.status }}</p>
      <p><strong>开始时间:</strong> {{ meeting.startTime }}</p>
      <p><strong>结束时间:</strong> {{ meeting.endTime }}</p>
      <p><strong>提交时间:</strong> {{ meeting.createdAt }}</p>
      <p><strong>描述:</strong></p>
      <div v-html="meeting.description"></div>
    </div>
    <div v-else>
      <p>加载中...</p>
    </div>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button v-if="meeting && meeting.status === 'pending_review'" type="success" @click="handleApprove">通过</el-button>
        <el-button v-if="meeting && meeting.status === 'pending_review'" type="warning" @click="handleReject">驳回</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import { ElDrawer, ElMessage, ElButton, ElMessageBox } from 'element-plus';
import meetingService from '../services/meetingService';
import { convertToCamelCase } from '../utils/caseConverter';

const props = defineProps({
  visible: Boolean,
  meetingUuid: String,
});

const emit = defineEmits(['update:visible', 'review-completed']);

const meeting = ref(null);

const handleApprove = () => {
  ElMessageBox.prompt('请输入通过理由（可选）', '通过会议', {
    confirmButtonText: '确定通过',
    cancelButtonText: '取消',
    type: 'success',
  }).then(async ({ value }) => {
    try {
      const reviewData = {
        meeting_version_uuid: props.meetingUuid,
        audit_status: 'APPROVED',
        comments: value,
      };
      await meetingService.reviewMeeting(reviewData);
      ElMessage.success('会议已通过');
      emit('review-completed');
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
    }
  }).catch(() => {});
};

const handleReject = () => {
  ElMessageBox.prompt('请输入驳回理由', '驳回会议', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '驳回理由不能为空',
  }).then(async ({ value }) => {
    try {
      const reviewData = {
        meeting_version_uuid: props.meetingUuid,
        audit_status: 'REJECTED',
        comments: value,
      };
      await meetingService.reviewMeeting(reviewData);
      ElMessage.success('会议已驳回');
      emit('review-completed');
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
    }
  });
};

watch(() => props.meetingUuid, async (newUuid) => {
  if (newUuid && props.visible) {
    meeting.value = null; // Reset on new UUID
    try {
      const response = await meetingService.getMeetingDetails(newUuid);
      if (response.data && response.data.data) {
        meeting.value = convertToCamelCase(response.data.data);
      } else {
        ElMessage.error('未找到会议详情');
      }
    } catch (error) {
      ElMessage.error('加载会议详情失败: ' + (error.response?.data?.message || error.message));
      console.error('Failed to fetch meeting details:', error);
    }
  }
}, { immediate: true });
</script>

<style scoped>
.drawer-content {
  padding: 20px;
}
</style>
