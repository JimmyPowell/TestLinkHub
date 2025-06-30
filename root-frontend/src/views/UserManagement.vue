<template>
  <div class="user-management-container">
    <div class="filters-container">
      <el-input v-model="filters.uuid" placeholder="用户UUID" class="filter-item" clearable />
      <el-input v-model="filters.name" placeholder="用户名" class="filter-item" clearable />
      <el-input v-model="filters.phoneNumber" placeholder="手机号" class="filter-item" clearable />
      <el-select v-model="filters.status" placeholder="用户状态" class="filter-item" clearable>
        <el-option label="正常" value="ACTIVE" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="暂停" value="SUSPENDED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleCreate">新增</el-button>
      <el-button type="warning" @click="handleBatchOperation">批量操作</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" border>
      <el-table-column prop="uuid" label="UUID" width="180">
         <template #default="scope">
          <el-tooltip :content="scope.row.uuid" placement="top">
            <span class="uuid-cell" @click="copyToClipboard(scope.row.uuid)">
              {{ truncateUUID(scope.row.uuid, 12) }}
            </span>
          </el-tooltip>
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
          <el-button size="small" type="danger" @click="handleRemove(scope.row)">移出</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
        <el-pagination
            background
            layout="prev, pager, next, total"
            :total="totalElements"
            :current-page="currentPage"
            :page-size="pageSize"
            @current-change="handlePageChange"
        />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElTag, ElTooltip, ElMessage, ElPagination } from 'element-plus';

// 筛选条件的数据模型
const filters = reactive({
  uuid: '',
  name: '',
  phoneNumber: '',
  status: '',
});

// 事件处理函数 (目前仅为占位符)
const handleSearch = () => {
  console.log('执行查询:', filters);
};

const handleReset = () => {
  Object.keys(filters).forEach(key => {
    filters[key] = '';
  });
  console.log('重置筛选条件');
};

const handleCreate = () => {
  console.log('打开新增用户对话框');
};

const handleEdit = (row) => {
  console.log('编辑用户:', row);
};

const handleRemove = (row) => {
  console.log('移除用户:', row);
};

const handleView = (row) => {
  console.log('查看用户:', row);
};

const handleBatchOperation = () => {
  console.log('执行批量操作');
};

// 模拟的API返回数据
const mockApiResponse = {
    "content": [
        { "uuid": "4d6b14b1-0510-4b62-9933-71537af32abc", "name": "22", "email": "293753419822@qq.com", "phone_number": "111111", "status": "ACTIVE", "updated_at": "2025-06-30 14:26:25", "description": "1" },
        { "uuid": "a1b2c3d4-0002-4002-8002-000000000002", "name": "Manager Li", "email": "manager.li@examplecorp.com", "phone_number": "18800000002", "status": "ACTIVE", "updated_at": "2025-06-30 14:25:52", "description": "Example Corp 的项目经理。" },
        { "uuid": "a1b2c3d4-0003-4003-8003-000000000003", "name": "Active Zhang", "email": "active.zhang@examplecorp.com", "phone_number": "18800000003", "status": "ACTIVE", "updated_at": "2025-06-30 14:25:52", "description": "一位活跃的普通用户。" },
        { "uuid": "a1b2c3d4-0004-4004-8004-000000000004", "name": "Pending Wang", "email": "pending.wang@newuser.com", "phone_number": "18800000004", "status": "PENDING", "updated_at": "2025-06-30 14:25:52", "description": "等待管理员审核的新注册用户。" },
        { "uuid": "a1b2c3d4-0005-4005-8005-000000000005", "name": "Suspended Zhao", "email": "suspended.zhao@olduser.com", "phone_number": "18800000005", "status": "SUSPENDED", "updated_at": "2025-06-30 14:25:52", "description": "因违反规定被暂停使用的用户。" },
        { "uuid": "a1b2c3d4-0001-4001-8001-000000000001", "name": "Admin User", "email": "admin@testlinkhub.com", "phone_number": "18800000001", "status": "ACTIVE", "updated_at": "2025-06-30 14:25:51", "description": "系统最高管理员，拥有所有权限。" }
    ],
    "total_elements": 6
};

const allUsers = ref(mockApiResponse.content);
const totalElements = ref(mockApiResponse.total_elements);
const currentPage = ref(1);
const pageSize = ref(10); // Or whatever page size you want

const tableData = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return allUsers.value.slice(start, end);
});

const handlePageChange = (page) => {
    currentPage.value = page;
};

// 辅助函数
const truncateUUID = (uuid, length = 8) => {
  if (!uuid) return '';
  const truncated = uuid.substring(0, length);
  return uuid.length > length ? `${truncated}...` : truncated;
};

const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('UUID 已复制到剪贴板');
  }).catch(err => {
    ElMessage.error('复制失败: ', err);
  });
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
  padding: 20px;
}

.filters-container {
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
  gap: 10px;
  margin-bottom: 20px;
}

.filter-item {
  width: 160px; /* 减小输入框宽度 */
}

.uuid-cell {
  cursor: pointer;
  color: #409eff;
  font-family: 'Courier New', Courier, monospace;
}

.pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
