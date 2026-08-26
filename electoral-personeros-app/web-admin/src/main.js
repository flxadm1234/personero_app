import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import es from 'element-plus/es/locale/lang/es'

import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent,
  DatasetComponent, DataZoomComponent
} from 'echarts/components'
import ECharts from 'vue-echarts'

import App from './App.vue'
import router from './router/index.js'
import './style.css'

use([
  CanvasRenderer,
  BarChart, PieChart, LineChart,
  GridComponent, TooltipComponent, LegendComponent,
  TitleComponent, DatasetComponent, DataZoomComponent
])

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.component('v-chart', ECharts)

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: es })

app.mount('#app')
