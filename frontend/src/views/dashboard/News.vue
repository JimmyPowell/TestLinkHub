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
          <el-button type="primary" @click="handleSearch">查询</el-button>
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
          <div class="news-info">
            <h3 class="news-title">{{ item.title }}</h3>
            <p class="news-desc">{{ item.description }}</p>
            <div class="news-meta">
              <span>发布于：{{ item.publishDate }}</span>
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

    <!-- 新增/编辑新闻对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增新闻' : '编辑新闻'" 
      v-model="dialogVisible" 
      width="60%"
    >
      <el-form :model="newsForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="newsForm.title" placeholder="请输入新闻标题"></el-input>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="newsForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入新闻内容"
          ></el-input>
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload
            action="/api/upload/image"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">点击上传</el-button>
            <div class="el-upload__tip">只能上传jpg/png文件，且不超过2MB</div>
          </el-upload>
          <el-image
            v-if="newsForm.imageUrl"
            :src="newsForm.imageUrl"
            fit="cover"
            style="width: 200px; height: 120px; margin-top: 10px"
          ></el-image>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveNews" :loading="saveLoading">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 新闻详情对话框 -->
    <el-dialog 
      title="新闻详情" 
      v-model="viewDialogVisible" 
      width="60%"
    >
      <div v-if="currentNews">
        <h2>{{ currentNews.title }}</h2>
        <p class="news-detail-meta">发布时间：{{ currentNews.publishDate }}</p>
        <el-image
          v-if="currentNews.imageUrl"
          :src="currentNews.imageUrl"
          fit="cover"
          style="width: 100%; max-height: 300px; margin: 15px 0"
        ></el-image>
        <div class="news-detail-content">{{ currentNews.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';
import newsService from '../../services/newsService';

// 搜索表单数据
const searchForm = reactive({
  uuid: '',
  title: '',
  publishDate: ''
});

// 新闻表单数据
const newsForm = reactive({
  uuid: '',
  title: '',
  content: '',
  description: '',
  imageUrl: '',
  publishDate: ''
});

// 页面状态
const newsList = ref([]);
const dialogVisible = ref(false);
const viewDialogVisible = ref(false);
const dialogType = ref('add'); // 'add' 或 'edit'
const saveLoading = ref(false);
const currentNews = ref(null);

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
    const response = await newsService.getNewsList(searchForm);
    newsList.value = response.data.data || [];
  } catch (error) {
    console.error('获取新闻列表失败:', error);
    ElMessage.error('获取新闻列表失败，请重试');
    // 使用模拟数据用于开发
    newsList.value = [
      {
        uuid: 'news-001',
        title: '标题--东北大学xxx实验室发布',
        description: '针对xxxx事件，我们成功开发了一款xxxxxx',
        imageUrl: '',
        publishDate: '2024-01-15',
        content: '详细内容...'
      },
      {
        uuid: 'news-002',
        title: '新技术研发成功',
        description: '我们的团队成功研发了新一代测试技术，将显著提高测试效率',
        imageUrl: '',
        publishDate: '2024-02-20',
        content: '详细内容...'
      }
    ];
  } finally {
    loading.close();
  }
};

// 搜索处理
const handleSearch = () => {
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
  try {
    if (dialogType.value === 'add') { // 如果是直接从列表点击查看
      const response = await newsService.getNewsDetail(news.uuid);
      currentNews.value = response.data.data;
    } else {
      // 直接使用已有数据
      currentNews.value = news;
    }
  } catch (error) {
    console.error('获取新闻详情失败:', error);
    ElMessage.error('获取新闻详情失败，请重试');
    // 使用传入的新闻对象作为备份
    currentNews.value = news;
  }
  
  viewDialogVisible.value = true;
};

// 编辑新闻
const handleEditNews = async (news) => {
  dialogType.value = 'edit';
  
  try {
    const response = await newsService.getNewsDetail(news.uuid);
    Object.assign(newsForm, response.data.data);
  } catch (error) {
    console.error('获取新闻详情失败:', error);
    ElMessage.error('获取新闻详情失败，请重试');
    // 使用列表中的数据
    Object.assign(newsForm, news);
  }
  
  dialogVisible.value = true;
};

// 删除新闻
const handleDeleteNews = (news) => {
  ElMessageBox.confirm(`确定要删除新闻"${news.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await newsService.deleteNews(news.uuid);
      ElMessage.success('删除成功');
      fetchNewsList();
    } catch (error) {
      console.error('删除新闻失败:', error);
      ElMessage.error('删除新闻失败，请重试');
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
  
  if (!newsForm.content.trim()) {
    ElMessage.warning('请输入新闻内容');
    return;
  }
  
  // 默认使用前100个字符作为描述
  if (!newsForm.description) {
    newsForm.description = newsForm.content.substring(0, 100);
  }
  
  saveLoading.value = true;
  
  try {
    if (dialogType.value === 'add') {
      await newsService.createNews(newsForm);
      ElMessage.success('新增成功');
    } else {
      await newsService.updateNews(newsForm.uuid, newsForm);
      ElMessage.success('编辑成功');
    }
    
    dialogVisible.value = false;
    fetchNewsList();
  } catch (error) {
    console.error('保存新闻失败:', error);
    ElMessage.error('保存新闻失败，请重试');
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

// 上传成功回调
const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    newsForm.imageUrl = response.data.url;
    ElMessage.success('上传成功');
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

// 重置表单
const resetNewsForm = () => {
  newsForm.uuid = '';
  newsForm.title = '';
  newsForm.content = '';
  newsForm.description = '';
  newsForm.imageUrl = '';
  newsForm.publishDate = '';
};
</script>

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

.news-title {
  margin-top: 0;
  margin-bottom: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #121212;
}

.news-desc {
  color: #646464;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  flex-grow: 1;
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
}
</style>
