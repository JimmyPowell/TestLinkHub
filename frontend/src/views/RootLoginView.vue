<template>
  <div class="root-login-view">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>超级管理员登录</span>
        </div>
      </template>
      <el-form @submit.prevent="handleLogin">
        <el-form-item label="邮箱">
          <el-input v-model="credentials.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="credentials.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRootAuthStore } from '../store/rootAuth';
import { ElMessage } from 'element-plus';

const rootAuthStore = useRootAuthStore();
const credentials = reactive({
  email: 'admin@testlinkhub.com',
  password: '111111',
});
const loading = ref(false);

const handleLogin = async () => {
  loading.value = true;
  try {
    await rootAuthStore.login(credentials);
  } catch (error) {
    ElMessage.error('登录失败，请检查您的凭据。');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.root-login-view {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
.card-header {
  text-align: center;
  font-size: 20px;
}
</style>
