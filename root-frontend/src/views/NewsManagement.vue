<template>
  <div class="management-container">
    <div class="filters-container">
      <el-input v-model="filters.title" placeholder="新闻标题" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-input v-model="filters.companyName" placeholder="公司名称" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-select v-model="filters.status" placeholder="审核状态" class="filter-item" clearable @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="pending" />
        <el-option label="已通过" value="published" />
        <el-option label="已驳回" value="rejected" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-container">
      <el-table :data="newsList" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="新闻标题" min-width="250" />
        <el-table-column prop="company_name" label="所属公司" min-width="180" />
        <el-table-column prop="created_at" label="提交时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看详情</el-button>
            <template v-if="scope.row.status === 'pending'">
              <el-button size="small" type="success" @click="handleApprove(scope.row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(scope.row)">驳回</el-button>
            </template>
            <template v-else>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
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

    <NewsAuditDetailDrawer
      :visible="isDetailDrawerVisible"
      :news-uuid="selectedNewsUuid"
      @update:visible="isDetailDrawerVisible = $event"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElTag, ElMessage, ElMessageBox, ElPagination } from 'element-plus';
import newsService from '../services/newsService';
import NewsAuditDetailDrawer from '../components/NewsAuditDetailDrawer.vue';

const loading = ref(false);
const newsList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const selectedNewsUuid = ref(null);
const isDetailDrawerVisible = ref(false);

const filters = reactive({
  title: '',
  companyName: '',
  status: '',
});

const fetchAuditNews = async (page = 1) => {
  loading.value = true;
  try {
    const params = {
      page: page - 1,
      pageSize: pageSize.value,
      status: filters.status,
      // title and companyName filters are not supported by the audit endpoint yet
    };
    const response = await newsService.getAuditNews(params);
    // Assuming the response is an array directly under `data.data`
    newsList.value = response.data.data; 
    // Assuming pagination info is not available from this endpoint yet.
    // totalElements.value = response.data.data.total_elements; 
    // For now, use the length of the returned list.
    totalElements.value = response.data.data.length;
    currentPage.value = page;
  } catch (error) {
    ElMessage.error('获取待审核新闻列表失败: ' + (error.response?.data?.message || error.message));
    // Mock data on failure for UI development
    newsList.value = [
        { uuid: 'news-uuid-1', title: '公司发布重要产品更新', publisherName: '张三', companyName: '未来科技集团', createdAt: '2025-06-30 16:00:00' },
        { uuid: 'news-uuid-2', title: '季度财报亮点回顾', publisherName: '李四', companyName: '沈阳泠启网络科技有限公司', createdAt: '2025-06-30 15:30:00' },
    ];
    totalElements.value = newsList.value.length;
  } finally {
    loading.value = false;
  }
};

onMounted(fetchAuditNews);

const handlePageChange = (page) => fetchAuditNews(page);
const handleSearch = () => fetchAuditNews(1);
const handleReset = () => {
  Object.keys(filters).forEach(key => filters[key] = '');
  fetchAuditNews(1);
};

const handleApprove = (row) => {
  ElMessageBox.prompt('请输入通过理由（可选）', '通过新闻', {
    confirmButtonText: '确定通过',
    cancelButtonText: '取消',
    type: 'success',
  }).then(async ({ value }) => {
    try {
      await newsService.auditNews(row.uuid, { auditStatus: 'approved', comments: value });
      ElMessage.success('新闻已通过');
      fetchAuditNews(currentPage.value);
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
    }
  }).catch(() => {
    // User cancelled
  });
};

const handleReject = (row) => {
  ElMessageBox.prompt('请输入驳回理由', '驳回新闻', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '驳回理由不能为空',
  }).then(async ({ value }) => {
    try {
      await newsService.auditNews(row.uuid, { auditStatus: 'rejected', comments: value });
      ElMessage.success('新闻已驳回');
      fetchAuditNews(currentPage.value);
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
    }
  });
};

const handleView = (row) => {
  selectedNewsUuid.value = row.uuid;
  isDetailDrawerVisible.value = true;
};

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除新闻 "${row.title}" 吗？此操作不可撤销。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await newsService.deleteNewsAsRoot(row.uuid);
      ElMessage.success('新闻已删除');
      fetchAuditNews(currentPage.value); // 重新加载当前页数据
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message));
    }
  }).catch(() => {
    // 用户取消操作
    ElMessage.info('已取消删除');
  });
};

const getStatusTagType = (status) => {
  const types = {
    pending: 'warning',
    published: 'success',
    rejected: 'danger',
    archived: 'info',
  };
  return types[status] || 'info';
};

const getStatusText = (status) => {
  const texts = {
    pending: '待审核',
    published: '已通过',
    rejected: '已驳回',
    archived: '已归档',
  };
  return texts[status] || '未知';
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
