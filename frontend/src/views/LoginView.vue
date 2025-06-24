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
            <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="login" class="login-button">登录</el-button>
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
import { reactive } from 'vue';

const form = reactive({
  email: '',
  password: '',
});

const login = () => {
  console.log('Email:', form.email);
  console.log('Password:', form.password);
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