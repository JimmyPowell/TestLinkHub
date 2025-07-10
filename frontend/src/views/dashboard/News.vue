<template>
  <div class="news-container">
    <!-- 搜索筛选区域 -->
    <div class="search-area">
      <el-form :inline="true" class="search-form">
        <el-form-item label="UUID">
          <el-input v-model="searchForm.uuid" placeholder="请输入" clearable></el-input>
        </el-form-item>
        <el-form-item label="新闻标题">
          <el-input v-model="searchForm.title" placeholder="请输入" clearable></el-input>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="searchForm.publishDate"
            type="date"
            placeholder="请输入"
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
          <el-button type="primary" @click="handleAddNews">新增</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 新闻列表区域 -->
    <div class="news-list">
      <div v-for="item in newsList" :key="item.uuid" class="news-item">
        <div class="news-content">
          <div class="news-image">
            <el-image 
              :src="item.cover_image_url || 'https://via.placeholder.com/200x120'"
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
          <div class="news-info">
            <div class="news-title-line">
              <h3 class="news-title">{{ item.title }}</h3>
              <el-tag :type="getStatusTagType(item.status)" size="small">{{ item.status }}</el-tag>
            </div>
            <p class="news-desc">{{ item.summary }}</p>
            <div class="news-meta">
              <span>发布于：{{ formatDateTime(item.created_at) }}</span>
            </div>
          </div>
        </div>
        <div class="news-actions">
          <el-button size="small" @click="handleViewNews(item)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEditNews(item)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteNews(item)">删除</el-button>
        </div>
      </div>
      
      <!-- 空数据展示 -->
      <el-empty v-if="newsList.length === 0" description="暂无数据"></el-empty>
    </div>

    <!-- 新增/编辑新闻抽屉 -->
    <el-drawer
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增新闻' : '编辑新闻'"
      direction="rtl"
      size="50%"
      destroy-on-close
      close-on-press-escape
      :with-header="true"
      :before-close="handleEditDrawerClose"
      :close-on-click-modal="true"
      :append-to-body="false"
      modal-class="news-edit-drawer-modal"
    >
      <div class="edit-drawer-content">
        <el-form :model="newsForm" label-width="80px">
          <el-form-item label="标题" required>
            <el-input v-model="newsForm.title" placeholder="请输入新闻标题"></el-input>
          </el-form-item>
          <el-form-item label="摘要" required>
            <el-input
              v-model="newsForm.summary"
              type="textarea"
              :rows="3"
              placeholder="请输入新闻摘要"
            ></el-input>
          </el-form-item>
          <el-form-item label="正文" required>
            <el-upload
              :show-file-list="true"
              :limit="1"
              :http-request="handleContentUpload"
              :before-upload="beforeContentUpload"
              :on-remove="handleContentRemove"
            >
              <el-button type="primary">点击上传HTML文件</el-button>
              <template #tip>
                <div class="el-upload__tip">只能上传.html文件</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="封面图">
            <div class="cover-image-uploader">
              <el-image
                v-if="newsForm.cover_image_url"
                :src="newsForm.cover_image_url"
                fit="cover"
                style="width: 200px; height: 120px; margin-right: 20px;"
              ></el-image>
              <el-upload
                :show-file-list="false"
                :http-request="handleCustomUpload"
                :before-upload="beforeUpload"
              >
                <el-button type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">只能上传jpg/png文件，且不超过2MB</div>
                </template>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveNews" :loading="saveLoading">确定</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-drawer>
    
<!-- 新闻详情抽屉 -->
<el-drawer
  v-model="viewDialogVisible"
  title="新闻详情"
  direction="rtl"
  size="50%"
  destroy-on-close
  close-on-press-escape
  :with-header="true"
  :before-close="handleDrawerClose"
  :close-on-click-modal="true"
  :append-to-body="false"
  modal-class="news-detail-drawer-modal"
>
      <div v-if="currentNews" class="detail-drawer-content">
        <h2>{{ currentNews.title }}</h2>
        <p class="news-detail-meta">发布于：{{ currentNews.publishDate }}</p>
<div class="detail-image-container">
  <el-image
    v-if="currentNews.cover_image_url"
    :src="currentNews.cover_image_url"
    fit="scale-down"
    style="width: 100%; height: auto; max-height: none; display: block; margin: auto;"
  ></el-image>
</div>
        <div class="news-detail-content" v-html="currentNews.content"></div>
      </div>
      <el-empty v-else description="暂无数据"></el-empty>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';
import { getAllNews, getNewsList, deleteNews, uploadNews, updateNews, getAdminNewsDetail } from '../../services/newsService';
import ossService from '../../services/ossService';
import { formatDateTime } from '../../utils/format';
import { useAuthStore } from '../../store/auth';
import { jwtDecode } from 'jwt-decode';

// 搜索表单数据
const searchForm = reactive({
  uuid: '',
  title: '',
  publishDate: ''
});

// 获取认证存储
const authStore = useAuthStore();

// 新闻表单数据
const newsForm = reactive({
  uuid: '',
  title: '',
  summary: '',
  cover_image_url: '',
  resource_url: '', // 假设富文本内容上传后会得到一个URL
  company_uuid: '', // 将从JWT中获取
});

// 页面状态
const newsList = ref([]);
const dialogVisible = ref(false);
const viewDialogVisible = ref(false);
const dialogType = ref('add'); // 'add' 或 'edit'
const saveLoading = ref(false);
const currentNews = ref(null);

// 处理抽屉关闭
const handleDrawerClose = (done) => {
  // 手动恢复body样式
  document.body.style.overflow = '';
  document.body.style.paddingRight = '';
  // 移除可能添加的类
  document.body.classList.remove('el-popup-parent--hidden');
  done();
};

// 处理编辑抽屉关闭
const handleEditDrawerClose = (done) => {
  // 手动恢复body样式
  document.body.style.overflow = '';
  document.body.style.paddingRight = '';
  // 移除可能添加的类
  document.body.classList.remove('el-popup-parent--hidden');
  done();
};

// 监听抽屉打开状态
import { watch } from 'vue';
watch(viewDialogVisible, (newVal) => {
  if (newVal) {
    // 抽屉打开时，立即恢复body样式，防止页面变形
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';
    document.body.classList.remove('el-popup-parent--hidden');
  }
});

// 监听编辑抽屉打开状态
watch(dialogVisible, (newVal) => {
  if (newVal) {
    // 抽屉打开时，立即恢复body样式，防止页面变形
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';
    document.body.classList.remove('el-popup-parent--hidden');
  }
});

// 页面初始化
onMounted(() => {
  fetchNewsList();
});

// 获取新闻列表
const fetchNewsList = async () => {
  const loading = ElLoading.service({
    lock: true,
    text: '加载中...',
    background: 'rgba(255, 255, 255, 0.7)'
  });
  
  try {
    // 从JWT中获取当前用户的公司UUID和身份
    let companyUuid = '';
    let identity = '';
    try {
      const token = authStore.accessToken;
      if (token) {
        const decoded = jwtDecode(token);
        companyUuid = decoded.uid;
        // 安全地获取角色
        if (decoded.roles && decoded.roles.length > 0) {
          identity = decoded.roles[0];
        } else {
          identity = 'COMPANY'; // 提供一个默认值，或者根据业务逻辑处理
        }
      }
    } catch (error) {
      console.error('解析Token失败:', error);
    }
    
    // 检查是否有搜索条件
    const hasSearchCriteria = (
      (searchForm.uuid && searchForm.uuid.trim()) || 
      (searchForm.title && searchForm.title.trim()) ||
      searchForm.publishDate
    );
    
    let response;
    
    if (hasSearchCriteria) {
      // 使用高级搜索接口
      const searchParams = {
        page: 1,
        pageSize: 10,
        userUuid: companyUuid,
        identity: identity
      };
      
      // 添加UUID搜索条件（如果有）
      if (searchForm.uuid && searchForm.uuid.trim()) {
        // 由于后端API不直接支持UUID搜索，我们可能需要在前端过滤
        searchParams.uuid = searchForm.uuid.trim();
      }
      
      // 添加标题搜索条件（如果有）
      if (searchForm.title && searchForm.title.trim()) {
        searchParams.title = searchForm.title.trim();
      }
      
      // 添加发布日期搜索条件（如果有）
      if (searchForm.publishDate) {
        const formattedDate = searchForm.publishDate;
        searchParams.start_time = formattedDate + ' 00:00:00';
        searchParams.end_time = formattedDate + ' 23:59:59';
      }
      
      response = await getNewsList(searchParams);
      
      // 如果使用UUID搜索，前端进行过滤（因为后端API不直接支持）
      let results = response.data.data || [];
      if (searchForm.uuid && searchForm.uuid.trim()) {
        results = results.filter(item => item.uuid && item.uuid.includes(searchForm.uuid.trim()));
      }
      
      newsList.value = results;
    } else {
      // 使用基本列表接口
      const params = {
        page: 1,
        pageSize: 10
      };
      
      response = await getAllNews(params);
      newsList.value = response.data.data || [];
    }
  } catch (error) {
    console.error('获取新闻列表失败:', error);
    ElMessage.error('获取新闻列表失败，请重试');
  } finally {
    loading.close();
  }
};

// 搜索处理
const handleSearch = () => {
  fetchNewsList();
};

// 重置处理
const handleReset = () => {
  searchForm.uuid = '';
  searchForm.title = '';
  searchForm.publishDate = '';
  fetchNewsList();
};

// 新增新闻
const handleAddNews = () => {
  dialogType.value = 'add';
  resetNewsForm();
  dialogVisible.value = true;
};

// 查看新闻
const handleViewNews = async (news) => {
  viewDialogVisible.value = true;
  currentNews.value = { ...news, content: '加载中...' }; // 先显示基本信息
  try {
    const response = await getAdminNewsDetail(news.uuid);
    const detail = response.data.data;
    
    // 异步获取并渲染HTML内容
    let htmlContent = '无法加载正文内容。';
    if (detail.resource_url) {
      try {
        const contentResponse = await fetch(detail.resource_url);
        if (contentResponse.ok) {
          htmlContent = await contentResponse.text();
        }
      } catch (fetchError) {
        console.error('获取HTML内容失败:', fetchError);
      }
    }

    currentNews.value = {
      ...detail,
      publishDate: formatDateTime(detail.created_at),
      imageUrl: detail.cover_image_url,
      content: htmlContent
    };
  } catch (error) {
    console.error('获取新闻详情失败:', error);
    ElMessage.error('获取新闻详情失败，请重试');
    currentNews.value = { ...news, content: '加载详情失败。' };
  }
};

// 编辑新闻
const handleEditNews = async (news) => {
  dialogType.value = 'edit';
  try {
    const response = await getAdminNewsDetail(news.uuid);
    const detail = response.data.data;
    
    // 关键修复：newsForm.uuid 必须始终是 news 主表的 uuid
    Object.assign(newsForm, {
      uuid: news.uuid, // 使用从列表项传入的 news 主表 uuid
      title: detail.title,
      summary: detail.summary,
      cover_image_url: detail.cover_image_url,
      resource_url: detail.resource_url,
      company_uuid: detail.company_uuid // 保留原有的公司UUID
    });
    
    dialogVisible.value = true;
  } catch (error) {
    console.error('获取新闻详情失败:', error);
    ElMessage.error('获取新闻详情失败，请重试');
  }
};

// 删除新闻
const handleDeleteNews = (news) => {
  ElMessageBox.confirm(`确定要删除新闻"${news.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNews(news.uuid);
      ElMessage.success('删除成功');
      fetchNewsList(); // 重新加载列表
    } catch (error) {
      console.error('删除新闻失败:', error);
      ElMessage.error(error.response?.data?.message || '删除新闻失败，请重试');
    }
  }).catch(() => {
    // 取消删除
  });
};

// 保存新闻
const handleSaveNews = async () => {
  if (!newsForm.title.trim()) {
    ElMessage.warning('请输入新闻标题');
    return;
  }
  
  if (!newsForm.summary.trim()) {
    ElMessage.warning('请输入新闻摘要');
    return;
  }
  
  saveLoading.value = true;
  
  // 从JWT中获取用户UUID
  try {
    const token = authStore.accessToken;
    if (token) {
      const decoded = jwtDecode(token);
      newsForm.company_uuid = decoded.uid;
    } else {
      ElMessage.error('未找到有效的认证信息');
      saveLoading.value = false;
      return;
    }
  } catch (error) {
    console.error('解析Token失败:', error);
    ElMessage.error('获取用户信息失败');
    saveLoading.value = false;
    return;
  }

  try {
    if (dialogType.value === 'add') {
      await uploadNews(newsForm);
      ElMessage.success('新增成功，等待审核');
    } else {
      await updateNews(newsForm.uuid, newsForm);
      ElMessage.success('编辑成功，等待审核');
    }
    
    dialogVisible.value = false;
    fetchNewsList();
  } catch (error) {
    console.error('保存新闻失败:', error);
    ElMessage.error(error.response?.data?.message || '保存新闻失败，请重试');
  } finally {
    saveLoading.value = false;
  }
};

// 上传前验证
const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!');
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!');
  }
  return isJPG && isLt2M;
};

// 自定义封面图上传处理
const handleCustomUpload = async (options) => {
  const { file } = options;
  const loading = ElLoading.service({
    lock: true,
    text: '上传中...',
    background: 'rgba(255, 255, 255, 0.7)'
  });
  try {
    const url = await ossService.uploadFile(file);
    newsForm.cover_image_url = url;
    ElMessage.success('上传成功');
  } catch (error) {
    console.error('上传失败:', error);
    ElMessage.error('上传失败，请检查控制台日志');
  } finally {
    loading.close();
  }
};

// 重置表单
const resetNewsForm = () => {
  Object.assign(newsForm, {
    uuid: '',
    title: '',
    summary: '',
    cover_image_url: '',
    resource_url: '',
    company_uuid: '',
  });
};

// 上传正文HTML前的验证
const beforeContentUpload = (file) => {
  const isHtml = file.type === 'text/html';
  if (!isHtml) {
    ElMessage.error('正文只能上传 HTML 文件!');
  }
  return isHtml;
};

// 自定义正文上传处理
const handleContentUpload = async (options) => {
  const { file } = options;
  const loading = ElLoading.service({ lock: true, text: '上传正文中...' });
  try {
    const url = await ossService.uploadFile(file);
    newsForm.resource_url = url;
    ElMessage.success('正文上传成功');
  } catch (error) {
    console.error('正文上传失败:', error);
    ElMessage.error('正文上传失败，请检查控制台日志');
  } finally {
    loading.close();
  }
};

// 移除已上传的正文文件
const handleContentRemove = () => {
  newsForm.resource_url = '';
};

// 根据状态获取标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'published':
      return 'success';
    case 'pending':
      return 'warning';
    case 'archived':
      return 'info';
    case 'rejected':
      return 'danger';
    default:
      return 'info';
  }
};
</script>

<style>
/* 覆盖Element Plus抽屉组件对body的样式修改 */
body.el-popup-parent--hidden {
  overflow: auto !important;
  padding-right: 0 !important;
  width: 100% !important;
  position: static !important;
}

/* 确保遮罩层不影响布局 */
.el-overlay {
  position: absolute !important;
  z-index: 2000;
}

/* 确保抽屉内容不影响外部布局 */
.news-detail-drawer-modal, .news-edit-drawer-modal {
  position: fixed;
}

/* 确保抽屉内容区域有足够的宽度 */
.el-drawer__body {
  width: 100%;
  overflow-x: hidden;
  padding: 0;
}

/* 防止抽屉影响主布局 */
.el-drawer__container {
  position: absolute !important;
}

/* 确保抽屉打开时不影响主布局 */
.el-drawer__wrapper {
  position: absolute !important;
  overflow: visible !important;
}

/* 确保侧边栏和顶部导航不受影响 */
.el-aside, .el-header {
  position: relative !important;
  z-index: auto !important;
}
</style>

<style scoped>
.news-container {
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
}

.search-form .el-input {
  width: 180px;
}

.news-list {
  margin-top: 0;
}

.news-item {
  padding: 20px 16px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.news-content {
  display: flex;
  align-items: flex-start;
}

.news-image {
  flex-shrink: 0;
  margin-right: 20px;
}

.news-info {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 120px;
}

.news-title-line {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.news-title {
  margin-top: 0;
  margin-bottom: 0;
  margin-right: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #121212;
}

.news-desc {
  color: #646464;
  margin: 8px 0 12px; /* 保持与其他元素的间距感 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  flex-grow: 1;
  padding-left: 0; /* 确保没有额外的内边距导致不对齐 */
}

.news-meta {
  font-size: 13px;
  color: #8590a6;
  display: flex;
  align-items: center;
}

.news-actions {
  margin-top: 16px;
  text-align: right;
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

.news-detail-meta {
  font-size: 14px;
  color: #8590a6;
  margin-bottom: 15px;
}

.news-detail-content {
  margin-top: 20px;
  line-height: 1.6;
  width: 100%;
  overflow-x: hidden;
}

.cover-image-uploader {
  display: flex;
  align-items: flex-end;
}

.detail-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 15px 0;
  background-color: #f9f9f9;
  border-radius: 4px;
  width: 100%;
  min-height: 200px;
  overflow: visible;
}

.detail-drawer-content, .edit-drawer-content {
  padding: 0 20px;
  width: 100%;
  box-sizing: border-box;
}
</style>
