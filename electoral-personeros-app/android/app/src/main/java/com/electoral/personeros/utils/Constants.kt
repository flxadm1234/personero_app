package com.electoral.personeros.utils

object Constants {
    // EMULADOR Android Studio: 10.0.2.2 es alias ESPECIAL que apunta al localhost de TU PC
    // (equivalente a 127.0.0.1 desde el punto de vista del host)
    // Dispositivo físico MISMA red Wi-Fi: usa tu IP LAN ipconfig (ej: http://192.168.1.179:8000)
    // 📲 MODO ACTIVO AHORA: DISPOSITIVO FÍSICO (IP WiFi LAN)
    const val BASE_URL = "http://192.168.1.179:8000"
    const val API_PREFIX = "/api"
    const val CONNECTION_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    const val ROLE_SUPER_ADMIN = "SUPER_ADMIN"
    const val ROLE_ADMINISTRADOR = "ADMINISTRADOR"
    const val ROLE_PERSONERO_CENTRO = "PERSONERO_CENTRO"
    const val ROLE_PERSONERO_MESA = "PERSONERO_MESA"

    const val TIPO_CENTRO = "CENTRO"
    const val TIPO_MESA = "MESA"

    const val ESTADO_SOLICITADO = "SOLICITADO"
    const val ESTADO_ASIGNADO = "ASIGNADO"
    const val ESTADO_RECHAZADO = "RECHAZADO"

    const val MESA_LIBRE = "LIBRE"
    const val MESA_OCUPADA = "OCUPADA"

    const val DATE_FORMAT = "dd/MM/yyyy"
    const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss"
    const val ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    const val WHATSAPP_PREFIX = "https://wa.me/51"
    const val TEL_PREFIX = "tel:"

    const val PREF_NAME = "electoral_personeros_prefs"
    const val KEY_TOKEN = "access_token"
    const val KEY_USER = "current_user"
    const val KEY_ROL = "current_rol"

    const val PAGE_SIZE = 20

    val PROVINCIAS_LORETO = listOf(
        "Maynas",
        "Alto Amazonas",
        "Loreto",
        "Mariscal Ramón Castilla",
        "Requena",
        "Ucayali",
        "Putumayo",
        "Datem del Marañón",
        "Alto Nanay"
    )

    val DISTRITOS_POR_PROVINCIA = mapOf(
        "Maynas" to listOf(
            "Iquitos", "Belén", "Punchana", "San Juan Bautista", "Mazán", "Fernando Lores"
        ),
        "Alto Amazonas" to listOf(
            "Yurimaguas", "Balsa Puerto", "Jeberos", "Lagunas", "Santa Cruz", "Huamanguillo",
            "Teniente César López Rojas"
        ),
        "Loreto" to listOf(
            "Nauta", "Parinari", "Tigre", "Urarinas", "Contamana"
        ),
        "Mariscal Ramón Castilla" to listOf(
            "Caballococha", "Santa Rosa", "Yavari", "Ramon Castilla", "San Pablo"
        ),
        "Requena" to listOf(
            "Requena", "Alto Tapiche", "Capelo", "Puinarana", "Saquena", "Soplin",
            "Tapiche", "Yaquerana"
        ),
        "Ucayali" to listOf(
            "Pucallpa", "Aguaytía", "Calleria", "Campoverde", "Cunia", "Iparía",
            "Masisea", "Neshuya", "Tulumanya"
        ),
        "Putumayo" to listOf(
            "San Antonio del Estrecho", "Putumayo", "Rosa Panduro", "Yaguas"
        ),
        "Datem del Marañón" to listOf(
            "Manseriche", "Alto Marañón", "Barranca", "Cahuapanas", "Marañón",
            "Pastaza", "Tigre", "Urarinas"
        ),
        "Alto Nanay" to listOf(
            "Santa María de Nanay", "Indiana", "Pebas", "Napo", "Putumayo"
        )
    )
}
