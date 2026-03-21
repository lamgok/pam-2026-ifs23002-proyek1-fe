package org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestUserChange (
    val name: String,
    val username: String
)

@Serializable
data class RequestUserChangePassword (
    val newPassword: String,
    val password: String
)