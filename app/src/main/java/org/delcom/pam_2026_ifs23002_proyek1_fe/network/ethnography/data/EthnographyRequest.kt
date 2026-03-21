package org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data

import kotlinx.serialization.Serializable

@Serializable
data class EthnographyRequest (
    val tribeName: String,
    val region: String,
    val language: String,
    val traditionalHouse: String,
    val traditionalWeapon: String,
    val beliefSystem: String,
    val description: String
)