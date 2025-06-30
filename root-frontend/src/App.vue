<template>
  <router-view v-if="$route.path === '/login'"></router-view>
  <div v-else class="common-layout">
    <el-container>
      <el-header class="header">
        <div class="logo">
          <span>测联汇</span>
          <span class="sub-title">测试人的一站式服务平台</span>
        </div>
        <div class="avatar">
          <el-dropdown>
            <el-avatar>User</el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu default-active="/user-management" router>
            <el-menu-item index="/user-management">用户管理</el-menu-item>
            <el-menu-item index="/company-management">公司管理</el-menu-item>
            <el-menu-item index="/news-management">新闻管理</el-menu-item>
            <el-menu-item index="/course-management">课程管理</el-menu-item>
            <el-menu-item index="/meeting-management">会议管理</el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <!-- Main content placeholder -->
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import { useAuthStore } from './store/auth';

const route = useRoute();
const authStore = useAuthStore();

const handleLogout = () => {
  authStore.logout();
};
</script>

<style scoped>
.common-layout {
  height: 100vh;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #e6f7ff;
  border-bottom: 1px solid #e4e7ed;
}
.logo {
  font-size: 20px;
  font-weight: bold;
}
.sub-title {
  margin-left: 10px;
  font-size: 14px;
  color: #606266;
}
.aside {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  min-height: calc(100vh - 60px); /* 60px is the header height */
}
.main {
  padding: 20px;
}
</style>
