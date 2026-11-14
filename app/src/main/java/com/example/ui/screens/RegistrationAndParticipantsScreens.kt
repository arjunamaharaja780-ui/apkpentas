package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateListOf
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
import com.example.model.Peserta
import com.example.model.UserRole
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
import com.example.ui.theme.Purple100
import com.example.ui.theme.Purple700
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose700
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.viewmodel.PentasUiState

@Composable
fun RegisterScreen(
    uiState: PentasUiState,
    onSubmit: (names: List<String>, sekolah: String, lomba: String, onSuccess: () -> Unit) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val isSekolahRole = uiState.session?.role == UserRole.SEKOLAH
    val lockedSchool = uiState.session?.schoolName.orEmpty()

    var selectedSekolah by remember(isSekolahRole, lockedSchool) {
        mutableStateOf(if (isSekolahRole) lockedSchool else "")
    }
    var selectedLomba by remember { mutableStateOf("") }
    val memberNames = remember { mutableStateListOf("") }

    val sortedSekolah = remember(uiState.daftarSekolah) { uiState.daftarSekolah.sorted() }
    val lombaOptions = remember(uiState.kategoriLomba) {
        uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
    }
    val maxPeserta = remember(selectedLomba, uiState.kategoriLomba) {
        if (selectedLomba.isBlank()) 1 else DefaultData.getMaxPesertaCabang(selectedLomba, uiState.kategoriLomba)
    }

    LaunchedEffect(maxPeserta) {
        while (memberNames.size > maxPeserta) {
            memberNames.removeAt(memberNames.lastIndex)
        }
        if (memberNames.isEmpty()) memberNames.add("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Form Pendaftaran Peserta",
            subtitle = "Daftarkan peserta lomba perwakilan sekolah Anda.",
            badge = "Registrasi Kontingen"
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
                        text = "Data Registrasi Peserta",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DropdownField(
                        label = "Asal Sekolah *",
                        selectedValue = selectedSekolah,
                        placeholder = "-- Pilih SD / SDIT --",
                        options = sortedSekolah,
                        onSelected = { selectedSekolah = it },
                        enabled = !isSekolahRole,
                        leadingIcon = Icons.Default.School,
                        testTagName = "register_school_dropdown"
                    )

                    DropdownField(
                        label = "Mata Lomba yang Diikuti *",
                        selectedValue = selectedLomba,
                        placeholder = "-- Silakan Pilih Kategori Lomba --",
                        options = lombaOptions,
                        onSelected = { selectedLomba = it },
                        leadingIcon = Icons.Default.EmojiEvents,
                        testTagName = "register_lomba_dropdown"
                    )

                    if (maxPeserta > 1) {
                        Surface(
                            color = PaiEmerald50,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = PaiEmerald700, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Cabang beregu: klik (+) untuk menambah anggota regu (maksimal $maxPeserta peserta).",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PaiEmerald800
                                )
                            }
                        }
                    }

                    memberNames.forEachIndexed { idx, value ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = value,
                                onValueChange = { memberNames[idx] = it },
                                label = {
                                    Text(if (maxPeserta > 1) "Anggota Regu ${idx + 1} *" else "Nama Lengkap Peserta *")
                                },
                                placeholder = { Text("Cth: Ahmad Farhan") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("register_name_input_$idx")
                            )
                            if (maxPeserta > 1 && idx == 0) {
                                Surface(
                                    color = PaiEmerald100,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (memberNames.size >= maxPeserta) {
                                                onShowMessage("Maksimal $maxPeserta peserta pada regu cabang ini.")
                                            } else {
                                                memberNames.add("")
                                            }
                                        },
                                        modifier = Modifier.testTag("register_add_member_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah Anggota", tint = PaiEmerald700)
                                    }
                                }
                            } else if (idx > 0) {
                                Surface(
                                    color = Rose100,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    IconButton(onClick = { memberNames.removeAt(idx) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Hapus Anggota", tint = Rose700)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            onSubmit(memberNames.toList(), selectedSekolah, selectedLomba) {
                                memberNames.clear()
                                memberNames.add("")
                                selectedLomba = ""
                                if (!isSekolahRole) selectedSekolah = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_submit_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daftarkan Peserta", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun PesertaScreen(
    uiState: PentasUiState,
    onUpdatePeserta: (id: String, noPeserta: String, nama: String, sekolah: String) -> Unit,
    onDeletePeserta: (id: String) -> Unit
) {
    val role = uiState.session?.role
    val isAdmin = role == UserRole.ADMIN
    val canEdit = role == UserRole.ADMIN || role == UserRole.SEKOLAH

    var filterLomba by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var editingPeserta by remember { mutableStateOf<Peserta?>(null) }
    var deletingPeserta by remember { mutableStateOf<Peserta?>(null) }

    val filterOptions = remember(uiState.kategoriLomba) {
        listOf("Semua Kategori") + uiState.kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
    }

    val filteredList = remember(uiState.pesertaList, filterLomba, searchQuery, uiState.session) {
        uiState.pesertaList
            .filter { p ->
                val matchLomba = filterLomba == "All" || DefaultData.normalisasiNamaLomba(p.lomba) == filterLomba
                val matchSchool = if (role == UserRole.SEKOLAH) p.sekolah == uiState.session?.schoolName else true
                val q = searchQuery.trim()
                val matchSearch = q.isBlank() ||
                    p.nama.contains(q, ignoreCase = true) ||
                    p.sekolah.contains(q, ignoreCase = true) ||
                    p.noPeserta.contains(q, ignoreCase = true)
                matchLomba && matchSchool && matchSearch
            }
            .sortedWith(DefaultData::compareNoUrut)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(
                title = "Data Pendaftar Peserta",
                subtitle = "Daftar peserta diurutkan otomatis berdasarkan Nomor Tampil (2 digit)."
            )

            DropdownField(
                label = "Filter Berdasarkan Lomba",
                selectedValue = if (filterLomba == "All") "Semua Kategori" else filterLomba,
                options = filterOptions,
                onSelected = { chosen -> filterLomba = if (chosen == "Semua Kategori") "All" else chosen },
                testTagName = "peserta_filter_dropdown"
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama peserta, sekolah, atau no tampil...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("peserta_search_input")
            )
        }

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    title = "Belum Ada Data Peserta",
                    subtitle = "Belum ada peserta yang sesuai dengan filter saat ini."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { p ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                    .size(48.dp)
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
                                Text(
                                    text = p.nama,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = p.sekolah,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = PaiEmerald50,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = DefaultData.normalisasiNamaLomba(p.lomba),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PaiEmerald800,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (canEdit) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { editingPeserta = p },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Blue100)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Blue700, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { deletingPeserta = p },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Rose100)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Rose700, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingPeserta?.let { target ->
        var noText by remember(target.id) { mutableStateOf(DefaultData.formatDuaDigit(target.noPeserta)) }
        var namaText by remember(target.id) { mutableStateOf(target.nama) }
        var sekText by remember(target.id) { mutableStateOf(target.sekolah) }

        AlertDialog(
            onDismissRequest = { editingPeserta = null },
            title = { Text("Perbarui Data Peserta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noText,
                        onValueChange = { noText = it },
                        enabled = isAdmin,
                        label = { Text("Nomor Tampil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = namaText,
                        onValueChange = { namaText = it },
                        label = { Text("Nama Peserta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownField(
                        label = "Asal Sekolah",
                        selectedValue = sekText,
                        options = uiState.daftarSekolah.sorted(),
                        onSelected = { sekText = it },
                        enabled = isAdmin
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePeserta(target.id, noText, namaText, sekText)
                        editingPeserta = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPeserta = null }) { Text("Batal") }
            }
        )
    }

    deletingPeserta?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingPeserta = null },
            title = { Text("Hapus Peserta Ini?") },
            text = { Text("Peserta \"${target.nama}\" (${target.sekolah}) akan dihapus dari database.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePeserta(target.id)
                        deletingPeserta = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose700)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPeserta = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun NoUrutScreen(
    uiState: PentasUiState,
    onSimpanSemua: (Map<String, String>) -> Unit
) {
    val sortedSekolah = remember(uiState.daftarSekolah) { uiState.daftarSekolah.sorted() }
    val draftNoUrut = remember(uiState.noUrutMap, sortedSekolah) {
        mutableStateMapOf<String, String>().apply {
            sortedSekolah.forEach { sek ->
                val v = DefaultData.formatDuaDigit(uiState.noUrutMap[sek])
                put(sek, if (v == "-") "" else v)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "Nomor Tampil Sekolah (2 Digit)",
                subtitle = "Atur atau acak nomor urut tampil kontingen untuk ${sortedSekolah.size} sekolah terdaftar."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val nums = (1..sortedSekolah.size).map { it.toString().padStart(2, '0') }.shuffled()
                        sortedSekolah.forEachIndexed { idx, sek ->
                            draftNoUrut[sek] = nums[idx]
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("nourut_acak_button")
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acak Otomatis")
                }
                Button(
                    onClick = { onSimpanSemua(draftNoUrut.toMap()) },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f).height(48.dp).testTag("nourut_simpan_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan & Sinkron")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(sortedSekolah) { idx, sek ->
                val countPeserta = uiState.pesertaList.count { it.sekolah == sek }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${idx + 1}.",
                            style = MaterialTheme.typography.labelLarge,
                            color = Slate500,
                            modifier = Modifier.width(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sek, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$countPeserta Peserta terdaftar",
                                style = MaterialTheme.typography.labelMedium,
                                color = PaiEmerald700
                            )
                        }
                        OutlinedTextField(
                            value = draftNoUrut[sek] ?: "",
                            onValueChange = { raw ->
                                draftNoUrut[sek] = raw.take(4)
                            },
                            placeholder = { Text("01", textAlign = TextAlign.Center) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = Purple700
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(86.dp)
                        )
                    }
                }
            }
        }
    }
}
