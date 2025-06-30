<template>
  <el-drawer
    :model-value="visible"
    :title="isEditMode ? '编辑公司' : '新增公司'"
    direction="rtl"
    size="40%"
    @update:model-value="$emit('update:visible', $event)"
    @closed="onDrawerClose"
  >
    <el-form :model="form" ref="formRef" label-width="100px" class="form-container">
      <el-form-item label="公司名称" prop="name" :rules="[{ required: true, message: '请输入公司名称' }]">
        <el-input v-model="form.name" placeholder="请输入公司名称"></el-input>
      </el-form-item>
      <el-form-item label="公司代码" prop="companyCode" :rules="[{ required: true, message: '请输入公司代码' }]">
        <el-input v-model="form.companyCode" placeholder="请输入公司代码"></el-input>
      </el-form-item>
      <el-form-item label="邮箱" prop="email" :rules="[{ required: true, type: 'email', message: '请输入有效的邮箱地址' }]">
        <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
      </el-form-item>
      <el-form-item label="手机号" prop="phoneNumber">
        <el-input v-model="form.phoneNumber" placeholder="请输入手机号"></el-input>
      </el-form-item>
      <el-form-item label="状态" prop="status" :rules="[{ required: true, message: '请选择状态' }]">
        <el-select v-model="form.status" placeholder="请选择状态">
          <el-option label="正常" value="ACTIVE"></el-option>
          <el-option label="待审核" value="PENDING"></el-option>
          <el-option label="暂停" value="SUSPENDED"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="地址" prop="address">
        <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入地址"></el-input>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import { ElDrawer, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElButton } from 'element-plus';

const props = defineProps({
  visible: Boolean,
  isEditMode: Boolean,
  companyData: Object,
});

const emit = defineEmits(['update:visible', 'submit']);

const formRef = ref(null);
const form = ref({});

watch(() => props.visible, (newVal) => {
  if (newVal) {
    // Use a deep copy to avoid modifying the original table data
    form.value = JSON.parse(JSON.stringify(props.companyData || {}));
  }
});

const onDrawerClose = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      emit('submit', form.value);
    }
  });
};
</script>

<style scoped>
.form-container {
  padding: 20px;
}
</style>
