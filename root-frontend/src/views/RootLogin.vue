<template>
  <div class="login-container">
    <div class="login-header">
      <span class="title">测联汇</span>
      <span class="subtitle">测试人的一站式服务中心</span>
    </div>
    <div class="login-form-container">
      <el-card class="login-card">
        <h2>管理员登录</h2>
        <el-form>
          <el-form-item label="邮箱">
            <el-input v-model="email" placeholder="请输入邮箱"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="password" type="password" placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%;" @click="handleLogin">登录</el-button>
          </el-form-item>
          <div class="form-links">
            <el-link type="primary">立即注册</el-link>
            <el-link type="info">忘记密码？</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../store/auth';

const email = ref('2937534198@qq.com');
const password = ref('');
const authStore = useAuthStore();

const handleLogin = () => {
  authStore.login({ email: email.value, password: password.value });
};
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-header {
  padding: 20px;
  background-color: #007bff;
  color: white;
  text-align: center;
}
.title {
  font-size: 24px;
  font-weight: bold;
}
.subtitle {
  font-size: 16px;
  margin-left: 10px;
}
.login-form-container {
  flex-grow: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}
.login-card {
  width: 400px;
}
.form-links {
  display: flex;
  justify-content: space-between;
}
</style>
