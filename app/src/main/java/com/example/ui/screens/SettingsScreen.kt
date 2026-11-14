package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.model.DefaultData
import com.example.model.ResetDataMode
import com.example.ui.components.DropdownField
import com.example.ui.components.LogoOrEmblem
import com.example.ui.components.SectionHeader
import com.example.ui.theme.Cyan100
import com.example.ui.theme.Cyan700
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo700
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PaiEmerald100
import com.example.ui.theme.PaiEmerald600
import com.example.ui.theme.PaiEmerald700
import com.example.ui.theme.PaiGold100
import com.example.ui.theme.PaiGold500
import com.example.ui.theme.PaiGold700
import com.example.ui.theme.Purple700
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose700
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.util.PrintAndExportHelper
import com.example.viewmodel.PentasUiState
import com.example.viewmodel.PentasViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    uiState: PentasUiState,
    viewModel: PentasViewModel,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var dbUrlInput by remember(uiState.dbUrl) { mutableStateOf(uiState.dbUrl) }
    var dbKeyInput by remember(uiState.dbAnonKey) { mutableStateOf(uiState.dbAnonKey) }
    var dbKeyVisible by remember { mutableStateOf(false) }

    var runningTextInput by remember(uiState.runningText) { mutableStateOf(uiState.runningText) }

    // Dialog states
    var showResetDataDialog by remember { mutableStateOf(false) }
    var showKecamatanDialog by remember { mutableStateOf(false) }
    var showTahunDialog by remember { mutableStateOf(false) }
    var editRoleTarget by remember { mutableStateOf<String?>(null) }
    var showTambahJuriDialog by remember { mutableStateOf(false) }
    var showTambahSekolahDialog by remember { mutableStateOf(false) }
    var showTambahLombaDialog by remember { mutableStateOf(false) }

    // SAF & Photo Picker Launchers (zero broad storage permissions)
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val base64 = PrintAndExportHelper.encodePickedImageToBase64(context, uri)
            if (base64 != null) viewModel.uploadLogoBase64(base64)
            else onShowMessage("Gagal memproses gambar logo.")
        }
    }

    val backupJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonStr = viewModel.generateBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(jsonStr.toByteArray(Charsets.UTF_8))
                    }
                    onShowMessage("Cadangan JSON berhasil diunduh!")
                } catch (e: Exception) {
                    onShowMessage("Gagal membuat cadangan: ${e.message}")
                }
            }
        }
    }

    val restoreJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (raw.isNotBlank()) viewModel.restoreBackupJson(raw)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Pengaturan & Reset Data",
            subtitle = "Konfigurasi koneksi Supabase, cadangan data, identitas kegiatan, akun juri, sekolah, dan cabang lomba.",
            badge = "Administrator"
        )

        // 1. Database Supabase Config
        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Cyan700.copy(alpha = 0.25f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = Cyan700)
                        Text("Koneksi Database Supabase", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    }
                    Surface(
                        color = if (uiState.isCustomDb) Cyan100 else PaiEmerald100,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (uiState.isCustomDb) "Kustom" else "Default",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isCustomDb) Cyan700 else PaiEmerald700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = dbUrlInput,
                    onValueChange = { dbUrlInput = it },
                    label = { Text("URL API (Supabase Project URL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dbKeyInput,
                    onValueChange = { dbKeyInput = it },
                    label = { Text("Anon Public Key") },
                    visualTransformation = if (dbKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { dbKeyVisible = !dbKeyVisible }) {
                            Icon(if (dbKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle Key")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { viewModel.testKoneksiDatabase(dbUrlInput, dbKeyInput) },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.SettingsEthernet, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tes")
                    }
                    OutlinedButton(
                        onClick = { viewModel.resetDatabaseDefault() },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Reset")
                    }
                    Button(
                        onClick = { viewModel.simpanDatabaseConfig(dbUrlInput, dbKeyInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan700),
                        modifier = Modifier.weight(1.4f).height(48.dp)
                    ) {
                        Text("Hubungkan")
                    }
                }
            }
        }

        // 2. Backup & Restore + Danger Zone Reset
        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Indigo700.copy(alpha = 0.25f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Cadangkan & Pulihkan Database", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            backupJsonLauncher.launch("Backup_Pentas_PAI_${uiState.namaKecamatan}_${uiState.tahunPelaksanaan}.json")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo700),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup JSON")
                    }
                    OutlinedButton(
                        onClick = { restoreJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore")
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Rose700.copy(alpha = 0.35f)),
            colors = CardDefaults.cardColors(containerColor = Rose100.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zona Berbahaya — Reset Semua Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Rose700)
                Text(
                    text = "Kosongkan seluruh nilai juri, hapus data peserta, atau reset penuh data aplikasi untuk memulai tahun lomba baru.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
                Button(
                    onClick = { showResetDataDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose700),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_reset_all_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Semua Data...")
                }
            }
        }

        // 3. Kecamatan, Tahun & Logo Aplikasi
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Identitas Kegiatan & Logo Resmi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showKecamatanDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(uiState.namaKecamatan, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { showTahunDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tahun ${uiState.tahunPelaksanaan}")
                    }
                }
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LogoOrEmblem(logoDataUrl = uiState.appLogoDataUrl, size = 56.dp, containerColor = PaiEmerald100, iconTint = PaiEmerald700)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Logo Kop Cetak & Header", style = MaterialTheme.typography.labelLarge)
                        Text("Pilih gambar PNG/JPG via Android Photo Picker.", style = MaterialTheme.typography.labelMedium, color = Slate500)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
                        modifier = Modifier.weight(1.3f).height(48.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Logo")
                    }
                    if (uiState.appLogoDataUrl.isNotBlank()) {
                        OutlinedButton(
                            onClick = { viewModel.hapusLogo() },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Reset Logo", color = Rose700)
                        }
                    }
                }
            }
        }

        // 4. Running Text
        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, PaiGold500.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = PaiGold700)
                    Text("Pengaturan Running Text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
                OutlinedTextField(
                    value = runningTextInput,
                    onValueChange = { runningTextInput = it },
                    label = { Text("Isi Pengumuman Berjalan") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val def = DefaultData.defaultRunningText(uiState.namaKecamatan, uiState.tahunPelaksanaan)
                            runningTextInput = def
                            viewModel.saveRunningText(def)
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Reset Default")
                    }
                    Button(
                        onClick = { viewModel.saveRunningText(runningTextInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = PaiGold500, contentColor = Slate900),
                        modifier = Modifier.weight(1.3f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Perbarui Teks")
                    }
                }
            }
        }

        // 5. Role Access Passwords
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Manajemen Sandi Hak Akses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                uiState.aksesList.forEach { a ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.role.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = a.password,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = PaiEmerald700
                            )
                        }
                        OutlinedButton(onClick = { editRoleTarget = a.role }) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ubah")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        // 6. Akun Dewan Juri
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Akun Dewan Juri (${uiState.juriList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Button(
                        onClick = { showTambahJuriDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Juri")
                    }
                }
                uiState.juriList.forEach { j ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(j.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Juri ${j.juriKe} • ${j.lomba}", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                            Text(
                                text = "Sandi: ${j.password}",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = Purple700
                            )
                        }
                        IconButton(
                            onClick = { viewModel.hapusJuri(j.id) },
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Rose100)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Juri", tint = Rose700, modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        // 7. Manajemen Daftar Sekolah
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daftar Sekolah (${uiState.daftarSekolah.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Button(
                        onClick = { showTambahSekolahDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sekolah")
                    }
                }
                uiState.daftarSekolah.sorted().forEach { sek ->
                    val count = uiState.pesertaList.count { it.sekolah == sek }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sek, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "No. Tampil: ${DefaultData.formatDuaDigit(uiState.noUrutMap[sek] ?: "-")} • $count Peserta",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate500
                            )
                        }
                        IconButton(
                            onClick = { viewModel.hapusSekolah(sek) },
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Rose100)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Sekolah", tint = Rose700, modifier = Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        // 8. Manajemen Cabang Lomba
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cabang Lomba (${uiState.kategoriLomba.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Button(
                        onClick = { showTambahLombaDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo700)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lomba")
                    }
                }
                uiState.kategoriLomba.forEachIndexed { idx, kat ->
                    val clean = DefaultData.normalisasiNamaLomba(kat.nama)
                    val pCount = uiState.pesertaList.count { DefaultData.normalisasiNamaLomba(it.lomba) == clean }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(clean, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Kuota: ${kat.maxPeserta} Peserta • Pendaftar: $pCount", style = MaterialTheme.typography.labelMedium, color = Slate500)
                        }
                        IconButton(
                            onClick = { viewModel.hapusKategoriLomba(idx) },
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Rose100)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Cabang", tint = Rose700, modifier = Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    // Dialogs
    if (showResetDataDialog) {
        var selectedMode by remember { mutableStateOf(ResetDataMode.NILAI_SAJA) }
        var confirmWord by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showResetDataDialog = false },
            title = { Text("Pilih Jenis Reset Data Lomba") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResetDataMode.entries.forEach { mode ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (selectedMode == mode) Rose700 else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth().clickable { selectedMode = mode }
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                RadioButton(selected = selectedMode == mode, onClick = { selectedMode = mode })
                                Column {
                                    Text(mode.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text(mode.description, style = MaterialTheme.typography.bodyMedium, color = Slate500)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = confirmWord,
                        onValueChange = { confirmWord = it },
                        label = { Text("Ketik RESET untuk konfirmasi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeResetData(selectedMode)
                        showResetDataDialog = false
                    },
                    enabled = confirmWord.trim().equals("RESET", ignoreCase = true),
                    colors = ButtonDefaults.buttonColors(containerColor = Rose700)
                ) { Text("Eksekusi Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDataDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showKecamatanDialog) {
        var input by remember { mutableStateOf(uiState.namaKecamatan) }
        AlertDialog(
            onDismissRequest = { showKecamatanDialog = false },
            title = { Text("Ubah Nama Kecamatan") },
            text = {
                OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("Nama Kecamatan") }, singleLine = true)
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateNamaKecamatan(input)
                    showKecamatanDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showKecamatanDialog = false }) { Text("Batal") } }
        )
    }

    if (showTahunDialog) {
        var input by remember { mutableStateOf(uiState.tahunPelaksanaan) }
        AlertDialog(
            onDismissRequest = { showTahunDialog = false },
            title = { Text("Ubah Tahun Pelaksanaan") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Tahun") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateTahunPelaksanaan(input)
                    showTahunDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showTahunDialog = false }) { Text("Batal") } }
        )
    }

    editRoleTarget?.let { role ->
        var newPwd by remember(role) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { editRoleTarget = null },
            title = { Text("Ubah Sandi (${role.uppercase()})") },
            text = {
                OutlinedTextField(value = newPwd, onValueChange = { newPwd = it }, label = { Text("Kata Sandi Baru") }, singleLine = true)
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.editAksesPassword(role, newPwd)
                    editRoleTarget = null
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { editRoleTarget = null }) { Text("Batal") } }
        )
    }

    if (showTambahJuriDialog) {
        var nama by remember { mutableStateOf("") }
        val lombaOpts = uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
        var lomba by remember { mutableStateOf(lombaOpts.firstOrNull().orEmpty()) }
        var juriKe by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showTambahJuriDialog = false },
            title = { Text("Tambah Akun Juri") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap Juri") }, singleLine = true)
                    DropdownField(label = "Mata Lomba", selectedValue = lomba, options = lombaOpts, onSelected = { lomba = it })
                    DropdownField(label = "Posisi Juri", selectedValue = "Juri $juriKe", options = listOf("Juri 1", "Juri 2", "Juri 3"), onSelected = { juriKe = it.last().toString() })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.tambahJuri(nama, lomba, juriKe.toIntOrNull() ?: 1)
                    showTambahJuriDialog = false
                }) { Text("Buat Akun") }
            },
            dismissButton = { TextButton(onClick = { showTambahJuriDialog = false }) { Text("Batal") } }
        )
    }

    if (showTambahSekolahDialog) {
        var nama by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTambahSekolahDialog = false },
            title = { Text("Tambah Sekolah Baru") },
            text = {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Sekolah / Madrasah") }, singleLine = true)
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.tambahSekolah(nama)
                    showTambahSekolahDialog = false
                }) { Text("Tambah") }
            },
            dismissButton = { TextButton(onClick = { showTambahSekolahDialog = false }) { Text("Batal") } }
        )
    }

    if (showTambahLombaDialog) {
        var nama by remember { mutableStateOf("") }
        var kuota by remember { mutableStateOf("1") }
        var desc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTambahLombaDialog = false },
            title = { Text("Tambah Cabang Lomba") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Cabang Lomba") }, singleLine = true)
                    OutlinedTextField(
                        value = kuota,
                        onValueChange = { kuota = it },
                        label = { Text("Kuota Peserta (1 atau 3)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Singkat") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.tambahKategoriLomba(nama, kuota.toIntOrNull() ?: 1, desc)
                    showTambahLombaDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showTambahLombaDialog = false }) { Text("Batal") } }
        )
    }
}
