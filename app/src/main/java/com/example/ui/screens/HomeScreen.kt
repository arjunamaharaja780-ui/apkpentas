package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.AppRoute
import com.example.model.DefaultData
import com.example.model.UserRole
import com.example.ui.components.IslamicGeometricBackdrop
import com.example.ui.components.LogoOrEmblem
import com.example.ui.components.StatTile
import com.example.ui.components.iconForCategory
import com.example.ui.theme.Blue700
import com.example.ui.theme.PaiEmerald100
import com.example.ui.theme.PaiEmerald200
import com.example.ui.theme.PaiEmerald50
import com.example.ui.theme.PaiEmerald600
import com.example.ui.theme.PaiEmerald700
import com.example.ui.theme.PaiEmerald800
import com.example.ui.theme.PaiEmerald900
import com.example.ui.theme.PaiGold100
import com.example.ui.theme.PaiGold600
import com.example.ui.theme.PaiGold700
import com.example.ui.theme.Purple700
import com.example.ui.theme.Slate900
import com.example.viewmodel.PentasUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: PentasUiState,
    onNavigate: (AppRoute) -> Unit
) {
    val role = uiState.session?.role
    val totalPeserta = uiState.pesertaList.size
    val sudahDinilai = remember(uiState.pesertaList) { uiState.pesertaList.count { it.skorAkhir > 0 } }
    val totalSekolah = uiState.daftarSekolah.size
    val totalCabang = uiState.kategoriLomba.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Slate900, PaiEmerald900)
                        )
                    )
            ) {
                IslamicGeometricBackdrop(
                    modifier = Modifier.matchParentSize(),
                    darkMode = true
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LogoOrEmblem(logoDataUrl = uiState.appLogoDataUrl, size = 72.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "KECAMATAN ${uiState.namaKecamatan.uppercase()} ${uiState.tahunPelaksanaan}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PaiEmerald200,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "APLIKASI PENTAS PAI KECAMATAN ${uiState.namaKecamatan.uppercase()} TAHUN ${uiState.tahunPelaksanaan}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"Menggali Talenta Islami, Mewujudkan Generasi Berkarakter dan Berprestasi\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = PaiEmerald100,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                when (role) {
                                    UserRole.ADMIN -> {
                                        Button(
                                            onClick = { onNavigate(AppRoute.CETAK_EXCEL) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.height(48.dp).testTag("hero_cetak_excel_button")
                                        ) {
                                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ekspor/Impor & Cetak")
                                        }
                                        Button(
                                            onClick = { onNavigate(AppRoute.REKAP_ADMIN) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Slate900),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.height(48.dp).testTag("hero_rekap_admin_button")
                                        ) {
                                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Rekap Nilai")
                                        }
                                    }
                                    UserRole.SEKOLAH -> {
                                        Button(
                                            onClick = { onNavigate(AppRoute.REGISTER) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Slate900),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.height(48.dp).testTag("hero_register_button")
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Daftar Lomba")
                                        }
                                    }
                                    UserRole.JURI -> {
                                        Button(
                                            onClick = { onNavigate(AppRoute.PENILAIAN) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.height(48.dp).testTag("hero_penilaian_button")
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Lembar Penilaian")
                                        }
                                    }
                                    UserRole.UMUM, null -> {
                                        Button(
                                            onClick = { onNavigate(AppRoute.LEADERBOARD) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Slate900),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.height(48.dp).testTag("hero_leaderboard_button")
                                        ) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Lihat Klasemen")
                                        }
                                    }
                                }
                                if (role != UserRole.UMUM) {
                                    OutlinedButton(
                                        onClick = { onNavigate(AppRoute.LEADERBOARD) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Klasemen")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Stats Summary Row
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        label = "Total Peserta",
                        value = totalPeserta.toString(),
                        icon = Icons.Default.Groups,
                        accentColor = PaiEmerald600,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Sudah Dinilai",
                        value = sudahDinilai.toString(),
                        icon = Icons.Default.HowToReg,
                        accentColor = Blue700,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        label = "Sekolah Peserta",
                        value = totalSekolah.toString(),
                        icon = Icons.Default.School,
                        accentColor = Purple700,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Cabang Lomba",
                        value = totalCabang.toString(),
                        icon = Icons.Default.EmojiEvents,
                        accentColor = PaiGold600,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Kategori Perlombaan
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Kategori Perlombaan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Cabang lomba yang dipertandingkan dalam PENTAS PAI ${uiState.tahunPelaksanaan}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(uiState.kategoriLomba.size) { index ->
            val kat = uiState.kategoriLomba[index]
            val cleanName = DefaultData.normalisasiNamaLomba(kat.nama)
            val kuota = kat.maxPeserta.takeIf { it > 0 } ?: DefaultData.getMaxPesertaCabang(cleanName, uiState.kategoriLomba)
            val pendaftarCount = uiState.pesertaList.count { DefaultData.normalisasiNamaLomba(it.lomba) == cleanName }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaiEmerald50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForCategory(kat.icon),
                            contentDescription = cleanName,
                            tint = PaiEmerald700,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = cleanName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                color = if (kuota > 1) PaiGold100 else PaiEmerald100,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (kuota > 1) "Beregu ($kuota)" else "Perorangan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (kuota > 1) PaiGold700 else PaiEmerald800,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = kat.desc.ifBlank { "Cabang lomba PENTAS PAI." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$pendaftarCount peserta terdaftar",
                            style = MaterialTheme.typography.labelSmall,
                            color = PaiEmerald700
                        )
                    }
                }
            }
        }
    }
}
