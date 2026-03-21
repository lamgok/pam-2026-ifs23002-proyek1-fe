package org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseEthnographies (
    val ethnographies: List<ResponseEthnographyData>
)

@Serializable
data class ResponseEthnography (
    val ethnography: ResponseEthnographyData
)

@Serializable
data class ResponseEthnographyData(
    val id: String = "",
    val userId: String = "",
    val tribeName: String,
    val region: String,
    val language: String,
    val traditionalHouse: String,
    val traditionalWeapon: String,
    val beliefSystem: String,
    val description: String,
    val imageUrl: String? = null,
    val createdAt: String = "",
    var updatedAt: String = ""
)

@Serializable
data class ResponseEthnographyAdd (
    val ethnographyId: String
)