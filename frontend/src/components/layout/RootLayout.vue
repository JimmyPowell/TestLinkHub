<template>
  <el-container class="root-layout-container">
    <!-- Root Admin Sidebar -->
    <el-aside width="220px" class="root-sidebar">
      <div class="logo-container">
        <span class="logo-text">测联汇 - Root Panel</span>
      </div>
      <el-menu
        router
        :default-active="$route.path"
        class="sidebar-menu"
        background-color="#1f2937"
        text-color="#d1d5db"
        active-text-color="#ffffff">
        <el-menu-item index="/root/news-management">
          <el-icon><component is="Collection" /></el-icon>
          <span>新闻管理</span>
        </el-menu-item>
        <el-menu-item index="/root/meeting-management">
          <el-icon><component is="VideoCamera" /></el-icon>
          <span>会议管理</span>
        </el-menu-item>
        <el-menu-item index="/root/course-management">
          <el-icon><component is="Reading" /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/root/company-management">
          <el-icon><component is="OfficeBuilding" /></el-icon>
          <span>公司管理</span>
        </el-menu-item>
        <el-menu-item index="/root/user-management">
          <el-icon><component is="User" /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container class="main-content">
      <!-- Header -->
      <el-header class="header">
        <div class="header-left">
          <h2 class="page-title">超级管理员控制台</h2>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="user-name">Root Admin</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goHome">返回主站</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- Main Content Area -->
      <el-main class="main">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRootAuthStore } from '../../store/rootAuth';

const router = useRouter();
const rootAuthStore = useRootAuthStore();

const goHome = () => {
  router.push('/dashboard');
};

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    rootAuthStore.logout();
    ElMessage.success('退出登录成功');
  }).catch(() => {
    // Cancelled
  });
};
</script>

<style scoped>
.root-layout-container {
  height: 100vh;
  background-color: #f3f4f6;
}

.root-sidebar {
  background-color: #111827;
  color: #d1d5db;
  height: 100vh;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #1f2937;
}

.logo-text {
  color: #ffffff;
  font-size: 22px;
  font-weight: bold;
  letter-spacing: 1px;
}

.sidebar-menu {
  border-right: none;
}

.main-content {
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background-color: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  height: 60px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  color: #374151;
}

.main {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  background-color: #374151;
}
</style>
