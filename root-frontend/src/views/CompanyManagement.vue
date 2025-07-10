<template>
  <div class="management-container">
    <div class="filters-container">
      <el-input v-model="filters.uuid" placeholder="公司UUID" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-input v-model="filters.name" placeholder="公司名称" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-input v-model="filters.companyCode" placeholder="公司代码" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-select v-model="filters.status" placeholder="公司状态" class="filter-item" clearable @change="handleSearch">
        <el-option label="正常" value="ACTIVE" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="暂停" value="SUSPENDED" />
      </el-select>
      <div class="button-group">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleCreate">新增公司</el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="companies" style="width: 100%" v-loading="loading">
        <el-table-column prop="uuid" label="公司UUID" width="140">
           <template #default="scope">
            <span class="uuid-cell" @click="copyToClipboard(scope.row.uuid)">{{ truncateUUID(scope.row.uuid) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="公司名称" min-width="180" />
        <el-table-column prop="company_code" label="公司代码" width="150" />
        <el-table-column prop="email" label="邮箱地址" min-width="200" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updated_at" label="更新时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
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

    <CompanyDetailDrawer
      :company="selectedCompany"
      :visible="isDetailDrawerVisible"
      @update:visible="isDetailDrawerVisible = $event"
    />

    <CompanyFormDrawer
      :visible="isFormDrawerVisible"
      :is-edit-mode="isEditMode"
      :company-data="selectedCompany"
      @update:visible="isFormDrawerVisible = $event"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElTag, ElMessage, ElMessageBox, ElPagination } from 'element-plus';
import companyService from '../services/companyService';
import CompanyDetailDrawer from '../components/CompanyDetailDrawer.vue';
import CompanyFormDrawer from '../components/CompanyFormDrawer.vue';

const loading = ref(false);
const companies = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const selectedCompany = ref(null);
const isDetailDrawerVisible = ref(false);
const isFormDrawerVisible = ref(false);
const isEditMode = ref(false);

const filters = reactive({
  uuid: '',
  name: '',
  companyCode: '',
  status: '',
});

const fetchCompanies = async (page = 1) => {
  loading.value = true;
  try {
    const params = {
      page: page - 1,
      size: pageSize.value,
    };
    for (const key in filters) {
      if (filters[key]) {
        params[key] = filters[key];
      }
    }
    const response = await companyService.getCompanies(params);
    companies.value = response.data.data.content;
    totalElements.value = response.data.data.total_elements;
    currentPage.value = response.data.data.page_number + 1;
  } catch (error) {
    ElMessage.error('获取公司列表失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

onMounted(fetchCompanies);

const handlePageChange = (page) => fetchCompanies(page);
const handleSearch = () => fetchCompanies(1);
const handleReset = () => {
  Object.keys(filters).forEach(key => filters[key] = '');
  fetchCompanies(1);
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除公司 "${row.name}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await companyService.deleteCompany(row.uuid);
      ElMessage.success('公司删除成功');
      fetchCompanies(currentPage.value);
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message));
    }
  });
};

const handleCreate = () => {
  isEditMode.value = false;
  selectedCompany.value = { status: 'PENDING' };
  isFormDrawerVisible.value = true;
};

const handleView = (company) => {
  selectedCompany.value = company;
  isDetailDrawerVisible.value = true;
};

const handleEdit = (company) => {
  isEditMode.value = true;
  selectedCompany.value = { ...company };
  isFormDrawerVisible.value = true;
};

const handleFormSubmit = async (formData) => {
  try {
    if (isEditMode.value) {
      await companyService.updateCompany(formData.uuid, formData);
      ElMessage.success('公司更新成功');
    } else {
      await companyService.createCompany(formData);
      ElMessage.success('公司创建成功');
    }
    isFormDrawerVisible.value = false;
    fetchCompanies(currentPage.value);
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
  }
};

const copyToClipboard = (text) => navigator.clipboard.writeText(text).then(() => ElMessage.success('UUID 已复制'));
const truncateUUID = (uuid) => uuid && uuid.length > 12 ? `${uuid.substring(0, 12)}...` : uuid;
const getStatusTagType = (status) => ({ 'ACTIVE': 'success', 'PENDING': 'warning', 'SUSPENDED': 'danger' }[status] || 'info');
const getStatusText = (status) => ({ 'ACTIVE': '正常', 'PENDING': '待审核', 'SUSPENDED': '暂停' }[status] || '未知');
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
  width: 200px;
}
.button-group {
  margin-left: auto;
}
.table-container, .pagination-container {
  margin-top: 16px;
}
.uuid-cell {
  font-family: 'Courier New', Courier, monospace;
  cursor: pointer;
}
.pagination-container {
  display: flex;
  justify-content: center;
}
</style>
