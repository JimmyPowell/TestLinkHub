<template>
  <el-card class="register-card">
    <h2 class="register-title">注册账号</h2>
    
    <el-form @submit.prevent="sendVerificationCode" :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="邮箱地址" prop="email">
        <el-input 
          v-model="form.email" 
          placeholder="请输入企业邮箱" 
          type="email"
          clearable>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button 
          type="primary" 
          @click="sendVerificationCode" 
          class="next-button" 
          :loading="loading"
          :disabled="!form.email || loading">
          获取验证码
        </el-button>
      </el-form-item>
    </el-form>

    <div class="form-footer">
      <span>已有账号？</span>
      <el-link type="primary" @click="$router.push('/')">立即登录</el-link>
    </div>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);

const form = reactive({
  email: ''
});

const rules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
};

const sendVerificationCode = async () => {
  if (!form.email) {
    ElMessage.warning('请输入邮箱地址');
    return;
  }

  try {
    loading.value = true;
    
    // 这里应该调用后端发送验证码的API
    await new Promise(resolve => setTimeout(resolve, 1000)); // 模拟API调用
    
    // 存储邮箱，以便后续步骤使用
    localStorage.setItem('registerEmail', form.email);

    ElMessage.success('验证码已发送');
    
    // 跳转到输入验证码的页面
    router.push('/register/verify-code');
  } catch (error) {
    console.error('发送验证码失败:', error);
    ElMessage.error('发送验证码失败，请重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-card {
  width: 450px;
  padding: 20px 40px;
}

.register-title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.step-indicator {
  margin-bottom: 30px;
}

.next-button {
  width: 100%;
  margin-top: 10px;
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 8px;
}

@media (max-width: 768px) {
  .register-card {
    width: 90%;
    max-width: 450px;
  }
}
</style> 