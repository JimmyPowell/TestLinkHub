<template>
  <div class="root-admin-page">
    <div class="welcome-section">
      <h1>超级管理员面板</h1>
      <p>在这里管理整个系统的核心设置和数据。</p>
    </div>

    <!-- Stats Cards -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon users">
            <el-icon><component is="User" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">1,234</div>
            <div class="stats-title">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon companies">
            <el-icon><component is="OfficeBuilding" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">56</div>
            <div class="stats-title">公司总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon pending-requests">
            <el-icon><component is="Bell" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">8</div>
            <div class="stats-title">待处理请求</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon system-status">
            <el-icon><component is="Monitor" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">正常</div>
            <div class="stats-title">系统状态</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Panels -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <el-card class="action-panel" shadow="hover">
          <template #header>
            <div class="panel-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="navigate('UserManagement')">用户管理</el-button>
            <el-button type="success" @click="navigate('CompanyManagement')">公司管理</el-button>
            <el-button type="warning" @click="navigate('SystemSettings')">系统设置</el-button>
            <el-button type="info" @click="navigate('Reports')">查看报告</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="activity-panel" shadow="hover">
          <template #header>
            <div class="panel-header">
              <span>最近系统活动</span>
            </div>
          </template>
          <div class="activity-list">
            <div class="activity-item" v-for="item in recentActivities" :key="item.id">
              <div class="activity-content">
                <p class="activity-description">{{ item.description }}</p>
                <span class="activity-time">{{ item.time }}</span>
              </div>
              <el-tag :type="item.type" size="small">{{ item.tag }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const navigate = (routeName) => {
  // In a real app, you would navigate to the corresponding route.
  // router.push({ name: routeName });
  console.log(`Navigating to ${routeName}`);
};

const recentActivities = ref([
  { id: 1, description: '管理员 "admin" 登录系统', time: '5分钟前', type: 'success', tag: '安全' },
  { id: 2, description: '公司 "NewTech Inc." 提交了注册申请', time: '1小时前', type: 'warning', tag: '审核' },
  { id: 3, description: '数据库备份成功', time: '3小时前', type: 'info', tag: '系统' },
  { id: 4, description: '用户 "John Doe" 更新了他的个人资料', time: '昨天', type: '', tag: '用户' },
]);
</script>

<style scoped>
.root-admin-page {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 20px;
}

.welcome-section h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.welcome-section p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.stats-cards {
  margin-bottom: 20px;
}

.stats-card {
  height: 120px;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.stats-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 8px;
  font-size: 28px;
  color: white;
  margin-right: 16px;
}

.stats-icon.users {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stats-icon.companies {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.stats-icon.pending-requests {
  background: linear-gradient(135deg, #ff9a9e, #fecfef);
}

.stats-icon.system-status {
  background: linear-gradient(135deg, #2af598, #009efd);
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stats-title {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.panel-header {
  font-weight: 600;
  color: #333;
}

.action-panel .action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-content {
  flex: 1;
}

.activity-description {
  margin: 0;
  font-size: 14px;
  color: #555;
}

.activity-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
