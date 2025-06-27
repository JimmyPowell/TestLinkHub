<template>
  <div class="members-management">
    <div class="header-info">
      <p>您的身份是xxxxxx的管理者，可以管理本公司内的成员信息。</p>
    </div>

    <div class="filters">
      <el-input v-model="filters.uuid" placeholder="用户UUID" class="filter-item"></el-input>
      <el-input v-model="filters.username" placeholder="用户名" class="filter-item"></el-input>
      <el-select v-model="filters.status" placeholder="用户状态" class="filter-item">
        <el-option label="全部" value=""></el-option>
        <el-option label="正常" value="1"></el-option>
        <el-option label="禁用" value="0"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleCreate">新增</el-button>
      <el-button type="warning" @click="handleBatchOperation">批量操作</el-button>
    </div>

    <el-table :data="users" style="width: 100%" class="user-table">
      <el-table-column prop="uuid" label="UUID">
        <template #default="scope">
          <el-tooltip :content="scope.row.uuid" placement="top">
            <span @click="copyToClipboard(scope.row.uuid)" style="cursor: pointer;">
              {{ truncateUUID(scope.row.uuid) }}
            </span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="email" label="邮箱地址" min-width="180">
        <template #default="scope">
          <el-tooltip :content="scope.row.email" placement="top">
            <span>{{ scope.row.email }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="registrationTime" label="注册时间"></el-table-column>
      <el-table-column prop="description" label="个人描述"></el-table-column>
      <el-table-column prop="registrationLocation" label="注册地点"></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleRemove(scope.row)">移出</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElInput, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElTag, ElTooltip, ElMessage } from 'element-plus';

const filters = reactive({
  uuid: '',
  username: '',
  status: '',
});

const users = ref([
  // 示例数据
  {
    uuid: 'a1b2c3d4-e5f6-7890-1234-567890abcdef',
    username: '张三',
    email: 'zhangsan@example.com',
    phone: '13800138000',
    status: 1,
    registrationTime: '2023-01-15 10:30:00',
    description: '一个活跃的用户',
    registrationLocation: '北京',
  },
  {
    uuid: 'b2c3d4e5-f6a7-8901-2345-67890abcdef0',
    username: '李四',
    email: 'lisi@example.com',
    phone: '13900139000',
    status: 0,
    registrationTime: '2023-02-20 14:00:00',
    description: '因违规被禁用',
    registrationLocation: '上海',
  },
]);

const handleSearch = () => {
  console.log('Searching with filters:', filters);
  // 在这里实现搜索逻辑
};

const handleReset = () => {
  filters.uuid = '';
  filters.username = '';
  filters.status = '';
  console.log('Filters reset');
  // 在这里实现重置逻辑
};

const handleCreate = () => {
  console.log('Create new user');
  // 在这里实现新增逻辑
};

const handleBatchOperation = () => {
  console.log('Batch operation');
  // 在这里实现批量操作逻辑
};

const handleView = (user) => {
  console.log('Viewing user:', user);
  // 在这里实现查看逻辑
};

const handleEdit = (user) => {
  console.log('Editing user:', user);
  // 在这里实现编辑逻辑
};

const handleRemove = (user) => {
  console.log('Removing user:', user);
  // 在这里实现移除逻辑
};

const truncateUUID = (uuid) => {
  if (uuid.length > 13) {
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
  margin-bottom: 10px; /* 减少下边距，使文本上移 */
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
</style>
