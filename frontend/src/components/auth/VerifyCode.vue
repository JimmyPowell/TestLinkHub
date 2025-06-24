<template>
  <el-card class="register-card">
    <h2 class="register-title">验证码确认</h2>
    
    <div class="email-info">
      <p>验证码已发送至邮箱: <strong>{{ maskEmail(email) }}</strong></p>
    </div>

    <div class="code-input-container">
      <div 
        v-for="(digit, index) in codeDigits" 
        :key="index" 
        class="code-input-box"
        :class="{ 'active-input': currentIndex === index }"
        @click="focusInput"
      >
        {{ digit }}
      </div>
    </div>
    
    <!-- 隐藏的输入框，用于实际接收键盘输入 -->
    <input 
      type="text" 
      ref="hiddenInput"
      :value="verificationCode"
      class="hidden-input" 
      maxlength="6"
      @input="handleInput"
    />
    
    <div class="action-buttons">
      <el-button 
        type="primary" 
        @click="verifyCode" 
        class="next-button"
        :loading="loading"
        :disabled="verificationCode.length !== 6 || loading">
        验证
      </el-button>
    </div>

    <div class="form-footer">
      <span>{{ timer > 0 ? `${timer}秒后可重新获取` : '未收到验证码？' }}</span>
      <el-link 
        type="primary" 
        :disabled="timer > 0"
        @click="resendCode"
        v-if="timer <= 0">
        重新发送
      </el-link>
    </div>
    
    <div class="navigation-links">
      <el-link type="info" @click="$router.push('/register/email')">返回上一步</el-link>
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const hiddenInput = ref(null);
const loading = ref(false);
const timer = ref(60);
let timerInterval = null;

// 从本地存储获取邮箱
const email = ref(localStorage.getItem('registerEmail') || '');

// 验证码
const verificationCode = ref('');
const currentIndex = ref(0);

// 计算得到的验证码数字
const codeDigits = computed(() => {
  const digits = Array(6).fill('');
  for (let i = 0; i < verificationCode.value.length; i++) {
    digits[i] = verificationCode.value[i];
  }
  return digits;
});

// 掩饰邮箱，只显示前两个字符和@后面的部分
const maskEmail = (email) => {
  if (!email) return '';
  const [username, domain] = email.split('@');
  if (!username || !domain) return email;
  
  const maskedUsername = username.length <= 2 
    ? username 
    : username.substring(0, 2) + '*'.repeat(username.length - 2);
    
  return `${maskedUsername}@${domain}`;
};

// 处理输入事件
const handleInput = (event) => {
  const value = event.target.value;
  verificationCode.value = value.replace(/[^0-9]/g, '').slice(0, 6);
  currentIndex.value = verificationCode.value.length;
};

// 聚焦到隐藏输入框
const focusInput = () => {
  if (hiddenInput.value) {
    hiddenInput.value.focus();
  }
};

// 开始倒计时
const startTimer = () => {
  timer.value = 60;
  clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    if (timer.value > 0) {
      timer.value--;
    } else {
      clearInterval(timerInterval);
    }
  }, 1000);
};

// 重新发送验证码
const resendCode = async () => {
  if (timer.value > 0) return;
  
  try {
    loading.value = true;
    // 这里应该调用后端重新发送验证码的API
    await new Promise(resolve => setTimeout(resolve, 1000)); // 模拟API调用
    ElMessage.success('验证码已重新发送');
    startTimer();
  } catch (error) {
    console.error('重新发送验证码失败:', error);
    ElMessage.error('重新发送验证码失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 验证验证码
const verifyCode = async () => {
  if (verificationCode.value.length !== 6) {
    ElMessage.warning('请输入6位验证码');
    return;
  }

  try {
    loading.value = true;
    
    // 这里应该调用后端验证验证码的API
    await new Promise(resolve => setTimeout(resolve, 1500)); // 模拟API调用
    
    // 存储验证码，以便后续步骤使用
    localStorage.setItem('verificationCode', verificationCode.value);
    
    ElMessage.success('验证码验证成功');
    
    // 跳转到完善信息的页面
    router.push('/register/company-info');
  } catch (error) {
    console.error('验证码验证失败:', error);
    ElMessage.error('验证码错误或已过期，请重试');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  // 聚焦输入框
  focusInput();
  
  // 如果没有邮箱，说明可能没有经过邮箱验证步骤
  if (!email.value) {
    ElMessage.warning('请先验证邮箱');
    router.push('/register/email');
    return;
  }
  
  // 开始倒计时
  startTimer();
});

onBeforeUnmount(() => {
  // 组件销毁前清除计时器
  clearInterval(timerInterval);
});
</script>

<style scoped>
.register-card {
  width: 450px;
  padding: 20px 40px;
  margin: 20px auto;
}

.register-title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.email-info {
  text-align: center;
  margin-bottom: 20px;
  color: #606266;
}

.code-input-container {
  display: flex;
  justify-content: space-between;
  margin: 30px 0;
}

.code-input-box {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 50px;
  height: 55px;
  font-size: 26px;
  font-weight: bold;
  color: #333;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.code-input-box.active-input {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  border-width: 2px;
}

.hidden-input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.next-button {
  width: 100%;
  margin-top: 20px;
  height: 44px;
  font-size: 16px;
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 25px;
  gap: 8px;
}

.navigation-links {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .register-card {
    width: 90%;
    max-width: 450px;
  }
  
  .code-input-box {
    width: 45px;
    height: 50px;
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .code-input-box {
    width: 40px;
    height: 45px;
    font-size: 20px;
  }
}
</style>
