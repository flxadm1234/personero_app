package com.electoral.personeros.data.repository

import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.CoberturaResponse
import com.electoral.personeros.data.models.DesgloseItem

class CoberturaRepository {

    suspend fun getCoberturaGeneral(): Result<CoberturaResponse> {
        return try {
            val response = RetrofitInstance.api.getMonitoreoCobertura()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al obtener cobertura"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCoberturaProvincia(provincia: String): Result<CoberturaResponse> {
        return try {
            val response = RetrofitInstance.api.getCoberturaProvincia(provincia)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCoberturaDistrito(provincia: String, distrito: String): Result<CoberturaResponse> {
        return try {
            val response = RetrofitInstance.api.getCoberturaDistrito(provincia, distrito)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDesglose(): Result<List<DesgloseItem>> {
        return try {
            val response = RetrofitInstance.api.getDashboardDesglose()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al obtener desglose"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
