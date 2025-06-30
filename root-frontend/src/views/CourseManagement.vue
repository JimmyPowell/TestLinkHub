<template>
  <div class="management-container">
    <div class="filters-container">
      <el-input v-model="filters.name" placeholder="课程名称" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-input v-model="filters.companyName" placeholder="公司名称" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-select v-model="filters.status" placeholder="审核状态" class="filter-item" clearable @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="pending_review" />
        <el-option label="已发布" value="active" />
        <el-option label="已归档" value="archived" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-container">
      <el-table :data="courseList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="课程名称" min-width="250" />
        <el-table-column prop="company_name" label="所属公司" min-width="180" />
        <el-table-column prop="author_name" label="作者" min-width="150" />
        <el-table-column prop="created_at" label="提交时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">预览</el-button>
            <template v-if="scope.row.status === 'pending_review'">
              <el-button size="small" type="success" @click="handleApprove(scope.row)">通过</el-button>
              <el-button size="small" type="warning" @click="handleReject(scope.row)">驳回</el-button>
            </template>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <CourseDetailDrawer
      :visible="isDetailDrawerVisible"
      :course-uuid="selectedCourseUuid"
      @update:visible="isDetailDrawerVisible = $event"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElMessage, ElMessageBox, ElPagination } from 'element-plus';
import courseService from '../services/courseService';
import CourseDetailDrawer from '../components/CourseDetailDrawer.vue';

const loading = ref(false);
const courseList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const selectedCourseUuid = ref(null);
const isDetailDrawerVisible = ref(false);

const filters = reactive({
  name: '',
  companyName: '',
  status: '',
});

const fetchCourses = async (page = 1) => {
  loading.value = true;
  try {
    const params = {
      page: page - 1,
      size: pageSize.value,
      status: filters.status,
      name: filters.name,
      companyName: filters.companyName,
    };
    const response = await courseService.getReviewLessons(params);
    courseList.value = response.data.data.list;
    totalElements.value = response.data.data.total;
    currentPage.value = page;
  } catch (error) {
    ElMessage.error('获取待审核课程列表失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

onMounted(fetchCourses);

const handlePageChange = (page) => fetchCourses(page);
const handleSearch = () => fetchCourses(1);
const handleReset = () => {
  Object.keys(filters).forEach(key => filters[key] = '');
  fetchCourses(1);
};

const handleApprove = (row) => {
  console.log('准备通过课程:', row);
  ElMessageBox.prompt('请输入通过理由（可选）', '通过课程', {
    confirmButtonText: '确定通过',
    cancelButtonText: '取消',
    type: 'success',
  }).then(async ({ value }) => {
    try {
      console.log(`调用 approveLesson, UUID: ${row.uuid}, comment: ${value}`);
      await courseService.approveLesson(row.uuid, { auditStatus: 'approved', comment: value });
      ElMessage.success('课程已通过');
      fetchCourses(currentPage.value);
    } catch (error) {
      console.error('审批课程失败:', error);
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
    }
  }).catch(() => {});
};

const handleReject = (row) => {
  ElMessageBox.prompt('请输入驳回理由', '驳回课程', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '驳回理由不能为空',
  }).then(async ({ value }) => {
    try {
      await courseService.approveLesson(row.uuid, { auditStatus: 'rejected', comment: value });
      ElMessage.success('课程已驳回');
      fetchCourses(currentPage.value);
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
    }
  });
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除课程 "${row.name}" 吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await courseService.deleteLessons([row.uuid]);
      ElMessage.success('课程已删除');
      fetchCourses(currentPage.value);
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message));
    }
  }).catch(() => {});
};

const handleView = (row) => {
  selectedCourseUuid.value = row.uuid;
  isDetailDrawerVisible.value = true;
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
