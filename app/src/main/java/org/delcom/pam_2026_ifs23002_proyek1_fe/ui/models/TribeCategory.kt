package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import org.delcom.pam_2026_ifs23002_proyek1_fe.R

sealed class TribeCategory(
    val name: String,
    val icon: Any, // Can be ImageVector or Int (Drawable Res)
    val regionKey: String
) {
    object All : TribeCategory("Semua", Icons.Default.AllInclusive, "")
    object Sumatera : TribeCategory("Sumatera", Icons.Default.Home, "Sumatera")
    object Jawa : TribeCategory("Jawa", Icons.Default.TheaterComedy, "Jawa")
    object Kalimantan : TribeCategory("Kalimantan", Icons.Default.Shield, "Kalimantan")
    object Sulawesi : TribeCategory("Sulawesi", Icons.Default.DirectionsBoat, "Sulawesi")
    object Papua : TribeCategory("Papua", Icons.Default.Pets, "Papua")
    object BaliNusa : TribeCategory("Bali & Nusa", Icons.Default.Landscape, "Bali")

    companion object {
        val list = listOf(All, Sumatera, Jawa, Kalimantan, Sulawesi, Papua, BaliNusa)
    }
}
