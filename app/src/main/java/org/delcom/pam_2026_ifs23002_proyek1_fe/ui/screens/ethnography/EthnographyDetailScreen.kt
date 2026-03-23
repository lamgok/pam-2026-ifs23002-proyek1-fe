package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.ethnography

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_2026_ifs23002_proyek1_fe.R
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.RouteHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ToolsHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.LoadingUI
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
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
    val scrollState = rememberScrollState()

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

    val data = (uiState.ethnography as? EthnographyUIState.Success)?.data

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (data != null) {
                Column(horizontalAlignment = Alignment.End) {
                    FloatingActionButton(
                        onClick = { RouteHelper.to(navController, "ethnographies/${id}/edit") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 12.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    FloatingActionButton(
                        onClick = { viewModel.deleteEthnography(authToken, id) },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (data != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Immersive Hero Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        AsyncImage(
                            model = ToolsHelper.getEthnographyImage(data.id, data.updatedAt),
                            contentDescription = data.tribeName,
                            placeholder = painterResource(R.drawable.img_placeholder),
                            error = painterResource(R.drawable.img_placeholder),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)
                                        ),
                                        startY = 500f
                                    )
                                )
                        )
                        // Title on top of image
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                                .padding(bottom = 32.dp)
                        ) {
                            Text(
                                text = data.tribeName,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 32.sp
                                ),
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = data.region,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    // Overlapping Info Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-32).dp),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .padding(top = 8.dp)
                        ) {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
                            ) {
                                Column {
                                    // Bento Grid for Stats
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        BentoItem(
                                            icon = Icons.Default.Translate,
                                            label = "Bahasa",
                                            value = data.language,
                                            modifier = Modifier.weight(1f)
                                        )
                                        BentoItem(
                                            icon = Icons.Default.Home,
                                            label = "Rumah Adat",
                                            value = data.traditionalHouse,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        BentoItem(
                                            icon = Icons.Default.Shield,
                                            label = "Senjata",
                                            value = data.traditionalWeapon,
                                            modifier = Modifier.weight(1f)
                                        )
                                        BentoItem(
                                            icon = Icons.Default.Church,
                                            label = "Kepercayaan",
                                            value = data.beliefSystem,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    EthnicDivider()

                                    Text(
                                        text = "Deskripsi Budaya",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif
                                        ),
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                    
                                    Text(
                                        text = data.description,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            lineHeight = 28.sp,
                                            textAlign = TextAlign.Justify
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(100.dp))
                                }
                            }
                        }
                    }
                }

                // Transparent Top Bar that adapts
                val alpha by remember {
                    derivedStateOf {
                        if (scrollState.value > 100) 1f else 0f
                    }
                }
                
                TopAppBar(
                    title = {
                        if (alpha > 0.5f) {
                            Text(data.tribeName, fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.background(
                                color = Color.Black.copy(alpha = 0.3f * (1f - alpha)),
                                shape = CircleShape
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (alpha > 0.5f) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            } else if (uiState.ethnography is EthnographyUIState.Error) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = (uiState.ethnography as EthnographyUIState.Error).message)
                }
            }
        }
    }
}

@Composable
fun BentoItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EthnicDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Icon(
            imageVector = Icons.Default.FilterList, // Using as a proxy for an ethnic pattern icon
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
