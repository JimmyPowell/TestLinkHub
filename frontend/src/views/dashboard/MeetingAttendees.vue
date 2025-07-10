<template>
  <div class="attendees-container">
    <el-card class="box-card">
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="会议名称">
            <el-input v-model="searchForm.meetingName" placeholder="请输入会议名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="申请者姓名">
            <el-input v-model="searchForm.applicantName" placeholder="请输入申请者姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
              <el-option label="待处理" value="pending"></el-option>
              <el-option label="已批准" value="approved"></el-option>
              <el-option label="已拒绝" value="rejected"></el-option>
              <el-option label="已取消" value="cancelled"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 列表区域 -->
      <el-table :data="applicationList" style="width: 100%" v-loading="loading">
        <el-table-column prop="uuid" label="申请UUID" width="150" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="meetingName" label="会议名称" width="180"></el-table-column>
        <el-table-column prop="applicantName" label="申请者姓名" width="150"></el-table-column>
        <el-table-column prop="joinReason" label="申请理由" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180">
            <template #default="scope">
                <span>{{ formatDateTime(scope.row.createdAt) }}</span>
            </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <div v-if="scope.row.status === 'pending'">
              <el-button size="small" type="success" @click="handleReview(scope.row, 'approved')">批准</el-button>
              <el-button size="small" type="danger" @click="handleReview(scope.row, 'rejected')">拒绝</el-button>
            </div>
            <span v-else>已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getMeetingApplications, reviewMeetingApplication } from '../../services/meetingService';
import { formatDateTime } from '../../utils/format';

const loading = ref(false);
const searchForm = reactive({
  meetingName: '',
  applicantName: '',
  status: '',
});

const applicationList = ref([]);

const fetchApplications = async () => {
  loading.value = true;
  try {
    const params = {
      page: 1, // You might want to add pagination later
      size: 100,
      status: searchForm.status || null,
      // meetingName and applicantName search are not implemented in backend yet
    };
    const response = await getMeetingApplications(params);
    if (response.data.code === 200) {
      let data = response.data.data;

      // Frontend search simulation for names, as backend does not support it yet
      if (searchForm.meetingName) {
        data = data.filter(item => item.meeting_name && item.meeting_name.includes(searchForm.meetingName));
      }
      if (searchForm.applicantName) {
        data = data.filter(item => item.applicant_name && item.applicant_name.includes(searchForm.applicantName));
      }

      applicationList.value = data.map(item => ({
        uuid: item.uuid,
        joinReason: item.join_reason,
        status: item.status,
        createdAt: item.created_at,
        meetingName: item.meeting_name,
        meetingUuid: item.meeting_uuid,
        applicantName: item.applicant_name,
        applicantUuid: item.applicant_uuid,
        applicantEmail: item.applicant_email
      }));
    } else {
      ElMessage.error(response.data.message || '获取申请列表失败');
    }
  } catch (error) {
    console.error('获取申请列表失败:', error);
    ElMessage.error('获取申请列表失败，请检查网络或联系管理员');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchApplications();
});

const handleSearch = () => {
  fetchApplications();
};

const handleReset = () => {
  searchForm.meetingName = '';
  searchForm.applicantName = '';
  searchForm.status = '';
  fetchApplications();
};

const handleReview = async (application, result) => {
  let comments = '';
  if (result === 'rejected') {
    try {
      const { value } = await ElMessageBox.prompt('请输入拒绝理由（可选）', '拒绝申请', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
      });
      comments = value || '';
    } catch {
      ElMessage.info('已取消操作');
      return;
    }
  }

  const reviewData = {
    part_uuid: application.uuid,
    review_result: result,
    comments: comments,
  };

  try {
    const response = await reviewMeetingApplication(reviewData);
    if (response.data.code === 200) {
      ElMessage.success(`已${result === 'approved' ? '批准' : '拒绝'}该申请`);
      fetchApplications(); // Refresh the list
    } else {
      ElMessage.error(response.data.message || '操作失败');
    }
  } catch (error) {
    console.error('审核操作失败:', error);
    ElMessage.error(error.response?.data?.message || '审核操作失败，请重试');
  }
};

const getStatusTagType = (status) => {
  switch (status) {
    case 'approved': return 'success';
    case 'pending': return 'warning';
    case 'rejected': return 'danger';
    default: return 'info';
  }
};
</script>

<style scoped>
.attendees-container {
  padding: 20px;
}
.search-area {
  margin-bottom: 20px;
}
.search-form .el-form-item {
  margin-bottom: 0;
}
</style>
