<template>
  <router-view />
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow-x: hidden; /* 防止横向滚动 */
  box-sizing: border-box; /* 确保内边距和边框不会增加元素尺寸 */
}

#app {
  width: 100%;
  height: 100vh;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

* {
  box-sizing: border-box;
}

.el-main {
  flex: 1;
}

.el-container {
  height: 100%;
}
</style>

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
