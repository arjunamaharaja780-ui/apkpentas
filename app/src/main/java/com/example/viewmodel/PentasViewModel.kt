package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionDataStore
import com.example.data.SupabaseRepository
import com.example.model.AkunJuri
import com.example.model.AksesItem
import com.example.model.AppRoute
import com.example.model.DefaultData
import com.example.model.HasilCabangSekolah
import com.example.model.KategoriLomba
import com.example.model.KriteriaItem
import com.example.model.LombaPenilaianConfig
import com.example.model.Peserta
import com.example.model.RekapSekolahItem
import com.example.model.ResetDataMode
import com.example.model.SkConfig
import com.example.model.UserRole
import com.example.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class PentasUiState(
    val isLoadingInitial: Boolean = true,
    val isRefreshing: Boolean = false,
    val isBusy: Boolean = false,
    val busyMessage: String = "",
    val session: UserSession? = null,
    val currentRoute: AppRoute = AppRoute.HOME,
    val aksesList: List<AksesItem> = DefaultData.DEFAULT_AKSES,
    val juriList: List<AkunJuri> = emptyList(),
    val pesertaList: List<Peserta> = emptyList(),
    val noUrutMap: Map<String, String> = emptyMap(),
    val daftarSekolah: List<String> = DefaultData.DEFAULT_SEKOLAH,
    val kategoriLomba: List<KategoriLomba> = DefaultData.DEFAULT_KATEGORI,
    val namaKecamatan: String = DefaultData.DEFAULT_KECAMATAN,
    val tahunPelaksanaan: String = DefaultData.DEFAULT_TAHUN,
    val runningText: String = DefaultData.defaultRunningText(),
    val appLogoDataUrl: String = "",
    val skConfig: SkConfig = SkConfig(),
    val konfigurasiPenilaian: Map<String, LombaPenilaianConfig> = emptyMap(),
    val dbUrl: String = DefaultData.DEFAULT_SUPABASE_URL,
    val dbAnonKey: String = DefaultData.DEFAULT_SUPABASE_ANON_KEY,
    val statusMessage: String? = null,
    val isStatusError: Boolean = false,
    val lastSyncSuccess: Boolean = false,
    val connectionError: String? = null
) {
    val isCustomDb: Boolean
        get() = dbUrl != DefaultData.DEFAULT_SUPABASE_URL || dbAnonKey != DefaultData.DEFAULT_SUPABASE_ANON_KEY

    fun credentialFor(role: String): String {
        return aksesList.firstOrNull { it.role.equals(role, ignoreCase = true) }?.password
            ?: DefaultData.DEFAULT_AKSES.firstOrNull { it.role.equals(role, ignoreCase = true) }?.password
            ?: ""
    }
}

class PentasViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SessionDataStore(application)
    private val repo = SupabaseRepository()

    private val _uiState = MutableStateFlow(PentasUiState())
    val uiState: StateFlow<PentasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (url, key) = dataStore.getDbConfig()
            repo.updateCredentials(url, key)
            val savedSession = dataStore.getSession()
            _uiState.update {
                it.copy(
                    dbUrl = url,
                    dbAnonKey = key,
                    session = savedSession,
                    isLoadingInitial = false
                )
            }
            refreshFromServer(showFeedback = false)
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, isStatusError = false) }
    }

    private fun showToast(msg: String, isError: Boolean = false) {
        _uiState.update { it.copy(statusMessage = msg, isStatusError = isError) }
    }

    fun navigate(route: AppRoute) {
        if (_uiState.value.session == null) return
        _uiState.update { it.copy(currentRoute = route) }
    }

    fun refreshFromServer(showFeedback: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, connectionError = null) }
            try {
                val snapshot = repo.fetchAllData()
                _uiState.update { state ->
                    state.copy(
                        isRefreshing = false,
                        aksesList = snapshot.aksesList,
                        juriList = snapshot.juriList,
                        pesertaList = snapshot.pesertaList,
                        noUrutMap = snapshot.noUrutMap,
                        daftarSekolah = snapshot.daftarSekolah,
                        kategoriLomba = snapshot.kategoriLomba,
                        namaKecamatan = snapshot.namaKecamatan,
                        tahunPelaksanaan = snapshot.tahunPelaksanaan,
                        runningText = snapshot.runningText,
                        appLogoDataUrl = snapshot.appLogoDataUrl,
                        skConfig = snapshot.skConfig,
                        konfigurasiPenilaian = snapshot.konfigurasiPenilaian,
                        lastSyncSuccess = true,
                        connectionError = null
                    )
                }
                if (showFeedback) {
                    showToast("Data berhasil disegarkan dari Supabase.")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        connectionError = e.message ?: "Gagal terhubung ke server Supabase"
                    )
                }
                if (showFeedback) {
                    showToast("Gagal memuat data: ${e.message}", isError = true)
                }
            }
        }
    }

    fun login(
        role: UserRole,
        passwordInput: String,
        selectedSchool: String?,
        selectedJuriId: String?
    ): Boolean {
        val state = _uiState.value
        val pwd = passwordInput.trim()
        val newSession: UserSession? = when (role) {
            UserRole.ADMIN -> {
                if (pwd == state.credentialFor("admin")) UserSession(role = UserRole.ADMIN) else null
            }
            UserRole.SEKOLAH -> {
                if (!selectedSchool.isNullOrBlank() && pwd == state.credentialFor("sekolah")) {
                    UserSession(role = UserRole.SEKOLAH, schoolName = selectedSchool)
                } else null
            }
            UserRole.UMUM -> {
                if (pwd == state.credentialFor("umum")) UserSession(role = UserRole.UMUM) else null
            }
            UserRole.JURI -> {
                val juri = state.juriList.firstOrNull { it.id == selectedJuriId }
                if (juri != null && pwd == juri.password) {
                    UserSession(
                        role = UserRole.JURI,
                        juriLomba = juri.lomba,
                        juriKe = juri.juriKe,
                        juriName = juri.nama
                    )
                } else null
            }
        }

        return if (newSession != null) {
            viewModelScope.launch { dataStore.saveSession(newSession) }
            _uiState.update { it.copy(session = newSession, currentRoute = AppRoute.HOME) }
            true
        } else {
            showToast("Akses Ditolak: Kredensial atau kata sandi tidak cocok.", isError = true)
            false
        }
    }

    fun logout() {
        viewModelScope.launch { dataStore.clearSession() }
        _uiState.update { it.copy(session = null, currentRoute = AppRoute.HOME) }
    }

    // ================= SCORING & CONFIG HELPERS =================

    fun getKonfigurasiLomba(lomba: String): LombaPenilaianConfig {
        val clean = DefaultData.normalisasiNamaLomba(lomba)
        if (clean.isBlank()) return LombaPenilaianConfig()
        val existing = _uiState.value.konfigurasiPenilaian[clean]
        if (existing != null && existing.kriteria.isNotEmpty()) {
            return existing
        }
        return LombaPenilaianConfig(
            jumlahJuri = 2,
            modeDuaJuri = "semua",
            kriteria = DefaultData.getDefaultKriteriaByLomba(clean)
        )
    }

    fun apakahKriteriaAktifUntukJuri(
        cfg: LombaPenilaianConfig,
        kriteria: KriteriaItem,
        juriKe: Int
    ): Boolean {
        if (cfg.jumlahJuri == 2 && cfg.modeDuaJuri == "terpisah") {
            if (kriteria.juriTarget == "all") return true
            return kriteria.juriTarget == juriKe.toString()
        }
        return true
    }

    fun getKriteriaUntukJuri(lomba: String, juriKe: Int? = null): List<KriteriaItem> {
        val cfg = getKonfigurasiLomba(lomba)
        return if (juriKe != null) {
            cfg.kriteria.filter { apakahKriteriaAktifUntukJuri(cfg, it, juriKe) }
        } else {
            cfg.kriteria
        }
    }

    fun getTotalMaksimalKriteria(lomba: String, juriKe: Int? = null): Double {
        return getKriteriaUntukJuri(lomba, juriKe).sumOf { it.max }
    }

    fun getTotalMaksimalGabunganLomba(lomba: String): Double {
        val cfg = getKonfigurasiLomba(lomba)
        var total = 0.0
        for (j in 1..cfg.jumlahJuri) {
            total += getTotalMaksimalKriteria(lomba, j)
        }
        return total
    }

    fun ambilRincianKriteriaJuri(peserta: Peserta, juriKe: Int): Map<String, Double> {
        val listK = getKriteriaUntukJuri(peserta.lomba, juriKe)
        val blokJuri = peserta.rincianKriteria[juriKe] ?: emptyMap()
        val hasil = mutableMapOf<String, Double>()
        val keysBlok = blokJuri.keys.toList()

        listK.forEachIndexed { idx, k ->
            val direct = blokJuri[k.id]
            if (direct != null) {
                hasil[k.id] = direct
            } else if (idx < keysBlok.size) {
                val fallback = blokJuri[keysBlok[idx]]
                if (fallback != null) hasil[k.id] = fallback
            }
        }

        val totalSkorJuri = when (juriKe) {
            1 -> peserta.skorJ1
            2 -> peserta.skorJ2
            3 -> peserta.skorJ3
            else -> 0.0
        }

        if (hasil.isEmpty() && totalSkorJuri > 0 && listK.isNotEmpty()) {
            val maxTotal = getTotalMaksimalKriteria(peserta.lomba, juriKe).takeIf { it > 0 } ?: 100.0
            var sisa = totalSkorJuri
            listK.forEachIndexed { idx, k ->
                if (idx == listK.lastIndex) {
                    hasil[k.id] = max(0.0, (sisa * 10.0).roundToInt() / 10.0)
                } else {
                    val proporsi = ((totalSkorJuri * (k.max / maxTotal)).roundToInt()).toDouble()
                    val nilaiAspek = min(k.max, max(0.0, proporsi))
                    hasil[k.id] = nilaiAspek
                    sisa -= nilaiAspek
                }
            }
        }
        return hasil
    }

    fun hitungRekapSekolahDanJuaraUmum(): List<RekapSekolahItem> {
        val state = _uiState.value
        val semuaSekolah = (state.daftarSekolah + state.pesertaList.map { it.sekolah }.filter { it.isNotBlank() })
            .distinct()
            .sorted()

        val mapPeringkat = mutableMapOf<String, List<Peserta>>()
        state.kategoriLomba.forEach { kat ->
            val cleanKat = DefaultData.normalisasiNamaLomba(kat.nama)
            mapPeringkat[cleanKat] = state.pesertaList
                .filter { DefaultData.normalisasiNamaLomba(it.lomba) == cleanKat && it.skorAkhir > 0 }
                .sortedByDescending { it.skorAkhir }
        }

        val labelGelar = listOf("Juara 1", "Juara 2", "Juara 3", "Harapan 1", "Harapan 2", "Harapan 3")
        val poinGelar = listOf(5, 3, 1, 0, 0, 0)

        return semuaSekolah.map { namaSekolah ->
            val hasilCabang = mutableMapOf<String, HasilCabangSekolah>()
            var j1 = 0
            var j2 = 0
            var j3 = 0
            var h1 = 0
            var h2 = 0
            var h3 = 0
            var totalPoin = 0
            var totalSkor = 0.0
            var jmlKontingen = 0

            state.kategoriLomba.forEach { kat ->
                val cleanKat = DefaultData.normalisasiNamaLomba(kat.nama)
                val pesertaS = state.pesertaList.filter {
                    it.sekolah == namaSekolah && DefaultData.normalisasiNamaLomba(it.lomba) == cleanKat
                }
                if (pesertaS.isNotEmpty()) {
                    jmlKontingen += pesertaS.size
                    val terbaik = pesertaS.maxByOrNull { it.skorAkhir } ?: pesertaS.first()
                    val skor = terbaik.skorAkhir
                    totalSkor += skor

                    val daftarUrut = mapPeringkat[cleanKat] ?: emptyList()
                    val idxRank = daftarUrut.indexOfFirst { it.id == terbaik.id }
                    var gelar = "-"
                    if (skor > 0 && idxRank != -1) {
                        if (idxRank < 6) {
                            gelar = labelGelar[idxRank]
                            totalPoin += poinGelar[idxRank]
                            when (idxRank) {
                                0 -> j1++
                                1 -> j2++
                                2 -> j3++
                                3 -> h1++
                                4 -> h2++
                                5 -> h3++
                            }
                        } else {
                            gelar = "Peringkat ${idxRank + 1}"
                        }
                    }
                    hasilCabang[cleanKat] = HasilCabangSekolah(
                        ada = true,
                        nama = terbaik.nama,
                        noPeserta = DefaultData.formatDuaDigit(terbaik.noPeserta),
                        skor = skor,
                        gelar = gelar,
                        peringkat = if (idxRank >= 0) idxRank + 1 else 0
                    )
                } else {
                    hasilCabang[cleanKat] = HasilCabangSekolah(
                        ada = false,
                        nama = "-",
                        noPeserta = "-",
                        skor = 0.0,
                        gelar = "-"
                    )
                }
            }

            RekapSekolahItem(
                sekolah = namaSekolah,
                noUrut = DefaultData.formatDuaDigit(state.noUrutMap[namaSekolah] ?: "-"),
                jmlKontingen = jmlKontingen,
                j1 = j1,
                j2 = j2,
                j3 = j3,
                h1 = h1,
                h2 = h2,
                h3 = h3,
                totalPoin = totalPoin,
                totalSkorAkumulasi = totalSkor,
                hasilCabang = hasilCabang
            )
        }
    }

    // ================= MUTATION ACTIONS =================

    private fun runMutation(busyMsg: String, successMsg: String? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, busyMessage = busyMsg) }
            try {
                block()
                val snapshot = repo.fetchAllData()
                _uiState.update { state ->
                    state.copy(
                        isBusy = false,
                        busyMessage = "",
                        aksesList = snapshot.aksesList,
                        juriList = snapshot.juriList,
                        pesertaList = snapshot.pesertaList,
                        noUrutMap = snapshot.noUrutMap,
                        daftarSekolah = snapshot.daftarSekolah,
                        kategoriLomba = snapshot.kategoriLomba,
                        namaKecamatan = snapshot.namaKecamatan,
                        tahunPelaksanaan = snapshot.tahunPelaksanaan,
                        runningText = snapshot.runningText,
                        appLogoDataUrl = snapshot.appLogoDataUrl,
                        skConfig = snapshot.skConfig,
                        konfigurasiPenilaian = snapshot.konfigurasiPenilaian
                    )
                }
                if (successMsg != null) showToast(successMsg)
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, busyMessage = "") }
                showToast("Gagal: ${e.message}", isError = true)
            }
        }
    }

    fun submitPendaftaran(names: List<String>, sekolah: String, lombaRaw: String, onSuccess: () -> Unit) {
        val gabungNama = names.map { it.trim() }.filter { it.isNotBlank() }.joinToString(", ")
        val lomba = DefaultData.normalisasiNamaLomba(lombaRaw)
        if (gabungNama.isBlank() || sekolah.isBlank() || lomba.isBlank()) {
            showToast("Pastikan nama peserta, sekolah, dan cabang lomba telah diisi.", isError = true)
            return
        }
        val noOtomatis = _uiState.value.noUrutMap[sekolah]?.let { DefaultData.formatDuaDigit(it) } ?: "-"
        val newPeserta = Peserta(
            id = "PST-${System.currentTimeMillis()}-${(1000..9999).random()}",
            noPeserta = noOtomatis,
            nama = gabungNama,
            sekolah = sekolah,
            lomba = lomba
        )
        runMutation("Mendaftarkan Peserta...", "Peserta berhasil didaftarkan!") {
            repo.insertPeserta(listOf(newPeserta))
            onSuccess()
        }
    }

    fun updatePesertaInfo(id: String, noPeserta: String, nama: String, sekolah: String) {
        runMutation("Menyimpan Data Peserta...", "Data peserta berhasil diperbarui.") {
            repo.updatePesertaInfo(id, DefaultData.formatDuaDigit(noPeserta), nama.trim(), sekolah)
        }
    }

    fun deletePeserta(id: String) {
        runMutation("Menghapus Peserta...", "Peserta telah dihapus.") {
            repo.deletePeserta(id)
        }
    }

    fun simpanNoUrutSekolah(updatedMap: Map<String, String>) {
        val formatted = updatedMap.mapValues { (_, v) ->
            if (v.isBlank()) "-" else DefaultData.formatDuaDigit(v)
        }
        runMutation("Menyimpan & Sinkronkan Nomor Tampil...", "Nomor tampil sekolah tersimpan & tersinkron.") {
            repo.upsertNoUrutAndSyncPeserta(formatted)
        }
    }

    fun submitPenilaianPeserta(
        pesertaId: String,
        lombaRaw: String,
        juriPos: Int,
        nilaiPerKriteria: Map<String, Double>
    ) {
        val lomba = DefaultData.normalisasiNamaLomba(lombaRaw)
        val peserta = _uiState.value.pesertaList.firstOrNull { it.id == pesertaId } ?: return
        val cfg = getKonfigurasiLomba(lomba)
        val totalJuri = nilaiPerKriteria.values.sum()

        val existingRincian = peserta.rincianKriteria.toMutableMap()
        existingRincian[juriPos] = nilaiPerKriteria

        var s1 = peserta.skorJ1
        var s2 = peserta.skorJ2
        var s3 = peserta.skorJ3
        when (juriPos) {
            1 -> s1 = totalJuri
            2 -> s2 = totalJuri
            3 -> s3 = totalJuri
        }
        val finalJ2 = if (cfg.jumlahJuri == 1) 0.0 else s2
        val skorAkhir = when (cfg.jumlahJuri) {
            1 -> s1
            3 -> s1 + finalJ2 + s3
            else -> s1 + finalJ2
        }

        runMutation("Menyimpan Nilai Juri...", "Nilai berhasil dikunci & disimpan!") {
            repo.updatePesertaScore(peserta.id, s1, finalJ2, s3, skorAkhir, existingRincian)
        }
    }

    fun submitLccPenilaian(lombaRaw: String, ranks: List<Pair<String, Double>>) {
        val lomba = DefaultData.normalisasiNamaLomba(lombaRaw)
        val noUrutMap = _uiState.value.noUrutMap
        val now = System.currentTimeMillis()
        val rows = ranks.mapIndexedNotNull { idx, (sekolah, skor) ->
            if (sekolah.isBlank()) null
            else Peserta(
                id = "LCC-$now-$idx",
                noPeserta = DefaultData.formatDuaDigit(noUrutMap[sekolah] ?: (idx + 1).toString()),
                nama = "Regu $sekolah",
                sekolah = sekolah,
                lomba = lomba,
                skorAkhir = skor,
                skorJ1 = skor,
                skorJ2 = 0.0
            )
        }
        if (rows.isEmpty()) {
            showToast("Pilih minimal 1 sekolah pemenang LCC.", isError = true)
            return
        }
        runMutation("Menyimpan Hasil Cerdas Cermat...", "Nilai Cerdas Cermat PAI tersimpan!") {
            repo.deletePesertaByLomba(lomba)
            repo.insertPeserta(rows)
        }
    }

    fun koreksiNilaiPeserta(
        pesertaId: String,
        rincianJ1: Map<String, Double>,
        rincianJ2: Map<String, Double>,
        totalJ1: Double,
        totalJ2: Double
    ) {
        val p = _uiState.value.pesertaList.firstOrNull { it.id == pesertaId } ?: return
        val newRincian = p.rincianKriteria.toMutableMap()
        if (rincianJ1.isNotEmpty()) newRincian[1] = rincianJ1
        if (rincianJ2.isNotEmpty()) newRincian[2] = rincianJ2
        val totalAkhir = totalJ1 + totalJ2 + p.skorJ3
        runMutation("Menyimpan Koreksi Nilai...", "Nilai berhasil dikoreksi!") {
            repo.updatePesertaScore(p.id, totalJ1, totalJ2, p.skorJ3, totalAkhir, newRincian)
        }
    }

    fun resetNilaiPeserta(pesertaId: String) {
        runMutation("Mereset Nilai Peserta...", "Nilai peserta direset ke 0.") {
            repo.updatePesertaScore(pesertaId, 0.0, 0.0, 0.0, 0.0, emptyMap())
        }
    }

    fun updateLocalKonfigurasiPenilaian(lomba: String, cfg: LombaPenilaianConfig) {
        val clean = DefaultData.normalisasiNamaLomba(lomba)
        _uiState.update { state ->
            val updated = state.konfigurasiPenilaian.toMutableMap()
            updated[clean] = cfg
            state.copy(konfigurasiPenilaian = updated)
        }
    }

    fun simpanSemuaKonfigurasiPenilaian() {
        val currentMap = _uiState.value.konfigurasiPenilaian.toMutableMap()
        _uiState.value.kategoriLomba.forEach { kat ->
            val clean = DefaultData.normalisasiNamaLomba(kat.nama)
            if (!currentMap.containsKey(clean)) {
                currentMap[clean] = getKonfigurasiLomba(clean)
            }
        }
        runMutation("Menyimpan Pengaturan Penilaian...", "Pengaturan penilaian tersimpan ke database!") {
            repo.saveKonfigurasiPenilaian(currentMap)
        }
    }

    fun resetPengaturanPenilaianDefault() {
        val defaultMap = mutableMapOf<String, LombaPenilaianConfig>()
        _uiState.value.kategoriLomba.forEach { kat ->
            val clean = DefaultData.normalisasiNamaLomba(kat.nama)
            defaultMap[clean] = LombaPenilaianConfig(
                jumlahJuri = 2,
                modeDuaJuri = "semua",
                kriteria = DefaultData.getDefaultKriteriaByLomba(clean)
            )
        }
        runMutation("Mereset Pengaturan Penilaian...", "Pengaturan penilaian dikembalikan ke standar awal.") {
            repo.saveKonfigurasiPenilaian(defaultMap)
        }
    }

    fun saveSkConfig(config: SkConfig) {
        runMutation("Menyimpan Properti SK...", "Properti SK Penetapan Juara tersimpan!") {
            repo.saveSkConfig(config)
        }
    }

    fun saveRunningText(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) {
            showToast("Kalimat running text tidak boleh kosong.", isError = true)
            return
        }
        runMutation("Menyimpan Running Text...", "Running Text diperbarui!") {
            repo.upsertAksesRow("running_text", "INFO", clean)
        }
    }

    fun updateNamaKecamatan(kecamatan: String) {
        val clean = kecamatan.trim()
        if (clean.isBlank()) return
        runMutation("Menyimpan Nama Kecamatan...", "Nama Kecamatan diperbarui!") {
            repo.upsertAksesRow("nama_kecamatan", clean, clean)
        }
    }

    fun updateTahunPelaksanaan(tahun: String) {
        val clean = tahun.trim()
        if (clean.isBlank()) return
        runMutation("Menyimpan Tahun Pelaksanaan...", "Tahun Pelaksanaan diperbarui!") {
            repo.upsertAksesRow("tahun_pelaksanaan", clean, clean)
        }
    }

    fun uploadLogoBase64(dataUrl: String) {
        runMutation("Mengunggah Logo Aplikasi...", "Logo resmi aplikasi tersimpan!") {
            repo.upsertAksesRow("app_logo", "LOGO", dataUrl)
        }
    }

    fun hapusLogo() {
        runMutation("Mereset Logo...", "Logo direset ke ikon default.") {
            repo.deleteAksesRow("app_logo")
        }
    }

    fun editAksesPassword(role: String, newPassword: String) {
        val clean = newPassword.trim()
        if (clean.isBlank()) return
        runMutation("Memperbarui Sandi...", "Kata sandi ${role.uppercase()} diperbarui!") {
            repo.upsertAksesRow(role.lowercase(), clean, "Password $role")
        }
    }

    fun tambahJuri(nama: String, lombaRaw: String, juriKe: Int) {
        val cleanNama = nama.trim()
        val lomba = DefaultData.normalisasiNamaLomba(lombaRaw)
        if (cleanNama.isBlank() || lomba.isBlank()) return
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val pass = (1..6).map { chars.random() }.joinToString("")
        val juri = AkunJuri(
            id = "JUR-${System.currentTimeMillis()}",
            nama = cleanNama,
            lomba = lomba,
            juriKe = juriKe,
            password = pass
        )
        runMutation("Membuat Akun Juri...", "Akun Juri dibuat! Sandi: $pass") {
            repo.insertJuri(juri)
        }
    }

    fun hapusJuri(id: String) {
        runMutation("Menghapus Akun Juri...", "Akun juri telah dihapus.") {
            repo.deleteJuri(id)
        }
    }

    fun tambahSekolah(namaBaru: String) {
        val clean = namaBaru.trim().uppercase()
        if (clean.isBlank()) return
        val current = _uiState.value.daftarSekolah
        if (current.contains(clean)) {
            showToast("Sekolah sudah terdaftar.", isError = true)
            return
        }
        runMutation("Menambah Sekolah...", "Sekolah $clean ditambahkan!") {
            repo.saveDaftarSekolah(current + clean)
        }
    }

    fun hapusSekolah(nama: String) {
        val updated = _uiState.value.daftarSekolah.filter { it != nama }
        runMutation("Menghapus Sekolah...", "Sekolah dihapus dari daftar.") {
            repo.saveDaftarSekolah(updated)
        }
    }

    fun tambahKategoriLomba(nama: String, kuota: Int, desc: String) {
        val clean = DefaultData.normalisasiNamaLomba(nama)
        if (clean.isBlank()) return
        val newKat = KategoriLomba(
            id = "lomba-${System.currentTimeMillis()}",
            nama = clean,
            icon = "fa-trophy",
            desc = desc.ifBlank { "Cabang perlombaan PENTAS PAI." },
            maxPeserta = kuota.coerceIn(1, 10)
        )
        runMutation("Menambah Cabang Lomba...", "Cabang lomba $clean ditambahkan!") {
            repo.saveKategoriLomba(_uiState.value.kategoriLomba + newKat)
        }
    }

    fun hapusKategoriLomba(index: Int) {
        val list = _uiState.value.kategoriLomba.toMutableList()
        if (index !in list.indices) return
        list.removeAt(index)
        runMutation("Menghapus Cabang Lomba...", "Cabang lomba dihapus.") {
            repo.saveKategoriLomba(list)
        }
    }

    fun importPesertaList(list: List<Peserta>) {
        if (list.isEmpty()) {
            showToast("Tidak ada baris peserta valid untuk diimpor.", isError = true)
            return
        }
        runMutation("Mengimpor ${list.size} Peserta...", "${list.size} peserta berhasil diimpor!") {
            repo.insertPeserta(list)
        }
    }

    fun testKoneksiDatabase(url: String, key: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, busyMessage = "Menguji Koneksi Supabase...") }
            try {
                val (latency, count) = repo.testConnection(url, key)
                _uiState.update { it.copy(isBusy = false, busyMessage = "") }
                showToast("Koneksi Normal! Respons: ${latency}ms • Tabel Peserta: $count baris")
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, busyMessage = "") }
                showToast("Koneksi Gagal: ${e.message}", isError = true)
            }
        }
    }

    fun simpanDatabaseConfig(url: String, key: String) {
        val cleanUrl = url.trim()
        val cleanKey = key.trim()
        if (!cleanUrl.startsWith("http") || cleanKey.length < 20) {
            showToast("Format URL API atau Anon Key Supabase tidak valid.", isError = true)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, busyMessage = "Menghubungkan ke Database...") }
            try {
                repo.testConnection(cleanUrl, cleanKey)
                dataStore.saveDbConfig(cleanUrl, cleanKey)
                repo.updateCredentials(cleanUrl, cleanKey)
                val snapshot = repo.fetchAllData()
                _uiState.update { state ->
                    state.copy(
                        isBusy = false,
                        busyMessage = "",
                        dbUrl = cleanUrl,
                        dbAnonKey = cleanKey,
                        aksesList = snapshot.aksesList,
                        juriList = snapshot.juriList,
                        pesertaList = snapshot.pesertaList,
                        noUrutMap = snapshot.noUrutMap,
                        daftarSekolah = snapshot.daftarSekolah,
                        kategoriLomba = snapshot.kategoriLomba,
                        namaKecamatan = snapshot.namaKecamatan,
                        tahunPelaksanaan = snapshot.tahunPelaksanaan,
                        runningText = snapshot.runningText,
                        appLogoDataUrl = snapshot.appLogoDataUrl,
                        skConfig = snapshot.skConfig,
                        konfigurasiPenilaian = snapshot.konfigurasiPenilaian
                    )
                }
                showToast("Database Supabase berhasil terhubung!")
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, busyMessage = "") }
                showToast("Gagal Terhubung: ${e.message}", isError = true)
            }
        }
    }

    fun resetDatabaseDefault() {
        viewModelScope.launch {
            dataStore.clearDbConfig()
            repo.updateCredentials(DefaultData.DEFAULT_SUPABASE_URL, DefaultData.DEFAULT_SUPABASE_ANON_KEY)
            _uiState.update {
                it.copy(
                    dbUrl = DefaultData.DEFAULT_SUPABASE_URL,
                    dbAnonKey = DefaultData.DEFAULT_SUPABASE_ANON_KEY
                )
            }
            refreshFromServer(showFeedback = true)
        }
    }

    suspend fun generateBackupJson(): String {
        val state = _uiState.value
        return repo.exportBackupJson(state.namaKecamatan, state.tahunPelaksanaan)
    }

    fun restoreBackupJson(rawJson: String) {
        runMutation("Memulihkan Database dari Cadangan...", "Database berhasil dipulihkan!") {
            repo.restoreBackupJson(rawJson)
        }
    }

    fun executeResetData(mode: ResetDataMode) {
        val msg = when (mode) {
            ResetDataMode.NILAI_SAJA -> "Seluruh nilai juri berhasil direset ke 0."
            ResetDataMode.PESERTA_NILAI -> "Semua data peserta dan nilai berhasil dihapus."
            ResetDataMode.TOTAL_SEMUA -> "Reset total seluruh data berhasil dilaksanakan."
        }
        runMutation("Memproses Reset Data...", msg) {
            when (mode) {
                ResetDataMode.NILAI_SAJA -> repo.resetAllScores()
                ResetDataMode.PESERTA_NILAI -> repo.deleteAllPeserta()
                ResetDataMode.TOTAL_SEMUA -> repo.deleteTotalAllData()
            }
        }
    }
}
