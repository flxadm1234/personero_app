<template>
  <el-container class="main-container">
    <el-aside
      :width="app.sidebarCollapsed ? '64px' : '230px'"
      class="aside-container"
    >
      <div class="sidebar-logo" :class="{ collapsed: app.sidebarCollapsed }">
        <el-icon :size="28" color="#fff"><DataAnalysis /></el-icon>
        <span v-if="!app.sidebarCollapsed" class="logo-text">Personeros</span>
      </div>
      <SidebarMenu />
    </el-aside>

    <el-container>
      <el-header class="header-container">
        <div class="header-left">
          <el-button
            text
            class="collapse-btn"
            @click="app.toggleSidebar"
          >
            <el-icon :size="22">
              <Fold v-if="!app.sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">Inicio</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-button type="success" @click="descargarReporteGlobal">
            <el-icon><Download /></el-icon>
            <span>Descargar Reporte</span>
          </el-button>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar
                :size="38"
                style="background: linear-gradient(135deg, #5b3c8f, #7a52b3)"
              >
                {{ userInitials }}
              </el-avatar>
              <div class="user-details" v-if="auth.user">
                <div class="user-name">{{ auth.user.usuario || auth.user.nombre || 'Usuario' }}</div>
                <div class="user-role">{{ auth.user.rol || 'Administrador' }}</div>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="perfil">
                  <el-icon><User /></el-icon>Mi Perfil
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>Cerrar Sesión
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataAnalysis, Fold, Expand, Download, ArrowDown,
  User, SwitchButton
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth.js'
import { useAppStore } from '../stores/app.js'
import SidebarMenu from '../components/SidebarMenu.vue'
import api from '../utils/api.js'
import { descargarBlobExcel } from '../utils/excel.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const app = useAppStore()

const nombresPaginas = {
  '': 'Dashboard',
  'persona': 'Personas',
  'usuarios': 'Usuarios',
  'usuarios_distritos': 'Distritos Asignados',
  'centro_votacion': 'Centros de Votación',
  'mesa': 'Mesas',
  'personeros_users': 'Personeros Asignaciones',
  'importar': 'Carga Masiva Excel',
  'documentacion': '📚 Documentación del Sistema'
}

const currentPageName = computed(() => {
  const tabla = route.params?.tabla || route.name?.toLowerCase() || ''
  const path = route.path.replace('/tables/', '').replace('/', '')
  return nombresPaginas[tabla] || nombresPaginas[path] || 'Página'
})

const userInitials = computed(() => {
  if (!auth.user) return 'U'
  const nombre = auth.user.nombre || auth.user.usuario || 'U'
  return nombre.toString().charAt(0).toUpperCase()
})

async function descargarReporteGlobal() {
  try {
    const res = await api.get('/admin-web/reporte/excel', {
      responseType: 'blob'
    })
    descargarBlobExcel(res.data, `reporte_personeros_${Date.now()}.xlsx`)
    ElMessage.success('Reporte descargado')
  } catch (err) {
    ElMessage.error('Error al descargar el reporte')
  }
}

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm(
        '¿Está seguro de cerrar sesión?',
        'Confirmar',
        { type: 'warning' }
      )
      auth.logout()
      ElMessage.success('Sesión cerrada')
      router.push('/login')
    } catch {
    }
  }
}
</script>

<style scoped>
.main-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.aside-container {
  background: #2a1a47;
  transition: width 0.28s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  color: #fff;
  flex-shrink: 0;
}

.sidebar-logo.collapsed {
  padding: 0;
  justify-content: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.header-container {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #eceef1;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  color: #5b3c8f;
  padding: 8px;
}

.collapse-btn:hover {
  background: #f5f3fb;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px 6px 6px;
  border-radius: 32px;
  cursor: pointer;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f3fb;
}

.user-details {
  text-align: right;
  line-height: 1.2;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.user-role {
  font-size: 12px;
  color: #6b7280;
}

.main-content {
  background: #f5f6fa;
  padding: 24px;
  overflow-y: auto;
}
</style>
