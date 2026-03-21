package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.ethnography

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_2026_ifs23002_proyek1_fe.R
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.RouteHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ToolsHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.LoadingUI
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.TopAppBarComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.*

@Composable
fun EthnographyDetailScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    viewModel: EthnographyViewModel,
    id: String
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val authToken = (uiStateAuth.auth as? AuthUIState.Success)?.data?.authToken ?: ""

    LaunchedEffect(id) {
        viewModel.getEthnographyById(authToken, id)
    }

    LaunchedEffect(uiState.ethnographyDelete) {
        if (uiState.ethnographyDelete is EthnographyActionUIState.Success) {
            viewModel.resetActionStates()
            navController.popBackStack()
        }
    }

    if (uiState.ethnography is EthnographyUIState.Loading) {
        LoadingUI()
        return
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                navController = navController,
                title = "Detail Etnografi",
                customMenuItems = emptyList()
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { RouteHelper.to(navController, "ethnographies/${id}/edit") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                FloatingActionButton(
                    onClick = { viewModel.deleteEthnography(authToken, id) },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus")
                }
            }
        }
    ) { padding ->
        if (uiState.ethnography is EthnographyUIState.Success) {
            val data = (uiState.ethnography as EthnographyUIState.Success).data
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = ToolsHelper.getEthnographyImage(data.id, data.updatedAt),
                    contentDescription = data.tribeName,
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = data.tribeName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = data.region, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                DetailItem("Bahasa Daerah", data.language)
                DetailItem("Rumah Adat", data.traditionalHouse)
                DetailItem("Senjata Tradisional", data.traditionalWeapon)
                DetailItem("Sistem Kepercayaan", data.beliefSystem)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Deskripsi Budaya", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(text = data.description, style = MaterialTheme.typography.bodyLarge)
            }
        } else if (uiState.ethnography is EthnographyUIState.Error) {
            Text(text = (uiState.ethnography as EthnographyUIState.Error).message)
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}