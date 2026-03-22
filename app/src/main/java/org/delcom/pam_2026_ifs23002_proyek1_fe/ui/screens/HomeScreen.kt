package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_2026_ifs23002_proyek1_fe.R
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ConstHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.RouteHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ToolsHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.ResponseEthnographyData
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.BottomNavComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.EmptyStateUI
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.LoadingUI
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
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

    val userName = (uiStateEthnography.profile as? ProfileUIState.Success)?.data?.name ?: "User"

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
                    ethnographyViewModel.getProfile(dataToken.authToken)
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Etnografi Indonesia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.Profile.path) }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modern Welcome Header
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Halo, $userName!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mari jelajahi kekayaan 1.340 suku bangsa di Nusantara.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Integrated Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    ethnographyViewModel.getAllEthnographies(authToken!!, it)
                                },
                                placeholder = { Text("Cari suku atau wilayah...") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                // Section Title
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Daftar Keseluruhan Suku",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // Main List
                when (val ethState = uiStateEthnography.ethnographies) {
                    is EthnographiesUIState.Loading -> {
                        item(span = { GridItemSpan(2) }) {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is EthnographiesUIState.Success -> {
                        if (ethState.data.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                EmptyStateUI()
                            }
                        } else {
                            items(ethState.data) { item ->
                                FadeInAnimatedVisibility {
                                    EthnographyGridCard(item) {
                                        RouteHelper.to(navController, ConstHelper.RouteNames.EthnographiesDetail.path.replace("{id}", item.id))
                                    }
                                }
                            }
                        }
                    }
                    is EthnographiesUIState.Error -> {
                        item(span = { GridItemSpan(2) }) {
                            Text("Error: ${ethState.message}", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EthnographyGridCard(item: ResponseEthnographyData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                AsyncImage(
                    model = ToolsHelper.getEthnographyImage(item.id),
                    contentDescription = item.tribeName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.img_placeholder)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                                startY = 100f
                            )
                        )
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.tribeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.region,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FadeInAnimatedVisibility(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 })
    ) {
        content()
    }
}
