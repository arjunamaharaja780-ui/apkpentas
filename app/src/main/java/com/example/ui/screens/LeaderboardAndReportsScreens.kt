package com.example.ui.screens

import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.DefaultData
import com.example.model.JuaraDocMode
import com.example.model.PrintDocFormat
import com.example.model.SkConfig
import com.example.ui.components.DropdownField
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue700
import com.example.ui.theme.PaiEmerald100
import com.example.ui.theme.PaiEmerald50
import com.example.ui.theme.PaiEmerald600
import com.example.ui.theme.PaiEmerald700
import com.example.ui.theme.PaiEmerald800
import com.example.ui.theme.PaiGold100
import com.example.ui.theme.PaiGold400
import com.example.ui.theme.PaiGold600
import com.example.ui.theme.PaiGold700
import com.example.ui.theme.Purple100
import com.example.ui.theme.Purple700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.util.PrintAndExportHelper
import com.example.viewmodel.PentasUiState
import com.example.viewmodel.PentasViewModel

@Composable
fun LeaderboardScreen(
    uiState: PentasUiState,
    onRefresh: () -> Unit
) {
    var filterLomba by remember { mutableStateOf("All") }
    val filterOptions = remember(uiState.kategoriLomba) {
        listOf("Semua Kategori") + uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
    }

    val rankedList = remember(uiState.pesertaList, filterLomba) {
        uiState.pesertaList
            .filter { filterLomba == "All" || DefaultData.normalisasiNamaLomba(it.lomba) == filterLomba }
            .sortedByDescending { it.skorAkhir }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(
                title = "Klasemen & Perolehan Nilai",
                subtitle = "Rekapitulasi peringkat kejuaraan real-time Tahun ${uiState.tahunPelaksanaan}."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropdownField(
                    label = "Filter Kategori",
                    selectedValue = if (filterLomba == "All") "Semua Kategori" else filterLomba,
                    options = filterOptions,
                    onSelected = { chosen -> filterLomba = if (chosen == "Semua Kategori") "All" else chosen },
                    modifier = Modifier.weight(1f),
                    testTagName = "leaderboard_filter_dropdown"
                )
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald100, contentColor = PaiEmerald800),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(54.dp).testTag("leaderboard_refresh_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Segarkan Data")
                }
            }
        }

        if (rankedList.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    title = "Belum Ada Perolehan Nilai",
                    subtitle = "Belum ada peserta atau nilai pada kategori yang dipilih.",
                    icon = Icons.Default.EmojiEvents
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(rankedList, key = { _, p -> p.id }) { index, p ->
                    val hasScore = p.skorAkhir > 0
                    val rankLabel = when {
                        !hasScore -> "-"
                        index == 0 -> "JUARA 1"
                        index == 1 -> "JUARA 2"
                        index == 2 -> "JUARA 3"
                        index == 3 -> "HARAPAN 1"
                        index == 4 -> "HARAPAN 2"
                        index == 5 -> "HARAPAN 3"
                        else -> "Peringkat ${index + 1}"
                    }
                    val badgeBg = when {
                        !hasScore -> Slate100
                        index == 0 -> PaiGold400
                        index == 1 -> Color(0xFFE2E8F0)
                        index == 2 -> Color(0xFFFED7AA)
                        else -> PaiEmerald50
                    }
                    val badgeText = when {
                        !hasScore -> Slate500
                        index == 0 -> Slate900
                        index == 1 -> Slate900
                        index == 2 -> Color(0xFF9A3412)
                        else -> PaiEmerald800
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = if (hasScore && index == 0) BorderStroke(1.5.dp, PaiGold400) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = if (hasScore && index < 3) 4.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (hasScore && index < 3) badgeBg else Purple100),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = DefaultData.formatDuaDigit(p.noPeserta),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (hasScore && index < 3) badgeText else Purple700
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.nama,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = p.sekolah,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (filterLomba == "All") {
                                    Text(
                                        text = DefaultData.normalisasiNamaLomba(p.lomba),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PaiEmerald700
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (hasScore) DefaultData.formatScore(p.skorAkhir) else "-",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PaiEmerald700
                                )
                                Surface(color = badgeBg, shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = rankLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CetakExcelScreen(
    uiState: PentasUiState,
    viewModel: PentasViewModel,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var cabangDipilih by remember { mutableStateOf("All") }
    var formatDokumen by remember { mutableStateOf(PrintDocFormat.HADIR) }

    val cabangOptions = remember(uiState.kategoriLomba) {
        listOf("Semua Cabang Lomba (Per Halaman)") + uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
    }

    val createTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(PrintAndExportHelper.buildTemplateCsv().toByteArray(Charsets.UTF_8))
            }
            onShowMessage("Template CSV berhasil disimpan (dapat dibuka di Excel).")
        }
    }

    val exportPesertaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val filtered = uiState.pesertaList
                .filter { cabangDipilih == "All" || DefaultData.normalisasiNamaLomba(it.lomba) == cabangDipilih }
                .sortedWith(DefaultData::compareNoUrut)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(PrintAndExportHelper.buildPesertaCsv(filtered).toByteArray(Charsets.UTF_8))
            }
            onShowMessage("Daftar peserta berhasil diekspor ke CSV/Excel.")
        }
    }

    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
            val parsed = PrintAndExportHelper.parseCsvPeserta(text, uiState.noUrutMap)
            if (parsed.isEmpty()) {
                onShowMessage("Tidak ditemukan baris peserta valid dalam file CSV.")
            } else {
                viewModel.importPesertaList(parsed)
            }
        }
    }

    val previewHtml = remember(
        cabangDipilih,
        formatDokumen,
        uiState.pesertaList,
        uiState.kategoriLomba,
        uiState.namaKecamatan,
        uiState.tahunPelaksanaan,
        uiState.appLogoDataUrl
    ) {
        PrintAndExportHelper.buildDokumenPesertaHtml(
            cabangDipilih = cabangDipilih,
            format = formatDokumen,
            pesertaList = uiState.pesertaList,
            kategoriLomba = uiState.kategoriLomba,
            kecamatan = uiState.namaKecamatan,
            tahun = uiState.tahunPelaksanaan,
            logoDataUrl = uiState.appLogoDataUrl
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(
            title = "Ekspor/Impor & Cetak Dokumen",
            subtitle = "Kelola daftar peserta massal (CSV/Excel) dan cetak daftar hadir, lembar blind judging, serta nilai resmi.",
            badge = "Pusat Dokumen"
        )

        // Action Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { createTemplateLauncher.launch("Template_Impor_Peserta.csv") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Template", maxLines = 1)
            }
            Button(
                onClick = { importCsvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/*")) },
                colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Impor CSV", maxLines = 1)
            }
        }

        Button(
            onClick = { exportPesertaLauncher.launch("Data_Peserta_PPAI_${uiState.tahunPelaksanaan}.csv") },
            colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ekspor Daftar Peserta ke CSV / Excel")
        }

        // Document Generator Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cetak Dokumen Lomba Per Cabang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)

                DropdownField(
                    label = "Pilih Cabang Lomba",
                    selectedValue = if (cabangDipilih == "All") cabangOptions[0] else cabangDipilih,
                    options = cabangOptions,
                    onSelected = { chosen ->
                        cabangDipilih = if (chosen == cabangOptions[0]) "All" else chosen
                    }
                )

                DropdownField(
                    label = "Format Dokumen Cetak",
                    selectedValue = formatDokumen.label,
                    options = PrintDocFormat.entries.map { it.label },
                    onSelected = { chosen ->
                        formatDokumen = PrintDocFormat.entries.firstOrNull { it.label == chosen } ?: PrintDocFormat.HADIR
                    }
                )

                Button(
                    onClick = {
                        PrintAndExportHelper.printHtmlDocument(
                            context = context,
                            jobName = "PentasPAI_${formatDokumen.key}_${uiState.tahunPelaksanaan}",
                            htmlBody = previewHtml
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("cetak_pdf_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cetak / Simpan PDF Sekarang")
                }

                // Embedded Document Preview
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp, max = 440.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                            }
                        },
                        update = { webView ->
                            val styled = "<style>body{font-family:sans-serif;padding:10px;}table{width:100%;border-collapse:collapse;}th,td{border:1px solid #cbd5e1;padding:6px;font-size:11px;}th{background:#f1f5f9;}</style>$previewHtml"
                            webView.loadDataWithBaseURL(null, styled, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxWidth().height(380.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CetakJuaraScreen(
    uiState: PentasUiState,
    viewModel: PentasViewModel,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(JuaraDocMode.SK_LENGKAP) }
    var cabangDipilih by remember { mutableStateOf("All") }
    var showSkConfigDialog by remember { mutableStateOf(false) }

    val cabangOptions = remember(uiState.kategoriLomba) {
        listOf("Semua Cabang Lomba") + uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
    }

    val rekapSekolah = remember(uiState.pesertaList, uiState.daftarSekolah, uiState.kategoriLomba, uiState.noUrutMap) {
        viewModel.hitungRekapSekolahDanJuaraUmum()
    }

    val exportJuaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(PrintAndExportHelper.buildRekapJuaraCsv(rekapSekolah).toByteArray(Charsets.UTF_8))
            }
            onShowMessage("Rekap Juara berhasil diekspor ke CSV/Excel.")
        }
    }

    val previewHtml = remember(
        mode,
        cabangDipilih,
        uiState.pesertaList,
        uiState.kategoriLomba,
        rekapSekolah,
        uiState.skConfig,
        uiState.namaKecamatan,
        uiState.tahunPelaksanaan,
        uiState.appLogoDataUrl
    ) {
        PrintAndExportHelper.buildDokumenJuaraHtml(
            mode = mode,
            cabangDipilih = cabangDipilih,
            pesertaList = uiState.pesertaList,
            kategoriLomba = uiState.kategoriLomba,
            rekapSekolah = rekapSekolah,
            skConfig = uiState.skConfig,
            kecamatan = uiState.namaKecamatan,
            tahun = uiState.tahunPelaksanaan,
            logoDataUrl = uiState.appLogoDataUrl
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(
            title = "SK Juara & Rekap Sekolah",
            subtitle = "Cetak SK Penetapan Juara Lomba resmi, Rekapitulasi Per Sekolah, dan Klasemen Juara Umum.",
            badge = "Dokumen Resmi"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showSkConfigDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Atur SK & TTD", maxLines = 1)
            }
            Button(
                onClick = { exportJuaraLauncher.launch("Rekap_Juara_PPAI_${uiState.tahunPelaksanaan}.csv") },
                colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ekspor CSV", maxLines = 1)
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField(
                    label = "Jenis Dokumen SK / Rekap",
                    selectedValue = mode.label,
                    options = JuaraDocMode.entries.map { it.label },
                    onSelected = { chosen ->
                        mode = JuaraDocMode.entries.firstOrNull { it.label == chosen } ?: JuaraDocMode.SK_LENGKAP
                    }
                )

                DropdownField(
                    label = "Filter Cabang Lomba",
                    selectedValue = if (cabangDipilih == "All") cabangOptions[0] else cabangDipilih,
                    options = cabangOptions,
                    onSelected = { chosen ->
                        cabangDipilih = if (chosen == cabangOptions[0]) "All" else chosen
                    }
                )

                Button(
                    onClick = {
                        PrintAndExportHelper.printHtmlDocument(
                            context = context,
                            jobName = "SK_Juara_PentasPAI_${uiState.tahunPelaksanaan}",
                            htmlBody = previewHtml
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaiGold600, contentColor = Slate900),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sk_cetak_pdf_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cetak / Simpan PDF SK Juara", fontWeight = FontWeight.ExtraBold)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().height(400.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                            }
                        },
                        update = { webView ->
                            val styled = "<style>body{font-family:'Times New Roman',serif;padding:12px;}table{width:100%;border-collapse:collapse;}th,td{border:1px solid #334155;padding:5px;font-size:10px;}th{background:#f1f5f9;}</style>$previewHtml"
                            webView.loadDataWithBaseURL(null, styled, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showSkConfigDialog) {
        val cfg = uiState.skConfig
        var nomorSK by remember { mutableStateOf(cfg.nomorSK) }
        var kotaTetap by remember { mutableStateOf(cfg.kotaTetap) }
        var tanggalSK by remember { mutableStateOf(cfg.tanggalSK) }
        var lokasiLomba by remember { mutableStateOf(cfg.lokasiLomba) }
        var ketuaPanitia by remember { mutableStateOf(cfg.ketuaPanitia) }
        var nipKetua by remember { mutableStateOf(cfg.nipKetua) }
        var sekretaris by remember { mutableStateOf(cfg.sekretarisPanitia) }
        var nipSekretaris by remember { mutableStateOf(cfg.nipSekretaris) }
        var ketuaKKG by remember { mutableStateOf(cfg.ketuaKKG) }
        var nipKKG by remember { mutableStateOf(cfg.nipKetuaKKG) }

        AlertDialog(
            onDismissRequest = { showSkConfigDialog = false },
            title = { Text("Pengaturan SK Penetapan Juara") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = nomorSK, onValueChange = { nomorSK = it }, label = { Text("Nomor SK") }, singleLine = true)
                    OutlinedTextField(value = kotaTetap, onValueChange = { kotaTetap = it }, label = { Text("Tempat Ditetapkan") }, singleLine = true)
                    OutlinedTextField(value = tanggalSK, onValueChange = { tanggalSK = it }, label = { Text("Tanggal Penetapan SK") }, singleLine = true)
                    OutlinedTextField(value = lokasiLomba, onValueChange = { lokasiLomba = it }, label = { Text("Tempat Pelaksanaan") }, singleLine = true)
                    OutlinedTextField(value = ketuaPanitia, onValueChange = { ketuaPanitia = it }, label = { Text("Nama Ketua Panitia") }, singleLine = true)
                    OutlinedTextField(value = nipKetua, onValueChange = { nipKetua = it }, label = { Text("NIP Ketua Panitia") }, singleLine = true)
                    OutlinedTextField(value = sekretaris, onValueChange = { sekretaris = it }, label = { Text("Nama Sekretaris") }, singleLine = true)
                    OutlinedTextField(value = nipSekretaris, onValueChange = { nipSekretaris = it }, label = { Text("NIP Sekretaris") }, singleLine = true)
                    OutlinedTextField(value = ketuaKKG, onValueChange = { ketuaKKG = it }, label = { Text("Nama Ketua KKG PAI") }, singleLine = true)
                    OutlinedTextField(value = nipKKG, onValueChange = { nipKKG = it }, label = { Text("NIP Ketua KKG PAI") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSkConfig(
                            SkConfig(
                                nomorSK = nomorSK.trim(),
                                kotaTetap = kotaTetap.trim(),
                                tanggalSK = tanggalSK.trim(),
                                lokasiLomba = lokasiLomba.trim(),
                                ketuaPanitia = ketuaPanitia.trim(),
                                nipKetua = nipKetua.trim(),
                                sekretarisPanitia = sekretaris.trim(),
                                nipSekretaris = nipSekretaris.trim(),
                                ketuaKKG = ketuaKKG.trim(),
                                nipKetuaKKG = nipKKG.trim()
                            )
                        )
                        showSkConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600)
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showSkConfigDialog = false }) { Text("Batal") }
            }
        )
    }
}
