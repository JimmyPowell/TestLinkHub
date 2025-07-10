<template>
  <div class="meetings-container">
    <!-- 搜索筛选区域 -->
    <div class="search-area">
      <el-form :inline="true" class="search-form">
        <el-form-item label="会议ID">
          <el-input v-model="searchForm.meetingId" placeholder="请输入" clearable></el-input>
        </el-form-item>
        <el-form-item label="会议主题">
          <el-input v-model="searchForm.title" placeholder="请输入" clearable></el-input>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="未开始" value="pending"></el-option>
            <el-option label="进行中" value="in_progress"></el-option>
            <el-option label="已结束" value="completed"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAddMeeting">新增会议</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 会议列表区域 -->
    <div class="meeting-list">
      <div v-for="item in meetingList" :key="item.id" class="meeting-item">
        <div class="meeting-content">
          <div class="meeting-info">
            <div class="meeting-header">
              <h3 class="meeting-title">{{ item.title }}</h3>
              <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusText(item.status) }}</el-tag>
            </div>
            <p class="meeting-desc">{{ item.description }}</p>
            <div class="meeting-meta">
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ item.startTime }} - {{ item.endTime }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ item.location }}</span>
              </div>
              <div class="meta-item">
                <el-icon><User /></el-icon>
                <span>主持人：{{ item.host }}</span>
              </div>
              <div class="meta-item">
                <el-icon><UserFilled /></el-icon>
                <span>参会人数：{{ item.participantsCount }}人</span>
              </div>
            </div>
          </div>
        </div>
        <div class="meeting-actions">
          <el-button size="small" @click="handleViewMeeting(item)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEditMeeting(item)">编辑</el-button>
          <el-button size="small" type="success" v-if="item.status === 'pending'" @click="handleStartMeeting(item)">开始会议</el-button>
          <el-button size="small" type="warning" v-if="item.status === 'in_progress'" @click="handleEndMeeting(item)">结束会议</el-button>
          <el-button size="small" type="danger" @click="handleDeleteMeeting(item)">删除</el-button>
        </div>
      </div>
      
      <!-- 空数据展示 -->
      <el-empty v-if="meetingList.length === 0" description="暂无数据"></el-empty>
    </div>

    <!-- 新增/编辑会议对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增会议' : '编辑会议'" 
      v-model="dialogVisible" 
      width="60%"
    >
      <el-form :model="meetingForm" label-width="80px" :rules="rules" ref="meetingFormRef">
        <el-form-item label="主题" prop="title">
          <el-input v-model="meetingForm.title" placeholder="请输入会议主题"></el-input>
        </el-form-item>
        <el-form-item label="时间" prop="timeRange">
          <el-date-picker
            v-model="meetingForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="meetingForm.location" placeholder="请输入会议地点"></el-input>
        </el-form-item>
        <el-form-item label="主持人" prop="host">
          <el-input v-model="meetingForm.host" placeholder="请输入主持人姓名"></el-input>
        </el-form-item>
        <el-form-item label="参会人员" prop="participants">
          <el-select
            v-model="meetingForm.participants"
            multiple
            filterable
            placeholder="请选择参会人员"
          >
            <el-option
              v-for="item in memberOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="会议内容" prop="description">
          <el-input
            v-model="meetingForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入会议内容"
          ></el-input>
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            action="/api/upload/file"
            :show-file-list="true"
            :on-success="handleFileUploadSuccess"
            :before-upload="beforeFileUpload"
            multiple
          >
            <el-button type="primary">点击上传</el-button>
            <div class="el-upload__tip">可上传各类文档、图片等附件</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMeeting" :loading="saveLoading">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 会议详情对话框 -->
    <el-dialog 
      title="会议详情" 
      v-model="viewDialogVisible" 
      width="60%"
    >
      <div v-if="currentMeeting" class="meeting-detail">
        <h2>{{ currentMeeting.title }}</h2>
        <div class="detail-status">
          <el-tag :type="getStatusType(currentMeeting.status)">{{ getStatusText(currentMeeting.status) }}</el-tag>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="会议时间">
            {{ currentMeeting.startTime }} - {{ currentMeeting.endTime }}
          </el-descriptions-item>
          <el-descriptions-item label="会议地点">
            {{ currentMeeting.location }}
          </el-descriptions-item>
          <el-descriptions-item label="主持人">
            {{ currentMeeting.host }}
          </el-descriptions-item>
          <el-descriptions-item label="会议ID">
            {{ currentMeeting.id }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h3>会议内容</h3>
          <p>{{ currentMeeting.description }}</p>
        </div>
        
        <div class="detail-section">
          <h3>参会人员 ({{ currentMeeting.participantsCount }}人)</h3>
          <el-tag v-for="(p, index) in currentMeeting.participantsList" 
                 :key="index" 
                 class="participant-tag">
            {{ p }}
          </el-tag>
        </div>
        
        <div class="detail-section" v-if="currentMeeting.attachments && currentMeeting.attachments.length > 0">
          <h3>附件</h3>
          <ul class="attachment-list">
            <li v-for="(file, index) in currentMeeting.attachments" :key="index" class="attachment-item">
              <el-icon><Document /></el-icon>
              <a :href="file.url" target="_blank">{{ file.name }}</a>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { Clock, Location, User, UserFilled, Document } from '@element-plus/icons-vue';

// 搜索表单数据
const searchForm = reactive({
  meetingId: '',
  title: '',
  dateRange: [],
  status: ''
});

// 会议表单数据
const meetingForm = reactive({
  id: '',
  title: '',
  timeRange: [],
  location: '',
  host: '',
  participants: [],
  description: '',
  attachments: []
});

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入会议主题', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择会议时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入会议地点', trigger: 'blur' }],
  host: [{ required: true, message: '请输入主持人', trigger: 'blur' }]
};

// 页面状态
const meetingList = ref([]);
const dialogVisible = ref(false);
const viewDialogVisible = ref(false);
const dialogType = ref('add');
const saveLoading = ref(false);
const currentMeeting = ref(null);
const meetingFormRef = ref(null);

// 模拟数据 - 成员选项
const memberOptions = ref([
  { id: '1', name: '张三' },
  { id: '2', name: '李四' },
  { id: '3', name: '王五' },
  { id: '4', name: '赵六' },
  { id: '5', name: '钱七' },
  { id: '6', name: '孙八' },
  { id: '7', name: '周九' }
]);

// 页面初始化
onMounted(() => {
  fetchMeetingList();
});

// 获取会议列表
const fetchMeetingList = () => {
  const loading = ElLoading.service({
    lock: true,
    text: '加载中...',
    background: 'rgba(255, 255, 255, 0.7)'
  });
  
  // 模拟数据
  setTimeout(() => {
    meetingList.value = [
      {
        id: 'M20240501001',
        title: '2024年Q2季度产品研发规划会议',
        description: '讨论第二季度产品研发计划，确定技术路线和时间节点，分配任务。',
        startTime: '2024-05-10 09:00:00',
        endTime: '2024-05-10 12:00:00',
        location: '线上会议室A01',
        host: '张三',
        status: 'pending',
        participantsCount: 8,
        participantsList: ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十'],
        attachments: [
          { name: '研发计划初稿.docx', url: '#' },
          { name: '技术路线图.pdf', url: '#' }
        ]
      },
      {
        id: 'M20240428002',
        title: '测试用例评审会',
        description: '对新功能的测试用例进行评审，确保测试覆盖率和质量。',
        startTime: '2024-04-28 14:00:00',
        endTime: '2024-04-28 16:30:00',
        location: '线下会议室B02',
        host: '李四',
        status: 'completed',
        participantsCount: 5,
        participantsList: ['李四', '王五', '赵六', '钱七', '孙八'],
        attachments: [
          { name: '测试用例集.xlsx', url: '#' }
        ]
      },
      {
        id: 'M20240503003',
        title: '项目周进度同步会',
        description: '同步各模块进度，解决开发过程中的阻塞问题。',
        startTime: '2024-05-03 10:00:00',
        endTime: '2024-05-03 11:00:00',
        location: '线上会议室A02',
        host: '王五',
        status: 'in_progress',
        participantsCount: 10,
        participantsList: ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十', '郑十一', '冯十二'],
        attachments: []
      }
    ];
    loading.close();
  }, 500);
};

// 获取状态标签类型
const getStatusType = (status) => {
  switch(status) {
    case 'pending': return 'info';
    case 'in_progress': return 'success';
    case 'completed': return '';
    case 'cancelled': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch(status) {
    case 'pending': return '未开始';
    case 'in_progress': return '进行中';
    case 'completed': return '已结束';
    case 'cancelled': return '已取消';
    default: return '未知状态';
  }
};

// 搜索处理
const handleSearch = () => {
  // 模拟搜索，实际应该调用API
  fetchMeetingList();
};

// 添加会议
const handleAddMeeting = () => {
  dialogType.value = 'add';
  resetMeetingForm();
  dialogVisible.value = true;
};

// 查看会议详情
const handleViewMeeting = (meeting) => {
  currentMeeting.value = meeting;
  viewDialogVisible.value = true;
};

// 编辑会议
const handleEditMeeting = (meeting) => {
  dialogType.value = 'edit';
  resetMeetingForm();
  
  // 填充表单数据
  meetingForm.id = meeting.id;
  meetingForm.title = meeting.title;
  meetingForm.timeRange = [meeting.startTime, meeting.endTime];
  meetingForm.location = meeting.location;
  meetingForm.host = meeting.host;
  meetingForm.description = meeting.description;
  // 实际项目中需要将参会人员ID赋值给participants
  meetingForm.participants = [];
  meetingForm.attachments = meeting.attachments || [];
  
  dialogVisible.value = true;
};

// 删除会议
const handleDeleteMeeting = (meeting) => {
  ElMessageBox.confirm(`确定要删除会议"${meeting.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 模拟删除操作
    meetingList.value = meetingList.value.filter(item => item.id !== meeting.id);
    ElMessage.success('删除成功');
  }).catch(() => {
    // 取消删除
  });
};

// 开始会议
const handleStartMeeting = (meeting) => {
  ElMessageBox.confirm(`确定开始会议"${meeting.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 模拟开始会议
    const index = meetingList.value.findIndex(item => item.id === meeting.id);
    if (index !== -1) {
      meetingList.value[index].status = 'in_progress';
      ElMessage.success('会议已开始');
    }
  }).catch(() => {
    // 取消操作
  });
};

// 结束会议
const handleEndMeeting = (meeting) => {
  ElMessageBox.confirm(`确定结束会议"${meeting.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 模拟结束会议
    const index = meetingList.value.findIndex(item => item.id === meeting.id);
    if (index !== -1) {
      meetingList.value[index].status = 'completed';
      ElMessage.success('会议已结束');
    }
  }).catch(() => {
    // 取消操作
  });
};

// 保存会议
const handleSaveMeeting = () => {
  if (!meetingForm.title.trim()) {
    ElMessage.warning('请输入会议主题');
    return;
  }
  
  if (!meetingForm.timeRange || meetingForm.timeRange.length !== 2) {
    ElMessage.warning('请选择会议时间');
    return;
  }
  
  saveLoading.value = true;
  
  // 模拟保存操作
  setTimeout(() => {
    if (dialogType.value === 'add') {
      // 模拟添加会议
      const newMeeting = {
        id: `M${new Date().getTime()}`,
        title: meetingForm.title,
        startTime: meetingForm.timeRange[0],
        endTime: meetingForm.timeRange[1],
        location: meetingForm.location,
        host: meetingForm.host,
        description: meetingForm.description,
        status: 'pending',
        participantsCount: meetingForm.participants.length,
        participantsList: meetingForm.participants.map(id => {
          const member = memberOptions.value.find(m => m.id === id);
          return member ? member.name : id;
        }),
        attachments: meetingForm.attachments
      };
      meetingList.value.unshift(newMeeting);
      ElMessage.success('添加成功');
    } else {
      // 模拟编辑会议
      const index = meetingList.value.findIndex(item => item.id === meetingForm.id);
      if (index !== -1) {
        meetingList.value[index] = {
          ...meetingList.value[index],
          title: meetingForm.title,
          startTime: meetingForm.timeRange[0],
          endTime: meetingForm.timeRange[1],
          location: meetingForm.location,
          host: meetingForm.host,
          description: meetingForm.description,
          participantsCount: meetingForm.participants.length,
          attachments: meetingForm.attachments
        };
        ElMessage.success('更新成功');
      }
    }
    
    saveLoading.value = false;
    dialogVisible.value = false;
  }, 500);
};

// 上传前验证
const beforeFileUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!');
  }
  return isLt10M;
};

// 上传成功回调
const handleFileUploadSuccess = (response, file) => {
  // 实际项目中应该处理服务器返回的数据
  meetingForm.attachments.push({
    name: file.name,
    url: URL.createObjectURL(file.raw)
  });
  ElMessage.success('上传成功');
};

// 重置表单
const resetMeetingForm = () => {
  meetingForm.id = '';
  meetingForm.title = '';
  meetingForm.timeRange = [];
  meetingForm.location = '';
  meetingForm.host = '';
  meetingForm.participants = [];
  meetingForm.description = '';
  meetingForm.attachments = [];
};
</script>

<style scoped>
.meetings-container {
  padding: 0;
  background-color: #fff;
}

.search-area {
  background-color: #fff;
  padding: 16px;
  margin-bottom: 0;
  border-bottom: 1px solid #f0f0f0;
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

.meeting-content {
  display: flex;
  align-items: flex-start;
}

.meeting-info {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.meeting-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.meeting-title {
  margin: 0;
  margin-right: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #121212;
}

.meeting-desc {
  color: #646464;
  margin-bottom: 12px;
  line-height: 1.5;
}

.meeting-meta {
  display: flex;
  flex-wrap: wrap;
  margin-top: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
  margin-bottom: 8px;
  color: #8590a6;
  font-size: 13px;
}

.meta-item .el-icon {
  margin-right: 4px;
}

.meeting-actions {
  margin-top: 16px;
  text-align: right;
}

.meeting-detail {
  padding: 0 10px;
}

.detail-status {
  margin: 8px 0 16px;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #121212;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.participant-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.attachment-list {
  padding: 0;
  list-style: none;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.attachment-item .el-icon {
  margin-right: 8px;
  color: #8590a6;
}

.attachment-item a {
  color: #1890ff;
  text-decoration: none;
}

.attachment-item a:hover {
  color: #40a9ff;
  text-decoration: underline;
}
</style>
