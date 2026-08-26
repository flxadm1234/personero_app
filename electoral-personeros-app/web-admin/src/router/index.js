import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'tables/:tabla',
        name: 'TableAdmin',
        component: () => import('../views/TableAdmin.vue'),
        props: true
      },
      {
        path: 'importar',
        name: 'ImportGeneric',
        component: () => import('../views/ImportGeneric.vue'),
        meta: { title: 'Carga Masiva Excel' }
      },
      {
        path: 'documentacion',
        name: 'Documentacion',
        component: () => import('../views/Documentacion.vue'),
        meta: { requiresAuth: true, title: 'Documentacion del Sistema' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && auth.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
