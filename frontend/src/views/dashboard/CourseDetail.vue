<template>
  <div class="course-detail-container">
    <el-page-header @back="goBack" :content="course.name || '课程详情'"></el-page-header>
    <el-card class="course-card">
      <div class="course-header">
        <el-image :src="course.imageUrl" class="course-image" fit="cover"></el-image>
        <div class="course-info">
          <h1>{{ course.name }}</h1>
          <p><strong>作者:</strong> {{ course.authorName }}</p>
          <p><strong>版本:</strong> {{ course.version }}</p>
          <p>{{ course.description }}</p>
        </div>
      </div>
      <div class="video-list">
        <h2>课程视频</h2>
        <el-table :data="course.resources" style="width: 100%">
          <el-table-column prop="name" label="视频名称"></el-table-column>
          <el-table-column label="预览">
            <template #default="scope">
              <el-link type="primary" :href="scope.row.resourcesUrl" target="_blank">观看视频</el-link>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import courseService from '../../services/courseService';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const course = ref({});

const fetchCourseDetail = async () => {
  try {
    const uuid = route.params.uuid;
    const response = await courseService.getLessonDetail(uuid);
    course.value = response.data;
  } catch (error) {
    ElMessage.error('获取课程详情失败');
    console.error(error);
  }
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchCourseDetail();
});
</script>

<style scoped>
.course-detail-container {
  padding: 20px;
}
.course-card {
  margin-top: 20px;
}
.course-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.course-image {
  width: 300px;
  height: 170px;
  border-radius: 8px;
}
.course-info {
  flex: 1;
}
.video-list h2 {
  margin-bottom: 10px;
}
</style>
