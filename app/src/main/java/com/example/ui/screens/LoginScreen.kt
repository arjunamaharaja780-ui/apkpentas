package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.model.DefaultData
import com.example.model.UserRole
import com.example.ui.components.DropdownField
import com.example.ui.components.IslamicGeometricBackdrop
import com.example.ui.components.LogoOrEmblem
import com.example.ui.components.MarqueeBanner
import com.example.ui.theme.PaiEmerald100
import com.example.ui.theme.PaiEmerald200
import com.example.ui.theme.PaiEmerald700
import com.example.ui.theme.PaiEmerald900
import com.example.ui.theme.Slate900
import com.example.viewmodel.PentasUiState

@Composable
fun LoginScreen(
    uiState: PentasUiState,
    onRefresh: () -> Unit,
    onLogin: (UserRole, String, String?, String?) -> Unit
) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var selectedSchool by remember { mutableStateOf("") }
    var selectedJuriId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val roleOptions = remember { UserRole.entries.map { it.label } }
    val sortedSchools = remember(uiState.daftarSekolah) { uiState.daftarSekolah.sorted() }
    val juriOptions = remember(uiState.juriList) {
        uiState.juriList.map { j ->
            "${j.nama} - Juri ${j.juriKe} (${DefaultData.normalisasiNamaLomba(j.lomba)})" to j.id
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        IslamicGeometricBackdrop()

        Column(modifier = Modifier.fillMaxSize()) {
            MarqueeBanner(
                runningText = uiState.runningText,
                kecamatan = uiState.namaKecamatan,
                isAdmin = false
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 580.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Emerald Pattern Header Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(PaiEmerald900, PaiEmerald700)
                                    )
                                )
                        ) {
                            IslamicGeometricBackdrop(
                                modifier = Modifier.matchParentSize(),
                                darkMode = true
                            )
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    LogoOrEmblem(logoDataUrl = uiState.appLogoDataUrl, size = 50.dp)
                                    Column {
                                        Text(
                                            text = "PENTAS PAI",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${uiState.namaKecamatan.uppercase()} ${uiState.tahunPelaksanaan}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PaiEmerald200
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Sistem Penilaian Terpadu",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Aplikasi resmi Pentas PAI Kecamatan ${uiState.namaKecamatan} untuk pendaftaran, blind judging dewan juri, dan rekapitulasi nilai real-time.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PaiEmerald100
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (uiState.lastSyncSuccess) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                                contentDescription = null,
                                                tint = PaiEmerald200,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (uiState.isRefreshing) "Menyinkronkan Supabase..."
                                                else if (uiState.lastSyncSuccess) "Terhubung Database Supabase (${uiState.juriList.size} Juri)"
                                                else "Mode Offline / Siap Sinkron",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    TextButton(onClick = onRefresh) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Segarkan",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Segarkan", color = Color.White, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }

                        // Login Form Body
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Masuk Sistem",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Pilih hak akses dan masukkan sandi terdaftar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownField(
                                label = "Hak Akses Pengguna",
                                selectedValue = selectedRole?.label ?: "",
                                placeholder = "-- Pilih Hak Akses --",
                                options = roleOptions,
                                onSelected = { chosen ->
                                    selectedRole = UserRole.entries.firstOrNull { it.label == chosen }
                                },
                                leadingIcon = Icons.Default.Shield,
                                testTagName = "login_role_dropdown"
                            )

                            AnimatedVisibility(visible = selectedRole == UserRole.SEKOLAH) {
                                DropdownField(
                                    label = "Pilih Nama Sekolah Anda",
                                    selectedValue = selectedSchool,
                                    placeholder = "-- Pilih Asal Sekolah --",
                                    options = sortedSchools,
                                    onSelected = { selectedSchool = it },
                                    leadingIcon = Icons.Default.School,
                                    testTagName = "login_school_dropdown"
                                )
                            }

                            AnimatedVisibility(visible = selectedRole == UserRole.JURI) {
                                val selectedLabel = juriOptions.firstOrNull { it.second == selectedJuriId }?.first ?: ""
                                DropdownField(
                                    label = "Pilih Akun Dewan Juri",
                                    selectedValue = selectedLabel,
                                    placeholder = if (juriOptions.isEmpty()) "-- Belum Ada Akun Juri --" else "-- Pilih Akun Juri --",
                                    options = juriOptions.map { it.first },
                                    onSelected = { label ->
                                        selectedJuriId = juriOptions.firstOrNull { it.first == label }?.second ?: ""
                                    },
                                    leadingIcon = Icons.Default.WorkOutline,
                                    testTagName = "login_juri_dropdown"
                                )
                            }

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Kata Sandi") },
                                placeholder = { Text("Masukkan kata sandi...") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Lihat Sandi"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input")
                            )

                            Button(
                                onClick = {
                                    val role = selectedRole ?: return@Button
                                    onLogin(
                                        role,
                                        password,
                                        selectedSchool.takeIf { it.isNotBlank() },
                                        selectedJuriId.takeIf { it.isNotBlank() }
                                    )
                                },
                                enabled = selectedRole != null &&
                                    password.isNotBlank() &&
                                    (selectedRole != UserRole.SEKOLAH || selectedSchool.isNotBlank()) &&
                                    (selectedRole != UserRole.JURI || selectedJuriId.isNotBlank()),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("login_submit_button")
                            ) {
                                Text(
                                    text = "Masuk Sistem",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
