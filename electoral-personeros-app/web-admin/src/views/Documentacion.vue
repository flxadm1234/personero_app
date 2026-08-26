<template>
  <div class="doc-container">
    <el-anchor class="doc-anchor" :scroll-offset="80">
      <el-anchor-link href="#inicio" title="ðŸ  Inicio" />
      <el-anchor-link href="#roles" title="ðŸ‘¥ Roles de Usuarios" />
      <el-anchor-link href="#web-admin" title="ðŸŒ Sistema Web" />
      <el-anchor-link href="#app-android" title="ðŸ“± App Android" />
      <el-anchor-link href="#ejecutar" title="ðŸš€ CÃ³mo Ejecutar" />
      <el-anchor-link href="#glosario" title="ðŸ“‹ Glosario BD" />
    </el-anchor>

    <div id="inicio" class="doc-section">
      <div class="hero-section">
        <el-icon :size="64" color="#5b3c8f" class="hero-icon"><Reading /></el-icon>
        <h1 class="hero-title">ðŸ“š Manual del Sistema Electoral de Personeros - Loreto</h1>
        <p class="hero-subtitle">GuÃ­a completa y detallada para el uso del sistema de gestiÃ³n electoral de personeros. Encuentra todo lo que necesitas saber en un solo lugar.</p>

        <div class="hero-cards">
          <el-card shadow="hover" class="hero-card purple-card">
            <div class="card-icon-wrap">
              <el-icon :size="36" color="#fff"><Promotion /></el-icon>
            </div>
            <h3 class="card-title">ðŸŽ¯ Objetivo</h3>
            <p class="card-text">Digitalizar y automatizar el registro, asignaciÃ³n y control de personeros electorales en la regiÃ³n de Loreto. Garantizar transparencia, seguridad y acceso por roles jurisdiccionales.</p>
          </el-card>

          <el-card shadow="hover" class="hero-card blue-card">
            <div class="card-icon-wrap blue">
              <el-icon :size="36" color="#fff"><Cpu /></el-icon>
            </div>
            <h3 class="card-title">ðŸ”§ Stack TecnolÃ³gico</h3>
            <ul class="card-list">
              <li>âš¡ <strong>Backend:</strong> FastAPI + PostgreSQL</li>
              <li>ðŸ¤– <strong>MÃ³vil:</strong> Android Kotlin</li>
              <li>ðŸ–¥ï¸ <strong>Web Admin:</strong> Vue 3 + Element Plus</li>
            </ul>
          </el-card>

          <el-card shadow="hover" class="hero-card green-card">
            <div class="card-icon-wrap green">
              <el-icon :size="36" color="#fff"><Service /></el-icon>
            </div>
            <h3 class="card-title">ðŸ¤ Soporte</h3>
            <p class="card-text">Si encuentras errores, dudas o necesitas capacitaciÃ³n, contacta al equipo de soporte tÃ©cnico. Revisa primero la secciÃ³n de errores comunes al final de este manual.</p>
          </el-card>
        </div>
      </div>
    </div>

    <div id="roles" class="doc-section">
      <div class="section-header">
        <el-icon :size="28" color="#5b3c8f"><UserFilled /></el-icon>
        <h2 class="section-title">ðŸ‘¥ ROLES DE USUARIOS</h2>
      </div>
      <p class="section-desc">El sistema cuenta con 4 niveles de acceso, cada uno con permisos y alcance jurisdiccional definido. Selecciona un rol para ver sus detalles:</p>

      <el-tabs v-model="tabRolActivo" class="roles-tabs">
        <el-tab-pane name="superadmin">
          <template #label>
            <span class="tab-label superadmin">
              <el-icon><UserFilled /></el-icon> SUPER_ADMIN
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="info-card superadmin-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#5b3c8f"><MagicStick /></el-icon>
                      <span>Â¿QuÃ© puede hacer?</span>
                    </div>
                  </template>
                  <ul class="info-list">
                    <li>âœ… Acceso TOTAL al sistema completo</li>
                    <li>âœ… Crear SUPER_ADMIN y ADMINISTRADORES</li>
                    <li>âœ… Vaciar tablas del sistema (funciÃ³n crÃ­tica)</li>
                    <li>âœ… Ver todas las provincias, distritos y centros</li>
                    <li>âœ… Gestionar cualquier usuario del sistema</li>
                    <li>âœ… Descargar reportes globales</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="info-card superadmin-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#5b3c8f"><Menu /></el-icon>
                      <span>MenÃº disponible</span>
                    </div>
                  </template>
                  <ul class="info-list">
                    <li>ðŸ“Š Dashboard completo</li>
                    <li>ðŸ‘¥ Personas (CRUD + Vaciar)</li>
                    <li>ðŸ‘¤ Usuarios (CRUD)</li>
                    <li>ðŸ“ Distritos Asignados</li>
                    <li>ðŸ¢ Centros de VotaciÃ³n</li>
                    <li>ðŸ—³ï¸ Mesas</li>
                    <li>ðŸ‘¥ Asignaciones</li>
                    <li>ðŸ“¥ Carga Masiva</li>
                    <li>ðŸ“¤ Descargar Reporte</li>
                    <li>ðŸ§¹ Vaciar Tablas</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="info-card superadmin-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#5b3c8f"><LocationFilled /></el-icon>
                      <span>Alcance Jurisdiccional</span>
                    </div>
                  </template>
                  <div class="scope-box purple-scope">
                    <el-icon :size="48" color="#5b3c8f"><LocationFilled /></el-icon>
                    <h4>TODA LA REGIÃ“N</h4>
                    <p>Todas las provincias, distritos, centros y mesas de Loreto. Sin restricciones de visualizaciÃ³n ni ediciÃ³n.</p>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <h4 class="perm-title">ðŸ“‹ Tabla de Permisos - SUPER_ADMIN</h4>
            <el-table :data="permisosSuperAdmin" border stripe class="perm-table">
              <el-table-column prop="modulo" label="MÃ³dulo / Tabla" width="220" />
              <el-table-column prop="listar" label="Listar" align="center" width="90">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
              <el-table-column prop="crear" label="Crear" align="center" width="90">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
              <el-table-column prop="editar" label="Editar" align="center" width="90">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
              <el-table-column prop="eliminar" label="Eliminar" align="center" width="90">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
              <el-table-column prop="importar" label="Importar Excel" align="center" width="120">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
              <el-table-column prop="vaciar" label="Vaciar Tabla" align="center" width="110">
                <template #default="{ row }">
                  <el-tag v-if="row.vaciar === 'SI'" type="danger" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reporte" label="Descargar Reporte" align="center" width="140">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
              <el-table-column prop="asignar" label="Asignar Mesas" align="center" width="120">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane name="admin">
          <template #label>
            <span class="tab-label admin">
              <el-icon><User /></el-icon> ADMINISTRADOR
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="info-card admin-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#3b82f6"><MagicStick /></el-icon>
                      <span>Â¿QuÃ© puede hacer?</span>
                    </div>
                  </template>
                  <ul class="info-list">
                    <li>âœ… Gestionar usuarios dentro de sus distritos</li>
                    <li>âœ… Crear Personeros de Centro y Mesa</li>
                    <li>âœ… Carga masiva Excel en su jurisdicciÃ³n</li>
                    <li>âœ… Asignar mesas a personeros</li>
                    <li>âœ… NO puede vaciar ninguna tabla</li>
                    <li>âœ… NO puede crear SUPER_ADMIN</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="info-card admin-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#3b82f6"><Menu /></el-icon>
                      <span>MenÃº disponible</span>
                    </div>
                  </template>
                  <ul class="info-list">
                    <li>ðŸ“Š Dashboard (solo sus distritos)</li>
                    <li>ðŸ‘¥ Personas (CRUD sin vaciar)</li>
                    <li>ðŸ‘¤ Usuarios (solo jurisdicciÃ³n)</li>
                    <li>ðŸ“ Distritos Asignados (solo ver)</li>
                    <li>ðŸ¢ Centros de VotaciÃ³n</li>
                    <li>ðŸ—³ï¸ Mesas</li>
                    <li>ðŸ‘¥ Asignaciones</li>
                    <li>ðŸ“¥ Carga Masiva</li>
                    <li>ðŸ“¤ Descargar Reporte</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="info-card admin-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#3b82f6"><LocationFilled /></el-icon>
                      <span>Alcance Jurisdiccional</span>
                    </div>
                  </template>
                  <div class="scope-box blue-scope">
                    <el-icon :size="48" color="#3b82f6"><MapLocation /></el-icon>
                    <h4>DISTRITOS ASIGNADOS</h4>
                    <p>Solo los distritos que el SUPER_ADMIN le haya asignado explÃ­citamente en la tabla usuarios_distritos. No ve informaciÃ³n de otras jurisdicciones.</p>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <h4 class="perm-title">ðŸ“‹ Tabla de Permisos - ADMINISTRADOR</h4>
            <el-table :data="permisosAdmin" border stripe class="perm-table">
              <el-table-column prop="modulo" label="MÃ³dulo / Tabla" width="220" />
              <el-table-column prop="listar" label="Listar" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.listar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="crear" label="Crear" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.crear === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="editar" label="Editar" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.editar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="eliminar" label="Eliminar" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.eliminar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="importar" label="Importar Excel" align="center" width="120">
                <template #default="{ row }">
                  <el-tag v-if="row.importar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="vaciar" label="Vaciar Tabla" align="center" width="110">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
              <el-table-column prop="reporte" label="Descargar Reporte" align="center" width="140">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
              <el-table-column prop="asignar" label="Asignar Mesas" align="center" width="120">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane name="centro">
          <template #label>
            <span class="tab-label centro">
              <el-icon><OfficeBuilding /></el-icon> PERSONERO_CENTRO
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="info-card centro-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#10b981"><MagicStick /></el-icon>
                      <span>Â¿QuÃ© puede hacer?</span>
                    </div>
                  </template>
                  <ul class="info-list">
                    <li>âœ… Ver solo su centro de votaciÃ³n asignado</li>
                    <li>âœ… Ver las mesas de su centro</li>
                    <li>âœ… Registrar personeros en su centro</li>
                    <li>âœ… Asignar mesas disponibles de su centro</li>
                    <li>âœ… NO puede editar datos maestros</li>
                    <li>âœ… NO tiene acceso a Web Admin</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="info-card centro-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#10b981"><Menu /></el-icon>
                      <span>MenÃº disponible (APP MÃ“VIL)</span>
                    </div>
                  </template>
                  <ul class="info-list">
                    <li>ðŸ“Š Dashboard (su centro)</li>
                    <li>ðŸ¢ Centros (solo el suyo)</li>
                    <li>ðŸ—³ï¸ Mesas (solo su centro)</li>
                    <li>ðŸ‘¤ Registro Personeros</li>
                    <li>ðŸ‘¥ Usuarios (solo su equipo)</li>
                    <li>ðŸ‘¤ Perfil</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="info-card centro-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#10b981"><LocationFilled /></el-icon>
                      <span>Alcance Jurisdiccional</span>
                    </div>
                  </template>
                  <div class="scope-box green-scope">
                    <el-icon :size="48" color="#10b981"><OfficeBuilding /></el-icon>
                    <h4>1 CENTRO DE VOTACIÃ“N</h4>
                    <p>Ãšnicamente el centro de votaciÃ³n al que fue asignado. Ve todas las mesas de ese local, pero no informaciÃ³n de otros centros ni distritos.</p>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <h4 class="perm-title">ðŸ“‹ Tabla de Permisos - PERSONERO_CENTRO</h4>
            <el-table :data="permisosCentro" border stripe class="perm-table">
              <el-table-column prop="modulo" label="MÃ³dulo / Tabla" width="220" />
              <el-table-column prop="listar" label="Listar" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.listar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="crear" label="Crear" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.crear === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="editar" label="Editar" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.editar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="eliminar" label="Eliminar" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.eliminar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="importar" label="Importar Excel" align="center" width="120">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
              <el-table-column prop="vaciar" label="Vaciar Tabla" align="center" width="110">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
              <el-table-column prop="reporte" label="Descargar Reporte" align="center" width="140">
                <template #default="{ row }">
                  <el-tag v-if="row.reporte === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="asignar" label="Asignar Mesas" align="center" width="120">
                <template #default><el-tag type="success" size="small">âœ“ SI</el-tag></template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane name="mesa">
          <template #label>
            <span class="tab-label mesa">
              <el-icon><Grid /></el-icon> PERSONERO_MESA
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="info-card mesa-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#f59e0b"><MagicStick /></el-icon>
                      <span>Â¿QuÃ© puede hacer?</span>
                    </div>
                  </template>
                  <ul class="info-list">
                    <li>âœ… Ver solo su MESA de votaciÃ³n asignada</li>
                    <li>âœ… Ver datos de su mesa y centro</li>
                    <li>âœ… Registrar asistencia en su mesa</li>
                    <li>âœ… Consultar su perfil</li>
                    <li>âœ… NO puede editar registros</li>
                    <li>âœ… NO tiene acceso a Web Admin</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="info-card mesa-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#f59e0b"><Menu /></el-icon>
                      <span>MenÃº disponible (APP MÃ“VIL)</span>
                    </div>
                  </template>
                  <ul class="info-list">
                    <li>ðŸ“Š Dashboard (solo su mesa)</li>
                    <li>ðŸ—³ï¸ Mi Mesa (detalles)</li>
                    <li>ðŸ‘¤ Mi Perfil</li>
                    <li>ðŸ“ Registro de Asistencia</li>
                  </ul>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="info-card mesa-border">
                  <template #header>
                    <div class="card-header">
                      <el-icon color="#f59e0b"><LocationFilled /></el-icon>
                      <span>Alcance Jurisdiccional</span>
                    </div>
                  </template>
                  <div class="scope-box orange-scope">
                    <el-icon :size="48" color="#f59e0b"><Grid /></el-icon>
                    <h4>1 MESA DE VOTACIÃ“N</h4>
                    <p>Exclusivamente la mesa electoral individual asignada. Solo visualizaciÃ³n de su propia informaciÃ³n y registro bÃ¡sico de asistencia.</p>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <h4 class="perm-title">ðŸ“‹ Tabla de Permisos - PERSONERO_MESA</h4>
            <el-table :data="permisosMesa" border stripe class="perm-table">
              <el-table-column prop="modulo" label="MÃ³dulo / Tabla" width="220" />
              <el-table-column prop="listar" label="Listar" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.listar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="crear" label="Crear" align="center" width="90">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
              <el-table-column prop="editar" label="Editar" align="center" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.editar === 'SI'" type="success" size="small">âœ“ SI</el-tag>
                  <el-tag v-else type="info" size="small">âœ— NO</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="eliminar" label="Eliminar" align="center" width="90">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
              <el-table-column prop="importar" label="Importar Excel" align="center" width="120">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
              <el-table-column prop="vaciar" label="Vaciar Tabla" align="center" width="110">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
              <el-table-column prop="reporte" label="Descargar Reporte" align="center" width="140">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
              <el-table-column prop="asignar" label="Asignar Mesas" align="center" width="120">
                <template #default><el-tag type="info" size="small">âœ— NO</el-tag></template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div id="web-admin" class="doc-section">
      <div class="section-header">
        <el-icon :size="28" color="#3b82f6"><Monitor /></el-icon>
        <h2 class="section-title">ðŸŒ SISTEMA WEB ADMINISTRATIVO</h2>
      </div>
      <p class="section-desc">Plataforma de escritorio para la gestiÃ³n completa del sistema. Disponible solo para SUPER_ADMIN y ADMINISTRADOR. Haz clic en cada panel para expandirlo:</p>

      <el-collapse v-model="activePanelsWeb" class="doc-collapse">
        <el-collapse-item name="1">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><Lock /></el-icon>
              <span><strong>Panel 1:</strong> ðŸ” Inicio de SesiÃ³n</span>
            </div>
          </template>
          <div class="collapse-content">
            <el-row :gutter="24">
              <el-col :span="12">
                <div class="step-box">
                  <div class="step-num">1</div>
                  <div class="step-body">
                    <h4>ðŸ“ CÃ³mo ingresar al sistema</h4>
                    <ol>
                      <li>Abre tu navegador (Chrome, Edge o Firefox recomendado)</li>
                      <li>Ingresa la URL: <code>http://localhost:5173</code> (desarrollo) o la URL de producciÃ³n</li>
                      <li>VerÃ¡s la pantalla de Login con el logo del sistema</li>
                      <li>Ingresa tu <strong>Usuario</strong> y <strong>ContraseÃ±a</strong></li>
                      <li>Haz clic en el botÃ³n <el-tag type="primary" size="small">INGRESAR</el-tag></li>
                      <li>Si los datos son correctos, serÃ¡s redirigido al Dashboard</li>
                    </ol>
                  </div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="step-box warning">
                  <div class="step-num">!</div>
                  <div class="step-body">
                    <h4>ðŸ”‘ Â¿Olvidaste tu contraseÃ±a? (Primer Super Admin)</h4>
                    <p>Para crear el primer usuario SUPER_ADMIN del sistema, ejecuta el script de inicializaciÃ³n:</p>
                    <el-alert type="info" :closable="false" class="code-alert">
                      <p><strong>UbicaciÃ³n:</strong> backend/scripts/create_superadmin.py</p>
                      <p><strong>Comando:</strong> <code>python backend/scripts/create_superadmin.py</code></p>
                      <p><strong>Datos por defecto:</strong></p>
                      <ul>
                        <li>ðŸ‘¤ Usuario: <code>superadmin</code></li>
                        <li>ðŸ”’ ContraseÃ±a: <code>admin123</code></li>
                        <li>ðŸŽ–ï¸ Rol: <code>SUPER_ADMIN</code></li>
                      </ul>
                    </el-alert>
                    <p class="tip-text">ðŸ’¡ <strong>Consejo:</strong> Cambia la contraseÃ±a por defecto inmediatamente despuÃ©s del primer inicio de sesiÃ³n.</p>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <el-collapse-item name="2">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><DataAnalysis /></el-icon>
              <span><strong>Panel 2:</strong> ðŸ“Š Dashboard - Panel de Control</span>
            </div>
          </template>
          <div class="collapse-content">
            <p>El Dashboard es la pantalla principal que muestra un resumen visual de todo el sistema. Cuenta con:</p>

            <h4>ðŸ“¦ Tarjetas de Totales (6 indicadores)</h4>
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="mini-stat purple-bg">
                  <div class="mini-icon"><el-icon><User /></el-icon></div>
                  <div>
                    <div class="mini-num">1,250</div>
                    <div class="mini-label">Personas Registradas</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="mini-stat blue-bg">
                  <div class="mini-icon"><el-icon><Avatar /></el-icon></div>
                  <div>
                    <div class="mini-num">125</div>
                    <div class="mini-label">Usuarios del Sistema</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="mini-stat green-bg">
                  <div class="mini-icon"><el-icon><OfficeBuilding /></el-icon></div>
                  <div>
                    <div class="mini-num">340</div>
                    <div class="mini-label">Centros de VotaciÃ³n</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="mini-stat orange-bg">
                  <div class="mini-icon"><el-icon><Grid /></el-icon></div>
                  <div>
                    <div class="mini-num">1,580</div>
                    <div class="mini-label">Mesas Registradas</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="mini-stat teal-bg">
                  <div class="mini-icon"><el-icon><UserFilled /></el-icon></div>
                  <div>
                    <div class="mini-num">420</div>
                    <div class="mini-label">Personeros Asignados</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="mini-stat red-bg">
                  <div class="mini-icon"><el-icon><Clock /></el-icon></div>
                  <div>
                    <div class="mini-num">85</div>
                    <div class="mini-label">Solicitudes Pendientes</div>
                  </div>
                </div>
              </el-col>
            </el-row>

            <h4 style="margin-top: 24px;">ðŸ“ˆ 5 GrÃ¡ficos ECharts Interactivos</h4>
            <el-table :data="graficosDashboard" border stripe>
              <el-table-column prop="nro" label="#" width="60" align="center" />
              <el-table-column prop="nombre" label="Nombre del GrÃ¡fico" width="260" />
              <el-table-column prop="tipo" label="Tipo" width="120" align="center">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.tipoTag">{{ row.tipo }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="descripcion" label="Â¿QuÃ© muestra?" />
            </el-table>
          </div>
        </el-collapse-item>

        <el-collapse-item name="3">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><User /></el-icon>
              <span><strong>Panel 3:</strong> ðŸ‘¥ Personas - CRUD Completo</span>
            </div>
          </template>
          <div class="collapse-content">
            <p>MÃ³dulo para gestionar el padrÃ³n de personas naturales que pueden ser registradas como partÃ­cipes del proceso electoral.</p>

            <h4>ðŸ“‹ Campos del Formulario</h4>
            <el-table :data="camposPersona" border stripe>
              <el-table-column prop="campo" label="Campo" width="200" />
              <el-table-column prop="tipo" label="Tipo de Dato" width="150" align="center" />
              <el-table-column prop="requerido" label="Requerido" width="120" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.requerido" type="danger" size="small">OBLIGATORIO</el-tag>
                  <el-tag v-else type="info" size="small">Opcional</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="descripcion" label="DescripciÃ³n y Ejemplo" />
            </el-table>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <el-alert type="success" :closable="false" class="feature-alert">
                  <template #title>ðŸ” Buscador Inteligente</template>
                  Filtra en tiempo real por DNI, nombres, apellidos, telÃ©fono, distrito, provincia o direcciÃ³n. Simplemente escribe en la barra de bÃºsqueda.
                </el-alert>
              </el-col>
              <el-col :span="12">
                <el-alert type="warning" :closable="false" class="feature-alert">
                  <template #title>ðŸ§¹ Vaciar Tabla (Solo SUPER_ADMIN)</template>
                  Elimina TODOS los registros de personas. Â¡FunciÃ³n irreversible! Aparece solo para SUPER_ADMIN y requiere doble confirmaciÃ³n.
                </el-alert>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <el-collapse-item name="4">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><Avatar /></el-icon>
              <span><strong>Panel 4:</strong> ðŸ‘¤ Usuarios - CRUD del Sistema</span>
            </div>
          </template>
          <div class="collapse-content">
            <p>GestiÃ³n de las cuentas de usuario que pueden acceder al sistema (tanto Web como App MÃ³vil).</p>

            <el-row :gutter="20">
              <el-col :span="14">
                <h4>ðŸ”— RelaciÃ³n con Persona (JOIN)</h4>
                <div class="join-box">
                  <el-icon :size="40" color="#5b3c8f"><Link /></el-icon>
                  <div>
                    <p>Cada usuario estÃ¡ vinculado a un registro de <strong>Persona</strong>. Al crear un usuario:</p>
                    <ol>
                      <li>Seleccionas una persona existente del padrÃ³n</li>
                      <li>Los datos (nombres, apellidos, DNI) se completan AUTOMÃTICAMENTE</li>
                      <li>Solo debes asignar: nombre de usuario, contraseÃ±a y rol</li>
                    </ol>
                    <p class="benefit-text">âœ… Beneficio: Evita duplicados y mantiene integridad de datos</p>
                  </div>
                </div>
              </el-col>
              <el-col :span="10">
                <h4>ðŸ” Seguridad de ContraseÃ±as</h4>
                <el-alert type="error" :closable="false">
                  <template #title>ðŸ›¡ï¸ Hashing AutomÃ¡tico</template>
                  Las contraseÃ±as NUNCA se guardan en texto plano. El sistema usa <strong>bcrypt</strong> con salt automÃ¡tico para encriptarlas antes de guardar en PostgreSQL.
                </el-alert>
                <div style="margin-top: 16px;">
                  <h4>ðŸŽ­ Selector de Rol</h4>
                  <el-select v-model="demoRol" placeholder="Selecciona un rol" style="width: 100%;">
                    <el-option label="SUPER_ADMIN" value="SUPER_ADMIN" />
                    <el-option label="ADMINISTRADOR" value="ADMINISTRADOR" />
                    <el-option label="PERSONERO_CENTRO" value="PERSONERO_CENTRO" />
                    <el-option label="PERSONERO_MESA" value="PERSONERO_MESA" />
                  </el-select>
                </div>
              </el-col>
            </el-row>

            <el-alert type="warning" :closable="false" style="margin-top: 20px;">
              <template #title>â›” RestricciÃ³n Importante</template>
              La tabla de Usuarios <strong>NO SE PUEDE VACIAR</strong> bajo ninguna circunstancia, ni siquiera por SUPER_ADMIN. Esto es una medida de seguridad para proteger las cuentas del sistema.
            </el-alert>
          </div>
        </el-collapse-item>

        <el-collapse-item name="5">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><Location /></el-icon>
              <span><strong>Panel 5:</strong> ðŸ“ Distritos Asignados (JurisdicciÃ³n)</span>
            </div>
          </template>
          <div class="collapse-content">
            <p>Tabla puente que define <strong>quÃ© distritos puede ver/gestionar cada ADMINISTRADOR</strong>. Es la base del control jurisdiccional del sistema.</p>

            <el-row :gutter="20">
              <el-col :span="8">
                <div class="jurisdiccion-demo purple">
                  <h4>ðŸ‘‘ SUPER_ADMIN</h4>
                  <p>No necesita asignaciones. Ve TODOS los distritos automÃ¡ticamente.</p>
                  <div class="all-badge">ðŸŒ TODOS LOS DISTRITOS</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="jurisdiccion-demo blue">
                  <h4>ðŸ§‘â€ðŸ’¼ ADMINISTRADOR</h4>
                  <p>Solo accede a los distritos que estÃ¡n en esta tabla con su ID de usuario.</p>
                  <div class="assign-badge">ðŸ“ Distrito A + Distrito B + ...</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="jurisdiccion-demo green">
                  <h4>ðŸ¢ PERSONERO_CENTRO</h4>
                  <p>No usa esta tabla. Su jurisdicciÃ³n es el centro_votacion_id vinculado directamente a su usuario.</p>
                  <div class="single-badge">ðŸ¢ 1 Centro de VotaciÃ³n</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <el-collapse-item name="6">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><OfficeBuilding /></el-icon>
              <span><strong>Panel 6:</strong> ðŸ¢ Centros de VotaciÃ³n CRUD + ðŸ“¥ Carga Masiva</span>
            </div>
          </template>
          <div class="collapse-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <h4>âœï¸ CRUD Individual</h4>
                <el-table :data="camposCentro" border stripe size="small">
                  <el-table-column prop="campo" label="Campo" width="160" />
                  <el-table-column prop="tipo" label="Tipo" width="100" align="center" />
                  <el-table-column prop="ejemplo" label="Ejemplo" />
                </el-table>
              </el-col>
              <el-col :span="12">
                <h4>ðŸ“¥ Carga Masiva Excel (Recomendado)</h4>
                <el-alert type="success" :closable="false">
                  <template #title>ðŸ“„ Plantilla Descargable</template>
                  Antes de importar, descarga la plantilla Excel oficial con los encabezados correctos. No cambies el orden de las columnas.
                </el-alert>
                <div style="margin-top: 16px;">
                  <h5>Pasos para la carga masiva:</h5>
                  <ol>
                    <li>1ï¸âƒ£ Haz clic en <el-tag type="primary" size="small">ðŸ“¥ Carga Masiva Excel</el-tag></li>
                    <li>2ï¸âƒ£ Selecciona la tabla: <strong>centro_votacion</strong></li>
                    <li>3ï¸âƒ£ Descarga la plantilla (botÃ³n azul)</li>
                    <li>4ï¸âƒ£ Llena los datos en Excel (una fila por centro)</li>
                    <li>5ï¸âƒ£ Sube el archivo .xlsx y espera la confirmaciÃ³n</li>
                    <li>6ï¸âƒ£ Revisa el resumen: filas exitosas vs filas con error</li>
                  </ol>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <el-collapse-item name="7">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><Grid /></el-icon>
              <span><strong>Panel 7:</strong> ðŸ—³ï¸ Mesas CRUD + ðŸ“¥ Carga Masiva Excel</span>
            </div>
          </template>
          <div class="collapse-content">
            <p>Cada mesa electoral pertenece a un Centro de VotaciÃ³n. La relaciÃ³n es de 1 a Muchos (1 centro â†’ N mesas).</p>

            <h4>ðŸ“ Campos de Mesa</h4>
            <el-table :data="camposMesa" border stripe>
              <el-table-column prop="campo" label="Campo" width="200" />
              <el-table-column prop="tipo" label="Tipo" width="130" align="center" />
              <el-table-column prop="requerido" label="Obligatorio" width="120" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.requerido" type="danger" size="small">SI</el-tag>
                  <el-tag v-else type="info" size="small">No</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="descripcion" label="DescripciÃ³n" />
            </el-table>

            <div class="import-flow" style="margin-top: 20px;">
              <h4>ðŸ”„ Flujo de Carga Masiva de Mesas</h4>
              <el-steps :active="4" finish-status="success" align-center>
                <el-step title="Descargar Plantilla" description="BotÃ³n azul descarga plantilla.xlsx" />
                <el-step title="Llenar Datos" description="Incluir centro_votacion_id vÃ¡lido" />
                <el-step title="Subir Archivo" description="Arrastrar o seleccionar .xlsx" />
                <el-step title="ValidaciÃ³n y Carga" description="âœ“ Filas OK / âœ— Filas con error" />
              </el-steps>
            </div>
          </div>
        </el-collapse-item>

        <el-collapse-item name="8">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><Upload /></el-icon>
              <span><strong>Panel 8:</strong> ðŸ‘¥ Personas - Carga Masiva Excel</span>
            </div>
          </template>
          <div class="collapse-content">
            <p>Ideal para importar el padrÃ³n electoral completo en lugar de registrar persona por persona.</p>

            <el-row :gutter="20">
              <el-col :span="14">
                <div class="excel-mockup">
                  <div class="excel-toolbar">ðŸ“Š Excel - padron_personas.xlsx</div>
                  <table class="excel-table">
                    <thead>
                      <tr>
                        <th>A</th><th>B</th><th>C</th><th>D</th><th>E</th><th>F</th><th>G</th><th>H</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td class="cell-header">dni</td>
                        <td class="cell-header">nombres</td>
                        <td class="cell-header">apellidos</td>
                        <td class="cell-header">telefono</td>
                        <td class="cell-header">provincia</td>
                        <td class="cell-header">distrito</td>
                        <td class="cell-header">direccion</td>
                        <td class="cell-header">correo</td>
                      </tr>
                      <tr><td>70012345</td><td>JUAN</td><td>PEREZ GOMEZ</td><td>987654321</td><td>MAYNAS</td><td>IQUITOS</td><td>AV. ABELLO 123</td><td>juan@correo.com</td></tr>
                      <tr><td>70012346</td><td>MARIA</td><td>LOPEZ DIAZ</td><td>987654322</td><td>MAYNAS</td><td>BELEN</td><td>JR. TUMBES 456</td><td>maria@correo.com</td></tr>
                      <tr><td>70012347</td><td>CARLOS</td><td>RAMOS RUIZ</td><td>987654323</td><td>REQUENA</td><td>REQUENA</td><td>PSJE. LIMA 789</td><td>carlos@correo.com</td></tr>
                    </tbody>
                  </table>
                </div>
              </el-col>
              <el-col :span="10">
                <h4>ðŸ’¡ Consejos de Excel</h4>
                <el-alert type="info" :closable="false" style="margin-bottom: 12px;">
                  <template #title>âœ… Columna DNI</template>
                  Formato TEXTO (no nÃºmero) para evitar perder ceros iniciales.
                </el-alert>
                <el-alert type="info" :closable="false" style="margin-bottom: 12px;">
                  <template #title>âœ… Provincias/Distritos</template>
                  Usa los nombres oficiales del INEI tal cual estÃ¡n registrados en el sistema.
                </el-alert>
                <el-alert type="info" :closable="false" style="margin-bottom: 12px;">
                  <template #title>âœ… ValidaciÃ³n Previa</template>
                  El sistema valida DNI duplicados y muestra un reporte con los errores fila por fila.
                </el-alert>
                <el-alert type="success" :closable="false">
                  <template #title>âœ… TamaÃ±o de Lote</template>
                  Se recomiendan archivos de hasta 5,000 filas por carga para mayor velocidad.
                </el-alert>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <el-collapse-item name="9">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#5b3c8f"><Download /></el-icon>
              <span><strong>Panel 9:</strong> ðŸ“¤ Descargar Reporte Excel (6 Hojas)</span>
            </div>
          </template>
          <div class="collapse-content">
            <p>Genera un archivo Excel completo con TODOS los datos del sistema en mÃºltiples hojas (pestaÃ±as). Se accede desde el botÃ³n del menÃº lateral o el botÃ³n superior derecho del Dashboard.</p>

            <h4>ðŸ“‘ Estructura del Archivo Excel Generado</h4>
            <el-table :data="hojasReporte" border stripe>
              <el-table-column prop="hoja" label="Hoja #" width="80" align="center" />
              <el-table-column prop="nombre" label="Nombre de la PestaÃ±a" width="220" />
              <el-table-column prop="icono" label="Icono" width="80" align="center" />
              <el-table-column prop="contenido" label="Contenido y Columnas Incluidas" />
            </el-table>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <el-alert type="success" :closable="false">
                  <template #title>ðŸŽ¯ Filtros AutomÃ¡ticos</template>
                  El reporte respeta la jurisdicciÃ³n:
                  <ul>
                    <li>ðŸ‘‘ SUPER_ADMIN â†’ Descarga TODO el sistema</li>
                    <li>ðŸ§‘â€ðŸ’¼ ADMINISTRADOR â†’ Solo sus distritos asignados</li>
                  </ul>
                </el-alert>
              </el-col>
              <el-col :span="12">
                <el-alert type="primary" :closable="false">
                  <template #title>ðŸ“ Nomenclatura de Archivo</template>
                  Nombre automÃ¡tico: <code>reporte_personeros_YYYYMMDD_HHMMSS.xlsx</code>
                  <p>Ejemplo: <em>reporte_personeros_20240815_143025.xlsx</em></p>
                </el-alert>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <el-collapse-item name="10">
          <template #title>
            <div class="collapse-title">
              <el-icon color="#ef4444"><DeleteFilled /></el-icon>
              <span><strong>Panel 10:</strong> ðŸ§¹ FunciÃ³n "Vaciar Tabla" (Solo SUPER_ADMIN)</span>
            </div>
          </template>
          <div class="collapse-content">
            <el-alert type="error" :closable="false" class="danger-alert">
              <template #title>âš ï¸ FUNCIÃ“N CRÃTICA - LEER ANTES DE USAR</template>
              <p>Esta funciÃ³n ELIMINA PERMANENTEMENTE todos los registros de una tabla. No se puede deshacer. Ãšsala solo si estÃ¡s 100% seguro.</p>
            </el-alert>

            <h4 style="margin-top: 24px;">ðŸ›¡ï¸ Protecciones de Seguridad Implementadas</h4>
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="protection-box">
                  <el-icon :size="36" color="#5b3c8f"><Lock /></el-icon>
                  <h5>Solo SUPER_ADMIN</h5>
                  <p>El botÃ³n ni siquiera aparece en la interfaz para otros roles.</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="protection-box">
                  <el-icon :size="36" color="#5b3c8f"><Warning /></el-icon>
                  <h5>Doble ConfirmaciÃ³n</h5>
                  <p>Modal 1: "Â¿EstÃ¡ seguro?" + Modal 2: "Escriba CONFIRMAR para proceder".</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="protection-box">
                  <el-icon :size="36" color="#5b3c8f"><CircleCheck /></el-icon>
                  <h5>Integridad FK</h5>
                  <p>Elimina en cascada respetando claves forÃ¡neas o advierte de dependencias.</p>
                </div>
              </el-col>
            </el-row>

            <h4 style="margin-top: 24px;">ðŸ“‹ Â¿QuÃ© tablas se PUEDEN y NO se pueden vaciar?</h4>
            <el-table :data="vaciarTablas" border stripe>
              <el-table-column prop="tabla" label="Nombre de Tabla" width="240" />
              <el-table-column prop="sePuede" label="Â¿Se puede vaciar?" width="160" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.sePuede" type="danger" size="small">ðŸ—‘ï¸ SI (Solo SuperAdmin)</el-tag>
                  <el-tag v-else type="info" size="small">â›” NO (Protegida)</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="motivo" label="ExplicaciÃ³n / Motivo" />
            </el-table>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <div id="app-android" class="doc-section">
      <div class="section-header">
        <el-icon :size="28" color="#10b981"><Iphone /></el-icon>
        <h2 class="section-title">ðŸ“± APLICACIÃ“N MÃ“VIL ANDROID</h2>
      </div>
      <p class="section-desc">AplicaciÃ³n mÃ³vil nativa en Android Kotlin para el trabajo de campo de los personeros y coordinadores. Disponible para los 4 roles, con alcance filtrado segÃºn el perfil del usuario.</p>

      <el-row :gutter="20" style="margin-bottom: 24px;">
        <el-col :span="6">
          <div class="android-card blue">
            <el-icon :size="40" color="#fff"><Lock /></el-icon>
            <h4>SecciÃ³n 1</h4>
            <p>ðŸ” Inicio de SesiÃ³n y BASE_URL</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="android-card purple">
            <el-icon :size="40" color="#fff"><Compass /></el-icon>
            <h4>SecciÃ³n 2</h4>
            <p>ðŸ§­ NavegaciÃ³n Inferior + FAB Central</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="android-card green">
            <el-icon :size="40" color="#fff"><DataAnalysis /></el-icon>
            <h4>SecciÃ³n 3</h4>
            <p>ðŸ“± Pantalla Dashboard</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="android-card orange">
            <el-icon :size="40" color="#fff"><Grid /></el-icon>
            <h4>SecciÃ³n 4</h4>
            <p>ðŸ—³ï¸ AsignaciÃ³n de Mesas</p>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-bottom: 24px;">
        <el-col :span="8">
          <div class="android-card teal">
            <el-icon :size="40" color="#fff"><EditPen /></el-icon>
            <h4>SecciÃ³n 5</h4>
            <p>ðŸ‘¤ Registro de Personero</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="android-card indigo">
            <el-icon :size="40" color="#fff"><Key /></el-icon>
            <h4>SecciÃ³n 6</h4>
            <p>ðŸ”’ Alcance Jurisdiccional por Rol</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="android-card red">
            <el-icon :size="40" color="#fff"><WarningFilled /></el-icon>
            <h4>SecciÃ³n 7</h4>
            <p>ðŸš€ SoluciÃ³n de Errores Comunes</p>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <div class="mobile-section">
        <h3 class="mobile-section-title">
          <el-icon color="#3b82f6"><Lock /></el-icon> SecciÃ³n 1: ðŸ” Inicio de SesiÃ³n y ConfiguraciÃ³n BASE_URL
        </h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="mobile-mockup">
              <div class="phone-frame">
                <div class="phone-status">ðŸ“¶ 9:41 AM</div>
                <div class="phone-content login-screen">
                  <el-icon :size="50" color="#5b3c8f"><User /></el-icon>
                  <h4>Sistema Personeros</h4>
                  <div class="mock-input">ðŸ‘¤ Usuario</div>
                  <div class="mock-input">ðŸ”’ ContraseÃ±a</div>
                  <div class="mock-btn-purple">INGRESAR</div>
                  <div class="mock-link">âš™ï¸ Configurar Servidor</div>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <h4>ðŸŒ ConfiguraciÃ³n de BASE_URL (Â¡IMPORTANTE!)</h4>
            <p>La app necesita saber la direcciÃ³n IP del servidor backend. Esta configuraciÃ³n es la causa mÃ¡s comÃºn de error "No se puede conectar al servidor".</p>
            <el-table :data="baseUrlConfig" border stripe size="small">
              <el-table-column prop="escenario" label="Escenario" width="180" />
              <el-table-column prop="valor" label="BASE_URL a configurar" />
              <el-table-column prop="ejemplo" label="Ejemplo" />
            </el-table>
            <el-alert type="warning" :closable="false" style="margin-top: 16px;">
              <template #title>ðŸ’¡ Tip para TelÃ©fono FÃ­sico</template>
              AsegÃºrate que el telÃ©fono estÃ© conectado a la MISMA red WiFi que la PC. Busca la IP de tu PC con <code>ipconfig</code> (Windows) en Adaptador WiFi â†’ IPv4.
            </el-alert>
          </el-col>
        </el-row>
      </div>

      <div class="mobile-section">
        <h3 class="mobile-section-title">
          <el-icon color="#5b3c8f"><Compass /></el-icon> SecciÃ³n 2: ðŸ§­ NavegaciÃ³n Inferior (5 items + FAB Central)
        </h3>
        <p>La app usa una barra de navegaciÃ³n inferior con 5 accesos directos mÃ¡s un botÃ³n FAB (Floating Action Button) morado central para la acciÃ³n mÃ¡s importante:</p>

        <div class="navbar-mockup">
          <div class="nav-item active">
            <el-icon><DataAnalysis /></el-icon>
            <span>Dashboard</span>
          </div>
          <div class="nav-item">
            <el-icon><OfficeBuilding /></el-icon>
            <span>Centros</span>
          </div>
          <div class="nav-fab">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="nav-item">
            <el-icon><Grid /></el-icon>
            <span>Mesas</span>
          </div>
          <div class="nav-item">
            <el-icon><UserFilled /></el-icon>
            <span>Usuarios</span>
          </div>
          <div class="nav-item">
            <el-icon><User /></el-icon>
            <span>Perfil</span>
          </div>
        </div>

        <h4 style="margin-top: 24px;">ðŸ“– FunciÃ³n de cada Ã­tem</h4>
        <el-table :data="navItems" border stripe>
          <el-table-column prop="pos" label="Pos." width="70" align="center" />
          <el-table-column prop="icono" label="Ãcono" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="primary" size="small">{{ row.icono }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="nombre" label="Pantalla" width="200" />
          <el-table-column prop="funcion" label="Â¿QuÃ© hace esa pantalla?" />
          <el-table-column prop="fab" label="FAB Central" width="140" align="center">
            <template #default="{ row }">
              <span v-if="row.fab" class="fab-badge">âœ¨ BOTÃ“N MORADO</span>
              <span v-else>â€”</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="mobile-section">
        <h3 class="mobile-section-title">
          <el-icon color="#10b981"><DataAnalysis /></el-icon> SecciÃ³n 3: ðŸ“± Pantalla Dashboard (Resumen MÃ³vil)
        </h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="mobile-stat-card purple-m">
              <div class="ms-icon">ðŸ‘¥</div>
              <div class="ms-num">847</div>
              <div class="ms-label">Personas en JurisdicciÃ³n</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="mobile-stat-card green-m">
              <div class="ms-icon">ðŸ¢</div>
              <div class="ms-num">36</div>
              <div class="ms-label">Centros Asignados</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="mobile-stat-card orange-m">
              <div class="ms-icon">ðŸ—³ï¸</div>
              <div class="ms-num">168</div>
              <div class="ms-label">Mesas por Asignar</div>
            </div>
          </el-col>
        </el-row>
        <p style="margin-top: 16px;">El Dashboard mÃ³vil muestra datos adaptados al rol del usuario: resumen de su jurisdicciÃ³n, grÃ¡ficos simplificados de barras y pastel, acceso rÃ¡pido a las funciones mÃ¡s usadas y notificaciones de pendientes.</p>
      </div>

      <div class="mobile-section">
        <h3 class="mobile-section-title">
          <el-icon color="#f59e0b"><Grid /></el-icon> SecciÃ³n 4: ðŸ—³ï¸ AsignaciÃ³n de Mesas a Personeros
        </h3>
        <el-alert type="success" :closable="false">
          <template #title>âœ… Mejora reciente: Fix de NullPointerException</template>
          Se corrigiÃ³ el error de puntero nulo al momento de asignar. Ahora el sistema valida todos los campos antes de enviar al backend y muestra mensajes de error AMIGABLES.
        </el-alert>

        <h4 style="margin-top: 20px;">ðŸ”„ Flujo Completo de AsignaciÃ³n</h4>
        <el-steps :active="5" finish-status="success" direction="vertical" align-center style="margin-top: 16px;">
          <el-step title="Paso 1: Buscar Persona por DNI">
            <div class="step-details">Escribe el nÃºmero de DNI (8 dÃ­gitos). El sistema busca AUTOMÃTICAMENTE en la base de datos y trae los datos si existe.</div>
          </el-step>
          <el-step title="Paso 2: Â¿No existe? Crear Registro Personero">
            <div class="step-details">Si la persona no estÃ¡ registrada, el botÃ³n <el-tag size="small" type="primary">+ Nuevo Registro Personero</el-tag> permite crearla en el momento, sin salir de la pantalla.</div>
          </el-step>
          <el-step title="Paso 3: Selector Dependiente Provincia â†’ Distrito">
            <div class="step-details">Primero eliges PROVINCIA, luego el sistema filtra y muestra SOLO los DISTRITOS de esa provincia. AsÃ­ no eliges combinaciones invÃ¡lidas.</div>
          </el-step>
          <el-step title="Paso 4: Seleccionar Centro y Mesa Disponible">
            <div class="step-details">El sistema filtra las mesas disponibles (sin asignar) del centro seleccionado. Las mesas ocupadas aparecen en gris y no se pueden seleccionar.</div>
          </el-step>
          <el-step title="Paso 5: Confirmar AsignaciÃ³n">
            <div class="step-details">Resumen final con datos de la persona + mesa. BotÃ³n CONFIRMAR de color verde. Se guarda en <code>personeros_users</code>.</div>
          </el-step>
        </el-steps>
      </div>

      <div class="mobile-section">
        <h3 class="mobile-section-title">
          <el-icon color="#14b8a6"><EditPen /></el-icon> SecciÃ³n 5: ðŸ‘¤ Registro de Personero (Flujo Completo)
        </h3>
        <p class="note-text">ðŸ’¡ Nota: Antes este mÃ³dulo se llamaba "PostulaciÃ³n". Ahora se denomina oficialmente <strong>"Registro Personero"</strong> en toda la app, backend y documentaciÃ³n.</p>

        <el-row :gutter="20" style="margin-top: 16px;">
          <el-col :span="7">
            <div class="flow-step-pill">
              <div class="fsp-num">1</div>
              <div class="fsp-content">
                <h5>Datos Personales</h5>
                <p>DNI, nombres, apellidos, telÃ©fono, correo, provincia, distrito, direcciÃ³n</p>
              </div>
            </div>
          </el-col>
          <el-col :span="7">
            <div class="flow-step-pill success">
              <div class="fsp-num">2</div>
              <div class="fsp-content">
                <h5>Crear Cuenta Usuario</h5>
                <p>Nombre usuario, contraseÃ±a temporal, asignaciÃ³n de rol (PC / PM)</p>
              </div>
            </div>
          </el-col>
          <el-col :span="5">
            <div class="flow-step-pill warning">
              <div class="fsp-num">3</div>
              <div class="fsp-content">
                <h5>Vincular Centro</h5>
                <p>Si es PERSONERO_CENTRO: asignar centro_votacion_id</p>
              </div>
            </div>
          </el-col>
          <el-col :span="5">
            <div class="flow-step-pill info">
              <div class="fsp-num">4</div>
              <div class="fsp-content">
                <h5>Asignar Mesa</h5>
                <p>Si es PERSONERO_MESA: vincular a mesa_id (solo mesas libres)</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="mobile-section">
        <h3 class="mobile-section-title">
          <el-icon color="#5b3c8f"><Key /></el-icon> SecciÃ³n 6: ðŸ”’ Alcance Jurisdiccional por Rol (App Android)
        </h3>
        <el-table :data="alcanceMovil" border stripe>
          <el-table-column prop="rol" label="Rol del Usuario" width="220" />
          <el-table-column prop="ve" label="Â¿QuÃ© ve en la App?" width="350">
            <template #default="{ row }">
              <el-tag v-for="tag in row.veTags" :key="tag" size="small" style="margin: 2px;" :type="row.tagType">{{ tag }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="noVe" label="Â¿QuÃ© NO ve?" />
        </el-table>
      </div>

      <div class="mobile-section">
        <h3 class="mobile-section-title">
          <el-icon color="#ef4444"><WarningFilled /></el-icon> SecciÃ³n 7: ðŸš€ SoluciÃ³n de Errores Comunes (Android)
        </h3>
        <el-table :data="erroresAndroid" border stripe>
          <el-table-column prop="nro" label="#" width="60" align="center" />
          <el-table-column prop="error" label="Mensaje / Error visible" width="260" />
          <el-table-column prop="causa" label="Causa probable" width="220" />
          <el-table-column prop="solucion" label="âœ… SoluciÃ³n Paso a Paso" />
        </el-table>
      </div>
    </div>

    <div id="ejecutar" class="doc-section">
      <div class="section-header">
        <el-icon :size="28" color="#f59e0b"><Promotion /></el-icon>
        <h2 class="section-title">ðŸš€ CÃ“MO EJECUTAR EL SISTEMA - PASO A PASO</h2>
      </div>
      <p class="section-desc">GuÃ­a para levantar todo el sistema desde cero en un entorno local de desarrollo. Sigue estos 5 pasos en orden:</p>

      <el-steps :active="5" finish-status="success" align-center class="big-steps">
        <el-step title="Paso 1" description="Base de Datos PostgreSQL + schema.sql" icon="Collection" />
        <el-step title="Paso 2" description="Backend FastAPI - launch_backend.py" icon="Cpu" />
        <el-step title="Paso 3" description="Celular/Emulador - Instalar APK" icon="Iphone" />
        <el-step title="Paso 4" description="Web Admin - npm run dev" icon="Monitor" />
        <el-step title="Paso 5" description="Â¡Listo! Probar Login" icon="CircleCheck" />
      </el-steps>

      <el-row :gutter="20" style="margin-top: 32px;">
        <el-col :span="12">
          <el-card class="step-card" shadow="hover">
            <template #header>
              <div class="step-card-header">
                <div class="step-badge blue">1</div>
                <div>
                  <h4>Paso 1: PostgreSQL + schema.sql ðŸ˜</h4>
                  <p class="step-sub">Preparar la base de datos</p>
                </div>
              </div>
            </template>
            <ol class="ordered-list">
              <li>Abre <strong>pgAdmin</strong> o <strong>DBeaver</strong> y conÃ©ctate a PostgreSQL</li>
              <li>Crea una base de datos nueva: <code>CREATE DATABASE bd_personeros_loreto;</code></li>
              <li>Selecciona la BD creada y abre el editor SQL</li>
              <li>Abre el archivo: <code>backend/database/schema.sql</code></li>
              <li>Ejecuta TODO el contenido del script (crea tablas, indices y FK)</li>
              <li>âœ… Verifica que aparezcan las 7 tablas listadas en el glosario</li>
            </ol>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="step-card" shadow="hover">
            <template #header>
              <div class="step-card-header">
                <div class="step-badge green">2</div>
                <div>
                  <h4>Paso 2: Backend FastAPI âš¡</h4>
                  <p class="step-sub">Lanzar API en el puerto 8000</p>
                </div>
              </div>
            </template>
            <ol class="ordered-list">
              <li>Abre PowerShell o CMD en la carpeta <code>backend/</code></li>
              <li>Crea entorno virtual (primera vez): <code>python -m venv venv</code></li>
              <li>ActÃ­valo: <code>.\venv\Scripts\activate</code> (Windows)</li>
              <li>Instala dependencias: <code>pip install -r requirements.txt</code></li>
              <li>ðŸš€ Ejecuta: <code>python launch_backend.py</code></li>
              <li>âœ… Abre navegador: <code>http://localhost:8000/docs</code> â†’ Swagger UI</li>
            </ol>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="step-card" shadow="hover">
            <template #header>
              <div class="step-card-header">
                <div class="step-badge orange">3</div>
                <div>
                  <h4>Paso 3: APK Android ðŸ¤–</h4>
                  <p class="step-sub">Emulador o telÃ©fono fÃ­sico</p>
                </div>
              </div>
            </template>
            <ol class="ordered-list">
              <li><strong>En Emulador Android Studio:</strong></li>
              <li>Configura BASE_URL = <code>http://10.0.2.2:8000</code></li>
              <li>Arrastra el APK al emulador o ejecuta desde Android Studio</li>
              <li class="sub-li"><strong>En TelÃ©fono FÃ­sico:</strong></li>
              <li>ConÃ©ctate a la MISMA red WiFi que la PC</li>
              <li>Configura BASE_URL = <code>http://IP_DE_TU_PC:8000</code></li>
              <li>Transfiere APK por USB y Ã¡brelo â†’ Instalar</li>
            </ol>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="step-card" shadow="hover">
            <template #header>
              <div class="step-card-header">
                <div class="step-badge purple">4</div>
                <div>
                  <h4>Paso 4: Web Admin Vue 3 ðŸ–¥ï¸</h4>
                  <p class="step-sub">Vite dev server en puerto 5173</p>
                </div>
              </div>
            </template>
            <ol class="ordered-list">
              <li>Abre terminal en: <code>cd web-admin</code></li>
              <li>Instala dependencias (primera vez): <code>npm install</code></li>
              <li>ðŸš€ Ejecuta: <code>npm run dev</code></li>
              <li>Espera el mensaje: <code>Local: http://localhost:5173/</code></li>
              <li>Abre esa URL en Chrome o Edge</li>
              <li>âœ… VerÃ¡s la pantalla de Login del sistema</li>
            </ol>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card class="step-card success-card" shadow="hover">
            <template #header>
              <div class="step-card-header">
                <div class="step-badge teal">5</div>
                <div>
                  <h4>ðŸŽ‰ Â¡Paso 5: Prueba Final - Login al Sistema!</h4>
                  <p class="step-sub">Ingresa y explora todas las funciones</p>
                </div>
              </div>
            </template>
            <div class="final-check">
              <div class="check-item">
                <el-icon color="#10b981" :size="22"><CircleCheckFilled /></el-icon>
                <span>Backend corriendo en <code>http://localhost:8000</code></span>
              </div>
              <div class="check-item">
                <el-icon color="#10b981" :size="22"><CircleCheckFilled /></el-icon>
                <span>Web Admin corriendo en <code>http://localhost:5173</code></span>
              </div>
              <div class="check-item">
                <el-icon color="#10b981" :size="22"><CircleCheckFilled /></el-icon>
                <span>Base de datos PostgreSQL con las 7 tablas creadas</span>
              </div>
              <div class="check-item">
                <el-icon color="#10b981" :size="22"><CircleCheckFilled /></el-icon>
                <span>Usuario SUPER_ADMIN creado (correo: superadmin / pass: admin123)</span>
              </div>
              <div class="check-item">
                <el-icon color="#10b981" :size="22"><CircleCheckFilled /></el-icon>
                <span>Â¡App Android instalada y BASE_URL correctamente configurada!</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div id="glosario" class="doc-section">
      <div class="section-header">
        <el-icon :size="28" color="#5b3c8f"><Collection /></el-icon>
        <h2 class="section-title">ðŸ“‹ GLOSARIO DE TABLAS - BASE DE DATOS (PostgreSQL)</h2>
      </div>
      <p class="section-desc">Las 7 tablas principales del esquema de base de datos <code>bd_personeros_loreto</code>, con sus campos Ãºnicos y relaciones de clave forÃ¡nea:</p>

      <el-table :data="tablasBD" border stripe class="bd-table">
        <el-table-column prop="nro" label="#" width="60" align="center" />
        <el-table-column prop="nombre" label="Nombre Tabla" width="200">
          <template #default="{ row }">
            <el-tag :type="row.tag" size="small">{{ row.nombre }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="descripcion" label="DescripciÃ³n de la Tabla" />
        <el-table-column prop="campoUnique" label="Campo UNIQUE / PK" width="200" align="center">
          <template #default="{ row }">
            <code class="code-inline">{{ row.campoUnique }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="relaciones" label="ðŸ”— Relaciones FK (Foreign Keys)">
          <template #default="{ row }">
            <el-tag v-for="fk in row.relaciones" :key="fk" size="small" type="warning" effect="plain" style="margin: 2px;">{{ fk }}</el-tag>
            <span v-if="row.relaciones.length === 0" style="color: #9ca3af;">â€” Sin FK (tabla padre)</span>
          </template>
        </el-table-column>
      </el-table>

      <el-card class="erd-card" shadow="hover" style="margin-top: 32px;">
        <template #header>
          <div style="display: flex; align-items: center; gap: 10px;">
            <el-icon color="#5b3c8f" :size="24"><Share /></el-icon>
            <h3 style="margin: 0;">ðŸ”— Diagrama Relacional (Resumen de Relaciones)</h3>
          </div>
        </template>
        <div class="erd-diagram">
          <div class="erd-chain">
            <div class="erd-table main">persona</div>
            <div class="erd-arrow">â† persona_id â†’</div>
            <div class="erd-table ref">usuarios</div>
            <div class="erd-arrow">â† usuario_id â†’</div>
            <div class="erd-table ref">usuarios_distritos</div>
          </div>
          <div class="erd-chain">
            <div class="erd-table main">usuarios</div>
            <div class="erd-arrow">â† usuario_id â†’</div>
            <div class="erd-table ref">personeros_users</div>
            <div class="erd-arrow">â† mesa_id â†’</div>
            <div class="erd-table ref">mesa</div>
            <div class="erd-arrow">â† centro_votacion_id â†’</div>
            <div class="erd-table ref">centro_votacion</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="footer-note">
      <el-icon :size="20"><Reading /></el-icon>
      <span>ðŸ“š Fin del Manual - Sistema Electoral de Personeros - RegiÃ³n Loreto Â© 2024</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import {
  Reading, Cpu, Service, UserFilled, User, OfficeBuilding, Grid,
  MagicStick, Menu, LocationFilled, MapLocation, Monitor, Lock, DataAnalysis,
  Upload, Download, DeleteFilled, Link, Warning, CircleCheck, Location, Iphone,
  Compass, EditPen, Key, WarningFilled, Promotion, CircleCheckFilled,
  Collection, Share
} from '@element-plus/icons-vue'

const tabRolActivo = ref('superadmin')

const activePanelsWeb = ref(['1'])

const demoRol = ref('')

const permisosSuperAdmin = [
  { modulo: 'ðŸ‘¥ persona', vaciar: 'SI' },
  { modulo: 'ðŸ‘¤ usuarios', vaciar: 'NO' },
  { modulo: 'ðŸ“ usuarios_distritos', vaciar: 'SI' },
  { modulo: 'ðŸ¢ centro_votacion', vaciar: 'SI' },
  { modulo: 'ðŸ—³ï¸ mesa', vaciar: 'SI' },
  { modulo: 'ðŸ‘¥ personeros_users', vaciar: 'SI' }
]

const permisosAdmin = [
  { modulo: 'ðŸ‘¥ persona', listar: 'SI', crear: 'SI', editar: 'SI', eliminar: 'SI', importar: 'SI' },
  { modulo: 'ðŸ‘¤ usuarios', listar: 'SI', crear: 'SI', editar: 'SI', eliminar: 'SI', importar: 'NO' },
  { modulo: 'ðŸ“ usuarios_distritos', listar: 'SI', crear: 'NO', editar: 'NO', eliminar: 'NO', importar: 'NO' },
  { modulo: 'ðŸ¢ centro_votacion', listar: 'SI', crear: 'SI', editar: 'SI', eliminar: 'SI', importar: 'SI' },
  { modulo: 'ðŸ—³ï¸ mesa', listar: 'SI', crear: 'SI', editar: 'SI', eliminar: 'SI', importar: 'SI' },
  { modulo: 'ðŸ‘¥ personeros_users', listar: 'SI', crear: 'SI', editar: 'SI', eliminar: 'SI', importar: 'NO' }
]

const permisosCentro = [
  { modulo: 'ðŸ‘¥ persona', listar: 'SI', crear: 'SI', editar: 'SI', eliminar: 'NO', reporte: 'NO' },
  { modulo: 'ðŸ¢ centro_votacion', listar: 'SI', crear: 'NO', editar: 'NO', eliminar: 'NO', reporte: 'NO' },
  { modulo: 'ðŸ—³ï¸ mesa', listar: 'SI', crear: 'NO', editar: 'NO', eliminar: 'NO', reporte: 'NO' },
  { modulo: 'ðŸ‘¥ personeros_users', listar: 'SI', crear: 'SI', editar: 'SI', eliminar: 'SI', reporte: 'SI' }
]

const permisosMesa = [
  { modulo: 'ðŸ‘¤ Mi Perfil', listar: 'SI', editar: 'SI' },
  { modulo: 'ðŸ—³ï¸ Mi Mesa', listar: 'SI', editar: 'NO' },
  { modulo: 'ðŸ“ Asistencia', listar: 'SI', editar: 'SI' }
]

const graficosDashboard = [
  { nro: 1, nombre: 'ðŸ“Š Mesas por Provincia', tipo: 'Barras Apiladas', tipoTag: 'primary', descripcion: 'Compara mesas LIBRES vs ASIGNADAS agrupadas por cada provincia de la regiÃ³n.' },
  { nro: 2, nombre: 'ðŸ¥§ Solicitudes por Estado', tipo: 'Dona (Pie)', tipoTag: 'success', descripcion: 'Porcentaje de solicitudes: Aprobado, Pendiente, Rechazado y En RevisiÃ³n.' },
  { nro: 3, nombre: 'ðŸ° Usuarios por Rol', tipo: 'Pastel (Pie)', tipoTag: 'warning', descripcion: 'DistribuciÃ³n de los usuarios del sistema entre los 4 roles disponibles.' },
  { nro: 4, nombre: 'ðŸ“ˆ Centros por Distrito (Top 10)', tipo: 'Barras Horizontales', tipoTag: 'info', descripcion: 'Ranking de los 10 distritos con mayor cantidad de locales de votaciÃ³n.' },
  { nro: 5, nombre: 'ðŸ“‰ Timeline Solicitudes 14 dÃ­as', tipo: 'LÃ­nea + Ãrea', tipoTag: 'danger', descripcion: 'Tendencia de registros diarios durante las Ãºltimas dos semanas.' }
]

const camposPersona = [
  { campo: 'dni', tipo: 'VARCHAR(8)', requerido: true, descripcion: 'Documento Nacional de Identidad (8 dÃ­gitos, ÃšNICO). Ej: 70012345' },
  { campo: 'nombres', tipo: 'VARCHAR(100)', requerido: true, descripcion: 'Nombres completos. Ej: JUAN CARLOS' },
  { campo: 'apellidos', tipo: 'VARCHAR(100)', requerido: true, descripcion: 'Apellido paterno y materno. Ej: PEREZ GOMEZ' },
  { campo: 'telefono', tipo: 'VARCHAR(20)', requerido: false, descripcion: 'Celular o telÃ©fono. Ej: 987654321' },
  { campo: 'provincia', tipo: 'VARCHAR(80)', requerido: true, descripcion: 'Provincia de residencia. Ej: MAYNAS' },
  { campo: 'distrito', tipo: 'VARCHAR(80)', requerido: true, descripcion: 'Distrito de residencia. Ej: IQUITOS' },
  { campo: 'direccion', tipo: 'VARCHAR(200)', requerido: false, descripcion: 'Domicilio completo. Ej: AV. ABELLO NRO 123' }
]

const camposCentro = [
  { campo: 'nombre', tipo: 'VARCHAR(200)', ejemplo: 'IE. PEDRO RUIZ GALLO' },
  { campo: 'provincia', tipo: 'VARCHAR(80)', ejemplo: 'MAYNAS' },
  { campo: 'distrito', tipo: 'VARCHAR(80)', ejemplo: 'IQUITOS' },
  { campo: 'direccion', tipo: 'VARCHAR(200)', ejemplo: 'AV. PROLONGACIÃ“N PUTUMAYO' },
  { campo: 'tipo_local', tipo: 'VARCHAR(50)', ejemplo: 'Colegio / Local Comunal' }
]

const camposMesa = [
  { campo: 'numero_mesa', tipo: 'INTEGER', requerido: true, descripcion: 'NÃºmero correlativo de mesa dentro del centro. Ej: 1, 2, 3...' },
  { campo: 'centro_votacion_id', tipo: 'FK INTEGER', requerido: true, descripcion: 'ID del Centro de VotaciÃ³n al que pertenece la mesa.' },
  { campo: 'letra_mesa', tipo: 'VARCHAR(5)', requerido: false, descripcion: 'Letra identificatoria. Ej: A, B, C, UNICA.' },
  { campo: 'estado', tipo: 'VARCHAR(20)', requerido: true, descripcion: 'DISPONIBLE / ASIGNADA / CERRADA.' }
]

const hojasReporte = [
  { hoja: 1, nombre: 'Personas', icono: 'ðŸ‘¥', contenido: 'DNI, nombres, apellidos, telÃ©fono, provincia, distrito, direcciÃ³n, correo.' },
  { hoja: 2, nombre: 'Usuarios', icono: 'ðŸ‘¤', contenido: 'ID usuario, username, rol, persona_id (JOIN con nombres y DNI), fecha_creacion.' },
  { hoja: 3, nombre: 'Distritos Asignados', icono: 'ðŸ“', contenido: 'usuario_id â†’ nombre admin, provincia, distrito asignados.' },
  { hoja: 4, nombre: 'Centros de VotaciÃ³n', icono: 'ðŸ¢', contenido: 'ID centro, nombre, provincia, distrito, direcciÃ³n, tipo_local.' },
  { hoja: 5, nombre: 'Mesas', icono: 'ðŸ—³ï¸', contenido: 'ID mesa, numero, letra, centro_id + nombre centro, estado.' },
  { hoja: 6, nombre: 'Asignaciones', icono: 'ðŸ”—', contenido: 'personeros_users completo con JOIN: datos personero, datos mesa, datos centro, fecha asignaciÃ³n.' }
]

const vaciarTablas = [
  { tabla: 'ðŸ‘¥ persona', sePuede: true, motivo: 'PadrÃ³n maestro. Se puede limpiar para recargar. Debe reasignar usuarios despuÃ©s.' },
  { tabla: 'ðŸ‘¤ usuarios', sePuede: false, motivo: 'â›” PROTEGIDA. Contiene cuentas de acceso. Si se borrara, perderÃ­as acceso al sistema.' },
  { tabla: 'ðŸ“ usuarios_distritos', sePuede: true, motivo: 'Tabla puente. Se puede vaciar para reasignar jurisdicciones desde cero.' },
  { tabla: 'ðŸ¢ centro_votacion', sePuede: true, motivo: 'Datos maestros de locales. Si se vacÃ­a, las mesas quedan sin padre (cascada).' },
  { tabla: 'ðŸ—³ï¸ mesa', sePuede: true, motivo: 'Mesas electorales. Al vaciar, tambiÃ©n se eliminan asignaciones de personeros en cascada.' },
  { tabla: 'ðŸ‘¥ personeros_users', sePuede: true, motivo: 'Tabla de asignaciones. Limpia solo los vÃ­nculos entre usuarios y mesas, sin borrar usuarios.' }
]

const baseUrlConfig = [
  { escenario: 'ðŸ“± Emulador Android Studio', valor: 'http://10.0.2.2:8000', ejemplo: 'Puerto 8000 = backend FastAPI' },
  { escenario: 'ðŸ¤– Genymotion / Otros', valor: 'http://10.0.3.2:8000', ejemplo: 'Revisa documentaciÃ³n del emulador' },
  { escenario: 'ðŸ“ž TelÃ©fono FÃ­sico (WiFi)', valor: 'http://IP_LAN_PC:8000', ejemplo: 'http://192.168.1.25:8000' },
  { escenario: 'â˜ï¸ Servidor ProducciÃ³n', valor: 'https://tu-dominio.com', ejemplo: 'https://api.personerosloreto.pe' }
]

const navItems = [
  { pos: 1, icono: 'ðŸ“Š Dashboard', nombre: 'Dashboard / Inicio', funcion: 'Resumen con tarjetas y grÃ¡ficos de mÃ©tricas segÃºn rol', fab: false },
  { pos: 2, icono: 'ðŸ¢ Centros', nombre: 'Centros de VotaciÃ³n', funcion: 'Lista de locales, buscar, ver detalle de cada centro y sus mesas', fab: false },
  { pos: 3, icono: 'âœï¸ FAB Central', nombre: 'Registro RÃ¡pido', funcion: 'âœ¨ BOTÃ“N MORADO CENTRAL: Acceso directo a "Nuevo Registro Personero" (funciÃ³n mÃ¡s usada)', fab: true },
  { pos: 4, icono: 'ðŸ—³ï¸ Mesas', nombre: 'Mesas Electorales', funcion: 'Listado de mesas, estado (libre/asignada), asignar personero', fab: false },
  { pos: 5, icono: 'ðŸ‘¥ Registro', nombre: 'Registro Personeros', funcion: 'Listar, buscar, crear y editar registros de personeros', fab: false },
  { pos: 6, icono: 'ðŸ‘¤ Perfil', nombre: 'Mi Perfil / Usuarios', funcion: 'Ver datos de tu cuenta, cambiar contraseÃ±a, cerrar sesiÃ³n', fab: false }
]

const alcanceMovil = [
  { rol: 'ðŸ‘‘ SUPER_ADMIN', veTags: ['TODOS los Centros', 'TODAS las Mesas', 'TODOS los Usuarios', 'Dashboard Global'], tagType: 'danger', noVe: 'Ninguna restricciÃ³n. Ve TODO el sistema completo.' },
  { rol: 'ðŸ§‘â€ðŸ’¼ ADMINISTRADOR', veTags: ['Solo sus Distritos', 'Centros de sus distritos', 'Mesas de sus distritos', 'Usuarios de su jurisdicciÃ³n'], tagType: 'primary', noVe: 'Centros, mesas y usuarios de distritos que no estÃ¡n en usuarios_distritos.' },
  { rol: 'ðŸ¢ PERSONERO_CENTRO', veTags: ['SOLO su Centro', 'Todas las mesas de SU centro', 'Personeros de SU centro'], tagType: 'success', noVe: 'Otros centros. No ve administraciÃ³n de usuarios masiva.' },
  { rol: 'ðŸ—³ï¸ PERSONERO_MESA', veTags: ['SOLO su MESA asignada', 'Datos de su centro', 'Su perfil'], tagType: 'warning', noVe: 'Listados de otras mesas, otros centros, administraciÃ³n. Solo lectura bÃ¡sica.' }
]

const erroresAndroid = [
  { nro: 1, error: 'âŒ "unexpected end of stream"', causa: 'ConexiÃ³n cortada / Timeout / Backend no responde', solucion: '1) Verifica backend en ejecuciÃ³n (puerto 8000). 2) Revisa BASE_URL. 3) Reinicia la app. 4) Desactiva VPN.' },
  { nro: 2, error: 'âŒ "No se puede conectar al servidor"', causa: 'BASE_URL incorrecta / WiFi distinto / Firewall', solucion: '1) Misma red WiFi. 2) ipconfig â†’ IPv4 correcto. 3) Firewall Windows: permitir puerto 8000 (regla entrada). 4) Ping desde PC a IP.' },
  { nro: 3, error: 'âŒ App se cierra sola / Crash', causa: 'APK vieja instalada en cachÃ©', solucion: '1) Desinstalar APK vieja completamente. 2) Reiniciar celular/emulador. 3) Instalar NUEVA APK firmada. 4) Limpiar datos de app en Ajustes.' },
  { nro: 4, error: 'âŒ Login falla: "Usuario o contraseÃ±a invÃ¡lidos"', causa: 'Datos mal escritos o usuario no existe', solucion: '1) Verifica en Web Admin que el usuario exista. 2) AsegÃºrate de usar MAYÃšSCULAS/minÃºsculas correctas. 3) Hashea la contraseÃ±a desde Web Admin.' },
  { nro: 5, error: 'âŒ Selects dependientes vacÃ­os', causa: 'No hay centros/mesas cargados en su jurisdicciÃ³n', solucion: '1) Desde Web Admin, carga Excel de Centros y Mesas. 2) Asigna distritos al ADMINISTRADOR. 3) Cierra y abre la app mÃ³vil.' },
  { nro: 6, error: 'âŒ Error al subir imagen / archivo', causa: 'Permisos de almacenamiento denegados', solucion: '1) Ajustes â†’ Aplicaciones â†’ App Personeros â†’ Permisos â†’ ACTIVAR "Almacenamiento" y "CÃ¡mara". 2) Versiones Android 13+: permisos separados.' },
  { nro: 7, error: 'âŒ Datos desactualizados en listas', causa: 'Cache local sin refrescar', solucion: '1) Desliza hacia abajo en cualquier lista (Pull to Refresh). 2) Cierra sesiÃ³n y vuelve a ingresar. 3) BotÃ³n ðŸ”„ Actualizar en Dashboard.' }
]

const tablasBD = [
  { nro: 1, nombre: 'persona', tag: 'danger', descripcion: 'PadrÃ³n maestro de personas naturales (DNI, nombres, apellidos, contacto, ubicaciÃ³n). Base de todos los registros.', campoUnique: 'id (PK) + dni (UNIQUE)', relaciones: [] },
  { nro: 2, nombre: 'usuarios', tag: 'primary', descripcion: 'Cuentas de acceso al sistema (username, password hash, rol). Cada usuario apunta a una persona.', campoUnique: 'id (PK) + usuario (UNIQUE)', relaciones: ['persona_id â†’ persona.id'] },
  { nro: 3, nombre: 'usuarios_distritos', tag: 'warning', descripcion: 'Tabla puente N:N que define jurisdicciÃ³n: quÃ© distritos puede gestionar cada ADMINISTRADOR.', campoUnique: 'id (PK) + (usuario_id,distrito) UNIQUE', relaciones: ['usuario_id â†’ usuarios.id'] },
  { nro: 4, nombre: 'centro_votacion', tag: 'success', descripcion: 'Locales de votaciÃ³n con su ubicaciÃ³n geogrÃ¡fica: provincia, distrito, direcciÃ³n.', campoUnique: 'id (PK)', relaciones: [] },
  { nro: 5, nombre: 'mesa', tag: '', descripcion: 'Mesas electorales individuales. Cada mesa pertenece a un centro_votacion (relaciÃ³n N:1).', campoUnique: 'id (PK)', relaciones: ['centro_votacion_id â†’ centro_votacion.id'] },
  { nro: 6, nombre: 'personeros_users', tag: 'info', descripcion: 'Tabla de asignaciones: vincula un usuario (personero) con una mesa, con fecha y estado.', campoUnique: 'id (PK)', relaciones: ['usuario_id â†’ usuarios.id', 'mesa_id â†’ mesa.id'] }
]
</script>

<style scoped>
.doc-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 8px 8px 60px 80px;
  position: relative;
}

.doc-anchor {
  position: fixed;
  top: 80px;
  left: 260px;
  z-index: 50;
  background: #fff;
  padding: 14px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(91,60,143,0.12);
  border-left: 4px solid #5b3c8f;
}

.doc-section {
  padding-top: 80px;
  margin-top: -40px;
}

.hero-section {
  background: linear-gradient(135deg, #faf7ff 0%, #f0e9ff 50%, #e0f0ff 100%);
  border-radius: 20px;
  padding: 48px 40px;
  text-align: center;
  border: 2px solid #e6dcff;
}

.hero-icon {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #fff, #f3eaff);
  padding: 20px;
  border-radius: 22px;
  display: inline-block;
  box-shadow: 0 10px 30px rgba(91,60,143,0.2);
}

.hero-title {
  font-size: 34px;
  font-weight: 800;
  color: #1f2937;
  margin: 10px 0 12px;
  background: linear-gradient(90deg, #5b3c8f, #3b82f6, #10b981);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 16px;
  color: #4b5563;
  max-width: 780px;
  margin: 0 auto 36px;
  line-height: 1.6;
}

.hero-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}

.hero-card {
  border: none !important;
  border-radius: 16px !important;
  overflow: hidden;
  transition: transform 0.25s, box-shadow 0.25s;
  text-align: left;
}

.hero-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 40px rgba(0,0,0,0.1);
}

.card-icon-wrap {
  width: 68px;
  height: 68px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #5b3c8f, #7a52b3);
  margin-bottom: 14px;
  box-shadow: 0 6px 14px rgba(91,60,143,0.3);
}

.card-icon-wrap.blue {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  box-shadow: 0 6px 14px rgba(59,130,246,0.3);
}

.card-icon-wrap.green {
  background: linear-gradient(135deg, #10b981, #34d399);
  box-shadow: 0 6px 14px rgba(16,185,129,0.3);
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 10px;
  color: #1f2937;
}

.card-text {
  color: #4b5563;
  line-height: 1.6;
  margin: 0;
  font-size: 14px;
}

.card-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.card-list li {
  padding: 5px 0;
  color: #4b5563;
  font-size: 14px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 3px solid #f3e8ff;
}

.section-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0;
  color: #1f2937;
}

.section-desc {
  color: #4b5563;
  font-size: 15px;
  line-height: 1.7;
  margin: 0 0 24px;
}

.roles-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #e5e7eb;
}

.roles-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  height: 52px;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 10px;
  font-weight: 700;
}

.tab-label.superadmin {
  background: #f5efff;
  color: #5b3c8f;
}

.tab-label.admin {
  background: #eff6ff;
  color: #3b82f6;
}

.tab-label.centro {
  background: #ecfdf5;
  color: #10b981;
}

.tab-label.mesa {
  background: #fffbeb;
  color: #b45309;
}

.tab-content {
  padding: 20px 4px 10px;
}

.info-card {
  border-radius: 14px !important;
  height: 100%;
}

.superadmin-border { border-left: 5px solid #5b3c8f !important; }
.admin-border { border-left: 5px solid #3b82f6 !important; }
.centro-border { border-left: 5px solid #10b981 !important; }
.mesa-border { border-left: 5px solid #f59e0b !important; }

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: #1f2937;
  font-size: 15px;
}

.info-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-list li {
  padding: 6px 0;
  color: #374151;
  font-size: 14px;
  border-bottom: 1px dashed #f3f4f6;
}

.info-list li:last-child { border: none; }

.scope-box {
  text-align: center;
  padding: 18px 10px;
  border-radius: 12px;
}

.purple-scope { background: #f5efff; }
.blue-scope { background: #eff6ff; }
.green-scope { background: #ecfdf5; }
.orange-scope { background: #fffbeb; }

.scope-box h4 {
  margin: 8px 0;
  color: #1f2937;
  font-size: 15px;
  font-weight: 800;
}

.scope-box p {
  margin: 0;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.5;
}

.perm-title {
  margin: 30px 0 14px;
  color: #374151;
  font-size: 16px;
}

.perm-table {
  border-radius: 10px;
  overflow: hidden;
}

.doc-collapse :deep(.el-collapse-item__header) {
  background: #fff;
  border-radius: 12px;
  padding: 6px 16px;
  margin: 10px 0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
  height: auto;
  line-height: 2.2;
}

.doc-collapse :deep(.el-collapse-item__wrap) {
  border: none;
  border-radius: 12px;
  background: #fff;
  margin-bottom: 10px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
}

.doc-collapse :deep(.el-collapse-item__content) {
  padding: 24px;
  line-height: 1.7;
  color: #374151;
}

.collapse-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
}

.collapse-content h4 {
  color: #1f2937;
  font-size: 16px;
  font-weight: 700;
  margin: 18px 0 12px;
}

.collapse-content h5 {
  color: #374151;
  font-size: 14px;
  font-weight: 700;
  margin: 14px 0 8px;
}

.step-box {
  background: #f8f6ff;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  border: 1px solid #e6dcff;
}

.step-box.warning {
  background: #fffbeb;
  border-color: #fde68a;
}

.step-num {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5b3c8f, #7a52b3);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
  flex-shrink: 0;
}

.step-box.warning .step-num {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.step-body h4 {
  margin: 0 0 8px !important;
  font-size: 16px;
}

.step-body ol {
  padding-left: 20px;
  margin: 0;
}

.step-body ol li {
  padding: 4px 0;
  color: #374151;
}

.code-alert {
  margin-top: 10px;
}

.tip-text {
  margin-top: 14px;
  padding: 10px 12px;
  background: #eff6ff;
  border-radius: 8px;
  color: #1e40af;
  font-size: 13px;
}

.mini-stat {
  padding: 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 14px;
  color: #fff;
  margin-bottom: 14px;
}

.purple-bg { background: linear-gradient(135deg, #5b3c8f, #7a52b3); }
.blue-bg { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.green-bg { background: linear-gradient(135deg, #10b981, #34d399); }
.orange-bg { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.teal-bg { background: linear-gradient(135deg, #14b8a6, #2dd4bf); }
.red-bg { background: linear-gradient(135deg, #ef4444, #f87171); }

.mini-icon {
  width: 46px;
  height: 46px;
  background: rgba(255,255,255,0.22);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.mini-num {
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}

.mini-label {
  font-size: 12px;
  opacity: 0.95;
  margin-top: 4px;
}

.feature-alert {
  border-radius: 12px;
}
.feature-alert :deep(.el-alert__title) {
  font-size: 15px;
  font-weight: 700;
}
.feature-alert :deep(.el-alert__description) {
  font-size: 13px;
  margin-top: 4px;
}

.join-box {
  background: #f8f6ff;
  border: 1px solid #e6dcff;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.benefit-text {
  margin: 10px 0 0;
  font-weight: 600;
  color: #059669;
}

.jurisdiccion-demo {
  padding: 18px;
  border-radius: 14px;
  text-align: center;
  color: #fff;
}

.jurisdiccion-demo.purple { background: linear-gradient(135deg, #5b3c8f, #7a52b3); }
.jurisdiccion-demo.blue { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.jurisdiccion-demo.green { background: linear-gradient(135deg, #10b981, #34d399); }

.jurisdiccion-demo h4 {
  margin: 8px 0;
  font-size: 16px;
  color: #fff !important;
}

.jurisdiccion-demo p {
  margin: 0 0 12px;
  font-size: 13px;
  opacity: 0.95;
}

.all-badge, .assign-badge, .single-badge {
  display: inline-block;
  padding: 8px 14px;
  background: rgba(255,255,255,0.22);
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}

.import-flow {
  background: #f8f6ff;
  padding: 20px;
  border-radius: 14px;
}

.excel-mockup {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(0,0,0,0.06);
}

.excel-toolbar {
  background: linear-gradient(90deg, #107c41, #149251);
  color: #fff;
  padding: 10px 16px;
  font-weight: 600;
  font-size: 14px;
}

.excel-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  font-family: Consolas, monospace;
}

.excel-table th {
  background: #f3f4f6;
  color: #6b7280;
  padding: 6px;
  text-align: center;
  border: 1px solid #e5e7eb;
  font-weight: 600;
}

.excel-table td {
  padding: 6px 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
}

.cell-header {
  background: #dbeafe !important;
  font-weight: 700;
  color: #1e40af;
}

.protection-box {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 20px;
  text-align: center;
}

.protection-box h5 {
  margin: 10px 0 8px !important;
  color: #1f2937;
  font-size: 15px;
  font-weight: 700;
}

.protection-box p {
  margin: 0;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.5;
}

.danger-alert {
  border-radius: 14px;
}

.danger-alert :deep(.el-alert__title) {
  font-size: 18px !important;
  font-weight: 800 !important;
}

.code-inline {
  background: #f3e8ff;
  color: #5b3c8f;
  padding: 2px 8px;
  border-radius: 6px;
  font-family: Consolas, monospace;
  font-size: 12px;
  font-weight: 600;
}

.android-card {
  border-radius: 16px;
  padding: 20px;
  color: #fff;
  text-align: center;
  transition: transform 0.2s;
}

.android-card:hover {
  transform: translateY(-4px);
}

.android-card h4 {
  margin: 10px 0 6px;
  color: #fff !important;
  font-size: 15px;
}

.android-card p {
  margin: 0;
  font-size: 13px;
  opacity: 0.95;
  line-height: 1.4;
  font-weight: 500;
}

.android-card.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.android-card.purple { background: linear-gradient(135deg, #5b3c8f, #7a52b3); }
.android-card.green { background: linear-gradient(135deg, #10b981, #059669); }
.android-card.orange { background: linear-gradient(135deg, #f59e0b, #d97706); }
.android-card.teal { background: linear-gradient(135deg, #14b8a6, #0d9488); }
.android-card.indigo { background: linear-gradient(135deg, #6366f1, #4f46e5); }
.android-card.red { background: linear-gradient(135deg, #ef4444, #dc2626); }

.mobile-section {
  padding: 20px 0;
}

.mobile-section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1f2937;
  font-size: 19px;
  font-weight: 800;
  margin: 14px 0 16px;
  padding: 12px 18px;
  background: linear-gradient(90deg, #f5efff, #eff6ff, #ecfdf5);
  border-radius: 12px;
}

.mobile-mockup {
  display: flex;
  justify-content: center;
}

.phone-frame {
  width: 280px;
  background: #1f2937;
  border-radius: 34px;
  padding: 14px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.25);
  border: 3px solid #374151;
}

.phone-status {
  background: #fff;
  color: #374151;
  text-align: right;
  padding: 4px 10px;
  border-radius: 8px 8px 0 0;
  font-size: 11px;
  font-weight: 600;
}

.phone-content {
  background: #fff;
  padding: 22px 18px;
  border-radius: 0 0 8px 8px;
  text-align: center;
  min-height: 340px;
}

.login-screen h4 {
  margin: 8px 0 20px;
  color: #5b3c8f;
  font-size: 16px;
}

.mock-input {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
  text-align: left;
  color: #6b7280;
  font-size: 13px;
}

.mock-btn-purple {
  background: linear-gradient(135deg, #5b3c8f, #7a52b3);
  color: #fff;
  padding: 11px;
  border-radius: 8px;
  font-weight: 700;
  margin-top: 8px;
  font-size: 14px;
}

.mock-link {
  color: #5b3c8f;
  font-size: 12px;
  margin-top: 14px;
  font-weight: 500;
}

.navbar-mockup {
  background: #fff;
  border-top: 1px solid #e5e7eb;
  border-radius: 22px;
  padding: 12px 16px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  position: relative;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #6b7280;
  padding: 6px 8px;
}

.nav-item.active {
  color: #5b3c8f;
  font-weight: 700;
}

.nav-fab {
  position: absolute;
  top: -26px;
  left: 50%;
  transform: translateX(-50%);
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5b3c8f, #7a52b3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 18px rgba(91,60,143,0.4);
  font-size: 24px;
}

.fab-badge {
  background: linear-gradient(90deg, #5b3c8f, #7a52b3);
  color: #fff;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
}

.mobile-stat-card {
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  color: #fff;
  transition: transform 0.2s;
}

.mobile-stat-card:hover {
  transform: translateY(-4px);
}

.purple-m { background: linear-gradient(135deg, #5b3c8f, #7a52b3); }
.green-m { background: linear-gradient(135deg, #10b981, #34d399); }
.orange-m { background: linear-gradient(135deg, #f59e0b, #fbbf24); }

.ms-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  background: rgba(255,255,255,0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

.ms-num {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.ms-label {
  font-size: 12px;
  opacity: 0.95;
  margin-top: 4px;
}

.step-details {
  background: #f9fafb;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 13px;
  color: #374151;
  text-align: left;
  margin-top: 6px;
}

.note-text {
  background: #ecfdf5;
  color: #065f46;
  padding: 12px 18px;
  border-radius: 10px;
  font-size: 14px;
  margin: 0;
  border-left: 4px solid #10b981;
}

.flow-step-pill {
  background: #f8f6ff;
  border: 2px solid #e6dcff;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  gap: 12px;
  height: 100%;
  border-top: 5px solid #5b3c8f;
}

.flow-step-pill.success { background: #ecfdf5; border-color: #a7f3d0; border-top-color: #10b981; }
.flow-step-pill.warning { background: #fffbeb; border-color: #fde68a; border-top-color: #f59e0b; }
.flow-step-pill.info { background: #eff6ff; border-color: #bfdbfe; border-top-color: #3b82f6; }

.fsp-num {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5b3c8f, #7a52b3);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  flex-shrink: 0;
}

.flow-step-pill.success .fsp-num { background: linear-gradient(135deg, #10b981, #34d399); }
.flow-step-pill.warning .fsp-num { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.flow-step-pill.info .fsp-num { background: linear-gradient(135deg, #3b82f6, #60a5fa); }

.fsp-content h5 {
  margin: 0 0 6px !important;
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
}

.fsp-content p {
  margin: 0;
  color: #4b5563;
  font-size: 12px;
  line-height: 1.4;
}

.big-steps {
  background: #fff;
  padding: 30px 20px;
  border-radius: 18px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.06);
  margin-bottom: 20px;
}

.step-card {
  border-radius: 16px !important;
  overflow: hidden;
  border: 2px solid #f3f4f6 !important;
}

.step-card.success-card {
  background: linear-gradient(135deg, #ecfdf5, #f0fdf4);
  border-color: #a7f3d0 !important;
}

.step-card-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.step-card-header h4 {
  margin: 0 !important;
  color: #1f2937;
  font-size: 16px;
  font-weight: 800;
}

.step-sub {
  margin: 2px 0 0 !important;
  color: #6b7280;
  font-size: 13px;
}

.step-badge {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 800;
  flex-shrink: 0;
}

.step-badge.blue { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.step-badge.green { background: linear-gradient(135deg, #10b981, #34d399); }
.step-badge.orange { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.step-badge.purple { background: linear-gradient(135deg, #5b3c8f, #7a52b3); }
.step-badge.teal { background: linear-gradient(135deg, #14b8a6, #2dd4bf); }

.ordered-list {
  padding-left: 22px;
  margin: 0;
}

.ordered-list li {
  padding: 5px 0;
  color: #374151;
  font-size: 14px;
  line-height: 1.6;
}

.ordered-list .sub-li {
  list-style: none;
  margin-top: 8px;
  font-weight: 700;
  margin-left: -18px;
  padding-left: 0 !important;
}

.ordered-list .sub-li::before {
  content: "ðŸ“Œ  ";
}

.final-check {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 12px 18px;
  border-radius: 10px;
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  border: 1px solid #d1fae5;
}

.bd-table {
  border-radius: 12px;
  overflow: hidden;
}

.erd-card {
  border-radius: 18px !important;
  background: linear-gradient(135deg, #faf7ff, #f0f9ff);
}

.erd-diagram {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  justify-content: center;
  align-items: center;
}

.erd-chain {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
}

.erd-table {
  padding: 12px 18px;
  border-radius: 10px;
  font-weight: 700;
  font-family: Consolas, monospace;
  font-size: 14px;
}

.erd-table.main {
  background: linear-gradient(135deg, #5b3c8f, #7a52b3);
  color: #fff;
  box-shadow: 0 6px 14px rgba(91,60,143,0.3);
}

.erd-table.ref {
  background: #fff;
  color: #374151;
  border: 2px solid #e6dcff;
}

.erd-arrow {
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
  background: #f3f4f6;
  padding: 4px 10px;
  border-radius: 8px;
}

.footer-note {
  margin-top: 50px;
  text-align: center;
  padding: 20px;
  background: linear-gradient(90deg, #f5efff, #eff6ff, #ecfdf5);
  border-radius: 14px;
  color: #4b5563;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

@media (max-width: 1024px) {
  .doc-anchor { display: none; }
  .doc-container { padding: 8px; }
  .hero-cards { grid-template-columns: 1fr; }
}
</style>
