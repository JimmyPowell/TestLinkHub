<template>
  <div class="course-management">
    <!-- 搜索和操作区域 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item>
          <el-input v-model="searchForm.query" placeholder="请输入课程名称..." class="search-input"></el-input>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="课程状态" class="status-select">
            <el-option label="已发布" value="published"></el-option>
            <el-option label="未发布" value="unpublished"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="search-btn">查询</el-button>
        </el-form-item>
        <el-form-item class="action-button">
          <el-button type="primary" @click="handleUploadNewCourse" class="upload-btn">上传新课程</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 课程列表 -->
    <div class="course-list">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in courses" :key="course.id">
          <div class="course-card">
            <div class="course-image-container">
              <el-image
                :src="course.imageUrl"
                class="course-image"
                fit="cover"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-if="course.status === 'published'" class="status-tag">已发布</div>
            </div>
            <div class="course-info">
              <span class="course-title">{{ course.title }}</span>
              <div class="course-actions">
                <div class="action-item" @click="handlePreview(course)">
                  <el-icon><View /></el-icon>
                  <span>预览</span>
                </div>
                <div class="action-item" @click="handleEdit(course)">
                  <el-icon><Edit /></el-icon>
                  <span>编辑</span>
                </div>
                <div class="action-item delete-btn" @click="handleDelete(course)">
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 课程表单对话框 -->
    <CourseFormDialog 
      v-model="courseFormVisible"
      :course="editingCourse"
      @submit="handleCourseSubmit"
      @close="editingCourse = null"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Picture, View, Edit, Delete } from '@element-plus/icons-vue';
import CourseFormDialog from '../../components/dashboard/CourseFormDialog.vue';
import courseService from '../../services/courseService';

// 搜索表单数据
const searchForm = reactive({
  query: '',
  status: '',
});

// 课程数据
const courses = ref([]);
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 获取课程列表
const fetchLessons = async () => {
  try {
    const response = await courseService.getLessons({
      page: pagination.page - 1,
      size: pagination.size,
      name: searchForm.query,
      status: searchForm.status
    });
    courses.value = response.data.list;
    pagination.total = response.data.total;
  } catch (error) {
    ElMessage.error('获取课程列表失败');
    console.error(error);
  }
};

onMounted(() => {
  fetchLessons();
});

// 控制课程表单对话框的显示/隐藏
const courseFormVisible = ref(false);

// --- 方法 ---

const handleSearch = () => {
  pagination.page = 1;
  fetchLessons();
};

const handleUploadNewCourse = () => {
  courseFormVisible.value = true;
};

// 处理课程表单提交
const handleCourseSubmit = (formData) => {
  // 在实际应用中，这里应该将表单数据提交到服务器
  // 这里仅做模拟，将新课程添加到列表中
  const newCourse = {
    id: Date.now(),
    title: formData.title,
    status: 'unpublished',
    imageUrl: formData.coverUrl || ''
  };
  
  courses.value.unshift(newCourse);
  ElMessage.success(`课程 "${formData.title}" 创建成功`);
  fetchLessons(); // 重新获取课程列表
};

import { useRouter } from 'vue-router';

const router = useRouter();

const handlePreview = (course) => {
  router.push({ name: 'CourseDetail', params: { uuid: course.uuid } });
};

const editingCourse = ref(null);

const handleEdit = (course) => {
  editingCourse.value = course;
  courseFormVisible.value = true;
};

const handleDelete = (course) => {
  ElMessageBox.confirm(
    `确定要删除课程《${course.title}》吗？此操作不可撤销。`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await courseService.deleteLesson([course.uuid]);
        ElMessage({
          type: 'success',
          message: '删除成功',
        });
        fetchLessons(); // 重新获取课程列表
      } catch (error) {
        ElMessage.error('删除失败，请稍后重试');
        console.error(error);
      }
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '已取消删除',
      });
    });
};
</script>

<style scoped>
.course-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.search-bar {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 15px;
  border-radius: 4px;
}

.search-input {
  width: 220px;
}

.status-select {
  width: 120px;
}

.search-btn {
  background-color: #409eff;
}

.upload-btn {
  background-color: #409eff;
}

.demo-form-inline .el-form-item {
  margin-bottom: 0;
  margin-right: 10px;
}

.action-button {
  float: right;
  margin-right: 0 !important;
}

.course-list {
  margin-top: 20px;
}

.course-card {
  margin-bottom: 20px;
  position: relative;
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.course-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.course-image-container {
  position: relative;
  width: 100%;
  height: 120px; /* 减小高度 */
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-image {
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #67c23a;
  color: #fff;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 2px;
}

.course-info {
  padding: 14px;
}

.course-title {
  font-size: 16px;
  font-weight: bold;
  display: block;
  margin-bottom: 10px;
  color: #303133;
}

.course-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
  margin-top: 10px;
}

.action-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 13px;
}

.action-item:hover {
  background-color: #f5f7fa;
}

.action-item .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.action-item span {
  line-height: 1;
}

.action-item:not(.delete-btn) {
  color: #409eff;
}

.delete-btn {
  color: #f56c6c;
}
</style>
