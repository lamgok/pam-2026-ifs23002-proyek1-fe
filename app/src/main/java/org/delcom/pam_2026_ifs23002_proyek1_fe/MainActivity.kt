package org.delcom.pam_2026_ifs23002_proyek1_fe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.UIApp
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.theme.DelcomTheme
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.AuthViewModel
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.EthnographyViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val ethnographyViewModel: EthnographyViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DelcomTheme {
                UIApp(
                    ethnographyViewModel = ethnographyViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}