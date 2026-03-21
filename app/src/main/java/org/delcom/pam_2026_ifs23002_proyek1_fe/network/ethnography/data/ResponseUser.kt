package org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseUser (
    val user: ResponseUserData
)

@Serializable
data class ResponseUserData(
    val id: String,
    val name: String,
    val username: String,
    val createdAt: String,
    val updatedAt: String
)