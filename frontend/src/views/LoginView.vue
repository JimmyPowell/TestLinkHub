<template>
  <el-container class="main-container">
    <el-header class="main-header">
      <div class="header-content">
        <div class="header-title">测联汇</div>
        <div class="subtitle">测试人的一站式服务中心</div>
      </div>
    </el-header>
    <el-main class="main-content">
      <el-card class="login-card">
        <h2 class="login-title">管理员登录</h2>
        <el-form @submit.prevent="login" :model="form" label-position="top">
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" :disabled="loading"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password :disabled="loading"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="login" class="login-button" :loading="loading">登录</el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <el-link type="primary" @click="$router.push('/register/email')">立即注册</el-link>
          <el-link type="info" class="forgot-password">忘记密码?</el-link>
        </div>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(false);

const form = reactive({
  email: '',
  password: '',
});

const login = async () => {
  if (!form.email || !form.password) {
    ElMessage.warning('请输入邮箱和密码');
    return;
  }
  
  loading.value = true;
  
  try {
    // 模拟登录API调用
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 成功登录后保存token
    localStorage.setItem('token', 'mock-token-12345');
    localStorage.setItem('user', JSON.stringify({
      name: '管理员',
      email: form.email,
      avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    }));
    
    ElMessage.success('登录成功');
    
    // 跳转到仪表盘
    router.push('/dashboard');
  } catch (error) {
    console.error('登录失败:', error);
    ElMessage.error('登录失败，请检查邮箱和密码');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.main-container {
  height: 100vh;
  background-color: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.main-header {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #409EFF;
  color: white;
  text-align: center;
  padding: 25px 0;
  height: auto;
  min-height: 80px;
  line-height: 1.2;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  padding: 0 20px;
}

.header-title {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
  line-height: 1.2;
}

.subtitle {
  font-size: 18px;
  line-height: 1.2;
  margin-top: 5px;
}

.main-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-bottom: 100px;
}

.login-card {
  width: 400px;
  padding: 10px 40px;
}

.login-title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.login-button {
  width: 100%;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header-title {
    font-size: 28px;
  }
  
  .subtitle {
    font-size: 16px;
  }
  
  .login-card {
    width: 90%;
    max-width: 400px;
  }
}

@media (max-width: 480px) {
  .header-title {
    font-size: 24px;
  }
  
  .subtitle {
    font-size: 14px;
  }
  
  .main-header {
    padding: 15px 0;
  }
}
</style> 