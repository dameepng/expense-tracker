package com.example.expense_tracker.ui.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.example.expense_tracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
    onLogoutSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = uiState.userName,
            currentPhotoUri = uiState.userPhotoUri,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, photoUri ->
                viewModel.updateProfile(name, photoUri)
                showEditProfileDialog = false
            }
        )
    }

    if (showThemeDialog) {
        val options = listOf("System Default", "Light Mode", "Dark Mode")
        SingleChoiceDialog(
            title = stringResource(R.string.profile_select_theme),
            options = options,
            selectedOption = uiState.themeMode,
            onOptionSelected = { viewModel.setThemeMode(it) },
            onDismissRequest = { showThemeDialog = false }
        )
    }

    if (showCurrencyDialog) {
        val options = listOf("IDR", "USD", "EUR")
        SingleChoiceDialog(
            title = stringResource(R.string.profile_select_currency),
            options = options,
            selectedOption = uiState.currency,
            onOptionSelected = { viewModel.setCurrency(it) },
            onDismissRequest = { showCurrencyDialog = false }
        )
    }

    if (showLanguageDialog) {
        val options = listOf("Indonesia", "English")
        SingleChoiceDialog(
            title = stringResource(R.string.profile_select_language),
            options = options,
            selectedOption = uiState.language,
            onOptionSelected = { viewModel.setLanguage(it) },
            onDismissRequest = { showLanguageDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_profile), fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ProfileHeader(
                    name = uiState.userName,
                    photoUri = uiState.userPhotoUri,
                    onEditClick = { showEditProfileDialog = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                SettingsGroup(title = stringResource(R.string.profile_preferences)) {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.profile_theme),
                        subtitle = uiState.themeMode,
                        onClick = { showThemeDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.AttachMoney,
                        title = stringResource(R.string.profile_currency),
                        subtitle = uiState.currency,
                        onClick = { showCurrencyDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.profile_language),
                        subtitle = uiState.language,
                        onClick = { showLanguageDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                SettingsGroup(title = stringResource(R.string.profile_security_data)) {
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = stringResource(R.string.profile_export_data),
                        subtitle = "CSV",
                        onClick = { viewModel.exportData(context) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.profile_screen_lock),
                        subtitle = "Biometric / PIN",
                        onClick = { 
                            val fragmentActivity = context as? androidx.fragment.app.FragmentActivity
                            if (fragmentActivity != null) {
                                com.example.expense_tracker.utils.BiometricHelper.authenticate(
                                    activity = fragmentActivity,
                                    onSuccess = {
                                        viewModel.setBiometricsEnabled(!uiState.isBiometricsEnabled)
                                    },
                                    onError = { error ->
                                        android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                android.widget.Toast.makeText(context, "FragmentActivity not found", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        trailingComponent = {
                            Switch(
                                checked = uiState.isBiometricsEnabled,
                                onCheckedChange = { isChecked -> 
                                    val fragmentActivity = context as? androidx.fragment.app.FragmentActivity
                                    if (fragmentActivity != null) {
                                        com.example.expense_tracker.utils.BiometricHelper.authenticate(
                                            activity = fragmentActivity,
                                            onSuccess = {
                                                viewModel.setBiometricsEnabled(isChecked)
                                            },
                                            onError = { error ->
                                                android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        android.widget.Toast.makeText(context, "FragmentActivity not found", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                SettingsGroup(title = "Bantuan & Informasi") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Pusat Bantuan / FAQ",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/search?q=kasflow+help"))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Browser tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Beri Rating Aplikasi",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val webIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                try {
                                    context.startActivity(webIntent)
                                } catch (e2: Exception) {
                                    android.widget.Toast.makeText(context, "Aplikasi Play Store tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Kebijakan Privasi",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/search?q=kasflow+privacy+policy"))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Browser tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                SettingsGroup(title = "Danger Zone") {
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Keluar Akun",
                        subtitle = "Anda harus login kembali nanti",
                        titleColor = MaterialTheme.colorScheme.error,
                        iconColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            viewModel.logout(context, onLogoutSuccess)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                // Footer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kasflow v1.0.0",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    name: String,
    photoUri: String?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Box
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (photoUri != null && photoUri.isNotEmpty()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
            
            // Edit Badge
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
    }
}

@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    trailingComponent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.background
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Texts
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Trailing
        if (trailingComponent != null) {
            trailingComponent()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SingleChoiceDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                onDismissRequest()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentName: String,
    currentPhotoUri: String?,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var photoUri by remember { mutableStateOf(currentPhotoUri) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Profil")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val context = LocalContext.current
                val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri != null) {
                        try {
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        photoUri = uri.toString()
                    }
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null && photoUri!!.isNotEmpty()) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Add Profile Picture",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Text(
                    text = "Ketuk untuk ubah foto",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, photoUri) }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Batal")
            }
        }
    )
}

