<template>
  <el-container class="dashboard-container">
    <!-- 侧边菜单 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo-container">
        <router-link to="/dashboard">
          <span class="logo-text">测联汇</span>
        </router-link>
      </div>
      <el-menu
        router
        default-active="/"
        class="sidebar-menu"
        background-color="#ffffff"
        text-color="#333333"
        active-text-color="#409EFF">
        <el-menu-item index="/dashboard">
          <el-icon><component is="House" /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/projects">
          <el-icon><component is="Menu" /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/testcases">
          <el-icon><component is="Document" /></el-icon>
          <span>测试用例</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/reports">
          <el-icon><component is="PieChart" /></el-icon>
          <span>测试报告</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/settings">
          <el-icon><component is="Setting" /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container class="main-content">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <h2 class="page-title">测试人的一站式服务平台</h2>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
              <span class="user-name">{{ userName }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="goToSettings">账号设置</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main">
        <router-view></router-view>
      </el-main>
      
      <!-- 页脚 -->
      <el-footer class="footer" height="40px">
        <div class="footer-content">
          <span>©2024 测联汇 版权所有</span>
        </div>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const userName = ref('管理员');

const goToProfile = () => {
  router.push('/dashboard/profile');
};

const goToSettings = () => {
  router.push('/dashboard/settings');
};

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除登录状态
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    
    // 提示退出成功
    ElMessage.success('退出登录成功');
    
    // 跳转到登录页
    router.push('/');
  }).catch(() => {
    // 取消退出
  });
};
</script>

<style scoped>
.dashboard-container {
  width: 100%;
  height: 100vh;
}

.sidebar {
  background-color: #ffffff;
  color: #333333;
  height: 100vh;
  overflow-x: hidden;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e6f4ff;
}

.logo-text {
  color: #1890ff;
  font-size: 20px;
  font-weight: bold;
}

.sidebar-menu {
  border-right: none;
}

.main-content {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #e6f4ff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  color: #333;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.footer {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 12px;
  border-top: 1px solid #e8e8e8;
  padding: 0;
}

.footer-content {
  text-align: center;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  background-color: #e6f4ff;
  color: #1890ff;
}

:deep(.el-menu-item.is-active::before) {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 3px;
  background-color: #1890ff;
}
</style> 