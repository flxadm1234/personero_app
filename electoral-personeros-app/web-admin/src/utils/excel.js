import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

export function descargarBlobExcel(blob, nombreArchivo) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = nombreArchivo
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

export function exportarTablaAExcel(datos, columnas, nombreArchivo = 'reporte.xlsx') {
  const cabeceras = columnas.map(c => c.label)
  const claves = columnas.map(c => c.prop)
  const filas = datos.map(fila => claves.map(clave => fila[clave] ?? ''))
  const wsDatos = [cabeceras, ...filas]
  const ws = XLSX.utils.aoa_to_sheet(wsDatos)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Datos')
  const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
  saveAs(new Blob([wbout], { type: 'application/octet-stream' }), nombreArchivo)
}

export function crearPlantilla(columnas, nombreArchivo = 'plantilla.xlsx') {
  const cabeceras = columnas.map(c => c.label)
  const ws = XLSX.utils.aoa_to_sheet([cabeceras])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Plantilla')
  const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
  saveAs(new Blob([wbout], { type: 'application/octet-stream' }), nombreArchivo)
}

export function leerExcelArchivo(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result)
        const workbook = XLSX.read(data, { type: 'array' })
        const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
        const json = XLSX.utils.sheet_to_json(firstSheet)
        resolve(json)
      } catch (err) {
        reject(err)
      }
    }
    reader.onerror = reject
    reader.readAsArrayBuffer(file)
  })
}
