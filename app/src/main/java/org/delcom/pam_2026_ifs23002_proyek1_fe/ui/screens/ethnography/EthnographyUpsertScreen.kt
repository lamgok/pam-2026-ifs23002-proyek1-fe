package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.screens.ethnography

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.ToolsHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.LoadingUI
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components.TopAppBarComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels.*

@Composable
fun EthnographyUpsertScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    viewModel: EthnographyViewModel,
    id: String? = null
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form State
    var tribeName by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var traditionalHouse by remember { mutableStateOf("") }
    var traditionalWeapon by remember { mutableStateOf("") }
    var beliefSystem by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var isProcessing by remember { mutableStateOf(false) }

    val isEdit = id != null
    val authToken = (uiStateAuth.auth as? AuthUIState.Success)?.data?.authToken ?: ""

    // Initial Load for Edit Mode
    LaunchedEffect(id) {
        if (isEdit && authToken.isNotEmpty()) {
            viewModel.getEthnographyById(authToken, id!!)
        }
    }

    // Populate form when data is loaded
    LaunchedEffect(uiState.ethnography) {
        if (isEdit && uiState.ethnography is EthnographyUIState.Success) {
            val data = (uiState.ethnography as EthnographyUIState.Success).data
            tribeName = data.tribeName
            region = data.region
            language = data.language
            traditionalHouse = data.traditionalHouse
            traditionalWeapon = data.traditionalWeapon
            beliefSystem = data.beliefSystem
            description = data.description
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedImageUri = uri
    }

    fun handleSave() {
        if (isProcessing) return
        
        // Simple Validation
        if (tribeName.isBlank() || region.isBlank() || description.isBlank()) {
            scope.launch { snackbarHost.showSnackbar("Nama Suku, Wilayah, dan Deskripsi wajib diisi") }
            return
        }

        if (authToken.isEmpty()) {
            scope.launch { snackbarHost.showSnackbar("Sesi berakhir, silakan login kembali") }
            return
        }
        
        isProcessing = true
        viewModel.upsert(
            authToken, id, tribeName, region, language, 
            traditionalHouse, traditionalWeapon, beliefSystem, description
        )
    }

    // Handle Upsert Success/Error
    LaunchedEffect(uiState.ethnographyAdd, uiState.ethnographyChange) {
        val addState = uiState.ethnographyAdd
        val changeState = uiState.ethnographyChange

        if (addState is EthnographyActionUIState.Success || changeState is EthnographyActionUIState.Success) {
            val successId = (addState as? EthnographyActionUIState.Success)?.id ?: id
            
            // If image selected, upload it next
            if (selectedImageUri != null && successId != null) {
                try {
                    val part = ToolsHelper.uriToMultipart(context, selectedImageUri!!, "image")
                    viewModel.putEthnographyImage(authToken, successId, part)
                } catch (e: Exception) {
                    isProcessing = false
                    scope.launch { snackbarHost.showSnackbar("Gagal memproses gambar: ${e.message}") }
                }
            } else {
                // No image to upload, finish process
                isProcessing = false
                val msg = if (isEdit) "Etnografi berhasil diperbarui" else "Etnografi berhasil ditambahkan"
                scope.launch { 
                    snackbarHost.showSnackbar(msg)
                    viewModel.resetActionStates()
                    viewModel.getAllEthnographies(authToken)
                    navController.popBackStack()
                }
            }
        } else if (addState is EthnographyActionUIState.Error || changeState is EthnographyActionUIState.Error) {
            isProcessing = false
            val errorMsg = (addState as? EthnographyActionUIState.Error)?.message ?: (changeState as? EthnographyActionUIState.Error)?.message ?: "Gagal menyimpan data"
            scope.launch { snackbarHost.showSnackbar(errorMsg) }
            viewModel.resetActionStates()
        }
    }

    // Handle Image Upload Success/Error
    LaunchedEffect(uiState.ethnographyChangeImage) {
        val imageState = uiState.ethnographyChangeImage
        if (imageState is EthnographyActionUIState.Success) {
            isProcessing = false
            scope.launch { 
                snackbarHost.showSnackbar("Data dan gambar berhasil disimpan")
                viewModel.resetActionStates()
                viewModel.getAllEthnographies(authToken)
                navController.popBackStack()
            }
        } else if (imageState is EthnographyActionUIState.Error) {
            isProcessing = false
            scope.launch { snackbarHost.showSnackbar("Gagal mengunggah gambar: ${imageState.message}") }
            viewModel.resetActionStates()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHost) },
        topBar = { TopAppBarComponent(navController, if (isEdit) "Edit Etnografi" else "Tambah Etnografi") }
    ) { padding ->
        if (isProcessing || (uiState.ethnography is EthnographyUIState.Loading && isEdit)) {
            LoadingUI()
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Area Gambar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { launcher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (isEdit && id != null) {
                            AsyncImage(
                                model = ToolsHelper.getEthnographyImage(id),
                                contentDescription = "Current Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp))
                                Text("Pilih Gambar", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Field Input
                OutlinedTextField(value = tribeName, onValueChange = { tribeName = it }, label = { Text("Nama Suku *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("Wilayah *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = language, onValueChange = { language = it }, label = { Text("Bahasa Daerah") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = traditionalHouse, onValueChange = { traditionalHouse = it }, label = { Text("Rumah Adat") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = traditionalWeapon, onValueChange = { traditionalWeapon = it }, label = { Text("Senjata Tradisional") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = beliefSystem, onValueChange = { beliefSystem = it }, label = { Text("Sistem Kepercayaan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi Budaya *") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = RoundedCornerShape(8.dp))

                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { handleSave() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isProcessing && tribeName.isNotBlank() && region.isNotBlank() && description.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEdit) "Perbarui" else "Simpan", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
