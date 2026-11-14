package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DefaultData
import com.example.model.KriteriaItem
import com.example.model.LombaPenilaianConfig
import com.example.model.Peserta
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue700
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo700
import com.example.ui.theme.PaiEmerald100
import com.example.ui.theme.PaiEmerald50
import com.example.ui.theme.PaiEmerald600
import com.example.ui.theme.PaiEmerald700
import com.example.ui.theme.PaiEmerald800
import com.example.ui.theme.PaiEmerald900
import com.example.ui.theme.PaiGold400
import com.example.ui.theme.PaiGold500
import com.example.ui.theme.PaiGold700
import com.example.ui.theme.Purple700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.util.PrintAndExportHelper
import kotlin.math.cos
import kotlin.math.sin

fun iconForCategory(iconKey: String): ImageVector {
    return when {
        iconKey.contains("book") -> Icons.AutoMirrored.Filled.MenuBook
        iconKey.contains("brain") -> Icons.Default.Psychology
        iconKey.contains("microphone") -> Icons.Default.Mic
        iconKey.contains("pen") -> Icons.Default.Palette
        iconKey.contains("users") -> Icons.Default.Groups
        iconKey.contains("bullhorn") -> Icons.Default.Campaign
        iconKey.contains("praying") -> Icons.Default.AutoAwesome
        else -> Icons.Default.EmojiEvents
    }
}

@Composable
fun IslamicGeometricBackdrop(
    modifier: Modifier = Modifier,
    darkMode: Boolean = false
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val starColor = if (darkMode) Color(0x16FFFFFF) else Color(0x14047857)
        val goldColor = if (darkMode) Color(0x18FBBF24) else Color(0x16D97706)
        val step = 120.dp.toPx()

        var y = step / 2f
        while (y < h + step) {
            var x = step / 2f
            while (x < w + step) {
                val rOuter = 38.dp.toPx()
                val rInner = 18.dp.toPx()
                val path = Path()
                val points = 16
                for (i in 0 until points) {
                    val angle = (i * Math.PI / 8.0).toFloat()
                    val r = if (i % 2 == 0) rOuter else rInner
                    val px = x + r * cos(angle)
                    val py = y + r * sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, color = starColor, style = Stroke(width = 1.2.dp.toPx()))
                drawCircle(color = goldColor, radius = 7.dp.toPx(), center = Offset(x, y), style = Stroke(width = 1.dp.toPx()))
                x += step
            }
            y += step
        }
    }
}

@Composable
fun LogoOrEmblem(
    logoDataUrl: String,
    size: Dp = 48.dp,
    containerColor: Color = Color.White.copy(alpha = 0.16f),
    iconTint: Color = PaiGold400
) {
    val bitmap = remember(logoDataUrl) { PrintAndExportHelper.decodeBase64Logo(logoDataUrl) }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(if (bitmap != null) Color.White else containerColor)
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Logo PENTAS PAI",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = Icons.Default.NightsStay,
                contentDescription = "Logo PENTAS PAI",
                tint = iconTint,
                modifier = Modifier.size(size * 0.56f)
            )
        }
    }
}

@Composable
fun MarqueeBanner(
    runningText: String,
    kecamatan: String,
    isAdmin: Boolean,
    onEditRunningText: (() -> Unit)? = null,
    onEditKecamatan: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(PaiEmerald900, PaiEmerald800, Slate900)
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = PaiGold500,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "INFO PENTAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate900,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Text(
            text = "$runningText   ✦   $runningText",
            style = MaterialTheme.typography.bodyMedium,
            color = PaiEmerald100,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .basicMarquee(iterations = Int.MAX_VALUE)
        )

        if (isAdmin) {
            if (onEditRunningText != null) {
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable(onClick = onEditRunningText)
                        .testTag("banner_edit_running_text")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Running Text",
                        tint = PaiGold400,
                        modifier = Modifier.padding(6.dp).size(16.dp)
                    )
                }
            }
            if (onEditKecamatan != null) {
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable(onClick = onEditKecamatan)
                        .testTag("banner_edit_kecamatan")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubah Kecamatan",
                            tint = Color(0xFF7DD3FC),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = kecamatan,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "-- Pilih --",
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTagName: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue.ifBlank { placeholder },
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .then(if (testTagName.isNotBlank()) Modifier.testTag(testTagName) else Modifier)
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!badge.isNullOrBlank()) {
            Surface(
                color = PaiEmerald100,
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = PaiEmerald800,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Default.Inbox,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PaiEmerald50),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PaiEmerald600, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingOverlay(
    visible: Boolean,
    message: String
) {
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = PaiEmerald600)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = message.ifBlank { "Memproses data..." },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun KoreksiNilaiDialog(
    peserta: Peserta,
    cfg: LombaPenilaianConfig,
    rincianAwalJ1: Map<String, Double>,
    rincianAwalJ2: Map<String, Double>,
    isJuriOnly: Boolean,
    juriAktif: Int,
    juri1Name: String,
    juri2Name: String,
    onDismiss: () -> Unit,
    onSave: (
        rincianJ1: Map<String, Double>,
        rincianJ2: Map<String, Double>,
        totalJ1: Double,
        totalJ2: Double
    ) -> Unit
) {
    val jmlJuri = cfg.jumlahJuri
    val maxJ1 = cfg.kriteria.filter {
        if (jmlJuri == 2 && cfg.modeDuaJuri == "terpisah" && it.juriTarget != "all") it.juriTarget == "1" else true
    }.sumOf { it.max }
    val maxJ2 = if (jmlJuri >= 2) cfg.kriteria.filter {
        if (jmlJuri == 2 && cfg.modeDuaJuri == "terpisah" && it.juriTarget != "all") it.juriTarget == "2" else true
    }.sumOf { it.max } else 0.0
    val maxGabungan = maxJ1 + maxJ2

    val mapJ1 = remember(peserta.id) {
        mutableStateMapOf<String, String>().apply {
            cfg.kriteria.forEach { k ->
                put(k.id, rincianAwalJ1[k.id]?.let { DefaultData.formatScore(it) } ?: "0")
            }
        }
    }
    val mapJ2 = remember(peserta.id) {
        mutableStateMapOf<String, String>().apply {
            cfg.kriteria.forEach { k ->
                put(k.id, rincianAwalJ2[k.id]?.let { DefaultData.formatScore(it) } ?: "0")
            }
        }
    }

    val sumInitialJ1 = if (peserta.skorJ1 > 0) peserta.skorJ1 else rincianAwalJ1.values.sum()
    val sumInitialJ2 = if (jmlJuri >= 2) {
        if (peserta.skorJ2 > 0) peserta.skorJ2 else rincianAwalJ2.values.sum()
    } else 0.0

    var totalJ1Str by remember(peserta.id) { mutableStateOf(DefaultData.formatScore(sumInitialJ1)) }
    var totalJ2Str by remember(peserta.id) { mutableStateOf(DefaultData.formatScore(sumInitialJ2)) }

    fun recomputeTotalJ1() {
        val sum = cfg.kriteria.sumOf { k ->
            val aktif = if (jmlJuri == 2 && cfg.modeDuaJuri == "terpisah" && k.juriTarget != "all") k.juriTarget == "1" else true
            if (aktif) mapJ1[k.id]?.toDoubleOrNull() ?: 0.0 else 0.0
        }
        totalJ1Str = DefaultData.formatScore(sum)
    }

    fun recomputeTotalJ2() {
        if (jmlJuri < 2) {
            totalJ2Str = "0"
            return
        }
        val sum = cfg.kriteria.sumOf { k ->
            val aktif = if (jmlJuri == 2 && cfg.modeDuaJuri == "terpisah" && k.juriTarget != "all") k.juriTarget == "2" else true
            if (aktif) mapJ2[k.id]?.toDoubleOrNull() ?: 0.0 else 0.0
        }
        totalJ2Str = DefaultData.formatScore(sum)
    }

    val t1 = totalJ1Str.toDoubleOrNull() ?: 0.0
    val t2 = if (jmlJuri >= 2) totalJ2Str.toDoubleOrNull() ?: 0.0 else 0.0
    val grandTotal = t1 + t2

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Koreksi Nilai Juri",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${peserta.lomba} • No. Tampil ${DefaultData.formatDuaDigit(peserta.noPeserta)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Purple700
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(peserta.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text(peserta.sekolah, style = MaterialTheme.typography.bodyMedium, color = Slate500)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "J1: $juri1Name" + if (jmlJuri >= 2) " • J2: $juri2Name" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Blue700
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    cfg.kriteria.forEachIndexed { idx, k ->
                        val aktifJ1 = if (jmlJuri == 2 && cfg.modeDuaJuri == "terpisah" && k.juriTarget != "all") k.juriTarget == "1" else true
                        val aktifJ2 = jmlJuri >= 2 && if (jmlJuri == 2 && cfg.modeDuaJuri == "terpisah" && k.juriTarget != "all") k.juriTarget == "2" else true

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${idx + 1}. ${k.label}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rentang Skor: ${DefaultData.formatScore(k.min)} s.d. ${DefaultData.formatScore(k.max)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (aktifJ1) {
                                        val canEditJ1 = !isJuriOnly || juriAktif == 1
                                        OutlinedTextField(
                                            value = mapJ1[k.id] ?: "0",
                                            onValueChange = { raw ->
                                                val v = raw.toDoubleOrNull()
                                                mapJ1[k.id] = if (v != null && v > k.max) DefaultData.formatScore(k.max) else raw
                                                recomputeTotalJ1()
                                            },
                                            enabled = canEditJ1,
                                            label = { Text("Juri 1 (/${DefaultData.formatScore(k.max)})") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (aktifJ2) {
                                        val canEditJ2 = !isJuriOnly || juriAktif == 2
                                        OutlinedTextField(
                                            value = mapJ2[k.id] ?: "0",
                                            onValueChange = { raw ->
                                                val v = raw.toDoubleOrNull()
                                                mapJ2[k.id] = if (v != null && v > k.max) DefaultData.formatScore(k.max) else raw
                                                recomputeTotalJ2()
                                            },
                                            enabled = canEditJ2,
                                            label = { Text("Juri 2 (/${DefaultData.formatScore(k.max)})") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = totalJ1Str,
                            onValueChange = { totalJ1Str = it },
                            enabled = !isJuriOnly || juriAktif == 1,
                            label = { Text("Total Juri 1 (/${DefaultData.formatScore(maxJ1)})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (jmlJuri >= 2) {
                            OutlinedTextField(
                                value = totalJ2Str,
                                onValueChange = { totalJ2Str = it },
                                enabled = !isJuriOnly || juriAktif == 2,
                                label = { Text("Total Juri 2 (/${DefaultData.formatScore(maxJ2)})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = PaiEmerald50,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PaiEmerald600.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL SKOR AKHIR", style = MaterialTheme.typography.labelSmall, color = PaiEmerald800)
                            Text("Maksimal Gabungan: ${DefaultData.formatScore(maxGabungan)}", style = MaterialTheme.typography.labelMedium, color = PaiEmerald700)
                        }
                        Text(
                            text = DefaultData.formatScore(grandTotal),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = PaiEmerald700
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val r1 = mutableMapOf<String, Double>()
                            val r2 = mutableMapOf<String, Double>()
                            cfg.kriteria.forEach { k ->
                                mapJ1[k.id]?.toDoubleOrNull()?.let { r1[k.id] = it.coerceAtMost(k.max) }
                                if (jmlJuri >= 2) {
                                    mapJ2[k.id]?.toDoubleOrNull()?.let { r2[k.id] = it.coerceAtMost(k.max) }
                                }
                            }
                            onSave(r1, r2, t1, t2)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
                        modifier = Modifier.weight(1.4f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Koreksi")
                    }
                }
            }
        }
    }
}
