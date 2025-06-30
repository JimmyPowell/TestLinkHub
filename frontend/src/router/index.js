import { createRouter, createWebHistory } from 'vue-router'

// 导入视图组件
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import RegisterEmailView from '../views/RegisterEmailView.vue'
import RegisterVerifyCodeView from '../views/RegisterVerifyCodeView.vue'
import RegisterCompanyInfoView from '../views/RegisterCompanyInfoView.vue'

// 导入仪表盘相关组件
import DashboardLayout from '../components/layout/DashboardLayout.vue'
import RootLayout from '../components/layout/RootLayout.vue'
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
        redirect: 'email'
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
        path: 'news',
        name: 'News',
        component: () => import('../views/dashboard/News.vue'),
        meta: { title: '新闻动态' }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('../views/dashboard/Courses.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: 'course/:uuid',
        name: 'CourseDetail',
        component: () => import('../views/dashboard/CourseDetail.vue'),
        meta: { title: '课程详情' }
      },
      {
        path: 'meetings',
        name: 'Meetings',
        component: () => import('../views/dashboard/Meetings.vue'),
        meta: { title: '会议管理' }
      },
      {
        path: 'meeting-info',
        name: 'MeetingInfo',
        component: () => import('../views/dashboard/MeetingInfo.vue'),
        meta: { title: '会议信息管理' }
      },
      {
        path: 'meeting-attendees',
        name: 'MeetingAttendees',
        component: () => import('../views/dashboard/MeetingAttendees.vue'),
        meta: { title: '参会人员管理' }
      },
      {
        path: 'members',
        name: 'Members',
        component: () => import('../views/dashboard/Members.vue'),
        meta: { title: '成员管理' }
      }
    ]
  },
  // 超级管理员面板
  {
    path: '/root',
    component: RootLayout,
    redirect: '/root/home',
    children: [
      {
        path: 'home',
        name: 'RootAdmin',
        component: () => import('../views/dashboard/RootAdmin.vue'),
        meta: { requiresAuth: true, role: 'ROOT', title: '超级管理员面板' }
      },
      {
        path: 'news-management',
        name: 'RootNewsManagement',
        component: { template: '<div>新闻管理页面，待实现</div>' },
        meta: { title: '新闻管理' }
      },
      {
        path: 'meeting-management',
        name: 'RootMeetingManagement',
        component: { template: '<div>会议管理页面，待实现</div>' },
        meta: { title: '会议管理' }
      },
      {
        path: 'course-management',
        name: 'RootCourseManagement',
        component: { template: '<div>课程管理页面，待实现</div>' },
        meta: { title: '课程管理' }
      },
      {
        path: 'company-management',
        name: 'RootCompanyManagement',
        component: { template: '<div>公司管理页面，待实现</div>' },
        meta: { title: '公司管理' }
      },
      {
        path: 'user-management',
        name: 'RootUserManagement',
        component: () => import('../views/dashboard/RootUserManagement.vue'),
        meta: { requiresAuth: true, role: 'ROOT', title: '用户管理' }
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

import { useAuthStore } from '../store/auth';

// 路由守卫，可以添加导航逻辑
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 测联汇`
  } else {
    document.title = '测联汇'
  }
  
  const authStore = useAuthStore();
  const publicPages = ['/', '/register', '/register/email', '/register/verify-code', '/register/company-info'];
  const authRequired = !publicPages.includes(to.path);

  // This is a simplified guard. For a full implementation, we'd verify the token with the backend.
  // For now, we'll trust the token in localStorage and the state in Pinia.
  if (authRequired && !authStore.isAuthenticated) {
    // If the user is not authenticated and the page requires auth, redirect to login.
    // A more robust implementation would try to verify the token with a backend call here.
    // e.g., if (authStore.accessToken && !authStore.user) { await authStore.fetchUser(); }
    next('/');
  } else {
    next();
  }
});

export default router
