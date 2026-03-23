package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

// Define Slate Palette (Tailwind Inspired)
private val Slate50 = Color(0xFFF8FAFC)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate900 = Color(0xFF0F172A)
private val Slate950 = Color(0xFF020617)
private val Slate800 = Color(0xFF1E293B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    ethnographyViewModel: EthnographyViewModel,
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateEthnography by ethnographyViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var isFreshToken by remember { mutableStateOf(false) }
    var authToken by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val userName = (uiStateEthnography.profile as? ProfileUIState.Success)?.data?.name ?: "User"

    // Colors based on theme
    val bgColor = if (isDark) Slate950 else Slate50
    val textColor = if (isDark) Slate100 else Slate900
    val cardBg = if (isDark) Slate900 else Color.White
    val borderColor = if (isDark) Slate800 else Slate200

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
        containerColor = bgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Etnografi Indonesia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Slate800 else Slate200)
                    ) {
                        AnimatedContent(
                            targetState = isDark,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(500)) + rotateIn() togetherWith
                                        fadeOut(animationSpec = tween(500)) + rotateOut()
                            }, label = "ThemeToggle"
                        ) { dark ->
                            Icon(
                                imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (dark) Color(0xFFFFD600) else Slate900,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.Profile.path) }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = bgColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.EthnographiesAdd.path) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        },
        bottomBar = { 
            Surface(shadowElevation = 8.dp) {
                BottomNavComponent(navController = navController)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderColor, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Etnografi Indonesia v2.0",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Halo, $userName!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "Jelajahi keberagaman budaya Nusantara.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    ethnographyViewModel.getAllEthnographies(authToken!!, it)
                                },
                                placeholder = { Text("Cari suku atau wilayah...", color = textColor.copy(alpha = 0.4f)) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = bgColor,
                                    unfocusedContainerColor = bgColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = borderColor,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Katalog Suku",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Lihat Semua",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { }
                        )
                    }
                }

                when (val ethState = uiStateEthnography.ethnographies) {
                    is EthnographiesUIState.Loading -> {
                        items(6) { SkeletonCard(borderColor) }
                    }
                    is EthnographiesUIState.Success -> {
                        if (ethState.data.isEmpty()) {
                            item(span = { GridItemSpan(2) }) { EmptyStateUI() }
                        } else {
                            items(ethState.data) { item ->
                                FadeInAnimatedVisibility {
                                    EthnographyGridCard(item, cardBg, borderColor, textColor) {
                                        RouteHelper.to(navController, ConstHelper.RouteNames.EthnographiesDetail.path.replace("{id}", item.id))
                                    }
                                }
                            }
                        }
                    }
                    is EthnographiesUIState.Error -> {
                        item(span = { GridItemSpan(2) }) {
                            Text("Error: ${ethState.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EthnographyGridCard(
    item: ResponseEthnographyData, 
    cardBg: Color, 
    borderColor: Color, 
    textColor: Color, 
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 200f
                            )
                        )
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.tribeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.region,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SkeletonCard(borderColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "alpha"
    )
    Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp)).background(borderColor.copy(alpha = alpha)))
}

fun rotateIn() = fadeIn() + expandIn()
fun rotateOut() = fadeOut() + shrinkOut()

@Composable
fun FadeInAnimatedVisibility(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { it / 3 })
    ) {
        content()
    }
}
