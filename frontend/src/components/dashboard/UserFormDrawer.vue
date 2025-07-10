<template>
  <el-drawer
    :model-value="visible"
    :title="isEditMode ? '编辑用户' : '新增用户'"
    direction="rtl"
    size="35%"
    @update:modelValue="$emit('update:visible', $event)"
    @closed="resetForm"
  >
    <div class="form-content">
      <el-form ref="userFormRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="头像" prop="avatar_url">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :http-request="handleAvatarUpload"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="formData.avatar_url" :src="formData.avatar_url" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEditMode">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone_number">
          <el-input v-model="formData.phone_number" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入地址"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色">
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="公司管理员" value="COMPANY"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="正常" value="ACTIVE"></el-option>
            <el-option label="禁用" value="INACTIVE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="formData.gender" placeholder="请选择性别">
            <el-option label="男" value="MALE"></el-option>
            <el-option label="女" value="FEMALE"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="个人描述" prop="description">
          <el-input v-model="formData.description" type="textarea" placeholder="请输入个人描述"></el-input>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, watch, defineProps, defineEmits } from 'vue';
import { ElDrawer, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElButton, ElMessage, ElUpload, ElIcon } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import OSS from 'ali-oss';
import userService from '../../services/userService';

const props = defineProps({
  visible: {
    type: Boolean,
    required: true,
  },
  isEditMode: {
    type: Boolean,
    default: false,
  },
  userData: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['update:visible', 'submit']);

const userFormRef = ref(null);

const initialFormData = {
  name: '',
  email: '',
  password: '',
  phone_number: '',
  address: '',
  role: 'USER',
  status: 'ACTIVE',
  gender: 'OTHER',
  description: '',
  company_id: null,
  avatar_url: '',
};

const formData = reactive({ ...initialFormData });

const rules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

watch(() => props.userData, (newVal) => {
  if (props.isEditMode && newVal) {
    // When editing, we receive camelCase data from Members.vue, convert it to snake_case for the form
    formData.name = newVal.name;
    formData.email = newVal.email;
    formData.phone_number = newVal.phoneNumber;
    formData.address = newVal.address;
    formData.role = newVal.role;
    formData.status = newVal.status;
    formData.gender = newVal.gender;
    formData.description = newVal.description;
    formData.company_id = newVal.companyId;
    formData.avatar_url = newVal.avatarUrl;
  }
});

const resetForm = () => {
  Object.assign(formData, initialFormData);
  userFormRef.value?.clearValidate();
};

const submitForm = () => {
  userFormRef.value.validate((valid) => {
    if (valid) {
      emit('submit', formData);
    } else {
      ElMessage.error('请检查表单输入');
      return false;
    }
  });
};

const beforeAvatarUpload = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片格式!');
    return false;
  }
  const isLt2M = rawFile.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

const handleAvatarUpload = async (options) => {
  const { file } = options;
  try {
    const stsResponse = await userService.getSTSCredentials();
    const stsData = stsResponse.data.data;

    const client = new OSS({
      region: stsData.region,
      accessKeyId: stsData.access_key_id,
      accessKeySecret: stsData.access_key_secret,
      stsToken: stsData.security_token,
      bucket: stsData.bucket_name,
    });

    const fileName = `avatars/${Date.now()}_${file.name}`;
    const result = await client.put(fileName, file);
    
    formData.avatar_url = result.url;
    ElMessage.success('头像上传成功');

  } catch (error) {
    console.error('头像上传失败:', error);
    ElMessage.error('头像上传失败，请重试');
  }
};
</script>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
}

.form-content {
  padding: 20px;
}
</style>
