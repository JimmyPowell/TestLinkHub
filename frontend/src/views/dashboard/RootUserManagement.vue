<template>
  <div class="root-user-management">
    <div class="header-info">
      <p>您好！作为超级管理员，您可以管理系统中的所有用户信息。</p>
    </div>

    <div class="filters">
      <el-input v-model="searchForm.uuid" placeholder="用户UUID" class="filter-item"></el-input>
      <el-input v-model="searchForm.name" placeholder="用户名" class="filter-item"></el-input>
      <el-input v-model="searchForm.email" placeholder="邮箱" class="filter-item"></el-input>
      <el-input v-model="searchForm.phoneNumber" placeholder="手机号" class="filter-item"></el-input>
      <el-input v-model="searchForm.companyName" placeholder="公司名" class="filter-item"></el-input>
      <el-select v-model="searchForm.status" placeholder="用户状态" class="filter-item" clearable>
        <el-option label="正常" value="ACTIVE"></el-option>
        <el-option label="禁用" value="INACTIVE"></el-option>
      </el-select>
      <el-select v-model="searchForm.role" placeholder="用户角色" class="filter-item" clearable>
        <el-option label="普通用户" value="USER"></el-option>
        <el-option label="管理员" value="ADMIN"></el-option>
        <el-option label="超级管理员" value="ROOT"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="users" style="width: 100%" class="user-table">
      <el-table-column prop="uuid" label="UUID" min-width="150">
        <template #default="scope">
          <el-tooltip :content="scope.row.uuid" placement="top">
            <span @click="copyToClipboard(scope.row.uuid)" style="cursor: pointer;">
              {{ truncateUUID(scope.row.uuid) }}
            </span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="用户名"></el-table-column>
      <el-table-column prop="company_name" label="公司名"></el-table-column>
      <el-table-column prop="email" label="邮箱地址" min-width="180">
        <template #default="scope">
          <el-tooltip :content="scope.row.email" placement="top">
            <span>{{ scope.row.email }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="phone_number" label="手机号"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ scope.row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色">
        <template #default="scope">
          <el-tag>{{ scope.row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updated_at" label="更新时间">
        <template #default="scope">
          <span>{{ formatDateTime(scope.row.updated_at) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-if="pagination.total_elements > 0"
        background
        layout="prev, pager, next, total"
        :total="pagination.total_elements"
        :current-page="pagination.page_number + 1"
        :page-size="pagination.page_size"
        @current-change="fetchUsers"
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
import { reactive, onMounted, ref } from 'vue';
import userService from '../../services/userService';
import UserDetailDrawer from '../../components/dashboard/UserDetailDrawer.vue';
import UserFormDrawer from '../../components/dashboard/UserFormDrawer.vue';
import { formatDateTime } from '../../utils/format';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElTag, ElTooltip, ElMessage, ElPagination } from 'element-plus';

const isDetailDrawerVisible = ref(false);
const isFormDrawerVisible = ref(false);
const selectedUser = ref(null);
const isEditMode = ref(false);
const users = ref([]);
const pagination = ref({
  page_number: 0,
  page_size: 10,
  total_elements: 0,
  total_pages: 0,
});

const searchForm = reactive({
  uuid: '',
  name: '',
  email: '',
  phoneNumber: '',
  companyName: '',
  status: '',
  role: '',
});

const fetchUsers = async (page = 1) => {
  try {
    const apiPage = page - 1;
    const response = await userService.getRootUsers({ ...searchForm, page: apiPage, size: pagination.value.page_size });
    users.value = response.data.data.content;
    pagination.value = response.data.data;
  } catch (error) {
    console.error('Failed to fetch users:', error);
    ElMessage.error('获取用户列表失败');
  }
};

onMounted(() => {
  fetchUsers(1);
});

const handleSearch = () => {
  fetchUsers(1);
};

const handleReset = () => {
  Object.keys(searchForm).forEach(key => searchForm[key] = '');
  fetchUsers(1);
};

const handleView = async (user) => {
  selectedUser.value = null;
  isDetailDrawerVisible.value = true;
  try {
    // Assuming there's a generic user detail fetcher, or we can just use the row data
    // For now, we'll just display the data we have.
    selectedUser.value = user;
  } catch (error) {
    console.error('Failed to fetch user details:', error);
    ElMessage.error('获取用户详情失败');
    isDetailDrawerVisible.value = false;
  }
};

const handleEdit = (user) => {
  isEditMode.value = true;
  const camelCaseUser = {
    ...user,
    phoneNumber: user.phone_number,
    avatarUrl: user.avatar_url,
    companyId: user.company_id,
    companyName: user.company_name,
  };
  selectedUser.value = camelCaseUser;
  isFormDrawerVisible.value = true;
};

const handleFormSubmit = async (formData) => {
  try {
    if (isEditMode.value) {
      // TODO: Need a root-level user update endpoint
      // await userService.updateRootUser(selectedUser.value.uuid, formData);
      ElMessage.success('用户更新成功（模拟）');
    } else {
      // TODO: Need a root-level user create endpoint
      // await userService.createRootUser(formData);
      ElMessage.success('用户创建成功（模拟）');
    }
    isFormDrawerVisible.value = false;
    fetchUsers(pagination.value.page_number + 1);
  } catch (error) {
    console.error('Failed to submit user form:', error);
    ElMessage.error(error.response?.data?.message || '操作失败');
  }
};

const truncateUUID = (uuid) => {
  if (uuid && uuid.length > 13) {
    return uuid.substring(0, 8) + '...' + uuid.substring(uuid.length - 4);
  }
  return uuid;
};

const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('UUID已复制到剪贴板');
  }).catch(err => {
    ElMessage.error('复制失败');
    console.error('Could not copy text: ', err);
  });
};
</script>

<style scoped>
.root-user-management {
  padding: 20px;
}

.header-info {
  margin-bottom: 10px;
}

.filters {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-item {
  width: 180px;
}

.user-table {
  margin-top: 20px;
}

.user-table :deep(.el-table__cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
