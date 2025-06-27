<template>
  <div class="members-management">
    <div class="header-info">
      <p>您好！作为 {{ authStore.userName }} 的管理者，您可以管理本公司内的成员信息。</p>
    </div>

    <div class="filters">
      <el-input v-model="searchForm.uuid" placeholder="用户UUID" class="filter-item"></el-input>
      <el-input v-model="searchForm.username" placeholder="用户名" class="filter-item"></el-input>
      <el-input v-model="searchForm.phoneNumber" placeholder="手机号" class="filter-item"></el-input>
      <el-select v-model="searchForm.status" placeholder="用户状态" class="filter-item" clearable>
        <el-option label="正常" value="ACTIVE"></el-option>
        <el-option label="禁用" value="INACTIVE"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleCreate">新增</el-button>
      <el-button type="warning" @click="handleBatchOperation">批量操作</el-button>
    </div>

    <el-table :data="members" style="width: 100%" class="user-table">
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
      <el-table-column prop="updated_at" label="更新时间">
        <template #default="scope">
          <span>{{ formatDateTime(scope.row.updated_at) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="个人描述"></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleRemove(scope.row)">移出</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-if="pagination.totalItems > 0"
        background
        layout="prev, pager, next, total"
        :total="pagination.totalItems"
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        @current-change="fetchMembersList"
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
import { reactive, onMounted, computed, ref } from 'vue';
import { useAuthStore } from '../../store/auth';
import userService from '../../services/userService';
import UserDetailDrawer from '../../components/dashboard/UserDetailDrawer.vue';
import UserFormDrawer from '../../components/dashboard/UserFormDrawer.vue';
import { formatDateTime } from '../../utils/format';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElTag, ElTooltip, ElMessage, ElPagination, ElMessageBox } from 'element-plus';

const authStore = useAuthStore();

const isDetailDrawerVisible = ref(false);
const isFormDrawerVisible = ref(false);
const selectedUser = ref(null);
const isEditMode = ref(false);

const searchForm = reactive({
  uuid: '',
  username: '',
  status: '',
  phoneNumber: '',
});

const members = computed(() => authStore.members);
const pagination = computed(() => authStore.pagination);

const fetchMembersList = (page = 1) => {
  // el-pagination's current-page is 1-based, our API is 0-based.
  const apiPage = page - 1;
  const { uuid, username, status, phoneNumber } = searchForm;
  const size = pagination.value.pageSize;
  authStore.fetchMembers({ page: apiPage, size, uuid, username, status, phoneNumber });
};

onMounted(() => {
  // Fetch the first page on component mount
  fetchMembersList(1);
});

const handleSearch = () => {
  // When searching, always go back to the first page
  fetchMembersList(1);
};

const handleReset = () => {
  searchForm.uuid = '';
  searchForm.username = '';
  searchForm.status = '';
  searchForm.phoneNumber = '';
  fetchMembersList(1);
};

const handleCreate = () => {
  isEditMode.value = false;
  selectedUser.value = null; // Ensure form is empty
  isFormDrawerVisible.value = true;
};

const handleBatchOperation = () => {
  console.log('Batch operation');
};

const handleView = async (user) => {
  console.log('Viewing user:', user);
  selectedUser.value = null; // Reset first to show loading state
  isDetailDrawerVisible.value = true;
  try {
    const response = await userService.getUserDetails(user.uuid);
    selectedUser.value = response.data.data;
  } catch (error) {
    console.error('Failed to fetch user details:', error);
    ElMessage.error('获取用户详情失败');
    isDetailDrawerVisible.value = false;
  }
};

const handleFormSubmit = async (formData) => {
  try {
    // The form data is in camelCase, the service will convert it to snake_case
    if (isEditMode.value) {
      await userService.updateCompanyUser(selectedUser.value.uuid, formData);
      ElMessage.success('用户更新成功');
    } else {
      await userService.createCompanyUser(formData);
      ElMessage.success('用户创建成功');
    }
    isFormDrawerVisible.value = false;
    fetchMembersList(pagination.value.currentPage); // Refresh the current page
  } catch (error) {
    console.error('Failed to submit user form:', error);
    ElMessage.error(error.response?.data?.message || '操作失败');
  }
};

const handleEdit = (user) => {
  isEditMode.value = true;
  // Use the user data directly from the table row, which includes company_id
  // Note: We need to convert snake_case from the list to camelCase for the form
  const camelCaseUser = {
    ...user,
    phoneNumber: user.phone_number,
    avatarUrl: user.avatar_url,
    companyId: user.company_id,
    postCount: user.post_count,
    lessonCount: user.lesson_count,
    meetingCount: user.meeting_count,
    createdAt: user.created_at,
    updatedAt: user.updated_at,
  };
  selectedUser.value = camelCaseUser;
  isFormDrawerVisible.value = true;
};

const handleRemove = (user) => {
  ElMessageBox.confirm(
    `确定要将用户 "${user.name}" 从组织中移出吗？此操作不可逆。`,
    '警告',
    {
      confirmButtonText: '确定移出',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await userService.removeUserFromCompany(user.uuid);
      ElMessage.success('用户已成功移出');
      fetchMembersList(pagination.value.currentPage); // Refresh the list
    } catch (error) {
      console.error('Failed to remove user:', error);
      ElMessage.error(error.response?.data?.message || '移出用户失败');
    }
  }).catch(() => {
    ElMessage.info('操作已取消');
  });
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
.members-management {
  padding: 20px;
}

.header-info {
  margin-bottom: 10px;
}

.filters {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.filter-item {
  width: 200px;
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
