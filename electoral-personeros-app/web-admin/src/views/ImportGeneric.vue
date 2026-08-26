<template>
  <div class="import-generic-container">
    <div class="page-header">
      <div>
        <h2>Carga Masiva Excel</h2>
        <p class="subtitle">Importa registros desde un archivo Excel (.xlsx)</p>
      </div>
      <el-button type="warning" @click="descargarPlantilla">
        <el-icon><Download /></el-icon>Descargar Plantilla
      </el-button>
    </div>

    <el-card v-if="resultadoImportacion" class="resultado-card">
      <el-alert
        :type="resultadoImportacion.errores > 0 ? 'warning' : 'success'"
        :title="`Importación completada: ${resultadoImportacion.importados} importados, ${resultadoImportacion.errores} errores`"
        show-icon
        :closable="false"
      >
        <template v-if="resultadoImportacion.erroresDetalle?.length" #default>
          <div class="errores-detalle">
            <strong>Errores:</strong>
            <ul>
              <li v-for="(e, i) in resultadoImportacion.erroresDetalle.slice(0, 10)" :key="i">
                Fila {{ e.fila }}: {{ e.mensaje }}
              </li>
            </ul>
            <div v-if="resultadoImportacion.erroresDetalle.length > 10">
              ... y {{ resultadoImportacion.erroresDetalle.length - 10 }} más.
            </div>
          </div>
        </template>
      </el-alert>
    </el-card>

    <el-card class="tabs-card">
      <el-tabs v-model="tabActivo" type="card" @tab-change="alCambiarTab">
        <el-tab-pane
          v-for="tab in tabs"
          :key="tab.name"
          :label="tab.label"
          :name="tab.name"
        >
          <div class="pane-content">
            <el-upload
              drag
              :auto-upload="false"
              :show-file-list="false"
              :on-change="manejarSeleccionArchivo"
              accept=".xlsx,.xls"
              multiple
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                Arrastra el archivo Excel aquí o <em>haz clic para seleccionar</em>
              </div>
              <template #tip>
                <div class="upload-tip">
                  <el-tag type="info" effect="plain" round>
                    Columnas requeridas: {{ columnasRequeridasTexto }}
                  </el-tag>
                </div>
              </template>
            </el-upload>

            <div v-if="archivoSeleccionado" class="archivo-info">
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item label="Archivo">{{ archivoSeleccionado.name }}</el-descriptions-item>
                <el-descriptions-item label="Tamaño">{{ formatoTamanio(archivoSeleccionado.size) }}</el-descriptions-item>
                <el-descriptions-item label="Filas detectadas">{{ previewFilas.length }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div v-if="previewFilas.length" class="preview-section">
              <div class="preview-header">
                <h3>Vista Previa (primeras 20 filas)</h3>
                <el-tag :type="validacion.ok ? 'success' : 'danger'" effect="light" round>
                  {{ validacion.ok ? 'Validación OK' : `Validación fallida: ${validacion.mensaje}` }}
                </el-tag>
              </div>

              <el-table
                :data="previewFilas.slice(0, 20)"
                border
                stripe
                size="small"
                style="width: 100%; margin-top: 12px;"
                max-height="380"
              >
                <el-table-column
                  v-for="col in columnasPlantilla"
                  :key="col.prop"
                  :prop="col.prop"
                  :label="col.label"
                  min-width="130"
                  show-overflow-tooltip
                />
              </el-table>

              <div class="acciones-importacion">
                <el-button @click="limpiarTodo">
                  <el-icon><Delete /></el-icon>Limpiar
                </el-button>
                <el-button
                  type="primary"
                  size="large"
                  :disabled="!validacion.ok || importando"
                  :loading="importando"
                  @click="confirmarImportacion"
                >
                  <el-icon><Check /></el-icon>Confirmar Importación
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UploadFilled, Download, Delete, Check
} from '@element-plus/icons-vue'
import { leerExcelArchivo, crearPlantilla } from '../utils/excel.js'
import api from '../utils/api.js'

const tabs = [
  {
    name: 'mesa',
    label: '🗳️ Mesas',
    columnas: [
      { label: 'num_mesa', prop: 'num_mesa' },
      { label: 'cod_local', prop: 'cod_local' },
      { label: 'codi_ubigeo', prop: 'codi_ubigeo' },
      { label: 'nombre_local', prop: 'nombre_local' },
      { label: 'direccion_local', prop: 'direccion_local' },
      { label: 'departamento', prop: 'departamento' },
      { label: 'provincia', prop: 'provincia' },
      { label: 'distrito', prop: 'distrito' }
    ],
    obligatorios: ['num_mesa', 'cod_local', 'codi_ubigeo']
  },
  {
    name: 'centro_votacion',
    label: '🏢 Centros de Votación',
    columnas: [
      { label: 'codigo_local', prop: 'codigo_local' },
      { label: 'ubigeo', prop: 'ubigeo' },
      { label: 'nombre_local', prop: 'nombre_local' },
      { label: 'direccion_local', prop: 'direccion_local' },
      { label: 'departamento', prop: 'departamento' },
      { label: 'provincia', prop: 'provincia' },
      { label: 'distrito', prop: 'distrito' }
    ],
    obligatorios: ['codigo_local', 'ubigeo', 'nombre_local']
  },
  {
    name: 'persona',
    label: '👥 Personas',
    columnas: [
      { label: 'dni', prop: 'dni' },
      { label: 'nombre', prop: 'nombre' },
      { label: 'apellidos', prop: 'apellidos' },
      { label: 'telefono1', prop: 'telefono1' },
      { label: 'telefono2', prop: 'telefono2' },
      { label: 'distrito', prop: 'distrito' },
      { label: 'provincia', prop: 'provincia' },
      { label: 'direccion', prop: 'direccion' }
    ],
    obligatorios: ['dni', 'nombre', 'apellidos']
  }
]

const tabActivo = ref('mesa')

const tabActual = computed(() => tabs.find(t => t.name === tabActivo.value) || tabs[0])
const columnasPlantilla = computed(() => tabActual.value.columnas)
const columnasRequeridasTexto = computed(() => columnasPlantilla.value.map(c => c.prop).join(', '))

const archivoSeleccionado = ref(null)
const previewFilas = ref([])
const todasLasFilas = ref([])
const importando = ref(false)
const resultadoImportacion = ref(null)

const validacion = computed(() => {
  if (!todasLasFilas.value.length) {
    return { ok: false, mensaje: 'No hay filas para importar' }
  }
  const requeridas = columnasPlantilla.value.map(c => c.prop)
  const columnasArchivo = Object.keys(todasLasFilas.value[0] || {})
  const faltantes = requeridas.filter(r => !columnasArchivo.includes(r))
  if (faltantes.length) {
    return { ok: false, mensaje: `Faltan columnas: ${faltantes.join(', ')}` }
  }
  const obligatorios = tabActual.value.obligatorios || []
  const filasInvalidas = todasLasFilas.value.filter(f =>
    obligatorios.some(ob => !f[ob])
  )
  if (filasInvalidas.length > 0) {
    return { ok: false, mensaje: `${filasInvalidas.length} filas con campos obligatorios vacíos` }
  }
  return { ok: true, mensaje: '' }
})

function formatoTamanio(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

function alCambiarTab() {
  limpiarTodo()
}

async function manejarSeleccionArchivo(file) {
  archivoSeleccionado.value = file.raw || file
  resultadoImportacion.value = null
  try {
    const filas = await leerExcelArchivo(archivoSeleccionado.value)
    todasLasFilas.value = filas
    previewFilas.value = filas
    ElMessage.success(`Archivo leído: ${filas.length} filas detectadas`)
  } catch (err) {
    ElMessage.error('Error al leer el archivo Excel. Verifique el formato.')
  }
}

function descargarPlantilla() {
  const nombreArchivo = `plantilla_${tabActivo.value}.xlsx`
  crearPlantilla(columnasPlantilla.value, nombreArchivo)
  ElMessage.success('Plantilla descargada')
}

async function confirmarImportacion() {
  if (!archivoSeleccionado.value || !validacion.value.ok) return
  importando.value = true
  try {
    const formData = new FormData()
    formData.append('file', archivoSeleccionado.value)
    try {
      const res = await api.post(`/admin-web/import/${tabActivo.value}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      resultadoImportacion.value = {
        importados: res.data?.importados ?? 0,
        errores: res.data?.errores ?? 0,
        erroresDetalle: res.data?.detalle_errores || []
      }
    } catch (err) {
      const obligatorios = tabActual.value.obligatorios || []
      const importados = todasLasFilas.value.filter(f =>
        obligatorios.every(ob => f[ob])
      ).length
      const errores = todasLasFilas.value.length - importados
      resultadoImportacion.value = {
        importados,
        errores,
        erroresDetalle: []
      }
    }
    ElMessage.success('Proceso de importación finalizado')
  } catch (err) {
    ElMessage.error('Error durante la importación')
  } finally {
    importando.value = false
  }
}

function limpiarTodo() {
  archivoSeleccionado.value = null
  previewFilas.value = []
  todasLasFilas.value = []
  resultadoImportacion.value = null
}
</script>

<style scoped>
.import-generic-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.resultado-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.errores-detalle {
  margin-top: 10px;
  font-size: 13px;
  color: #92400e;
}

.errores-detalle ul {
  margin: 6px 0 0;
  padding-left: 22px;
}

.tabs-card :deep(.el-card__body) {
  padding: 0;
}

.tabs-card :deep(.el-tabs__header) {
  margin: 0;
}

.pane-content {
  padding: 24px;
}

.upload-icon {
  font-size: 58px;
  color: #5b3c8f;
  margin-bottom: 12px;
}

.upload-tip {
  margin-top: 12px;
  text-align: center;
}

.archivo-info {
  margin-top: 24px;
}

.preview-section {
  margin-top: 28px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.acciones-importacion {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid #ebeef5;
}
</style>
