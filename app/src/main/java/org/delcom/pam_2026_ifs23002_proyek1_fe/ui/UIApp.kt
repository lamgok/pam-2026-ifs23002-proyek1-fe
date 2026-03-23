package org.delcom.pam_2026_ifs23002_proyek1_fe.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ConstHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.CustomSnackbar
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.HomeScreen
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.ProfileScreen
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.auth.AuthLoginScreen
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.auth.AuthRegisterScreen
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.ethnography.EthnographyDetailScreen
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.ethnography.EthnographyScreen
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.ethnography.EthnographyUpsertScreen
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.theme.DelcomTheme
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.AuthViewModel
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.EthnographyViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UIApp(
    navController: NavHostController = rememberNavController(),
    ethnographyViewModel: EthnographyViewModel,
    authViewModel: AuthViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Global Dark Mode State
    val systemInDark = isSystemInDarkTheme()
    var isDark by remember { mutableStateOf(systemInDark) }

    DelcomTheme(darkTheme = isDark) {
        Scaffold(
            snackbarHost = { 
                SnackbarHost(snackbarHostState) { snackbarData ->
                    CustomSnackbar(snackbarData, onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() })
                } 
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { _ ->
            NavHost(
                navController = navController,
                startDestination = ConstHelper.RouteNames.Home.path,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(route = ConstHelper.RouteNames.AuthLogin.path) {
                    AuthLoginScreen(
                        navController = navController,
                        snackbarHost = snackbarHostState,
                        authViewModel = authViewModel,
                    )
                }

                composable(route = ConstHelper.RouteNames.AuthRegister.path) {
                    AuthRegisterScreen(
                        navController = navController,
                        snackbarHost = snackbarHostState,
                        authViewModel = authViewModel,
                    )
                }

                composable(route = ConstHelper.RouteNames.Home.path) {
                    HomeScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        ethnographyViewModel = ethnographyViewModel,
                        isDark = isDark,
                        onToggleTheme = { isDark = !isDark }
                    )
                }

                composable(route = ConstHelper.RouteNames.Profile.path) {
                    ProfileScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        ethnographyViewModel = ethnographyViewModel
                    )
                }

                composable(route = ConstHelper.RouteNames.Ethnographies.path) {
                    EthnographyScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        viewModel = ethnographyViewModel
                    )
                }

                composable(route = ConstHelper.RouteNames.EthnographiesAdd.path) {
                    EthnographyUpsertScreen(
                        navController = navController,
                        snackbarHost = snackbarHostState,
                        authViewModel = authViewModel,
                        viewModel = ethnographyViewModel
                    )
                }

                composable(
                    route = ConstHelper.RouteNames.EthnographiesDetail.path,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: ""
                    EthnographyDetailScreen(
                        navController = navController,
                        snackbarHost = snackbarHostState,
                        authViewModel = authViewModel,
                        viewModel = ethnographyViewModel,
                        id = id
                    )
                }

                composable(
                    route = ConstHelper.RouteNames.EthnographiesEdit.path,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: ""
                    EthnographyUpsertScreen(
                        navController = navController,
                        snackbarHost = snackbarHostState,
                        authViewModel = authViewModel,
                        viewModel = ethnographyViewModel,
                        id = id
                    )
                }
            }
        }
    }
}
