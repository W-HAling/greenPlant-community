import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'plant',
        name: 'Plant',
        component: () => import('@/views/plant/index.vue'),
        meta: { title: '绿植管理', icon: 'Cherry' }
      },
      {
        path: 'adoption',
        name: 'Adoption',
        component: () => import('@/views/adoption/index.vue'),
        meta: { title: '领养管理', icon: 'Document' }
      },
      {
        path: 'care',
        name: 'Care',
        component: () => import('@/views/care/index.vue'),
        meta: { title: '养护记录', icon: 'EditPen' }
      },
      {
        path: 'care-plan',
        name: 'CarePlan',
        component: () => import('@/views/care-plan/index.vue'),
        meta: { title: '养护模板', icon: 'Calendar' }
      },
      {
        path: 'care-task',
        name: 'CareTask',
        component: () => import('@/views/care-task/index.vue'),
        meta: { title: '养护任务', icon: 'List' }
      },
      {
        path: 'drift-bottle',
        name: 'DriftBottle',
        component: () => import('@/views/drift-bottle/index.vue'),
        meta: { title: '漂流瓶管理', icon: 'Promotion' }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/index.vue'),
        meta: { title: '社区管理', icon: 'ChatDotRound' }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/department/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/config/index.vue'),
        meta: { title: '系统配置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
