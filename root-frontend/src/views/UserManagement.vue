<template>
  <div class="user-management-container">
    <div class="filters-container">
      <el-input v-model="filters.uuid" placeholder="用户UUID" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-input v-model="filters.username" placeholder="用户名" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-input v-model="filters.phoneNumber" placeholder="手机号" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-input v-model="filters.companyName" placeholder="公司名称" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-input v-model="filters.companyUuid" placeholder="公司UUID" class="filter-item" clearable @keyup.enter="handleSearch" />
      <el-select v-model="filters.status" placeholder="用户状态" class="filter-item" clearable @change="handleSearch">
        <el-option label="正常" value="ACTIVE" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="暂停" value="SUSPENDED" />
      </el-select>
      <div class="button-group">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleCreate">新增</el-button>
        <el-button type="warning" @click="handleBatchOperation">批量操作</el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="uuid" label="UUID" width="140">
           <template #default="scope">
            <span class="uuid-cell" @click="copyToClipboard(scope.row.uuid)">{{ truncateUUID(scope.row.uuid, 12) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱地址" min-width="200" />
        <el-table-column prop="phone_number" label="手机号" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updated_at" label="更新时间" width="180" />
        <el-table-column prop="description" label="个人描述" min-width="200" />
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

    <UserDetailDrawer
      :user="selectedUser"
      :visible="isDetailDrawerVisible"
      @update:visible="isDetailDrawerVisible = $event"
    />

    <UserFormDrawer
      :visible="isFormDrawerVisible"
      :is-edit-mode="isEditMode"
      :user-data="selectedUser"
      @update:visible="isFormDrawerVisible = $event"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElTag, ElMessage, ElMessageBox, ElPagination } from 'element-plus';
import userService from '../services/userService';
import UserDetailDrawer from '../components/UserDetailDrawer.vue';
import UserFormDrawer from '../components/UserFormDrawer.vue';

const loading = ref(false);
const users = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const selectedUser = ref(null);
const isDetailDrawerVisible = ref(false);
const isFormDrawerVisible = ref(false);
const isEditMode = ref(false);

const filters = reactive({
  uuid: '',
  username: '',
  phoneNumber: '',
  status: '',
  companyName: '',
  companyUuid: '',
});

const fetchUsers = async (page = 1) => {
  loading.value = true;
  try {
    const params = {
      page: page - 1,
      size: pageSize.value,
    };
    // Add filters to params if they have a value
    for (const key in filters) {
      if (filters[key]) {
        params[key] = filters[key];
      }
    }
    const response = await userService.getUsers(params);
    users.value = response.data.data.content;
    totalElements.value = response.data.data.total_elements;
    currentPage.value = response.data.data.page_number + 1;
  } catch (error) {
    ElMessage.error('获取用户列表失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchUsers();
});

const handlePageChange = (page) => {
  fetchUsers(page);
};

const handleSearch = () => {
  fetchUsers(1); // Search always goes to the first page
};

const handleReset = () => {
  Object.keys(filters).forEach(key => filters[key] = '');
  fetchUsers(1);
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.name}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await userService.deleteUser(row.uuid);
      ElMessage.success('用户删除成功');
      fetchUsers(currentPage.value); // Refresh current page
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message));
    }
  }).catch(() => {
    // User cancelled
  });
};

const handleCreate = () => {
  isEditMode.value = false;
  selectedUser.value = {}; // Start with an empty object for a new user
  isFormDrawerVisible.value = true;
};

const handleEdit = (user) => {
  isEditMode.value = true;
  // Create a deep copy for editing to avoid modifying the table data directly
  selectedUser.value = JSON.parse(JSON.stringify(user));
  isFormDrawerVisible.value = true;
};

const handleFormSubmit = async (formData) => {
  try {
    if (isEditMode.value) {
      await userService.updateUser(formData.uuid, formData);
      ElMessage.success('用户更新成功');
    } else {
      await userService.createUser(formData);
      ElMessage.success('用户创建成功');
    }
    isFormDrawerVisible.value = false;
    fetchUsers(currentPage.value);
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
  }
};

const handleBatchOperation = () => ElMessage.info('批量操作待实现');

const handleView = (user) => {
  selectedUser.value = user;
  isDetailDrawerVisible.value = true;
};

const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('UUID 已复制到剪贴板');
  }).catch(err => {
    ElMessage.error('复制失败: ', err);
  });
};

const truncateUUID = (uuid, length = 12) => {
  if (!uuid) return '';
  const truncated = uuid.substring(0, length);
  return uuid.length > length ? `${truncated}...` : uuid;
};

const getStatusTagType = (status) => {
  switch (status) {
    case 'ACTIVE': return 'success';
    case 'PENDING': return 'warning';
    case 'SUSPENDED': return 'danger';
    default: return 'info';
  }
};

const getStatusText = (status) => {
    switch (status) {
        case 'ACTIVE': return '正常';
        case 'PENDING': return '待审核';
        case 'SUSPENDED': return '暂停';
        default: return '未知';
    }
}
</script>

<style scoped>
.user-management-container {
  padding: 24px;
  background-color: #f5f7fa;
  height: 100%;
}
.filters-container {
  background-color: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
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
.table-container {
  margin-top: 16px;
  background-color: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.uuid-cell {
  font-family: 'Courier New', Courier, monospace;
  color: #333;
  cursor: pointer;
}
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  background-color: #fff;
  padding: 16px 0;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
</style>
