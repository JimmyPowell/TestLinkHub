<template>
  <el-dialog
    v-model="dialogVisible"
    title="新建课程"
    width="60%"
    :before-close="handleClose"
    :close-on-click-modal="false"
  >
    <el-form :model="courseForm" label-width="100px" label-position="top">
      <!-- 基本信息区域 -->
      <div class="section-title">基本信息</div>
      <el-form-item label="课程标题">
        <el-input v-model="courseForm.title" placeholder="请输入课程标题"></el-input>
      </el-form-item>
      <el-form-item label="课程描述">
        <el-input
          v-model="courseForm.description"
          type="textarea"
          :rows="4"
          placeholder="请输入课程描述"
        ></el-input>
      </el-form-item>
      <el-form-item label="封面图片">
        <div class="cover-upload-container">
          <div v-if="!courseForm.coverUrl" class="cover-placeholder">
            <el-icon class="upload-icon"><Picture /></el-icon>
            <div class="upload-text">上传封面</div>
          </div>
          <el-image v-else :src="courseForm.coverUrl" fit="cover" class="cover-image"></el-image>
          <el-upload
            class="cover-upload"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleCoverChange"
          >
            <el-button type="primary" class="upload-button">
              <el-icon><Upload /></el-icon>
              上传封面
            </el-button>
          </el-upload>
        </div>
      </el-form-item>

      <!-- 课程视频区域 -->
      <div class="section-title">课程视频</div>
      <div class="video-section">
        <div v-if="courseForm.videos.length === 0" class="no-videos">
          <el-icon class="video-icon"><VideoPlay /></el-icon>
          <div class="video-text">还没有添加视频，点击上方按钮开始添加</div>
        </div>
        <div v-else>
          <div 
            v-for="(video, index) in courseForm.videos" 
            :key="video.id"
            class="video-item"
            draggable="true"
            @dragstart="handleDragStart($event, index)"
            @dragover.prevent
            @dragenter.prevent
            @drop="handleDrop($event, index)"
          >
            <div class="drag-handle">
              <el-icon><Operation /></el-icon>
            </div>
            <div class="video-preview">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="video-info">
              <el-input v-model="video.name" placeholder="视频名称"></el-input>
            </div>
            <div class="video-actions">
              <el-upload
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="(file) => handleVideoChange(file, index)"
              >
                <el-button type="primary" plain>
                  <el-icon><Upload /></el-icon>
                  上传
                </el-button>
              </el-upload>
              <el-button type="danger" plain @click="removeVideo(index)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>
        <div class="add-video-container">
          <el-button type="primary" @click="addVideo" class="add-video-btn">
            <el-icon><Plus /></el-icon>
            添加视频
          </el-button>
        </div>
      </div>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">创建课程</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, defineEmits, defineProps, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Picture, Upload, VideoPlay, Operation, Delete, Plus } from '@element-plus/icons-vue';
import courseService from '../../services/courseService';
import ossService from '../../services/ossService'; // 假设有一个ossService

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  course: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:modelValue', 'submit']);

const dialogVisible = ref(props.modelValue);
const dragIndex = ref(-1);

// 监听modelValue属性变化
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal;
  if (newVal && props.course) {
    // 编辑模式，填充表单
    courseForm.title = props.course.title;
    courseForm.description = props.course.description;
    courseForm.coverUrl = props.course.imageUrl;
    // 注意：视频和文件需要单独处理
  } else {
    // 新建模式，重置表单
    courseForm.title = '';
    courseForm.description = '';
    courseForm.coverUrl = '';
    courseForm.coverFile = null;
    courseForm.videos = [];
  }
});

// 监听对话框状态变化
watch(dialogVisible, (newVal) => {
  emit('update:modelValue', newVal);
});

// 课程表单数据
const courseForm = reactive({
  title: '',
  description: '',
  coverUrl: '',
  coverFile: null,
  videos: []
});

// 添加视频
const addVideo = () => {
  courseForm.videos.push({
    id: Date.now(), // 使用时间戳作为临时ID
    name: `视频 ${courseForm.videos.length + 1}`,
    file: null,
    url: ''
  });
};

// 移除视频
const removeVideo = (index) => {
  courseForm.videos.splice(index, 1);
};

// 处理拖拽开始
const handleDragStart = (event, index) => {
  dragIndex.value = index;
  event.dataTransfer.effectAllowed = 'move';
  // 添加一些视觉反馈
  event.target.classList.add('dragging');
};

// 处理放置
const handleDrop = (event, index) => {
  event.preventDefault();
  // 移除视觉反馈
  document.querySelectorAll('.video-item').forEach(item => {
    item.classList.remove('dragging');
  });
  
  if (dragIndex.value !== index) {
    // 获取被拖拽的项
    const draggedItem = courseForm.videos[dragIndex.value];
    // 创建新数组（不直接修改原数组）
    const updatedVideos = [...courseForm.videos];
    // 移除拖拽项
    updatedVideos.splice(dragIndex.value, 1);
    // 在新位置插入拖拽项
    updatedVideos.splice(index, 0, draggedItem);
    // 更新数组
    courseForm.videos = updatedVideos;
  }
  dragIndex.value = -1;
};

// 处理封面图片变更
const handleCoverChange = async (file) => {
  courseForm.coverFile = file.raw;
  try {
    const url = await ossService.uploadFile(file.raw);
    courseForm.coverUrl = url;
    ElMessage.success('封面上传成功');
  } catch (error) {
    ElMessage.error('封面上传失败');
  }
};

// 处理视频文件变更
const handleVideoChange = async (file, index) => {
  courseForm.videos[index].file = file.raw;
  try {
    const url = await ossService.uploadFile(file.raw);
    courseForm.videos[index].url = url;
    ElMessage.success(`视频 "${courseForm.videos[index].name}" 上传成功`);
  } catch (error) {
    ElMessage.error(`视频 "${courseForm.videos[index].name}" 上传失败`);
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
};

// 提交表单
const handleSubmit = async () => {
  // 表单验证
  if (!courseForm.title) {
    ElMessage.warning('请输入课程标题');
    return;
  }

  if (!courseForm.description) {
    ElMessage.warning('请输入课程描述');
    return;
  }

  if (!courseForm.coverUrl) {
    ElMessage.warning('请上传课程封面');
    return;
  }

  if (courseForm.videos.some(video => !video.url)) {
    ElMessage.warning('请确保所有视频都已上传');
    return;
  }

  try {
    // 提交课程数据
    const lessonData = {
      title: courseForm.title,
      description: courseForm.description,
      coverUrl: courseForm.coverUrl,
      authorName: '当前用户', // 假设作者是当前用户
      videos: courseForm.videos
    };

    if (props.course) {
      // 编辑模式
      await courseService.updateLesson(props.course.uuid, lessonData);
      ElMessage.success('课程更新成功');
    } else {
      // 新建模式
      await courseService.uploadLesson(lessonData);
      ElMessage.success('课程创建成功');
    }

    emit('submit', courseForm);
    dialogVisible.value = false;
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试');
    console.error(error);
  }
};
</script>

<style scoped>
.section-title {
  font-size: 18px;
  font-weight: bold;
  margin: 20px 0 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.cover-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.cover-placeholder {
  width: 240px;
  height: 135px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
  background-color: #f5f7fa;
}

.cover-image {
  width: 240px;
  height: 135px;
  border-radius: 6px;
  object-fit: cover;
}

.upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
}

.video-section {
  margin-top: 10px;
}

.no-videos {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 150px;
  background-color: #f5f7fa;
  border-radius: 6px;
  color: #909399;
}

.video-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.video-text {
  font-size: 14px;
}

.video-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background-color: #fff;
}

.drag-handle {
  cursor: move;
  padding: 0 10px;
  color: #909399;
}

.video-preview {
  width: 80px;
  height: 45px;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  margin-right: 15px;
  color: #909399;
}

.video-info {
  flex: 1;
  margin-right: 15px;
}

.video-actions {
  display: flex;
  gap: 10px;
}

.add-video-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.add-video-btn {
  width: 200px;
}

.video-item.dragging {
  opacity: 0.5;
  background: #c8ebfb;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
