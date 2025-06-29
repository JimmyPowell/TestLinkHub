<template>
  <div class="meeting-container">
    <!-- 搜索筛选区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="UUID">
          <el-input v-model="searchForm.uuid" placeholder="请输入UUID" clearable></el-input>
        </el-form-item>
        <el-form-item label="会议标题">
          <el-input v-model="searchForm.title" placeholder="请输入会议标题" clearable></el-input>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="searchForm.publishDate"
            type="date"
            placeholder="请选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 会议列表区域 -->
    <div class="meeting-list">
      <div v-for="item in meetingList" :key="item.id" class="meeting-item">
        <div class="meeting-content">
          <div class="meeting-image">
            <el-image 
              :src="item.imageUrl || 'https://via.placeholder.com/200x120'"
              fit="cover"
              style="width: 200px; height: 120px"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="meeting-info">
            <div class="meeting-title-line">
              <h3 class="meeting-title">{{ item.title }}</h3>
              <el-tag :type="getStatusTagType(item.status)" size="small">{{ item.status }}</el-tag>
            </div>
            <p class="meeting-desc">{{ item.description }}</p>
            <div class="meeting-meta">
              <span>发布于：{{ item.submittedAt }}</span>
            </div>
          </div>
        </div>
        <div class="meeting-actions">
          <el-button size="small" @click="handleView(item)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(item)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
      
      <el-empty v-if="meetingList.length === 0" description="暂无数据"></el-empty>
    </div>

    <!-- 新增/编辑会议抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="dialogTitle"
      direction="rtl"
      size="40%">
      <div class="drawer-content">
        <el-form :model="meetingForm" label-width="100px">
          <el-form-item label="会议名称" required>
            <el-input v-model="meetingForm.name" placeholder="请输入会议名称"></el-input>
          </el-form-item>
          <el-form-item label="会议描述" required>
            <el-input 
              type="textarea"
              :rows="3"
              v-model="meetingForm.description" 
              placeholder="请输入会议描述">
            </el-input>
          </el-form-item>
          <el-form-item label="开始时间" required>
            <el-date-picker
              v-model="meetingForm.start_time"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="结束时间" required>
            <el-date-picker
              v-model="meetingForm.end_time"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="封面图">
            <div class="cover-image-uploader">
              <el-image
                v-if="meetingForm.image_url"
                :src="meetingForm.image_url"
                fit="cover"
                style="width: 200px; height: 120px; margin-right: 20px;"
              ></el-image>
              <el-upload
                :show-file-list="false"
                :http-request="handleCoverUpload"
                :before-upload="beforeCoverUpload"
              >
                <el-button type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">只能上传jpg/png文件，且不超过2MB</div>
                </template>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="是否可见">
            <el-switch v-model="meetingForm.visiable" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div style="flex: auto">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveMeeting" :loading="saveLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 查看详情抽屉 -->
    <el-drawer
      v-model="viewDrawerVisible"
      title="会议详情"
      direction="rtl"
      size="50%">
      <div v-if="currentMeeting" class="detail-drawer-content">
        <h2>{{ currentMeeting.title }}</h2>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="版本UUID">{{ currentMeeting.uuid }}</el-descriptions-item>
          <el-descriptions-item label="主会议UUID">{{ currentMeeting.meetingUuid }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentMeeting.status)" size="small">{{ currentMeeting.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(currentMeeting.start_time) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(currentMeeting.end_time) }}</el-descriptions-item>
          <el-descriptions-item label="发布于">{{ currentMeeting.submittedAt }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <h3>描述</h3>
        <p>{{ currentMeeting.description }}</p>
        <h3>封面图</h3>
        <el-image
          v-if="currentMeeting.imageUrl"
          :src="currentMeeting.imageUrl"
          fit="contain"
          style="max-width: 100%;"
        ></el-image>
        <el-empty v-else description="暂无封面图"></el-empty>
      </div>
    </el-drawer>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';
import { createMeeting, getMeetings, updateMeeting, deleteMeeting, getMeetingDetail } from '../../services/meetingService';
import ossService from '../../services/ossService';
import { formatDateTime } from '../../utils/format';

// 搜索表单数据
const searchForm = reactive({
  uuid: '',
  title: '',
  publishDate: ''
});

const meetingList = ref([]);
const listLoading = ref(false);

// 抽屉状态
const drawerVisible = ref(false);
const viewDrawerVisible = ref(false);
const currentMeeting = ref(null);
const saveLoading = ref(false);
const dialogType = ref('add');
const dialogTitle = computed(() => dialogType.value === 'add' ? '新增会议' : '编辑会议');

// 会议表单
const meetingForm = reactive({
  meeting_uuid: '',
  name: '',
  description: '',
  start_time: '',
  end_time: '',
  image_url: '',
  visiable: 1
});

const fetchMeetings = async () => {
  listLoading.value = true;
  const params = {
    page: 1, // Hardcoded for now, can be extended with pagination state
    size: 10,
    name: searchForm.title || null,
    // The date picker for a single date is named publishDate in the form
    startTime: searchForm.publishDate ? `${searchForm.publishDate}T00:00:00` : null,
    endTime: searchForm.publishDate ? `${searchForm.publishDate}T23:59:59` : null,
  };

  // Remove null or empty params to keep the request clean
  Object.keys(params).forEach(key => {
    if (params[key] === null || params[key] === '') {
      delete params[key];
    }
  });

  try {
    const response = await getMeetings(params);
    if (response.data.code === 200) {
      meetingList.value = response.data.data.map(item => ({
        id: item.id,
        uuid: item.uuid, // This is the version UUID
        meetingUuid: item.meeting_uuid, // This is the parent meeting UUID
        title: item.name,
        description: item.description,
        submittedAt: formatDateTime(item.created_at),
        imageUrl: item.cover_image_url,
        status: item.status,
        start_time: item.start_time,
        end_time: item.end_time,
        visiable: item.is_deleted === 0 ? 1 : 0
      }));
    } else {
      ElMessage.error(response.data.message || '获取会议列表失败');
    }
  } catch (error) {
    console.error("获取会议列表失败:", error);
    ElMessage.error('获取会议列表失败，请检查网络或联系管理员');
  } finally {
    listLoading.value = false;
  }
};

onMounted(() => {
  fetchMeetings();
});

const resetMeetingForm = () => {
  Object.assign(meetingForm, {
    meeting_uuid: '',
    name: '',
    description: '',
    start_time: '',
    end_time: '',
    image_url: '',
    visiable: 1
  });
};

// 事件处理
const handleSearch = () => {
  fetchMeetings();
};

const handleReset = () => {
  searchForm.uuid = '';
  searchForm.title = '';
  searchForm.publishDate = '';
  fetchMeetings();
  ElMessage.success('已重置搜索条件');
};

const handleAdd = () => {
  resetMeetingForm();
  dialogType.value = 'add';
  drawerVisible.value = true;
};

const handleEdit = (item) => {
  resetMeetingForm();
  dialogType.value = 'edit';
  Object.assign(meetingForm, {
    meeting_uuid: item.meetingUuid, // Use the correct parent meeting UUID
    name: item.title,
    description: item.description,
    start_time: item.start_time,
    end_time: item.end_time,
    image_url: item.imageUrl,
    visiable: item.visiable
  });
  drawerVisible.value = true;
};

const handleSaveMeeting = async () => {
  if (!meetingForm.name || !meetingForm.description || !meetingForm.start_time || !meetingForm.end_time) {
    ElMessage.warning('请填写所有必填项');
    return;
  }
  saveLoading.value = true;
  try {
    let response;
    if (dialogType.value === 'add') {
      // Create payload for add, excluding meeting_uuid
      const { meeting_uuid, ...addPayload } = meetingForm;
      response = await createMeeting(addPayload);
    } else {
      response = await updateMeeting(meetingForm);
    }

    if (response.data.code === 200) {
      ElMessage.success(dialogType.value === 'add' ? '会议创建成功' : '会议更新成功');
      drawerVisible.value = false;
      fetchMeetings(); // 刷新列表
    } else {
      ElMessage.error(response.data.message || '操作失败');
    }
  } catch (error) {
    console.error('保存会议失败:', error);
    ElMessage.error(error.response?.data?.message || '操作失败，请重试');
  } finally {
    saveLoading.value = false;
  }
};

const handleView = async (item) => {
  viewDrawerVisible.value = true;
  currentMeeting.value = { ...item, description: '加载中...' };
  try {
    const response = await getMeetingDetail(item.uuid);
    if (response.data.code === 200) {
      const detail = response.data.data;
      currentMeeting.value = {
        ...item, // a-lia-ma-de, the backend returns different time formats
        description: detail.description,
        // You can add more fields from the detail response here if needed
      };
    } else {
      ElMessage.error(response.data.message || '获取详情失败');
      currentMeeting.value = { ...item, description: '加载详情失败' };
    }
  } catch (error) {
    console.error("获取详情失败:", error);
    ElMessage.error(error.response?.data?.message || '获取详情失败');
    currentMeeting.value = { ...item, description: '加载详情失败' };
  }
};

const handleDelete = (item) => {
  ElMessageBox.confirm(
    `确定要删除会议 "${item.title}" 吗？此操作不可撤销。`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const response = await deleteMeeting(item.meetingUuid);
      if (response.data.code === 200) {
        ElMessage.success('删除成功');
        fetchMeetings(); // 重新加载列表
      } else {
        ElMessage.error(response.data.message || '删除失败');
      }
    } catch (error) {
      console.error('删除会议失败:', error);
      ElMessage.error(error.response?.data?.message || '删除失败，请重试');
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// --- 图片上传逻辑 ---
const beforeCoverUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('上传封面只能是 JPG 或 PNG 格式!');
  }
  if (!isLt2M) {
    ElMessage.error('上传封面大小不能超过 2MB!');
  }
  return isJPG && isLt2M;
};

const handleCoverUpload = async (options) => {
  const { file } = options;
  const loading = ElLoading.service({
    lock: true,
    text: '上传中...',
    background: 'rgba(255, 255, 255, 0.7)'
  });
  try {
    const url = await ossService.uploadFile(file);
    meetingForm.image_url = url;
    ElMessage.success('封面上传成功');
  } catch (error) {
    console.error('封面上传失败:', error);
    ElMessage.error('封面上传失败，请检查控制台日志');
  } finally {
    loading.close();
  }
};
// --- 图片上传逻辑结束 ---

// 根据状态获取标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'published':
      return 'success';
    case 'pending_review':
      return 'warning';
    case 'archived':
      return 'info';
    default:
      return 'info';
  }
};
</script>

<style scoped>
.meeting-container {
  padding: 0;
  background-color: #fff;
}

.search-area {
  background-color: #fff;
  padding: 16px;
  margin-bottom: 0;
  border-bottom: 1px solid #f0f0f0;
}

.search-form .el-form-item {
  margin-right: 10px;
  margin-bottom: 0; /* 移除内联表单的下边距 */
}

.search-form .el-input {
  width: 180px;
}

.meeting-list {
  margin-top: 0;
}

.meeting-item {
  padding: 20px 16px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.meeting-item:last-child {
  border-bottom: none;
}

.meeting-content {
  display: flex;
  align-items: flex-start;
}

.meeting-image {
  flex-shrink: 0;
  margin-right: 20px;
}

.image-placeholder {
  width: 200px;
  height: 120px;
  background-color: #f6f6f6;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 30px;
}

.meeting-info {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 120px;
}

.meeting-title-line {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.meeting-title {
  margin-top: 0;
  margin-bottom: 0;
  margin-right: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #121212;
}

.meeting-desc {
  color: #646464;
  margin: 8px 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  flex-grow: 1;
  padding-left: 0;
}

.meeting-meta {
  font-size: 13px;
  color: #8590a6;
  display: flex;
  align-items: center;
}

.meeting-actions {
  margin-top: 16px;
  text-align: right;
}

.drawer-content {
  padding: 20px;
}

.cover-image-uploader {
  display: flex;
  align-items: flex-end;
}

.detail-drawer-content {
    padding: 20px;
}
</style>
