<template>
  <el-card class="register-card" shadow="never">
    <h2 class="register-title">完善企业信息</h2>

    <el-form @submit.prevent="submitCompanyInfo" :model="form" :rules="rules" ref="formRef" label-position="top">
      <!-- 两列布局 -->
      <div class="form-row">
        <div class="form-col">
          <el-form-item label="公司名称" prop="companyName">
            <el-input v-model="form.companyName" placeholder="请输入公司名称"></el-input>
          </el-form-item>
          
          <el-form-item label="公司地址" prop="companyAddress">
            <el-input v-model="form.companyAddress" placeholder="请输入公司地址"></el-input>
          </el-form-item>
        </div>
        
        <div class="form-col">
          <el-form-item label="联系人姓名" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人姓名"></el-input>
          </el-form-item>

          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系电话"></el-input>
            <div v-if="showPhoneError" class="error-tip">请输入有效的手机号码</div>
          </el-form-item>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <el-form-item label="设置密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请设置登录密码" 
              show-password
            ></el-input>
            <div class="password-tip">
              <span>密码长度不少于6位字符</span>
              <div v-if="form.password" class="password-strength">
                <span>强度：</span>
                <div class="strength-meter">
                  <div class="strength-bar" :class="passwordStrengthClass"></div>
                </div>
                <span class="strength-text" :class="passwordStrengthClass">{{ passwordStrengthText }}</span>
              </div>
            </div>
          </el-form-item>
        </div>
        <div class="form-col">
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请再次输入登录密码" 
              show-password
            ></el-input>
          </el-form-item>
        </div>
      </div>

      <div class="button-container">
        <el-button 
          type="primary" 
          @click="submitCompanyInfo" 
          class="next-button" 
          :loading="loading">
          提交注册
        </el-button>
      </div>
    </el-form>

    <div class="navigation-links">
      <el-link type="info" @click="$router.push('/register/verify-code')" :underline="false">
        <span class="back-text">返回上一步</span>
      </el-link>
    </div>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);
const formSubmitted = ref(false);

// 从本地存储获取邮箱和验证码
const email = localStorage.getItem('registerEmail') || '';
const verificationCode = localStorage.getItem('verificationCode') || '';

// 表单数据
const form = reactive({
  companyName: '',
  companyAddress: '',
  contactName: '',
  contactPhone: '',
  password: '',
  confirmPassword: ''
});

// 根据手机号码是否合法计算是否显示错误提示
const showPhoneError = computed(() => {
  if (!form.contactPhone) return false;
  if (!formSubmitted.value && form.contactPhone.length < 11) return false;
  const phoneRegex = /^1[3-9]\d{9}$/;
  return !phoneRegex.test(form.contactPhone);
});

// 密码强度检查
const passwordStrength = computed(() => {
  if (!form.password) return 0;
  
  let score = 0;
  // 长度检查
  if (form.password.length >= 6) score += 1;
  if (form.password.length >= 10) score += 1;
  
  // 复杂度检查
  if (/[A-Z]/.test(form.password)) score += 1;
  if (/[a-z]/.test(form.password)) score += 1;
  if (/[0-9]/.test(form.password)) score += 1;
  if (/[^A-Za-z0-9]/.test(form.password)) score += 1;
  
  return Math.min(3, score);
});

// 密码强度类名
const passwordStrengthClass = computed(() => {
  if (!form.password) return '';
  const strengthClasses = ['weak', 'medium', 'strong'];
  return strengthClasses[passwordStrength.value - 1] || '';
});

// 密码强度文字
const passwordStrengthText = computed(() => {
  if (!form.password) return '';
  const texts = ['弱', '中', '强'];
  return texts[passwordStrength.value - 1] || '';
});

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'));
  } else {
    if (form.confirmPassword !== '') {
      formRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

const validateConfirmPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/;
  if (value === '') {
    callback(new Error('请输入手机号码'));
  } else if (!phoneRegex.test(value)) {
    callback(new Error('请输入有效的手机号码'));
  } else {
    callback();
  }
};

const rules = {
  companyName: [
    { required: true, message: '请输入公司名称', trigger: 'blur' },
    { min: 2, max: 50, message: '公司名称长度应在2到50个字符之间', trigger: 'blur' }
  ],
  companyAddress: [
    { required: true, message: '请输入公司地址', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '联系人姓名长度应在2到20个字符之间', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPass, trigger: 'blur' }
  ]
};

// 提交公司信息
const submitCompanyInfo = async () => {
  if (!formRef.value) return;
  
  formSubmitted.value = true;
  
  try {
    await formRef.value.validate();
    
    loading.value = true;

    // 构建注册请求数据
    const registerData = {
      email,
      verificationCode,
      ...form
    };
    
    // 删除确认密码字段
    delete registerData.confirmPassword;
    
    // 这里应该调用后端注册API
    await new Promise(resolve => setTimeout(resolve, 1500)); // 模拟API调用
    
    // 清除本地存储的注册过程数据
    localStorage.removeItem('registerEmail');
    localStorage.removeItem('verificationCode');
    
    ElMessage.success('注册成功！');
    
    // 跳转到登录页面
    router.push('/');
  } catch (error) {
    console.error('注册失败:', error);
    ElMessage.error(error?.message || '注册失败，请重试');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  // 检查是否有邮箱和验证码
  if (!email || !verificationCode) {
    ElMessage.warning('请先完成邮箱验证和验证码确认步骤');
    router.push('/register/email');
  }
});
</script>

<style scoped>
.register-card {
  width: 700px;
  padding: 30px 50px 40px;
  margin: 20px auto;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
  background-color: #fff;
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -15px;
}

.form-col {
  flex: 1;
  padding: 0 15px;
  min-width: 280px;
}

.button-container {
  display: flex;
  justify-content: center;
  padding: 10px 0;
  margin-top: 10px;
  margin-bottom: 10px;
}

.next-button {
  width: 280px;
  margin: 10px 0;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  border-radius: 4px;
}

.next-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.next-button:active {
  transform: translateY(0);
}

.full-width {
  width: 100%;
}

.navigation-links {
  display: flex;
  justify-content: center;
  margin-top: 25px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}

.back-text {
  position: relative;
  padding-left: 20px;
}

.back-text::before {
  content: "←";
  position: absolute;
  left: 0;
  font-size: 14px;
  top: 50%;
  transform: translateY(-50%);
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-form-item__label.is-required::before) {
  color: #f56c6c;
}

:deep(.el-input.is-focus .el-input__inner) {
  border-color: #409EFF;
}

:deep(.el-input__inner:hover) {
  border-color: #c0c4cc;
}

:deep(.el-input__inner) {
  transition: all 0.3s;
}

:deep(.el-select .el-input__inner:focus) {
  border-color: #409EFF;
}

:deep(.el-form-item.is-error .el-input__inner) {
  border-color: #f56c6c;
}

:deep(.el-card__body) {
  padding: 0;
}

.error-tip {
  margin-top: 5px;
  color: #f56c6c;
  font-size: 12px;
  line-height: 1.2;
}

.password-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
  line-height: 1.2;
}

.password-strength {
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.strength-meter {
  width: 100px;
  height: 4px;
  background-color: #e4e7ed;
  border-radius: 2px;
  margin: 0 8px;
  overflow: hidden;
  position: relative;
}

.strength-bar {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s ease;
}

.strength-bar.weak {
  width: 33%;
  background-color: #f56c6c;
}

.strength-bar.medium {
  width: 66%;
  background-color: #e6a23c;
}

.strength-bar.strong {
  width: 100%;
  background-color: #67c23a;
}

.strength-text {
  font-size: 12px;
}

.strength-text.weak {
  color: #f56c6c;
}

.strength-text.medium {
  color: #e6a23c;
}

.strength-text.strong {
  color: #67c23a;
}

@media (max-width: 768px) {
  .register-card {
    width: 95%;
    max-width: 700px;
    padding: 20px 20px 30px;
  }
  
  .form-col {
    flex: 100%;
    margin-bottom: 10px;
  }
  
  .next-button {
    width: 100%;
  }
}
</style>
