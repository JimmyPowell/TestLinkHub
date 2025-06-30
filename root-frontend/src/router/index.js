import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import UserManagement from '../views/UserManagement.vue'
import CompanyManagement from '../views/CompanyManagement.vue'
import NewsManagement from '../views/NewsManagement.vue'
import CourseManagement from '../views/CourseManagement.vue'
import MeetingManagement from '../views/MeetingManagement.vue'
import RootLogin from '../views/RootLogin.vue'

const routes = [
  { path: '/login', component: RootLogin },
  { path: '/', redirect: '/user-management' },
  { path: '/user-management', component: UserManagement },
  { path: '/company-management', component: CompanyManagement },
  { path: '/news-management', component: NewsManagement },
  { path: '/course-management', component: CourseManagement },
  { path: '/meeting-management', component: MeetingManagement }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.path !== '/login' && !authStore.isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
