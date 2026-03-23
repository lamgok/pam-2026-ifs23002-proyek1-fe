package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens

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
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.theme.EthnoGold
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.theme.EthnoMaroon
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBarComponent(navController = navController, title = "Profile", showBackButton = false)
        Box(modifier = Modifier.weight(1f)) {
            ProfileUI(
                profile = profile!!, 
                onEditPhoto = { launcher.launch("image/*") },
                onLogout = { authViewModel.logout(authToken ?: "") }
            )
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun ProfileUI(profile: ResponseUserData, onEditPhoto: () -> Unit, onLogout: () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Header: Foto profil lingkaran besar di tengah dengan border gradasi
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = ToolsHelper.getUserImage(profile.id),
                contentDescription = "Photo Profil",
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(colors = listOf(EthnoMaroon, EthnoGold)),
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
            
            SmallFloatingActionButton(
                onClick = onEditPhoto,
                containerColor = EthnoMaroon,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Edit Photo", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Info: Nama dan username di bawah foto
        Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "@${profile.username}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bento Cards Grid
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            BentoCard(
                modifier = Modifier.weight(1.5f),
                title = "Username",
                value = profile.username,
                icon = Icons.Default.Badge,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "Role",
                value = "Administrator", // Hardcoded as per existing UI logic
                icon = Icons.Default.AdminPanelSettings,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "Status",
                value = "Aktif",
                icon = Icons.Default.VerifiedUser,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BentoCard(
                modifier = Modifier.weight(1.5f),
                title = "Bergabung",
                value = profile.createdAt.split("T").firstOrNull() ?: "-",
                icon = Icons.Default.CalendarToday,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Action: Tombol Logout
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EthnoMaroon)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
