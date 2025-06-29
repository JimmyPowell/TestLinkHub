<template>
  <div class="course-management">
    <!-- 搜索和操作区域 -->
    <div class="search-bar">
      <div class="search-fields">
        <div class="field">
          <label>课程UUID</label>
          <input v-model="searchForm.uuid" placeholder="请输入" />
        </div>
        <div class="field">
          <label>课程名称</label>
          <input v-model="searchForm.name" placeholder="请输入" />
        </div>
        <div class="field">
          <label>课程状态</label>
          <el-select v-model="searchForm.status" placeholder="请选择" clearable fit-input-width style="width: 180px;">
            <el-option label="已发布" value="active"></el-option>
            <el-option label="待审核" value="pending_review"></el-option>
          </el-select>
        </div>
        <button class="search-btn" @click="handleSearch">查询</button>
        <button class="reset-btn" @click="handleReset">重置</button>
        <button class="add-btn" @click="handleUploadNewCourse">新增</button>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="course-list-container">
      <div v-if="loading" class="loading-placeholder">
        正在加载...
      </div>
      <div v-else-if="!loading && courses.length === 0" class="no-data-placeholder">
        暂无课程数据
      </div>
      <div v-else class="course-list">
        <div v-for="course in courses" :key="course.uuid" class="course-list-item">
          <div class="item-image">
            <img :src="course.imageUrl" alt="课程图片" @error="handleImageError">
          </div>
          <div class="item-content">
            <div class="item-title">{{ course.name }}</div>
            <div class="item-description">{{ course.description }}</div>
            <div class="item-meta">
              <span>发布时间: {{ course.updatedAt || 'N/A' }}</span>
              <span class="item-status">状态: {{ course.status }}</span>
            </div>
          </div>
          <div class="item-actions">
            <button class="action-btn preview-btn" @click="handlePreview(course)">预览</button>
            <button class="action-btn edit-btn" @click="handleEdit(course)">编辑</button>
            <button class="action-btn delete-btn" @click="handleDelete(course)">删除</button>
          </div>
        </div>
      </div>
    </div>
    <CourseFormDialog
      v-model="courseFormVisible"
      :course="editingCourse"
      @submit="handleCourseSubmit"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import courseService from '../../services/courseService';
import CourseFormDialog from '../../components/dashboard/CourseFormDialog.vue';

const router = useRouter();

// 搜索表单数据
const searchForm = reactive({
  uuid: '',
  name: '',
  status: '',
});

// 课程数据
const courses = ref([]);
const loading = ref(true);
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 获取课程列表
const fetchLessons = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      uuid: searchForm.uuid,
      name: searchForm.name,
      status: searchForm.status,
    };
    const response = await courseService.getLessons(params);
    courses.value = response.data.data.list.map(course => ({
      uuid: course.uuid,
      name: course.name,
      description: course.description,
      imageUrl: course.image_url || 'https://via.placeholder.com/160x90',
      authorName: course.author_name,
      version: course.version,
      status: course.status,
      updatedAt: course.updated_at,
    }));
    pagination.total = response.data.data.total;
  } catch (error) {
    ElMessage.error('获取课程列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchLessons();
});

// 图片加载错误处理
const handleImageError = (e) => {
  // 检查是否已经设置为占位图，避免无限循环
  if (!e.target.src.includes('data:image')) {
    e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYwIiBoZWlnaHQ9IjkwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIxNjAiIGhlaWdodD0iOTAiIGZpbGw9IiNlYWVhZSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIxNiIgc3R5bGU9ImZpbGw6I2NjYyI+572R57ucPC90ZXh0Pjwvc3ZnPg==';
  }
};

// 搜索操作
const handleSearch = () => {
  pagination.page = 1; // Reset to first page for new search
  fetchLessons();
};

// 重置搜索表单
const handleReset = () => {
  searchForm.uuid = '';
  searchForm.name = '';
  searchForm.status = '';
  pagination.page = 1;
  fetchLessons();
};

// 新增课程操作
const handleUploadNewCourse = () => {
  editingCourse.value = null;
  courseFormVisible.value = true;
};

// 预览课程
const handlePreview = (course) => {
  router.push({ name: 'CourseDetail', params: { uuid: course.uuid } });
};

// 编辑课程
const handleEdit = (course) => {
  editingCourse.value = course;
  courseFormVisible.value = true;
};

// 删除课程
const handleDelete = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程《${course.name}》吗？此操作不可撤销。`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await courseService.deleteLesson([course.uuid]);
    ElMessage.success('课程删除成功');
    fetchLessons(); // 重新加载列表
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后重试');
      console.error('Delete lesson error:', error);
    }
  }
};

const courseFormVisible = ref(false);
const editingCourse = ref(null);

const handleCourseSubmit = () => {
  courseFormVisible.value = false;
  fetchLessons();
};
</script>

<style scoped>
.course-management {
  padding: 15px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.search-bar {
  margin-bottom: 15px;
  background-color: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.search-fields {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.field {
  margin-right: 15px;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.field label {
  margin-right: 8px;
  font-size: 14px;
  color: #333;
}

.field input, .field select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  width: 180px;
  font-size: 14px;
}

.search-btn, .add-btn, .reset-btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  margin-right: 10px;
  border: none;
}

.search-btn {
  background-color: #409eff;
  color: white;
}

.add-btn {
  background-color: #fff;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.reset-btn {
  background-color: #f5f7fa;
  color: #909399;
  border: 1px solid #dcdfe6;
}

.reset-btn:focus, .reset-btn:focus-visible {
  outline: none;
  box-shadow: none;
}

.course-list-container {
  background-color: #fff;
  border-radius: 4px;
  min-height: 150px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.loading-placeholder, .no-data-placeholder {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.course-list-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.course-list-item:hover {
  background-color: #f5f7fa;
}

.course-list-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 160px;
  height: 90px;
  margin-right: 20px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 4px;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-content {
  flex-grow: 1;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #303133;
}

.item-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  font-size: 12px;
  color: #909399;
}

.item-status {
  margin-left: 16px;
}

.item-actions {
  display: flex;
  margin-left: 20px;
  flex-shrink: 0;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  margin-left: 8px;
  border: none;
}

.preview-btn {
  background-color: #ecf5ff;
  color: #409eff;
}

.edit-btn {
  background-color: #f0f9eb;
  color: #67c23a;
}

.delete-btn {
  background-color: #fef0f0;
  color: #f56c6c;
}
</style>
