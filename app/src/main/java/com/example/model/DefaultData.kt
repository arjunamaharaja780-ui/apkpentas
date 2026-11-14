package com.example.model

import kotlin.math.roundToInt

object DefaultData {
    const val DEFAULT_SUPABASE_URL = "https://fyfzqtylhsukbetdjqce.supabase.co"
    const val DEFAULT_SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZ5ZnpxdHlsaHN1a2JldGRqcWNlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTIxODQ2NzIsImV4cCI6MjEwNzc2MDY3Mn0.XBQZU7lFMqwuEU2y86JxJlsRt9yo69Q_qxFqLuP_xu4"

    const val DEFAULT_KECAMATAN = "Bantargebang"
    const val DEFAULT_TAHUN = "2026"

    val RESERVED_ROLES = setOf(
        "running_text", "tahun_pelaksanaan", "nama_kecamatan",
        "daftar_sekolah", "kategori_lomba", "sk_config",
        "konfigurasi_penilaian", "app_logo",
        "jadwal_pendaftaran", "jadwal_klasemen"
    )

    val DEFAULT_SEKOLAH = listOf(
        "SDN BANTARGEBANG 1", "SDN BANTARGEBANG 2", "SDN BANTARGEBANG 3",
        "SDN BANTARGEBANG 4", "SDN BANTARGEBANG 5", "SDN BANTARGEBANG 6",
        "SDN CIKIWUL 1", "SDN CIKIWUL 2", "SDN CIKIWUL 3", "SDN CIKIWUL 4",
        "SDN CIKETING UDIK 1", "SDN CIKETING UDIK 2", "SDN CIKETING UDIK 3", "SDN CIKETING UDIK 4",
        "SDN SUMUR BATU 1", "SDN SUMUR BATU 2", "SDN SUMUR BATU 4",
        "SD ISLAM EL GOZALI", "SD ISLAM IBNU HAJAR", "SDIT NURUL IMAN", "SD ISLAM CIKIWUL",
        "SDIT AL-BARKAH", "SDIT ROUDHOTUL JANNAH", "SDIT YA BUNAYYA", "SD ISLAM INSAN KAMIL",
        "SD DINAMIKA INDONESIA"
    )

    val DEFAULT_KATEGORI = listOf(
        KategoriLomba("mtq-pa", "MTQ Putra", "fa-book-quran", "Seni baca Al-Qur'an dengan tartil dan tajwid.", 1),
        KategoriLomba("mtq-pi", "MTQ Putri", "fa-book-quran", "Seni baca Al-Qur'an dengan tartil dan tajwid.", 1),
        KategoriLomba("mhq-pa", "MHQ Putra", "fa-brain", "Hafalan ayat suci Al-Qur'an.", 1),
        KategoriLomba("mhq-pi", "MHQ Putri", "fa-brain", "Hafalan ayat suci Al-Qur'an.", 1),
        KategoriLomba("pidato-pa", "PILDACIL Putra", "fa-microphone-lines", "Seni orasi menyampaikan pesan dan syiar Islam.", 1),
        KategoriLomba("pidato-pi", "PILDACIL Putri", "fa-microphone-lines", "Seni orasi menyampaikan pesan dan syiar Islam.", 1),
        KategoriLomba("kaligrafi-pa", "Kaligrafi Putra", "fa-pen-nib", "Seni menulis indah huruf Arab.", 1),
        KategoriLomba("kaligrafi-pi", "Kaligrafi Putri", "fa-pen-nib", "Seni menulis indah huruf Arab.", 1),
        KategoriLomba("lcc", "Cerdas Cermat PAI", "fa-users", "Adu wawasan pengetahuan Islam.", 3),
        KategoriLomba("azan", "Azan", "fa-bullhorn", "Mengumandangkan azan dengan pelafalan & irama tepat.", 1),
        KategoriLomba("sholat", "Kesempurnaan Sholat Berjamaah", "fa-hands-praying", "Penilaian keselarasan gerakan dan bacaan sholat.", 3)
    )

    val DEFAULT_AKSES = listOf(
        AksesItem("admin", "PENTASPAI26", "Password Administrator"),
        AksesItem("sekolah", "123456", "Password Pendaftaran Sekolah"),
        AksesItem("umum", "pentas2026", "Akses Umum / Klasemen")
    )

    fun defaultRunningText(kecamatan: String = DEFAULT_KECAMATAN, tahun: String = DEFAULT_TAHUN): String =
        "Selamat Datang di Portal Resmi PENTAS PAI Kecamatan $kecamatan Tahun $tahun — Pastikan Nomor Tampil Kontingen telah sesuai!"

    fun normalisasiNamaLomba(nama: String?): String {
        if (nama.isNullOrBlank()) return ""
        return nama.trim()
            .replace(Regex("\\s*\\(\\s*\\d+\\s*(?:Peserta|Orang)\\s*\\)\\s*$", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    fun getMaxPesertaCabang(namaLomba: String, kategoriList: List<KategoriLomba>): Int {
        val norm = normalisasiNamaLomba(namaLomba)
        val kat = kategoriList.firstOrNull {
            normalisasiNamaLomba(it.nama).equals(norm, ignoreCase = true)
        }
        if (kat != null && kat.maxPeserta > 0) return kat.maxPeserta
        val upper = norm.uppercase()
        return if (upper.contains("CERDAS CERMAT") || upper.contains("SHOLAT")) 3 else 1
    }

    fun formatDuaDigit(valStr: String?): String {
        if (valStr.isNullOrBlank() || valStr.trim() == "-") return "-"
        val clean = valStr.trim()
        val num = clean.toIntOrNull()
        return if (num != null) {
            if (num in 0..9) "0$num" else num.toString()
        } else clean
    }

    fun formatScore(score: Double): String {
        return if (score % 1.0 == 0.0) score.toInt().toString()
        else ((score * 10.0).roundToInt() / 10.0).toString()
    }

    fun compareNoUrut(a: Peserta, b: Peserta): Int {
        val noA = formatDuaDigit(a.noPeserta)
        val noB = formatDuaDigit(b.noPeserta)
        if (noA == "-" && noB == "-") return a.sekolah.compareTo(b.sekolah, ignoreCase = true)
        if (noA == "-") return 1
        if (noB == "-") return -1
        val numA = noA.toIntOrNull()
        val numB = noB.toIntOrNull()
        return if (numA != null && numB != null && numA != numB) {
            numA - numB
        } else {
            noA.compareTo(noB, ignoreCase = true)
        }
    }

    fun getDefaultKriteriaByLomba(lomba: String): List<KriteriaItem> {
        val l = normalisasiNamaLomba(lomba).uppercase()
        return when {
            l.contains("MTQ") -> listOf(
                KriteriaItem("t1", "Tajwid & Makharijul Huruf", 10.0, 35.0, "all"),
                KriteriaItem("t2", "Fashahah & Adab Tilawah", 10.0, 30.0, "all"),
                KriteriaItem("t3", "Suara, Lagu & Irama (Nagham)", 10.0, 35.0, "all")
            )
            l.contains("MHQ") -> listOf(
                KriteriaItem("m1", "Tahfizh (Kelancaran Hafalan)", 20.0, 50.0, "all"),
                KriteriaItem("m2", "Tajwid & Makharijul Huruf", 10.0, 30.0, "all"),
                KriteriaItem("m3", "Fashahah & Adab/Irama", 10.0, 20.0, "all")
            )
            l.contains("PILDACIL") || l.contains("PIDATO") -> listOf(
                KriteriaItem("p1", "Penguasaan & Kesesuaian Materi/Isi", 15.0, 40.0, "all"),
                KriteriaItem("p2", "Kaidah Bahasa, Vokal & Artikulasi", 10.0, 30.0, "all"),
                KriteriaItem("p3", "Ekspresi, Retorika & Adab/Sikap", 10.0, 30.0, "all")
            )
            l.contains("KALIGRAFI") -> listOf(
                KriteriaItem("k1", "Kebenaran Kaidah Khat & Huruf", 15.0, 40.0, "all"),
                KriteriaItem("k2", "Keindahan, Komposisi & Warna", 10.0, 35.0, "all"),
                KriteriaItem("k3", "Kebersihan & Kerapihan Karya", 10.0, 25.0, "all")
            )
            l.contains("AZAN") -> listOf(
                KriteriaItem("a1", "Makharijul Huruf & Tajwid", 15.0, 35.0, "all"),
                KriteriaItem("a2", "Irama, Suara & Lagu (Nagham)", 15.0, 40.0, "all"),
                KriteriaItem("a3", "Penghayatan, Adab & Sikap", 10.0, 25.0, "all")
            )
            l.contains("SHOLAT") -> listOf(
                KriteriaItem("s1", "Ketepatan & Fashahah Bacaan Sholat", 15.0, 40.0, "all"),
                KriteriaItem("s2", "Kesempurnaan Gerakan & Thuma'ninah", 15.0, 40.0, "all"),
                KriteriaItem("s3", "Kekompakan Regu & Adab Berjamaah", 10.0, 20.0, "all")
            )
            l.contains("CERDAS CERMAT") -> listOf(
                KriteriaItem("c1", "Babak Penyisihan / Wajib", 0.0, 100.0, "all"),
                KriteriaItem("c2", "Babak Rebutan / Final", 0.0, 100.0, "all")
            )
            else -> listOf(
                KriteriaItem("u1", "Penguasaan Materi / Teknik Utama", 15.0, 40.0, "all"),
                KriteriaItem("u2", "Kualitas Penampilan & Keindahan", 10.0, 35.0, "all"),
                KriteriaItem("u3", "Adab, Sikap & Ketepatan Waktu", 10.0, 25.0, "all")
            )
        }
    }
}
