<template>
  <div class="dashboard-home">
    <div class="welcome-section">
      <h1>欢迎回来，{{ userName }}</h1>
      <p>今天是 {{ currentDate }}，{{ greeting }}</p>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon courses">
            <el-icon><component is="Collection" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ courseStats.total }}</div>
            <div class="stats-title">课程总数</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8">
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

      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-icon pending">
            <el-icon><component is="Clock" /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">5</div>
            <div class="stats-title">待审核课程</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="panel-row">
      <el-card class="course-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <span>最近更新课程</span>
            <el-button type="primary" size="small" plain @click="goToCourseList">查看全部</el-button>
          </div>
        </template>
        <div class="course-list">
          <div v-if="loading" class="loading-placeholder">正在加载...</div>
          <div v-else-if="recentCourses.length === 0" class="no-data-placeholder">暂无课程</div>
          <div v-else>
            <div v-for="course in recentCourses" :key="course.uuid" class="course-item">
              <div class="course-content">
                <div class="course-title">{{ course.name }}</div>
                <div class="course-desc">作者: {{ course.authorName }}</div>
              </div>
              <div class="course-meta">
                <div class="course-date">更新于: {{ course.updatedAt.split('T')[0] }}</div>
                <el-tag :type="getCourseStatusType(course.status)" size="small">{{ course.status }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import courseService from '../../services/courseService';

const router = useRouter();
const userName = ref('管理员');

const courseStats = reactive({
  total: 0,
});
const recentCourses = ref([]);
const loading = ref(true);

const fetchDashboardData = async () => {
  loading.value = true;
  try {
    const response = await courseService.getAllLessons({ page: 0, size: 5 }); // 获取前5条作为最近更新
    if (response.data && response.data.data) {
      const { list, total } = response.data.data;
      recentCourses.value = list.map(course => ({
        uuid: course.uuid,
        name: course.name,
        authorName: course.author_name,
        status: course.status,
        updatedAt: course.updated_at,
      }));
      courseStats.total = total;
    }
  } catch (error) {
    ElMessage.error('获取首页数据失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchDashboardData();
});

const goToCourseList = () => {
  router.push({ name: 'Courses' });
};

const getCourseStatusType = (status) => {
  switch (status) {
    case 'active': return 'success';
    case 'pending_review': return 'warning';
    default: return 'info';
  }
};

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
</script>

<style scoped>
.dashboard-home {
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

.stats-icon.courses {
  background: linear-gradient(135deg, #36d1dc, #5b86e5);
}

.stats-icon.users {
  background: linear-gradient(135deg, #ff9a9e, #fad0c4);
}

.stats-icon.pending {
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

.panel-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.course-panel {
  width: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #333;
}

.course-list {
  max-height: 400px;
  overflow-y: auto;
}

.course-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.course-item:last-child {
  border-bottom: none;
}

.course-content {
  flex: 1;
  padding-right: 10px;
}

.course-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  font-size: 14px;
}

.course-desc {
  color: #666;
  font-size: 12px;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.course-date {
  color: #909399;
  font-size: 12px;
}
</style>
