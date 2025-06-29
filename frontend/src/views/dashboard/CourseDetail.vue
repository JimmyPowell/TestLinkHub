<template>
  <div class="course-detail-container">
    <div class="header">
      <el-button @click="goBack" :icon="ArrowLeft" text>返回</el-button>
      <h2 class="chapter-title">{{ course.name || '课程章节1--基础语法' }}</h2>
    </div>
    
    <div class="content-container">
      <!-- 左侧视频播放区域 -->
      <div class="video-container">
        <div class="video-player" ref="videoPlayerRef">
          <div v-if="!currentVideo" class="video-placeholder">
            <span>视频将在预览时播放</span>
          </div>
          <video v-else controls class="video-element" :key="currentVideo.resourcesUrl" ref="videoRef">
            <source :src="currentVideo.resourcesUrl" type="video/mp4">
            您的浏览器不支持视频播放
          </video>
          <el-button class="fullscreen-btn" :icon="FullScreen" @click="toggleFullScreen" text></el-button>
        </div>
      </div>
      
      <!-- 右侧章节列表 -->
      <div class="chapters-container">
        <div class="course-title">
          <h3>{{ course.name || '课程标题--java速成' }}</h3>
        </div>
        
        <el-scrollbar class="chapter-list-scrollbar">
          <div class="chapter-list">
            <div 
              v-for="(resource, index) in courseResources" 
              :key="index" 
              class="chapter-item"
              :class="{ 'active': currentVideoIndex === index }"
              @click="selectVideo(index)"
            >
              <div class="chapter-info">
                <div class="chapter-name">{{ resource.name || `课程章节${index+1}--基础语法` }}</div>
                <div class="chapter-duration">{{ resource.duration || (index % 2 === 0 ? '20:10' : '19:21') }}</div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>
    
    <!-- 课程描述 -->
    <div class="course-description">
      <h3>课程描述</h3>
      <p>{{ course.description || 'xxxxxxxxx' }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import courseService from '../../services/courseService';
import { ElMessage } from 'element-plus';
import { ArrowLeft, FullScreen } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const course = ref({});
const currentVideoIndex = ref(0);
const videoPlayerRef = ref(null);
const videoRef = ref(null);

// 模拟课程资源数据
const courseResources = ref([
  { name: '课程章节1--基础语法', duration: '20:10', resourcesUrl: '' },
  { name: '课程章节1--基础语法', duration: '19:21', resourcesUrl: '' },
  { name: '课程章节2--进阶操作', duration: '15:45', resourcesUrl: '' },
  { name: '课程章节3--实战应用', duration: '22:30', resourcesUrl: '' },
  { name: '课程章节4--总结回顾', duration: '18:15', resourcesUrl: '' }
]);

const currentVideo = computed(() => {
  if (course.value.resources && course.value.resources.length > 0) {
    return course.value.resources[currentVideoIndex.value];
  } else if (courseResources.value.length > 0) {
    return courseResources.value[currentVideoIndex.value];
  }
  return null;
});

const fetchCourseDetail = async () => {
  try {
    const uuid = route.params.uuid;
    const response = await courseService.getLessonDetail(uuid);
    course.value = response.data;
    
    // 如果有资源，更新模拟数据
    if (response.data.resources && response.data.resources.length > 0) {
      courseResources.value = response.data.resources.map((resource, index) => ({
        ...resource,
        duration: resource.duration || (index % 2 === 0 ? '20:10' : '19:21')
      }));
    }
  } catch (error) {
    ElMessage.error('获取课程详情失败');
    console.error(error);
  }
};

const selectVideo = (index) => {
  currentVideoIndex.value = index;
};

const goBack = () => {
  router.back();
};

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    videoPlayerRef.value?.requestFullscreen();
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    }
  }
};

onMounted(() => {
  fetchCourseDetail();
});
</script>

<style scoped>
.course-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.chapter-title {
  margin-left: 10px;
  font-size: 18px;
  font-weight: 500;
}

.content-container {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.video-container {
  flex: 1.5; /* 调整视频容器的 flex 比例 */
  background-color: #000;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  padding-top: 35%; /* 16:9 Aspect Ratio for smaller height */
}

.video-player {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.video-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  background-color: #333;
}

.video-element {
  width: 100%;
  height: 100%;
}

.fullscreen-btn {
  position: absolute;
  bottom: 10px;
  right: 10px;
  color: white;
  background-color: rgba(0, 0, 0, 0.5);
}

.chapters-container {
  flex: 0.8; /* 调整章节列表的 flex 比例 */
  background-color: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  max-height: 400px; /* 限制章节列表的最大高度 */
}

.course-title {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.course-title h3 {
  margin: 0;
  font-size: 16px;
}

.chapter-list-scrollbar {
  flex-grow: 1;
}

.chapter-list {
  padding: 0;
}

.chapter-item {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.chapter-item:hover {
  background-color: #f5f7fa;
}

.chapter-item.active {
  background-color: #ecf5ff;
  color: #409eff;
}

.chapter-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chapter-name {
  font-size: 14px;
}

.chapter-duration {
  font-size: 12px;
  color: #909399;
}

.course-description {
  margin-top: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.course-description h3 {
  margin-top: 0;
  font-size: 16px;
  margin-bottom: 10px;
}

.course-description p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}
</style>
