package org.delcom.pam_2026_ifs23002_proyek1_fe.network.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseMessage<T>(
    val status: String,
    val message: String,
    val data: T? = null
)