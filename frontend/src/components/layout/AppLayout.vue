<template>
  <el-container class="main-container">
    <el-header class="main-header">
      <div class="header-content">
        <div class="header-title">测联汇</div>
        <div class="subtitle">测试人的一站式服务中心</div>
      </div>
    </el-header>
    <el-main class="main-content">
      <slot></slot>
    </el-main>
  </el-container>
</template>

<script setup>
// 无需逻辑，仅提供布局
</script>

<style scoped>
.main-container {
  min-height: 100vh;
  height: 100%;
  background-color: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  display: flex;
  flex-direction: column;
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
  flex-shrink: 0;
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
  flex-grow: 1;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header-title {
    font-size: 28px;
  }
  
  .subtitle {
    font-size: 16px;
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