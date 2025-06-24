import { createRouter, createWebHistory } from 'vue-router'

// 导入视图组件
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import RegisterEmailView from '../views/RegisterEmailView.vue'
import RegisterVerifyCodeView from '../views/RegisterVerifyCodeView.vue'
import RegisterCompanyInfoView from '../views/RegisterCompanyInfoView.vue'

// 导入仪表盘相关组件
import DashboardLayout from '../components/layout/DashboardLayout.vue'
import DashboardHome from '../views/dashboard/DashboardHome.vue'
import ProjectList from '../views/dashboard/ProjectList.vue'

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
  // 仪表盘路由配置
  {
    path: '/dashboard',
    component: DashboardLayout,
    redirect: '/dashboard/home',
    children: [
      {
        path: 'home',
        name: 'DashboardHome',
        component: DashboardHome,
        meta: { title: '首页' }
      },
      {
        path: 'projects',
        name: 'ProjectList',
        component: ProjectList,
        meta: { title: '项目管理' }
      },
      {
        path: 'testcases',
        name: 'TestCases',
        component: () => import('../views/dashboard/TestCases.vue'),
        meta: { title: '测试用例' }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('../views/dashboard/Reports.vue'),
        meta: { title: '测试报告' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/dashboard/Settings.vue'),
        meta: { title: '系统设置' }
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
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 测联汇`
  } else {
    document.title = '测联汇'
  }
  
  // 检查是否需要登录验证
  const publicPages = ['/', '/register', '/register/email', '/register/verify-code', '/register/company-info']
  const authRequired = !publicPages.includes(to.path)
  const loggedIn = localStorage.getItem('token')
  
  if (authRequired && !loggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router 