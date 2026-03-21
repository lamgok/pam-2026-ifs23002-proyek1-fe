package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_2026_ifs23002_proyek1_fe.R
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ConstHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.RouteHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ToolsHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.ResponseUserData
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.BottomNavComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.LoadingUI
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.TopAppBarComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.TopAppBarMenuItem
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.*

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    ethnographyViewModel: EthnographyViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateEthnography by ethnographyViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<ResponseUserData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && authToken != null) {
            val part = ToolsHelper.uriToMultipart(context, uri, "photo")
            ethnographyViewModel.putUserMePhoto(authToken!!, part)
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        if(uiStateAuth.auth !is AuthUIState.Success){
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        ethnographyViewModel.getProfile(authToken ?: "")
    }

    LaunchedEffect(uiStateEthnography.profile) {
        if(uiStateEthnography.profile !is ProfileUIState.Loading){
            isLoading = false
            if(uiStateEthnography.profile is ProfileUIState.Success){
                profile = (uiStateEthnography.profile as ProfileUIState.Success).data
            }
        }
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading && uiStateAuth.authLogout !is AuthLogoutUIState.Idle) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if(isLoading || profile == null){
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(text = "Logout", icon = Icons.AutoMirrored.Filled.Logout, route = null, onClick = { authViewModel.logout(authToken ?: "") })
    )

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        TopAppBarComponent(navController = navController, title = "Profile", showBackButton = false, customMenuItems = menuItems)
        Box(modifier = Modifier.weight(1f)) {
            ProfileUI(profile = profile!!, onEditPhoto = { launcher.launch("image/*") })
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun ProfileUI(profile: ResponseUserData, onEditPhoto: () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Profile Image Section
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = ToolsHelper.getUserImage(profile.id),
                    contentDescription = "Photo Profil",
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder),
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                IconButton(
                    onClick = onEditPhoto,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit Photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name and Username
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "@${profile.username}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info Cards
        Text(
            text = "Informasi Akun",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        InfoItem(icon = Icons.Default.Person, label = "Nama Lengkap", value = profile.name)
        InfoItem(icon = Icons.Default.Email, label = "Username", value = profile.username)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Version or Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Status Akun",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Aktif (Administrator)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
