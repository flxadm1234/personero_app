package com.electoral.personeros.utils

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object JWTUtils {

    fun decode(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
            JSONObject(decodedString)
        } catch (e: Exception) {
            null
        }
    }

    fun getClaim(token: String, claim: String): Any? {
        return try {
            decode(token)?.opt(claim)
        } catch (e: Exception) {
            null
        }
    }

    fun getSubject(token: String): String? {
        return getClaim(token, "sub") as? String
    }

    fun getIssuer(token: String): String? {
        return getClaim(token, "iss") as? String
    }

    fun getAudience(token: String): Any? {
        return getClaim(token, "aud")
    }

    fun getExpiration(token: String): Long? {
        return (getClaim(token, "exp") as? Number)?.toLong()
    }

    fun getIssuedAt(token: String): Long? {
        return (getClaim(token, "iat") as? Number)?.toLong()
    }

    fun isExpired(token: String): Boolean {
        val exp = getExpiration(token) ?: return true
        val now = System.currentTimeMillis() / 1000
        return now >= exp
    }

    fun getUserId(token: String): Int? {
        return (getClaim(token, "user_id") as? Number)?.toInt()
            ?: (getClaim(token, "id") as? Number)?.toInt()
    }

    fun getRole(token: String): String? {
        return getClaim(token, "rol") as? String
            ?: getClaim(token, "role") as? String
    }

    fun getUsername(token: String): String? {
        return getClaim(token, "username") as? String
            ?: getClaim(token, "user") as? String
    }

    fun getRemainingTimeMillis(token: String): Long {
        val exp = getExpiration(token) ?: return 0L
        val now = System.currentTimeMillis() / 1000
        return (exp - now) * 1000L
    }

    fun isValid(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return try {
            !isExpired(token) && decode(token) != null
        } catch (e: Exception) {
            false
        }
    }

    fun prettyPrint(token: String): String {
        return try {
            decode(token)?.toString(2) ?: "Token inválido"
        } catch (e: Exception) {
            "Error al decodificar: ${e.message}"
        }
    }
}
