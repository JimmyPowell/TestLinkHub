<template>
  <div class="management-container">
    <div class="filters-container">
      <el-input v-model="filters.name" placeholder="会议名称" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-select v-model="filters.status" placeholder="审核状态" class="filter-item" clearable @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-container">
      <el-table :data="meetingList" style="width: 100%" v-loading="loading">
        <el-table-column prop="meetingName" label="会议名称" min-width="200" />
        <el-table-column prop="description" label="会议描述" min-width="150" />
        <el-table-column prop="initiatorName" label="发起者" min-width="150" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="scope">
            <div style="display: flex; flex-wrap: nowrap; align-items: center; gap: 8px;">
              <el-button size="small" @click="handleView(scope.row)">预览</el-button>
              <el-button v-if="scope.row.status === 'pending_review'" size="small" type="success" @click="handleApprove(scope.row)">通过</el-button>
              <el-button v-if="scope.row.status === 'pending_review'" size="small" type="warning" @click="handleReject(scope.row)">驳回</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-container">
        <el-pagination
            v-if="totalElements > 0"
            background
            layout="prev, pager, next, total"
            :total="totalElements"
            :current-page="currentPage"
            :page-size="pageSize"
            @current-change="handlePageChange"
        />
    </div>

    <MeetingDetailDrawer
      :visible="isDetailDrawerVisible"
      :meeting-uuid="selectedMeetingUuid"
      @update:visible="isDetailDrawerVisible = $event"
      @review-completed="handleReviewCompleted"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElMessage, ElMessageBox, ElPagination } from 'element-plus';
import meetingService from '../services/meetingService';
import MeetingDetailDrawer from '../components/MeetingDetailDrawer.vue';
import { convertToCamelCase } from '../utils/caseConverter';

const loading = ref(false);
const meetingList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const selectedMeetingUuid = ref(null);
const isDetailDrawerVisible = ref(false);

const filters = reactive({
  name: '',
  status: '',
});

const fetchMeetings = async (page = 1) => {
  loading.value = true;
  try {
    const params = {
      page: page - 1,
      size: pageSize.value,
      status: filters.status,
      name: filters.name,
    };
    const response = await meetingService.getReviewMeetings(params);
    const camelCaseData = convertToCamelCase(response.data.data);

    meetingList.value = camelCaseData.map(item => ({
      ...item,
      meetingName: item.name,
      initiatorName: item.initiatorName,
    }));
    
    totalElements.value = response.data.total;
    currentPage.value = page;
  } catch (error) {
    ElMessage.error('获取待审核会议列表失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

onMounted(fetchMeetings);

const handlePageChange = (page) => fetchMeetings(page);
const handleSearch = () => fetchMeetings(1);
const handleReset = () => {
  Object.keys(filters).forEach(key => filters[key] = '');
  fetchMeetings(1);
};

const handleApprove = (row) => {
  ElMessageBox.prompt('请输入通过理由（可选）', '通过会议', {
    confirmButtonText: '确定通过',
    cancelButtonText: '取消',
    type: 'success',
  }).then(async ({ value }) => {
    try {
      const reviewData = {
        meeting_version_uuid: row.uuid,
        audit_status: 'APPROVED',
        comments: value,
      };
      await meetingService.reviewMeeting(reviewData);
      ElMessage.success('会议已通过');
      fetchMeetings(currentPage.value);
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
    }
  }).catch(() => {});
};

const handleReject = (row) => {
  ElMessageBox.prompt('请输入驳回理由', '驳回会议', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '驳回理由不能为空',
  }).then(async ({ value }) => {
    try {
      const reviewData = {
        meeting_version_uuid: row.uuid,
        audit_status: 'REJECTED',
        comments: value,
      };
      await meetingService.reviewMeeting(reviewData);
      ElMessage.success('会议已驳回');
      fetchMeetings(currentPage.value);
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
    }
  });
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除会议 "${row.meetingName}" 吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await meetingService.deleteMeetings(row.meetingUuid);
      ElMessage.success('会议已删除');
      fetchMeetings(currentPage.value);
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message));
    }
  }).catch(() => {});
};

const handleView = (row) => {
  selectedMeetingUuid.value = row.uuid;
  isDetailDrawerVisible.value = true;
};

const handleReviewCompleted = () => {
  isDetailDrawerVisible.value = false;
  fetchMeetings(currentPage.value);
};
</script>

<style scoped>
.management-container {
  padding: 24px;
  background-color: #f5f7fa;
  height: 100%;
}
.filters-container, .table-container, .pagination-container {
  background-color: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.filters-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}
.filter-item {
  width: 220px;
}
.table-container, .pagination-container {
  margin-top: 16px;
}
.pagination-container {
  display: flex;
  justify-content: center;
}
</style>
