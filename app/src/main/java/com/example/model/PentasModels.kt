package com.example.model

enum class UserRole(val key: String, val label: String) {
    ADMIN("admin", "Administrator"),
    JURI("juri", "Dewan Juri (Khusus Penilaian)"),
    SEKOLAH("sekolah", "Sekolah / Madrasah (Pendaftaran)"),
    UMUM("umum", "Umum (Hanya Lihat Klasemen)");

    companion object {
        fun fromKey(key: String?): UserRole? = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }
}

enum class AppRoute(val title: String) {
    HOME("Beranda Utama"),
    REGISTER("Pendaftaran Peserta"),
    PESERTA("Data Peserta"),
    NO_URUT("Nomor Tampil Sekolah"),
    CETAK_EXCEL("Ekspor/Impor & Cetak"),
    CETAK_JUARA("SK Juara & Rekap Sekolah"),
    LEADERBOARD("Klasemen & Skor"),
    PENILAIAN("Formulir Penilaian"),
    REKAP_JURI("Rekap Nilai Juri"),
    REKAP_ADMIN("Rekap & Koreksi Admin"),
    PENGATURAN_PENILAIAN("Kriteria & Penilaian"),
    AKSES("Pengaturan & Reset Data")
}

data class UserSession(
    val role: UserRole,
    val schoolName: String? = null,
    val juriLomba: String? = null,
    val juriKe: Int? = null,
    val juriName: String? = null
) {
    val displayName: String
        get() = when (role) {
            UserRole.ADMIN -> "Administrator"
            UserRole.JURI -> "${juriName ?: "Juri"} (Juri ${juriKe ?: 1})"
            UserRole.SEKOLAH -> schoolName ?: "Sekolah"
            UserRole.UMUM -> "Penonton Umum"
        }
}

data class KategoriLomba(
    val id: String,
    val nama: String,
    val icon: String = "fa-trophy",
    val desc: String = "Cabang perlombaan PENTAS PAI.",
    val maxPeserta: Int = 1
)

data class KriteriaItem(
    val id: String,
    val label: String,
    val min: Double,
    val max: Double,
    val juriTarget: String = "all" // "all", "1", "2"
)

data class LombaPenilaianConfig(
    val jumlahJuri: Int = 2,
    val modeDuaJuri: String = "semua", // "semua" or "terpisah"
    val kriteria: List<KriteriaItem> = emptyList()
)

data class Peserta(
    val id: String,
    val noPeserta: String,
    val nama: String,
    val sekolah: String,
    val lomba: String,
    val skorAkhir: Double = 0.0,
    val skorJ1: Double = 0.0,
    val skorJ2: Double = 0.0,
    val skorJ3: Double = 0.0,
    val rincianKriteria: Map<Int, Map<String, Double>> = emptyMap()
)

data class AkunJuri(
    val id: String,
    val nama: String,
    val lomba: String,
    val juriKe: Int = 1,
    val password: String
)

data class AksesItem(
    val role: String,
    val password: String,
    val keterangan: String
)

data class SkConfig(
    val nomorSK: String = "001/SK-PAN/PENTAS-PAI/2026",
    val kotaTetap: String = "Bekasi",
    val tanggalSK: String = "15 Juni 2026",
    val lokasiLomba: String = "Kecamatan Bantargebang",
    val ketuaPanitia: String = "Ahmad Fauzi, S.Pd.I",
    val nipKetua: String = "198201152005011003",
    val sekretarisPanitia: String = "Siti Aminah, M.Pd",
    val nipSekretaris: String = "198504202009022001",
    val ketuaKKG: String = "Drs. H. Mulyadi, M.Pd",
    val nipKetuaKKG: String = "196812101993031004"
)

data class HasilCabangSekolah(
    val ada: Boolean,
    val nama: String,
    val noPeserta: String,
    val skor: Double,
    val gelar: String,
    val peringkat: Int = 0
)

data class RekapSekolahItem(
    val sekolah: String,
    val noUrut: String,
    val jmlKontingen: Int,
    val j1: Int,
    val j2: Int,
    val j3: Int,
    val h1: Int,
    val h2: Int,
    val h3: Int,
    val totalPoin: Int,
    val totalSkorAkumulasi: Double,
    val hasilCabang: Map<String, HasilCabangSekolah>
)

enum class PrintDocFormat(val key: String, val label: String) {
    HADIR("hadir", "Daftar Nomor Tampil & Tanda Tangan Kehadiran"),
    JURI("juri", "Lembar Dewan Juri (Blind Judging - Asal Sekolah Disembunyikan)"),
    FORM_PENILAIAN("form-penilaian", "Lembar Penilaian Resmi Dewan Juri"),
    JUARA("juara", "Daftar Pemenang / Juara Lomba")
}

enum class JuaraDocMode(val key: String, val label: String) {
    SK_LENGKAP("sk-lengkap", "1. SK Penetapan Juara Lomba (Resmi & Lengkap)"),
    REKAP_SEKOLAH("rekap-sekolah", "2. Rekap Hasil Perlombaan Per Sekolah (Semua Cabang)"),
    JUARA_UMUM("juara-umum", "3. Klasemen Juara Umum Kontingen Sekolah (Medali & Poin)"),
    DAFTAR_JUARA("daftar-juara", "4. Tabel Daftar Pemenang Per Cabang Lomba")
}

enum class ResetDataMode(val key: String, val title: String, val description: String) {
    NILAI_SAJA(
        "nilai-saja",
        "1. Reset Semua Nilai Juri Saja (Skor Kembali 0)",
        "Daftar peserta dan nomor tampil tetap aman, namun seluruh nilai Juri 1, Juri 2, dan Skor Akhir direset ke 0."
    ),
    PESERTA_NILAI(
        "peserta-nilai",
        "2. Hapus Semua Data Peserta & Nilai Lomba",
        "Mengosongkan seluruh daftar peserta terdaftar beserta nilainya. Akun juri dan daftar sekolah tetap tersimpan."
    ),
    TOTAL_SEMUA(
        "total-semua",
        "3. Reset Total Seluruh Data (Peserta, Nilai, Nomor Urut & Akun Juri)",
        "Menghapus seluruh peserta, nilai, pengacakan nomor tampil sekolah, dan daftar akun juri untuk memulai tahun lomba baru dari awal."
    )
}
