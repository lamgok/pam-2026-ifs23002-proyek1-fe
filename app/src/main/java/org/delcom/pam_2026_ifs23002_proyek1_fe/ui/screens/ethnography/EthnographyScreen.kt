package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.ethnography

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23002_proyek1_fe.R
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ConstHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.RouteHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ToolsHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.ResponseEthnographyData
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.BottomNavComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.LoadingUI
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.TopAppBarComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.TopAppBarMenuItem
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.theme.EthnoGold
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.theme.EthnoMaroon
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EthnographyScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    viewModel: EthnographyViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val authToken = (uiStateAuth.auth as? AuthUIState.Success)?.data?.authToken ?: ""

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val regions = listOf("Semua", "Sumatera", "Jawa", "Kalimantan", "Sulawesi", "Papua", "Bali", "Nusa Tenggara", "Maluku")

    // Infinite Scroll logic
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = gridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItemsCount - 2 && totalItemsCount > 0
        }
    }

    LaunchedEffect(Unit) {
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        viewModel.getAllEthnographies(authToken, searchQuery.text, uiState.selectedRegion)
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.ethnographies is EthnographiesUIState.Success) {
            viewModel.loadNextPage(authToken)
        }
    }

    val menuItems = listOf(
        TopAppBarMenuItem(text = "Profile", icon = Icons.Filled.Person, route = ConstHelper.RouteNames.Profile.path),
        TopAppBarMenuItem(text = "Logout", icon = Icons.AutoMirrored.Filled.Logout, route = null, onClick = { authViewModel.logout(authToken) })
    )

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBarComponent(
            navController = navController,
            title = "Daftar Etnografi",
            showBackButton = false,
            customMenuItems = menuItems,
            withSearch = true,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearchAction = { viewModel.getAllEthnographies(authToken, searchQuery.text, uiState.selectedRegion) }
        )

        // Region Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(regions) { region ->
                FilterChip(
                    selected = uiState.selectedRegion == region,
                    onClick = {
                        viewModel.getAllEthnographies(authToken, searchQuery.text, region)
                        coroutineScope.launch {
                            gridState.animateScrollToItem(0)
                        }
                    },
                    label = { Text(region) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EthnoMaroon,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState.ethnographies) {
                is EthnographiesUIState.Loading -> LoadingUI()
                is EthnographiesUIState.Error -> {
                    Text(text = state.message, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                }
                is EthnographiesUIState.Success -> {
                    if (state.data.isEmpty()) {
                        Text(text = "Data tidak ditemukan.", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            state = gridState,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                        ) {
                            items(state.data, key = { it.id }) { item ->
                                Column {
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 50 })
                                    ) {
                                        EthnographyItemUI(item) {
                                            RouteHelper.to(navController, "ethnographies/${item.id}")
                                        }
                                    }
                                }
                            }
                            
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = EthnoGold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.EthnographiesAdd.path) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun EthnographyItemUI(item: ResponseEthnographyData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ToolsHelper.getEthnographyImage(item.id, item.updatedAt),
                contentDescription = item.tribeName,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.tribeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = item.region, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
