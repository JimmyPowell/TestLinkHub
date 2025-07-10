<template>
  <div class="project-page">
    <div class="page-header">
      <h2>项目管理</h2>
      <el-button type="primary">
        <el-icon><component is="Plus" /></el-icon>
        新建项目
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="项目名称">
          <el-input v-model="filterForm.name" placeholder="项目名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="项目状态" clearable>
            <el-option label="进行中" value="running"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="暂停" value="paused"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table
      :data="projectList"
      style="width: 100%"
      border
      v-loading="loading"
      class="project-table">
      <el-table-column prop="name" label="项目名称" min-width="160">
        <template #default="{ row }">
          <div class="project-name">
            <div class="project-icon" :style="{ backgroundColor: getRandomColor(row.name) }">
              {{ row.name.charAt(0).toUpperCase() }}
            </div>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="项目描述" min-width="200"></el-table-column>
      <el-table-column prop="lead" label="负责人" width="120"></el-table-column>
      <el-table-column prop="members" label="团队成员" width="100">
        <template #default="{ row }">
          {{ row.members }}人
        </template>
      </el-table-column>
      <el-table-column prop="progress" label="进度" width="180">
        <template #default="{ row }">
          <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)"></el-progress>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewProject(row)">查看</el-button>
          <el-button type="primary" link @click="editProject(row)">编辑</el-button>
          <el-dropdown trigger="click">
            <el-button type="primary" link>
              更多<el-icon class="el-icon--right"><component is="ArrowDown" /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="duplicateProject(row)">复制</el-dropdown-item>
                <el-dropdown-item @click="archiveProject(row)">归档</el-dropdown-item>
                <el-dropdown-item @click="deleteProject(row)" class="danger">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 筛选表单
const filterForm = reactive({
  name: '',
  status: ''
});

// 分页
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 加载状态
const loading = ref(false);

// 项目列表数据
const projectList = ref([
  {
    id: 1,
    name: '测联汇官网开发',
    description: '开发并上线测联汇官方网站，包括产品展示和注册功能',
    lead: '张三',
    members: 5,
    progress: 80,
    status: '进行中'
  },
  {
    id: 2,
    name: '移动端应用测试',
    description: '针对Android和iOS平台进行应用兼容性和功能测试',
    lead: '李四',
    members: 3,
    progress: 60,
    status: '进行中'
  },
  {
    id: 3,
    name: '后台管理系统',
    description: '开发企业内部使用的测试数据分析和管理系统',
    lead: '王五',
    members: 8,
    progress: 45,
    status: '进行中'
  },
  {
    id: 4,
    name: '性能测试平台',
    description: '搭建自动化性能测试环境和报告生成系统',
    lead: '赵六',
    members: 4,
    progress: 30,
    status: '暂停'
  },
  {
    id: 5,
    name: '接口文档工具',
    description: '开发REST API接口文档自动生成工具',
    lead: '张三',
    members: 2,
    progress: 100,
    status: '已完成'
  }
]);

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchProjectList();
};

const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchProjectList();
};

// 查询处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchProjectList();
};

// 重置筛选条件
const resetFilter = () => {
  filterForm.name = '';
  filterForm.status = '';
  currentPage.value = 1;
  fetchProjectList();
};

// 获取项目列表
const fetchProjectList = () => {
  loading.value = true;
  
  // 模拟接口调用
  setTimeout(() => {
    total.value = 5;
    loading.value = false;
  }, 500);
};

// 获取随机颜色 (用于项目图标)
const getRandomColor = (name) => {
  // 使用项目名称的首字母字符码作为种子
  const seed = name.charCodeAt(0);
  const colors = [
    '#409EFF', // 蓝色
    '#67C23A', // 绿色
    '#E6A23C', // 橙色
    '#F56C6C', // 红色
    '#909399', // 灰色
    '#8957E5', // 紫色
    '#19BE6B', // 翠绿色
    '#FF9900', // 橙黄色
    '#1989FA', // 深蓝色
    '#F7BA2A'  // 黄色
  ];
  return colors[seed % colors.length];
};

// 获取进度条状态
const getProgressStatus = (progress) => {
  if (progress >= 100) return 'success';
  if (progress >= 80) return 'warning';
  return '';
};

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case '进行中': return '';
    case '已完成': return 'success';
    case '暂停': return 'info';
    default: return '';
  }
};

// 查看项目
const viewProject = (row) => {
  ElMessage.info(`查看项目: ${row.name}`);
};

// 编辑项目
const editProject = (row) => {
  ElMessage.info(`编辑项目: ${row.name}`);
};

// 复制项目
const duplicateProject = (row) => {
  ElMessage.success(`复制项目: ${row.name}`);
};

// 归档项目
const archiveProject = (row) => {
  ElMessage.success(`归档项目: ${row.name}`);
};

// 删除项目
const deleteProject = (row) => {
  ElMessageBox.confirm(`确定要删除项目"${row.name}"吗？此操作不可逆`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success(`删除项目: ${row.name}`);
  }).catch(() => {});
};

// 组件挂载时获取数据
fetchProjectList();
</script>

<style scoped>
.project-page {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.project-table {
  margin-bottom: 20px;
}

.project-name {
  display: flex;
  align-items: center;
}

.project-icon {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  margin-right: 8px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.danger) {
  color: #f56c6c;
}

:deep(.danger:hover) {
  background-color: #fef0f0;
  color: #f56c6c;
}

:deep(.el-dropdown-link) {
  cursor: pointer;
}
</style> 