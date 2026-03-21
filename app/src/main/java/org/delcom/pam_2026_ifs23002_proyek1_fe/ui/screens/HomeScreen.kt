package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ConstHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.RouteHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ToolsHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.ResponseEthnographyData
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.BottomNavComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.LoadingUI
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.TopAppBarComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.TopAppBarMenuItem
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.*

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    ethnographyViewModel: EthnographyViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateEthnography by ethnographyViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var isFreshToken by remember { mutableStateOf(false) }
    var authToken by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (isLoading) return@LaunchedEffect
        isLoading = true
        isFreshToken = true
        authViewModel.loadTokenFromPreferences()
    }

    LaunchedEffect(uiStateAuth.auth) {
        when (val state = uiStateAuth.auth) {
            is AuthUIState.Success -> {
                val dataToken = state.data
                if (isFreshToken) {
                    authViewModel.refreshToken(dataToken.authToken, dataToken.refreshToken)
                    isFreshToken = false
                } else {
                    authToken = dataToken.authToken
                    ethnographyViewModel.getAllEthnographies(dataToken.authToken)
                    isLoading = false
                }
            }
            is AuthUIState.Error -> {
                if (isLoading) {
                    isLoading = false
                    authViewModel.logout("")
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout is AuthLogoutUIState.Success || uiStateAuth.authLogout is AuthLogoutUIState.Error) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isLoading || (authToken == null && uiStateAuth.auth !is AuthUIState.Success)) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(text = "Profile", icon = Icons.Filled.Person, route = ConstHelper.RouteNames.Profile.path),
        TopAppBarMenuItem(text = "Logout", icon = Icons.AutoMirrored.Filled.Logout, route = null, onClick = { authViewModel.logout(authToken ?: "") })
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.EthnographiesAdd.path) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        },
        bottomBar = { BottomNavComponent(navController = navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Halo, Selamat Datang!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Etnografi Indonesia",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            ethnographyViewModel.getAllEthnographies(authToken!!, it)
                        },
                        placeholder = { Text("Cari suku atau wilayah...") },
                        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            // List Content
            when (val ethState = uiStateEthnography.ethnographies) {
                is EthnographiesUIState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is EthnographiesUIState.Success -> {
                    if (ethState.data.isEmpty()) {
                        EmptyStateUI()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(ethState.data) { item ->
                                EthnographyCard(item) {
                                    RouteHelper.to(navController, ConstHelper.RouteNames.EthnographiesDetail.path.replace("{id}", item.id))
                                }
                            }
                        }
                    }
                }
                is EthnographiesUIState.Error -> Text("Error: ${ethState.message}", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun EthnographyCard(item: ResponseEthnographyData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ToolsHelper.getEthnographyImage(item.id),
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(org.delcom.pam_2026_ifs23002_proyek1_fe.R.drawable.img_placeholder)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.tribeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = item.region, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(text = item.language, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyStateUI() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tidak ada data ditemukan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
