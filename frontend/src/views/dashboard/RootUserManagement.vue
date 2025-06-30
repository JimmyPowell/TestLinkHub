<template>
  <div class="root-user-management-page">
    <div class="filters-container">
      <el-input v-model="filters.userUuid" placeholder="用户UUID" class="filter-item"></el-input>
      <el-input v-model="filters.userName" placeholder="用户名称" class="filter-item"></el-input>
      <el-input v-model="filters.companyUuid" placeholder="用户公司UUID" class="filter-item"></el-input>
      <el-input v-model="filters.companyName" placeholder="用户公司名称" class="filter-item"></el-input>
      <el-select v-model="filters.userStatus" placeholder="用户状态" class="filter-item">
        <el-option label="全部" value=""></el-option>
        <el-option label="正常" value="active"></el-option>
        <el-option label="禁用" value="inactive"></el-option>
        <el-option label="待审核" value="pending"></el-option>
        <el-option label="暂停" value="suspended"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleCreate">新增</el-button>
    </div>

    <!-- 用户数据显示表格将在此处实现 -->
    <div class="table-placeholder">
      <!-- <el-table :data="users"> ... </el-table> -->
      <p>用户数据表格区域</p>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { ElInput, ElSelect, ElOption, ElButton } from 'element-plus';

// 筛选条件的数据模型
const filters = reactive({
  userUuid: '',
  userName: '',
  companyUuid: '',
  companyName: '',
  userStatus: '',
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
</script>

<style scoped>
.root-user-management-page {
  padding: 20px;
}

.filters-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.filter-item {
  width: 200px;
}

.table-placeholder {
  margin-top: 20px;
  padding: 20px;
  border: 1px dashed #ccc;
  text-align: center;
  color: #999;
}
</style>
