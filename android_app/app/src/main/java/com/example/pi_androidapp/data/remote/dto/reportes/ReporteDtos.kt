package com.example.pi_androidapp.data.remote.dto.reportes

import com.google.gson.annotations.SerializedName

/** Resultado de crear un reporte. */
data class CrearReporteResultData(
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("reporte_id") val reporteId: Int?
)
