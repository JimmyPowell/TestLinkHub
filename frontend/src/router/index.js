import { createRouter, createWebHistory } from 'vue-router'

// 导入视图组件
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import RegisterEmailView from '../views/RegisterEmailView.vue'
import RegisterVerifyCodeView from '../views/RegisterVerifyCodeView.vue'
import RegisterCompanyInfoView from '../views/RegisterCompanyInfoView.vue'

// 创建路由
const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    component: RegisterView,
    children: [
      {
        path: 'email',
        name: 'registerEmail',
        component: RegisterEmailView
      },
      {
        path: 'verify-code',
        name: 'registerVerifyCode',
        component: RegisterVerifyCodeView
      },
      {
        path: 'company-info',
        name: 'registerCompanyInfo',
        component: RegisterCompanyInfoView
      },
      // 默认重定向到第一步
      {
        path: '',
        redirect: '/register/email'
      }
    ]
  },
  // 捕获所有未匹配路由
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫，可以添加导航逻辑
router.beforeEach((to, from, next) => {
  // 可以在这里添加导航守卫逻辑
  next()
})

export default router 