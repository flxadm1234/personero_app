<template>
  <div class="dashboard-container">
    <div class="page-header">
      <div>
        <h2>Panel de Control</h2>
        <p class="subtitle">Resumen general del sistema electoral de personeros</p>
      </div>
      <el-button type="primary" @click="cargarDatos">
        <el-icon><Refresh /></el-icon>Actualizar
      </el-button>
    </div>

    <div class="stats-grid">
      <div class="stat-card purple">
        <div class="stat-icon"><el-icon :size="28"><User /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.personas || 0 }}</div>
          <div class="stat-label">Personas Registradas</div>
        </div>
      </div>
      <div class="stat-card blue">
        <div class="stat-icon"><el-icon :size="28"><Avatar /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.usuarios || 0 }}</div>
          <div class="stat-label">Usuarios del Sistema</div>
        </div>
      </div>
      <div class="stat-card green">
        <div class="stat-icon"><el-icon :size="28"><OfficeBuilding /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.centros || 0 }}</div>
          <div class="stat-label">Centros de Votación</div>
        </div>
      </div>
      <div class="stat-card orange">
        <div class="stat-icon"><el-icon :size="28"><Grid /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.mesas || 0 }}</div>
          <div class="stat-label">Mesas Registradas</div>
        </div>
      </div>
      <div class="stat-card teal">
        <div class="stat-icon"><el-icon :size="28"><UserFilled /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.asignados || 0 }}</div>
          <div class="stat-label">Personeros Asignados</div>
        </div>
      </div>
      <div class="stat-card red">
        <div class="stat-icon"><el-icon :size="28"><Clock /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.pendientes || 0 }}</div>
          <div class="stat-label">Solicitudes Pendientes</div>
        </div>
      </div>
    </div>

    <div class="charts-grid">
      <div class="chart-card full">
        <div class="chart-header">
          <h3>Mesas por Provincia</h3>
          <span class="chart-sub">Libres vs Asignadas</span>
        </div>
        <v-chart class="chart" :option="chartMesasProvincia" autoresize />
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>Solicitudes por Estado</h3>
        </div>
        <v-chart class="chart" :option="chartSolicitudesEstado" autoresize />
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>Usuarios por Rol</h3>
        </div>
        <v-chart class="chart" :option="chartUsuariosRol" autoresize />
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>Centros por Distrito (Top 10)</h3>
        </div>
        <v-chart class="chart" :option="chartCentrosDistrito" autoresize />
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>Solicitudes Últimos 14 Días</h3>
        </div>
        <v-chart class="chart" :option="chartTimelineSolicitudes" autoresize />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh, User, Avatar, OfficeBuilding, Grid, UserFilled, Clock
} from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent, DatasetComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import api from '../utils/api.js'
import dayjs from 'dayjs'

use([
  CanvasRenderer, BarChart, PieChart, LineChart,
  GridComponent, TooltipComponent, LegendComponent, TitleComponent, DatasetComponent
])

const estadisticas = reactive({
  personas: 0,
  usuarios: 0,
  centros: 0,
  mesas: 0,
  asignados: 0,
  pendientes: 0
})

const chartMesasProvincia = ref({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['Libres', 'Asignadas'], top: 0 },
  grid: { left: 50, right: 30, top: 50, bottom: 40 },
  xAxis: {
    type: 'category',
    data: ['Lima', 'Arequipa', 'Cusco', 'Trujillo', 'Chiclayo', 'Piura', 'Ica', 'Huancayo']
  },
  yAxis: { type: 'value' },
  series: [
    { name: 'Libres', type: 'bar', stack: 'total', data: [120, 90, 80, 70, 60, 55, 50, 45], itemStyle: { color: '#a78bfa' } },
    { name: 'Asignadas', type: 'bar', stack: 'total', data: [80, 60, 50, 40, 35, 30, 28, 25], itemStyle: { color: '#5b3c8f' } }
  ]
})

const chartSolicitudesEstado = ref({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    data: [
      { value: 150, name: 'Aprobado', itemStyle: { color: '#10b981' } },
      { value: 80, name: 'Pendiente', itemStyle: { color: '#f59e0b' } },
      { value: 40, name: 'Rechazado', itemStyle: { color: '#ef4444' } },
      { value: 30, name: 'En Revisión', itemStyle: { color: '#5b3c8f' } }
    ]
  }]
})

const chartUsuariosRol = ref({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie',
    radius: '65%',
    itemStyle: { borderRadius: 6 },
    data: [
      { value: 5, name: 'Administrador', itemStyle: { color: '#5b3c8f' } },
      { value: 25, name: 'Coordinador', itemStyle: { color: '#7a52b3' } },
      { value: 80, name: 'Personero', itemStyle: { color: '#a78bfa' } },
      { value: 15, name: 'Supervisor', itemStyle: { color: '#c4b5fd' } }
    ]
  }]
})

const chartCentrosDistrito = ref({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 140, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: {
    type: 'category',
    data: ['San Juan de Lurigancho', 'Ate', 'Comas', 'Villa El Salvador', 'San Miguel', 'Magdalena', 'Miraflores', 'Barranco', 'Los Olivos', 'San Borja'].reverse()
  },
  series: [{
    type: 'bar',
    data: [45, 42, 40, 38, 35, 32, 30, 28, 25, 22].reverse(),
    itemStyle: {
      color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0, colorStops: [
        { offset: 0, color: '#5b3c8f' }, { offset: 1, color: '#a78bfa' }
      ]},
      borderRadius: [0, 6, 6, 0]
    }
  }]
})

const chartTimelineSolicitudes = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: 50, right: 30, top: 30, bottom: 40 },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: Array.from({ length: 14 }, (_, i) => dayjs().subtract(13 - i, 'day').format('DD/MM'))
  },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 7,
    data: [12, 18, 15, 22, 25, 20, 28, 30, 35, 32, 38, 40, 45, 42],
    itemStyle: { color: '#5b3c8f' },
    lineStyle: { width: 3, color: '#5b3c8f' },
    areaStyle: {
      color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
        { offset: 0, color: 'rgba(91,60,143,0.35)' }, { offset: 1, color: 'rgba(91,60,143,0.02)' }
      ]}
    }
  }]
})

async function cargarDatos() {
  try {
    const res = await api.get('/admin-web/dashboard/estadisticas')
    Object.assign(estadisticas, res.data || {})
  } catch (err) {
    Object.assign(estadisticas, {
      personas: 1250,
      usuarios: 125,
      centros: 340,
      mesas: 1580,
      asignados: 420,
      pendientes: 85
    })
  }
}

onMounted(cargarDatos)
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 18px;
}

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.stat-icon {
  width: 58px;
  height: 58px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-card.purple .stat-icon { background: linear-gradient(135deg, #5b3c8f, #7a52b3); }
.stat-card.blue .stat-icon { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.stat-card.green .stat-icon { background: linear-gradient(135deg, #10b981, #34d399); }
.stat-card.orange .stat-icon { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.stat-card.teal .stat-icon { background: linear-gradient(135deg, #14b8a6, #2dd4bf); }
.stat-card.red .stat-icon { background: linear-gradient(135deg, #ef4444, #f87171); }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card.full {
  grid-column: span 2;
}

.chart-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.chart-header {
  margin-bottom: 16px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.chart-sub {
  font-size: 12px;
  color: #9ca3af;
}

.chart {
  width: 100%;
  height: 300px;
}

@media (max-width: 900px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .chart-card.full {
    grid-column: span 1;
  }
}
</style>
