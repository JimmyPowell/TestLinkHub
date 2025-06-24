<template>
  <div class="dashboard-home">
    <div class="welcome-section">
      <h1>欢迎回来，{{ userName }}</h1>
      <p>今天是 {{ currentDate }}，{{ greeting }}</p>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon projects">
            <el-icon><component is="Files" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.projects }}</div>
            <div class="stats-title">项目总数</div>
          </div>
          <div class="stats-compare">
            <span :class="stats.projectsGrowth >= 0 ? 'up' : 'down'">
              {{ stats.projectsGrowth >= 0 ? '+' : '' }}{{ stats.projectsGrowth }}%
            </span>
            <span class="compare-text">较上周</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon testcases">
            <el-icon><component is="Document" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.testcases }}</div>
            <div class="stats-title">测试用例</div>
          </div>
          <div class="stats-compare">
            <span :class="stats.testcasesGrowth >= 0 ? 'up' : 'down'">
              {{ stats.testcasesGrowth >= 0 ? '+' : '' }}{{ stats.testcasesGrowth }}%
            </span>
            <span class="compare-text">较上周</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon success-rate">
            <el-icon><component is="Check" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.successRate }}%</div>
            <div class="stats-title">测试通过率</div>
          </div>
          <div class="stats-compare">
            <span :class="stats.successRateGrowth >= 0 ? 'up' : 'down'">
              {{ stats.successRateGrowth >= 0 ? '+' : '' }}{{ stats.successRateGrowth }}%
            </span>
            <span class="compare-text">较上周</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon bugs">
            <el-icon><component is="WarningFilled" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.bugs }}</div>
            <div class="stats-title">Bug 总数</div>
          </div>
          <div class="stats-compare">
            <span :class="stats.bugsGrowth <= 0 ? 'up' : 'down'">
              {{ stats.bugsGrowth >= 0 ? '+' : '' }}{{ stats.bugsGrowth }}%
            </span>
            <span class="compare-text">较上周</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="panel-row">
      <el-card class="chart-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <span>测试执行趋势</span>
            <el-radio-group v-model="timePeriod" size="small">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="year">全年</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div class="chart-container">
          <!-- 这里可以放置图表组件 -->
          <div class="chart-placeholder">图表区域</div>
        </div>
      </el-card>

      <el-card class="task-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <span>近期任务</span>
            <el-button type="primary" size="small" plain>查看全部</el-button>
          </div>
        </template>
        <div class="task-list">
          <div v-for="(task, index) in recentTasks" :key="index" class="task-item">
            <div class="task-content">
              <div class="task-title">{{ task.title }}</div>
              <div class="task-desc">{{ task.description }}</div>
            </div>
            <div class="task-meta">
              <div class="task-date">{{ task.dueDate }}</div>
              <el-tag :type="getTaskStatusType(task.status)" size="small">{{ task.status }}</el-tag>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';

const userName = ref('管理员');
const timePeriod = ref('week');

// 当前日期和问候语
const currentDate = computed(() => {
  const date = new Date();
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
});

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '凌晨好';
  if (hour < 9) return '早上好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  if (hour < 22) return '晚上好';
  return '夜深了';
});

// 统计数据
const stats = reactive({
  projects: 12,
  projectsGrowth: 8.5,
  testcases: 368,
  testcasesGrowth: 12.3,
  successRate: 92.7,
  successRateGrowth: 3.2,
  bugs: 24,
  bugsGrowth: -5.8
});

// 近期任务
const recentTasks = reactive([
  {
    title: '登录功能测试',
    description: '测试用户登录功能，包括正常登录和异常处理',
    dueDate: '今天',
    status: '进行中'
  },
  {
    title: '注册流程优化',
    description: '优化用户注册流程，降低表单填写错误率',
    dueDate: '明天',
    status: '待处理'
  },
  {
    title: '数据导出功能测试',
    description: '测试数据导出为Excel和PDF格式的功能',
    dueDate: '3天后',
    status: '待处理'
  },
  {
    title: 'API接口性能测试',
    description: '测试核心API接口在高并发下的响应时间',
    dueDate: '昨天',
    status: '已完成'
  },
  {
    title: '移动端兼容性测试',
    description: '测试系统在iOS和Android平台上的兼容性问题',
    dueDate: '2天前',
    status: '已完成'
  }
]);

// 获取任务状态对应的标签类型
const getTaskStatusType = (status) => {
  switch (status) {
    case '进行中': return 'warning';
    case '待处理': return 'info';
    case '已完成': return 'success';
    default: return '';
  }
};
</script>

<style scoped>
.dashboard-home {
  padding: 10px;
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
  position: relative;
  margin-bottom: 20px;
  overflow: hidden;
  display: flex;
  align-items: center;
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

.stats-icon.projects {
  background: linear-gradient(135deg, #36d1dc, #5b86e5);
}

.stats-icon.testcases {
  background: linear-gradient(135deg, #ff9a9e, #fad0c4);
}

.stats-icon.success-rate {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.stats-icon.bugs {
  background: linear-gradient(135deg, #fa709a, #fee140);
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

.stats-compare {
  font-size: 12px;
  margin-top: 8px;
}

.stats-compare .up {
  color: #67c23a;
}

.stats-compare .down {
  color: #f56c6c;
}

.compare-text {
  color: #909399;
  margin-left: 4px;
}

.panel-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-panel {
  flex: 2;
  margin-bottom: 20px;
}

.task-panel {
  flex: 1;
  min-width: 300px;
  margin-bottom: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #333;
}

.chart-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  color: #ccc;
  font-size: 18px;
  text-align: center;
  border: 1px dashed #ddd;
  padding: 150px;
  width: 100%;
  border-radius: 4px;
}

.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-content {
  flex: 1;
  padding-right: 10px;
}

.task-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  font-size: 14px;
}

.task-desc {
  color: #666;
  font-size: 12px;
  line-height: 1.4;
}

.task-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.task-date {
  color: #909399;
  font-size: 12px;
}

@media (max-width: 768px) {
  .panel-row {
    flex-direction: column;
  }
  
  .task-panel {
    min-width: auto;
  }
  
  .chart-placeholder {
    padding: 80px;
  }
}
</style> 