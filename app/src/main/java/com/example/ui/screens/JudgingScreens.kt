package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.DefaultData
import com.example.model.KriteriaItem
import com.example.model.LombaPenilaianConfig
import com.example.model.Peserta
import com.example.model.UserRole
import com.example.ui.components.DropdownField
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.KoreksiNilaiDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue700
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo700
import com.example.ui.theme.PaiEmerald100
import com.example.ui.theme.PaiEmerald50
import com.example.ui.theme.PaiEmerald600
import com.example.ui.theme.PaiEmerald700
import com.example.ui.theme.PaiEmerald800
import com.example.ui.theme.PaiGold100
import com.example.ui.theme.PaiGold50
import com.example.ui.theme.PaiGold500
import com.example.ui.theme.PaiGold600
import com.example.ui.theme.PaiGold700
import com.example.ui.theme.Purple100
import com.example.ui.theme.Purple700
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.viewmodel.PentasUiState
import com.example.viewmodel.PentasViewModel

@Composable
fun PenilaianScreen(
    uiState: PentasUiState,
    viewModel: PentasViewModel,
    onShowError: (String) -> Unit
) {
    val isJuri = uiState.session?.role == UserRole.JURI
    val juriPos = uiState.session?.juriKe ?: 1
    val lombaOptions = remember(uiState.kategoriLomba) {
        uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
    }

    var selectedLomba by remember(isJuri, uiState.session?.juriLomba) {
        mutableStateOf(
            if (isJuri) DefaultData.normalisasiNamaLomba(uiState.session?.juriLomba)
            else lombaOptions.firstOrNull().orEmpty()
        )
    }

    val isLcc = selectedLomba.uppercase().contains("CERDAS CERMAT")
    val pesertaCabang = remember(uiState.pesertaList, selectedLomba) {
        uiState.pesertaList
            .filter { DefaultData.normalisasiNamaLomba(it.lomba) == selectedLomba }
            .sortedWith(DefaultData::compareNoUrut)
    }

    var selectedPesertaId by remember(selectedLomba) { mutableStateOf("") }
    val selectedPeserta = remember(pesertaCabang, selectedPesertaId) {
        pesertaCabang.firstOrNull { it.id == selectedPesertaId }
    }

    val cfgLomba = remember(selectedLomba, uiState.konfigurasiPenilaian) {
        viewModel.getKonfigurasiLomba(selectedLomba)
    }
    val activeKriteria = remember(cfgLomba, juriPos) {
        viewModel.getKriteriaUntukJuri(selectedLomba, juriPos)
    }
    val maxSempurna = remember(activeKriteria) { activeKriteria.sumOf { it.max } }

    val nilaiInputs = remember(selectedPesertaId, selectedLomba) {
        mutableStateMapOf<String, String>()
    }

    LaunchedEffect(selectedPeserta?.id, selectedLomba, activeKriteria) {
        nilaiInputs.clear()
        val p = selectedPeserta ?: return@LaunchedEffect
        val existing = viewModel.ambilRincianKriteriaJuri(p, juriPos)
        activeKriteria.forEach { k ->
            val v = existing[k.id]
            nilaiInputs[k.id] = if (v != null) DefaultData.formatScore(v) else ""
        }
    }

    // LCC state
    val lccSekolah = remember(selectedLomba) { mutableStateMapOf<Int, String>() }
    val lccSkor = remember(selectedLomba) { mutableStateMapOf<Int, String>() }
    val sortedSchools = remember(uiState.daftarSekolah) { uiState.daftarSekolah.sorted() }

    val liveTotal = activeKriteria.sumOf { k -> nilaiInputs[k.id]?.toDoubleOrNull() ?: 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Formulir Penilaian Lomba",
            subtitle = "Beri penilaian objektif dengan sistem Blind Judging (asal sekolah disembunyikan).",
            badge = "Dewan Juri $juriPos"
        )

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Lembar Penilaian Juri",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DropdownField(
                        label = "Mata Lomba",
                        selectedValue = selectedLomba,
                        options = lombaOptions,
                        onSelected = {
                            selectedLomba = it
                            selectedPesertaId = ""
                        },
                        enabled = !isJuri,
                        testTagName = "penilaian_lomba_dropdown"
                    )

                    if (isLcc) {
                        Surface(
                            color = PaiGold50,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PaiGold600.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = PaiGold700)
                                Text(
                                    text = "Khusus Cerdas Cermat PAI, tentukan Juara 1 s.d. Harapan 3 beserta Asal Sekolah dan Skor Akhir.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PaiGold700
                                )
                            }
                        }

                        val rankLabels = listOf("Juara 1", "Juara 2", "Juara 3", "Harapan 1", "Harapan 2", "Harapan 3")
                        rankLabels.forEachIndexed { idx, rankLabel ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DropdownField(
                                    label = rankLabel,
                                    selectedValue = lccSekolah[idx].orEmpty(),
                                    placeholder = "-- Pilih Sekolah --",
                                    options = sortedSchools,
                                    onSelected = { lccSekolah[idx] = it },
                                    modifier = Modifier.weight(1.7f)
                                )
                                OutlinedTextField(
                                    value = lccSkor[idx].orEmpty(),
                                    onValueChange = { lccSkor[idx] = it },
                                    label = { Text("Skor") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.9f)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val ranks = (0 until 6).mapNotNull { idx ->
                                    val sek = lccSekolah[idx].orEmpty()
                                    val skr = lccSkor[idx]?.toDoubleOrNull() ?: 0.0
                                    if (sek.isBlank()) null else (sek to skr)
                                }
                                viewModel.submitLccPenilaian(selectedLomba, ranks)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kunci & Simpan Nilai LCC")
                        }
                    } else {
                        val pesertaLabels = pesertaCabang.map {
                            "No. Tampil: ${DefaultData.formatDuaDigit(it.noPeserta)} - ${it.nama}" to it.id
                        }
                        val selectedLabel = pesertaLabels.firstOrNull { it.second == selectedPesertaId }?.first.orEmpty()

                        DropdownField(
                            label = "Pilih Nomor Tampil Peserta",
                            selectedValue = selectedLabel,
                            placeholder = if (pesertaLabels.isEmpty()) "-- Belum Ada Peserta --" else "-- Pilih Nomor Tampil --",
                            options = pesertaLabels.map { it.first },
                            onSelected = { chosen ->
                                selectedPesertaId = pesertaLabels.firstOrNull { it.first == chosen }?.second.orEmpty()
                            },
                            enabled = pesertaLabels.isNotEmpty(),
                            testTagName = "penilaian_peserta_dropdown"
                        )

                        Surface(
                            color = PaiEmerald50,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = PaiEmerald700, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Blind Judging Aktif (Nama Sekolah Disembunyikan)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PaiEmerald800
                                )
                            }
                        }

                        if (cfgLomba.jumlahJuri == 2 && cfgLomba.modeDuaJuri == "terpisah") {
                            Surface(
                                color = Indigo100,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Mode Pembagian Tugas 2 Juri Aktif: Menampilkan kriteria khusus untuk Juri $juriPos.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Indigo700,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Surface(
                            color = PaiEmerald100.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Standar Nilai Sempurna Juri $juriPos",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PaiEmerald800
                                )
                                Surface(color = PaiEmerald600, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "Maks: ${DefaultData.formatScore(maxSempurna)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (selectedPeserta == null) {
                            Text(
                                text = "Silakan pilih nomor tampil peserta terlebih dahulu.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate500,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            )
                        } else {
                            activeKriteria.forEach { k ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(k.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Rentang: ${DefaultData.formatScore(k.min)} s.d. ${DefaultData.formatScore(k.max)}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Slate500
                                            )
                                        }
                                        OutlinedTextField(
                                            value = nilaiInputs[k.id] ?: "",
                                            onValueChange = { nilaiInputs[k.id] = it },
                                            placeholder = { Text("${DefaultData.formatScore(k.min)}-${DefaultData.formatScore(k.max)}") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.width(106.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL SKOR ANDA:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Slate500
                                    )
                                    Text(
                                        text = "${DefaultData.formatScore(liveTotal)} / ${DefaultData.formatScore(maxSempurna)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PaiEmerald700
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val parsedMap = mutableMapOf<String, Double>()
                                    for (k in activeKriteria) {
                                        val v = nilaiInputs[k.id]?.toDoubleOrNull()
                                        if (v == null || v < k.min || v > k.max) {
                                            onShowError("Nilai \"${k.label}\" harus di antara ${DefaultData.formatScore(k.min)} s.d. ${DefaultData.formatScore(k.max)}.")
                                            return@Button
                                        }
                                        parsedMap[k.id] = v
                                    }
                                    viewModel.submitPenilaianPeserta(
                                        pesertaId = selectedPeserta.id,
                                        lombaRaw = selectedLomba,
                                        juriPos = juriPos,
                                        nilaiPerKriteria = parsedMap
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("penilaian_submit_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kunci & Simpan Nilai")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RekapNilaiScreen(
    uiState: PentasUiState,
    viewModel: PentasViewModel,
    isAdminMode: Boolean
) {
    val isJuri = uiState.session?.role == UserRole.JURI
    val lombaOptions = remember(uiState.kategoriLomba) {
        uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
    }
    var selectedLomba by remember(isJuri, uiState.session?.juriLomba, lombaOptions) {
        mutableStateOf(
            if (isJuri && !uiState.session?.juriLomba.isNullOrBlank()) {
                DefaultData.normalisasiNamaLomba(uiState.session?.juriLomba)
            } else {
                lombaOptions.firstOrNull().orEmpty()
            }
        )
    }

    val cfg = remember(selectedLomba, uiState.konfigurasiPenilaian) {
        viewModel.getKonfigurasiLomba(selectedLomba)
    }
    val listPeserta = remember(uiState.pesertaList, selectedLomba) {
        uiState.pesertaList
            .filter { DefaultData.normalisasiNamaLomba(it.lomba) == selectedLomba }
            .sortedWith(DefaultData::compareNoUrut)
    }

    val maxJ1 = remember(cfg) { viewModel.getTotalMaksimalKriteria(selectedLomba, 1) }
    val maxJ2 = remember(cfg) { if (cfg.jumlahJuri >= 2) viewModel.getTotalMaksimalKriteria(selectedLomba, 2) else 0.0 }
    val maxGab = remember(cfg) { viewModel.getTotalMaksimalGabunganLomba(selectedLomba) }

    var koreksiTarget by remember { mutableStateOf<Peserta?>(null) }
    var resetTarget by remember { mutableStateOf<Peserta?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(
                title = if (isAdminMode) "Rekapitulasi & Koreksi Nilai (Admin)" else "Rekapitulasi Nilai Dewan Juri",
                subtitle = "Rincian nilai per aspek dan total nilai Juri 1 & Juri 2 (Maks Gabungan: ${DefaultData.formatScore(maxGab)})."
            )

            DropdownField(
                label = "Cabang Lomba",
                selectedValue = selectedLomba,
                options = lombaOptions,
                onSelected = { selectedLomba = it },
                enabled = !isJuri,
                testTagName = if (isAdminMode) "rekap_admin_lomba_dropdown" else "rekap_juri_lomba_dropdown"
            )
        }

        if (listPeserta.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    title = "Belum Ada Peserta",
                    subtitle = "Belum ada peserta terdaftar pada cabang $selectedLomba."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listPeserta, key = { it.id }) { p ->
                    val r1 = viewModel.ambilRincianKriteriaJuri(p, 1)
                    val r2 = viewModel.ambilRincianKriteriaJuri(p, 2)
                    val t1 = if (p.skorJ1 > 0) p.skorJ1 else r1.values.sum()
                    val t2 = if (cfg.jumlahJuri >= 2) (if (p.skorJ2 > 0) p.skorJ2 else r2.values.sum()) else 0.0
                    val total = if (p.skorAkhir > 0) p.skorAkhir else (t1 + t2)

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Purple100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = DefaultData.formatDuaDigit(p.noPeserta),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Purple700
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (isAdminMode) {
                                        Text(p.sekolah, style = MaterialTheme.typography.bodyMedium, color = Slate500)
                                    }
                                }
                                Surface(color = PaiEmerald100, shape = RoundedCornerShape(10.dp)) {
                                    Text(
                                        text = DefaultData.formatScore(total),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PaiEmerald800,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            HorizontalDivider()

                            cfg.kriteria.forEach { k ->
                                val aktifJ1 = viewModel.apakahKriteriaAktifUntukJuri(cfg, k, 1)
                                val aktifJ2 = cfg.jumlahJuri >= 2 && viewModel.apakahKriteriaAktifUntukJuri(cfg, k, 2)
                                val v1 = if (aktifJ1) r1[k.id]?.let { DefaultData.formatScore(it) } ?: "-" else "N/A"
                                val v2 = if (aktifJ2) r2[k.id]?.let { DefaultData.formatScore(it) } ?: "-" else "N/A"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(k.label, style = MaterialTheme.typography.labelLarge)
                                        Text(
                                            text = "Rentang ${DefaultData.formatScore(k.min)}–${DefaultData.formatScore(k.max)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }
                                    Surface(color = Slate100, shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            text = if (cfg.jumlahJuri >= 2) "J1: $v1  |  J2: $v2" else "J1: $v1",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Blue700,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "J1: ${DefaultData.formatScore(t1)}/${DefaultData.formatScore(maxJ1)}" +
                                        if (cfg.jumlahJuri >= 2) " • J2: ${DefaultData.formatScore(t2)}/${DefaultData.formatScore(maxJ2)}" else "",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo700
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { koreksiTarget = p },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAdminMode) PaiGold500 else Blue700,
                                            contentColor = if (isAdminMode) Slate900 else Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Koreksi", style = MaterialTheme.typography.labelMedium)
                                    }
                                    if (isAdminMode) {
                                        IconButton(
                                            onClick = { resetTarget = p },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Rose100)
                                        ) {
                                            Icon(Icons.Default.CleaningServices, contentDescription = "Reset Nilai", tint = Rose700, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    koreksiTarget?.let { p ->
        val j1Name = uiState.juriList.firstOrNull {
            DefaultData.normalisasiNamaLomba(it.lomba) == selectedLomba && it.juriKe == 1
        }?.nama ?: "Dewan Juri 1"
        val j2Name = uiState.juriList.firstOrNull {
            DefaultData.normalisasiNamaLomba(it.lomba) == selectedLomba && it.juriKe == 2
        }?.nama ?: "Dewan Juri 2"

        KoreksiNilaiDialog(
            peserta = p,
            cfg = cfg,
            rincianAwalJ1 = viewModel.ambilRincianKriteriaJuri(p, 1),
            rincianAwalJ2 = viewModel.ambilRincianKriteriaJuri(p, 2),
            isJuriOnly = !isAdminMode && isJuri,
            juriAktif = uiState.session?.juriKe ?: 1,
            juri1Name = j1Name,
            juri2Name = j2Name,
            onDismiss = { koreksiTarget = null },
            onSave = { r1, r2, t1, t2 ->
                viewModel.koreksiNilaiPeserta(p.id, r1, r2, t1, t2)
                koreksiTarget = null
            }
        )
    }

    resetTarget?.let { p ->
        AlertDialog(
            onDismissRequest = { resetTarget = null },
            title = { Text("Reset Nilai Peserta?") },
            text = { Text("Nilai Juri 1 & Juri 2 untuk ${p.nama} (${p.sekolah}) akan dikembalikan ke 0.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetNilaiPeserta(p.id)
                        resetTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose700)
                ) {
                    Text("Ya, Reset ke 0")
                }
            },
            dismissButton = {
                TextButton(onClick = { resetTarget = null }) { Text("Batal") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PengaturanPenilaianScreen(
    uiState: PentasUiState,
    viewModel: PentasViewModel,
    onShowError: (String) -> Unit
) {
    val lombaOptions = remember(uiState.kategoriLomba) {
        uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
    }
    var selectedLomba by remember(lombaOptions) {
        mutableStateOf(lombaOptions.firstOrNull().orEmpty())
    }
    val cfg = remember(selectedLomba, uiState.konfigurasiPenilaian) {
        viewModel.getKonfigurasiLomba(selectedLomba)
    }

    var quickLabel by remember { mutableStateOf("") }
    var quickMin by remember { mutableStateOf("10") }
    var quickMax by remember { mutableStateOf("35") }

    var showCopyDialog by remember { mutableStateOf(false) }
    var editingKriteriaIndex by remember { mutableStateOf<Int?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Pengaturan Penilaian & Kriteria",
            subtitle = "Atur jumlah juri per cabang lomba, bobot nilai min/maks tiap kriteria, serta pembagian kriteria 2 juri.",
            badge = "Konfigurasi Juri"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showResetConfirm = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Standar")
            }
            Button(
                onClick = { viewModel.simpanSemuaKonfigurasiPenilaian() },
                colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.4f).height(48.dp).testTag("pengaturan_penilaian_save_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan Semua")
            }
        }

        // 1. Summary Grid of All Cabang Lomba
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("1. Ringkasan Jumlah Juri Cabang Lomba", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    lombaOptions.forEach { lomba ->
                        val c = viewModel.getKonfigurasiLomba(lomba)
                        val isSelected = lomba == selectedLomba
                        val maxGab = viewModel.getTotalMaksimalGabunganLomba(lomba)
                        Surface(
                            color = if (isSelected) PaiEmerald50 else Slate100,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) PaiEmerald600 else Color.Transparent),
                            modifier = Modifier.clickable { selectedLomba = lomba }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(lomba, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${c.jumlahJuri} Juri • ${c.kriteria.size} Kriteria (Maks ${DefaultData.formatScore(maxGab)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) PaiEmerald700 else Slate500
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Editor Detail
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                DropdownField(
                    label = "Cabang Lomba yang Diatur",
                    selectedValue = selectedLomba,
                    options = lombaOptions,
                    onSelected = { selectedLomba = it }
                )

                val juriCountOptions = listOf(
                    "1 Orang Juri (Penilaian Tunggal)" to 1,
                    "2 Orang Juri (Juri 1 & Juri 2)" to 2,
                    "3 Orang Juri (Juri 1, Juri 2 & Juri 3)" to 3
                )
                DropdownField(
                    label = "Jumlah Dewan Juri",
                    selectedValue = juriCountOptions.firstOrNull { it.second == cfg.jumlahJuri }?.first ?: juriCountOptions[1].first,
                    options = juriCountOptions.map { it.first },
                    onSelected = { label ->
                        val count = juriCountOptions.firstOrNull { it.first == label }?.second ?: 2
                        val updated = if (count != 2) {
                            cfg.copy(
                                jumlahJuri = count,
                                modeDuaJuri = "semua",
                                kriteria = cfg.kriteria.map { it.copy(juriTarget = "all") }
                            )
                        } else cfg.copy(jumlahJuri = count)
                        viewModel.updateLocalKonfigurasiPenilaian(selectedLomba, updated)
                    }
                )

                if (cfg.jumlahJuri == 2) {
                    val modeOptions = listOf(
                        "Semua Kriteria Dinilai Bersama" to "semua",
                        "Kriteria Dibagi Khusus per Juri (J1 / J2)" to "terpisah"
                    )
                    DropdownField(
                        label = "Sistem Penentuan Kriteria 2 Juri",
                        selectedValue = modeOptions.firstOrNull { it.second == cfg.modeDuaJuri }?.first ?: modeOptions[0].first,
                        options = modeOptions.map { it.first },
                        onSelected = { label ->
                            val mode = modeOptions.firstOrNull { it.first == label }?.second ?: "semua"
                            val updated = if (mode == "semua") {
                                cfg.copy(modeDuaJuri = mode, kriteria = cfg.kriteria.map { it.copy(juriTarget = "all") })
                            } else cfg.copy(modeDuaJuri = mode)
                            viewModel.updateLocalKonfigurasiPenilaian(selectedLomba, updated)
                        }
                    )
                }

                HorizontalDivider()

                // Quick Add Form
                Surface(
                    color = Indigo100.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tambah Kriteria Baru ($selectedLomba)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Indigo700
                        )
                        OutlinedTextField(
                            value = quickLabel,
                            onValueChange = { quickLabel = it },
                            label = { Text("Nama Kriteria / Aspek") },
                            placeholder = { Text("Cth: Tajwid & Makharijul Huruf") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quickMin,
                                onValueChange = { quickMin = it },
                                label = { Text("Nilai Min") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = quickMax,
                                onValueChange = { quickMax = it },
                                label = { Text("Nilai Maks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                val lbl = quickLabel.trim()
                                val mn = quickMin.toDoubleOrNull() ?: 0.0
                                val mx = quickMax.toDoubleOrNull() ?: 100.0
                                if (lbl.isBlank() || mx <= 0 || mn > mx) {
                                    onShowError("Isi nama kriteria dan rentang nilai (min <= maks) dengan benar.")
                                    return@Button
                                }
                                val newItem = KriteriaItem(
                                    id = "kr_${System.currentTimeMillis().toString(36)}_${cfg.kriteria.size + 1}",
                                    label = lbl,
                                    min = mn,
                                    max = mx,
                                    juriTarget = "all"
                                )
                                viewModel.updateLocalKonfigurasiPenilaian(
                                    selectedLomba,
                                    cfg.copy(kriteria = cfg.kriteria + newItem)
                                )
                                quickLabel = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambahkan Kriteria")
                        }
                    }
                }

                // Existing Criteria List
                cfg.kriteria.forEachIndexed { idx, k ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${idx + 1}. ${k.label}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Min: ${DefaultData.formatScore(k.min)} • Maks: ${DefaultData.formatScore(k.max)}" +
                                            if (cfg.jumlahJuri == 2 && cfg.modeDuaJuri == "terpisah") {
                                                " • Target: " + when (k.juriTarget) {
                                                    "1" -> "Juri 1 Saja"
                                                    "2" -> "Juri 2 Saja"
                                                    else -> "J1 & J2"
                                                }
                                            } else "",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Slate500
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { editingKriteriaIndex = idx },
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Blue100)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Blue700, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            if (cfg.kriteria.size <= 1) {
                                                onShowError("Setiap cabang lomba minimal memiliki 1 kriteria.")
                                            } else {
                                                val list = cfg.kriteria.toMutableList().apply { removeAt(idx) }
                                                viewModel.updateLocalKonfigurasiPenilaian(selectedLomba, cfg.copy(kriteria = list))
                                            }
                                        },
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Rose100)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Rose700, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Summary Max Points & Copy
                val maxJ1 = viewModel.getTotalMaksimalKriteria(selectedLomba, 1)
                val maxJ2 = if (cfg.jumlahJuri >= 2) viewModel.getTotalMaksimalKriteria(selectedLomba, 2) else 0.0
                val maxGab = viewModel.getTotalMaksimalGabunganLomba(selectedLomba)
                Surface(
                    color = PaiEmerald50,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Skor Sempurna Gabungan (${cfg.jumlahJuri} Juri): ${DefaultData.formatScore(maxGab)} Poin", style = MaterialTheme.typography.titleMedium, color = PaiEmerald800, fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = "Maks Juri 1: ${DefaultData.formatScore(maxJ1)} • Maks Juri 2: ${DefaultData.formatScore(maxJ2)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PaiEmerald700
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCopyDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin ke Lomba Lain")
                    }
                    Button(
                        onClick = { viewModel.simpanSemuaKonfigurasiPenilaian() },
                        colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
                        modifier = Modifier.weight(1.2f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan ke Database")
                    }
                }
            }
        }
    }

    editingKriteriaIndex?.let { idx ->
        val k = cfg.kriteria.getOrNull(idx)
        if (k != null) {
            var lbl by remember(idx) { mutableStateOf(k.label) }
            var mn by remember(idx) { mutableStateOf(DefaultData.formatScore(k.min)) }
            var mx by remember(idx) { mutableStateOf(DefaultData.formatScore(k.max)) }
            var trg by remember(idx) { mutableStateOf(k.juriTarget) }

            AlertDialog(
                onDismissRequest = { editingKriteriaIndex = null },
                title = { Text("Edit Kriteria #${idx + 1}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = lbl,
                            onValueChange = { lbl = it },
                            label = { Text("Nama Kriteria") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mn,
                                onValueChange = { mn = it },
                                label = { Text("Min") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mx,
                                onValueChange = { mx = it },
                                label = { Text("Maks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (cfg.jumlahJuri == 2 && cfg.modeDuaJuri == "terpisah") {
                            val tOpts = listOf("Dinilai J1 & J2" to "all", "Khusus Juri 1" to "1", "Khusus Juri 2" to "2")
                            DropdownField(
                                label = "Penugasan Juri",
                                selectedValue = tOpts.firstOrNull { it.second == trg }?.first ?: tOpts[0].first,
                                options = tOpts.map { it.first },
                                onSelected = { chosen -> trg = tOpts.firstOrNull { it.first == chosen }?.second ?: "all" }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val mnD = mn.toDoubleOrNull() ?: 0.0
                            val mxD = mx.toDoubleOrNull() ?: 100.0
                            if (lbl.isBlank() || mxD <= 0 || mnD > mxD) {
                                onShowError("Nama kriteria dan rentang nilai harus valid.")
                                return@Button
                            }
                            val updatedList = cfg.kriteria.toMutableList()
                            updatedList[idx] = k.copy(label = lbl.trim(), min = mnD, max = mxD, juriTarget = trg)
                            viewModel.updateLocalKonfigurasiPenilaian(selectedLomba, cfg.copy(kriteria = updatedList))
                            editingKriteriaIndex = null
                        }
                    ) { Text("Perbarui") }
                },
                dismissButton = {
                    TextButton(onClick = { editingKriteriaIndex = null }) { Text("Batal") }
                }
            )
        }
    }

    if (showCopyDialog) {
        val targets = lombaOptions.filter { it != selectedLomba }
        var targetLomba by remember { mutableStateOf(targets.firstOrNull().orEmpty()) }
        AlertDialog(
            onDismissRequest = { showCopyDialog = false },
            title = { Text("Salin Kriteria dari \"$selectedLomba\"") },
            text = {
                DropdownField(
                    label = "Mata Lomba Tujuan",
                    selectedValue = targetLomba,
                    options = targets,
                    onSelected = { targetLomba = it }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetLomba.isNotBlank()) {
                            viewModel.updateLocalKonfigurasiPenilaian(targetLomba, cfg.copy())
                            viewModel.simpanSemuaKonfigurasiPenilaian()
                        }
                        showCopyDialog = false
                    }
                ) { Text("Salin & Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showCopyDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Pengaturan Penilaian?") },
            text = { Text("Seluruh jumlah juri dan kriteria semua lomba akan dikembalikan ke standar awal.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPengaturanPenilaianDefault()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose700)
                ) { Text("Ya, Reset Standar") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Batal") }
            }
        )
    }
}
