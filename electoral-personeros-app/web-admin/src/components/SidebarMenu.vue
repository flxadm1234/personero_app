<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="app.sidebarCollapsed"
    background-color="#2a1a47"
    text-color="#d1c4e9"
    active-text-color="#ffffff"
    router
    class="sidebar-menu"
  >
    <el-menu-item index="/">
      <el-icon><DataAnalysis /></el-icon>
      <template #title>Dashboard</template>
    </el-menu-item>

    <el-menu-item index="/tables/persona">
      <el-icon><User /></el-icon>
      <template #title>Personas</template>
    </el-menu-item>

    <el-menu-item index="/tables/usuarios">
      <el-icon><Avatar /></el-icon>
      <template #title>Usuarios</template>
    </el-menu-item>

    <el-menu-item index="/tables/usuarios_distritos">
      <el-icon><Location /></el-icon>
      <template #title>Distritos Asignados</template>
    </el-menu-item>

    <el-menu-item index="/tables/centro_votacion">
      <el-icon><OfficeBuilding /></el-icon>
      <template #title>Centros de Votación</template>
    </el-menu-item>

    <el-menu-item index="/tables/mesa">
      <el-icon><Grid /></el-icon>
      <template #title>Mesas</template>
    </el-menu-item>

    <el-menu-item index="/tables/personeros_users">
      <el-icon><UserFilled /></el-icon>
      <template #title>Personeros Asignaciones</template>
    </el-menu-item>

    <el-menu-item index="/importar">
      <el-icon><Upload /></el-icon>
      <template #title>📥 Carga Masiva Excel</template>
    </el-menu-item>

    <el-menu-item index="download" @click.prevent="descargarReporte">
      <el-icon><Download /></el-icon>
      <template #title>Descargar Reporte Excel</template>
    </el-menu-item>

    <el-menu-item index="/documentacion">
      <el-icon><Reading /></el-icon>
      <template #title>📚 Documentación</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  DataAnalysis, User, Avatar, Location, OfficeBuilding,
  Grid, UserFilled, Upload, Download, Reading
} from '@element-plus/icons-vue'
import { useAppStore } from '../stores/app.js'
import api from '../utils/api.js'
import { descargarBlobExcel } from '../utils/excel.js'

const route = useRoute()
const app = useAppStore()

const activeMenu = computed(() => route.path)

async function descargarReporte() {
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
</script>

<style scoped>
.sidebar-menu {
  border-right: none;
  height: 100%;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 230px;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 10px;
  border-radius: 10px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #5b3c8f, #7a52b3) !important;
  box-shadow: 0 4px 12px rgba(91, 60, 143, 0.4);
}

:deep(.el-menu-item:hover) {
  background-color: #3a255f !important;
}
</style>
