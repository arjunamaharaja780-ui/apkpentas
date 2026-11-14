package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.graphics.scale
import com.example.model.DefaultData
import com.example.model.JuaraDocMode
import com.example.model.KategoriLomba
import com.example.model.Peserta
import com.example.model.PrintDocFormat
import com.example.model.RekapSekolahItem
import com.example.model.SkConfig
import java.io.ByteArrayOutputStream

object PrintAndExportHelper {

    fun decodeBase64Logo(dataUrl: String?): Bitmap? {
        if (dataUrl.isNullOrBlank() || !dataUrl.startsWith("data:image")) return null
        return try {
            val base64Part = dataUrl.substringAfter("base64,", "")
            if (base64Part.isBlank()) null
            else {
                val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun encodePickedImageToBase64(context: Context, uri: Uri): String? {
        return try {
            val stream = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(stream)
            stream.close()
            if (original == null) return null
            val scaled = original.scale(200, 200)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.PNG, 90, baos)
            val bytes = baos.toByteArray()
            "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    fun printHtmlDocument(context: Context, jobName: String, htmlBody: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null && view != null) {
                    val printAdapter = view.createPrintDocumentAdapter(jobName)
                    printManager.print(
                        jobName,
                        printAdapter,
                        PrintAttributes.Builder().build()
                    )
                }
            }
        }
        val fullHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <style>
                    body { font-family: 'Times New Roman', serif; margin: 16px; color: #0f172a; }
                    table { width: 100%; border-collapse: collapse; }
                    th, td { border: 1px solid #334155; padding: 6px; font-size: 11px; }
                    th { background: #f1f5f9; }
                </style>
            </head>
            <body>$htmlBody</body>
            </html>
        """.trimIndent()
        webView.loadDataWithBaseURL(null, fullHtml, "text/html", "UTF-8", null)
    }

    fun buildTemplateCsv(): String {
        val header = "Nomor Tampil,Nama Peserta,Asal Sekolah,Kategori Lomba"
        val sample = "01,Ahmad Farhan,SDN BANTARGEBANG 1,MTQ Putra"
        return "\uFEFF$header\n$sample\n"
    }

    fun buildPesertaCsv(pesertaList: List<Peserta>): String {
        val sb = StringBuilder("\uFEFFNo,Nomor Tampil,Nama Peserta,Asal Sekolah,Kategori Lomba,Skor Akhir\n")
        pesertaList.forEachIndexed { idx, p ->
            sb.append("${idx + 1},")
                .append("${csvEscape(DefaultData.formatDuaDigit(p.noPeserta))},")
                .append("${csvEscape(p.nama)},")
                .append("${csvEscape(p.sekolah)},")
                .append("${csvEscape(p.lomba)},")
                .append("${DefaultData.formatScore(p.skorAkhir)}\n")
        }
        return sb.toString()
    }

    fun buildRekapJuaraCsv(rekap: List<RekapSekolahItem>): String {
        val sb = StringBuilder("\uFEFFNo,Sekolah,Kontingen,Juara 1,Juara 2,Juara 3,Total Poin,Total Skor\n")
        rekap.forEachIndexed { idx, s ->
            sb.append("${idx + 1},")
                .append("${csvEscape(s.sekolah)},")
                .append("${s.jmlKontingen},")
                .append("${s.j1},${s.j2},${s.j3},${s.totalPoin},")
                .append("${DefaultData.formatScore(s.totalSkorAkumulasi)}\n")
        }
        return sb.toString()
    }

    private fun csvEscape(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return "\"$clean\""
    }

    fun parseCsvPeserta(
        csvContent: String,
        noUrutMap: Map<String, String>
    ): List<Peserta> {
        val lines = csvContent.replace("\uFEFF", "").lines().filter { it.isNotBlank() }
        if (lines.size < 2) return emptyList()
        val delimiter = if (lines[0].contains(';')) ';' else ','
        val headers = splitCsvLine(lines[0], delimiter).map { it.trim().lowercase() }

        val idxNo = headers.indexOfFirst { it.contains("nomor") || it == "no tampil" }
        val idxNama = headers.indexOfFirst { it.contains("nama") }
        val idxSekolah = headers.indexOfFirst { it.contains("sekolah") }
        val idxLomba = headers.indexOfFirst { it.contains("lomba") || it.contains("kategori") }

        val result = mutableListOf<Peserta>()
        val now = System.currentTimeMillis()
        for (i in 1 until lines.size) {
            val cols = splitCsvLine(lines[i], delimiter)
            val nama = cols.getOrNull(if (idxNama >= 0) idxNama else 1)?.trim().orEmpty()
            val sekolah = cols.getOrNull(if (idxSekolah >= 0) idxSekolah else 2)?.trim().orEmpty().uppercase()
            val lomba = DefaultData.normalisasiNamaLomba(cols.getOrNull(if (idxLomba >= 0) idxLomba else 3))
            var noPeserta = cols.getOrNull(if (idxNo >= 0) idxNo else 0)?.trim().orEmpty()
            if (noPeserta.isBlank() && noUrutMap.containsKey(sekolah)) {
                noPeserta = noUrutMap[sekolah].orEmpty()
            }
            if (nama.isNotBlank() && sekolah.isNotBlank() && lomba.isNotBlank()) {
                result.add(
                    Peserta(
                        id = "PST-$now-$i-${(1000..9999).random()}",
                        noPeserta = DefaultData.formatDuaDigit(noPeserta.ifBlank { "-" }),
                        nama = nama,
                        sekolah = sekolah,
                        lomba = lomba
                    )
                )
            }
        }
        return result
    }

    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }

    fun buildDokumenPesertaHtml(
        cabangDipilih: String,
        format: PrintDocFormat,
        pesertaList: List<Peserta>,
        kategoriLomba: List<KategoriLomba>,
        kecamatan: String,
        tahun: String,
        logoDataUrl: String
    ): String {
        val logoImg = if (logoDataUrl.startsWith("data:image")) {
            "<img src=\"$logoDataUrl\" style=\"height:52px;width:52px;object-fit:contain;margin-right:14px;\"/>"
        } else ""

        val daftarCabang = if (cabangDipilih == "All") {
            kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
        } else {
            listOf(DefaultData.normalisasiNamaLomba(cabangDipilih))
        }

        val sb = StringBuilder()
        daftarCabang.forEachIndexed { idx, namaCabang ->
            val list = pesertaList.filter {
                DefaultData.normalisasiNamaLomba(it.lomba) == namaCabang
            }.let {
                if (format == PrintDocFormat.JUARA) {
                    it.filter { p -> p.skorAkhir > 0 }.sortedByDescending { p -> p.skorAkhir }
                } else {
                    it.sortedWith(DefaultData::compareNoUrut)
                }
            }
            if (list.isEmpty() && cabangDipilih == "All") return@forEachIndexed
            val pageBreak = if (idx > 0) "page-break-before:always;" else ""
            val colHeaderLast = when (format) {
                PrintDocFormat.HADIR -> "Tanda Tangan / Paraf"
                PrintDocFormat.JURI -> "Catatan Juri"
                PrintDocFormat.FORM_PENILAIAN -> "Nilai Akhir"
                PrintDocFormat.JUARA -> "Skor / Peringkat"
            }

            sb.append(
                """
                <div style="$pageBreak margin-bottom:24px;">
                    <div style="display:flex;align-items:center;justify-content:center;border-bottom:4px double #0f172a;padding-bottom:10px;margin-bottom:14px;">
                        $logoImg
                        <div style="text-align:center;">
                            <div style="font-size:15px;font-weight:800;text-transform:uppercase;">PANITIA PENTAS PAI KECAMATAN ${kecamatan.uppercase()} $tahun</div>
                            <div style="font-size:12px;font-weight:700;color:#047857;margin-top:4px;text-transform:uppercase;">${format.label} — CABANG: $namaCabang</div>
                        </div>
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th style="width:40px;text-align:center;">No</th>
                                <th style="width:85px;text-align:center;">Nomor Tampil</th>
                                <th style="text-align:left;">Nama Peserta</th>
                                <th style="text-align:left;">Asal Sekolah</th>
                                <th style="width:120px;text-align:center;">$colHeaderLast</th>
                            </tr>
                        </thead>
                        <tbody>
                """.trimIndent()
            )
            if (list.isEmpty()) {
                sb.append("<tr><td colspan=\"5\" style=\"text-align:center;color:#64748b;padding:16px;\">Belum ada peserta pada cabang ini.</td></tr>")
            } else {
                list.forEachIndexed { i, p ->
                    val sekolahTxt = if (format == PrintDocFormat.JURI) "(Disembunyikan - Blind Judging)" else p.sekolah
                    val lastCell = when (format) {
                        PrintDocFormat.JUARA -> "${DefaultData.formatScore(p.skorAkhir)} (Juara ${i + 1})"
                        PrintDocFormat.FORM_PENILAIAN -> if (p.skorAkhir > 0) DefaultData.formatScore(p.skorAkhir) else ".........."
                        else -> "${i + 1}. .........."
                    }
                    sb.append(
                        """
                        <tr>
                            <td style="text-align:center;">${i + 1}</td>
                            <td style="text-align:center;font-weight:bold;">${DefaultData.formatDuaDigit(p.noPeserta)}</td>
                            <td style="font-weight:bold;">${p.nama}</td>
                            <td>$sekolahTxt</td>
                            <td style="text-align:center;">$lastCell</td>
                        </tr>
                        """.trimIndent()
                    )
                }
            }
            sb.append("</tbody></table></div>")
        }
        return sb.toString().ifBlank { "<p style='text-align:center;'>Belum ada data peserta untuk dicetak.</p>" }
    }

    fun buildDokumenJuaraHtml(
        mode: JuaraDocMode,
        cabangDipilih: String,
        pesertaList: List<Peserta>,
        kategoriLomba: List<KategoriLomba>,
        rekapSekolah: List<RekapSekolahItem>,
        skConfig: SkConfig,
        kecamatan: String,
        tahun: String,
        logoDataUrl: String
    ): String {
        val logoImg = if (logoDataUrl.startsWith("data:image")) {
            "<img src=\"$logoDataUrl\" style=\"height:52px;width:52px;object-fit:contain;margin-right:14px;\"/>"
        } else ""

        val kopSurat = """
            <div style="display:flex;align-items:center;justify-content:center;border-bottom:4px double #0f172a;padding-bottom:12px;margin-bottom:18px;">
                $logoImg
                <div style="text-align:center;">
                    <div style="font-size:12px;font-weight:800;text-transform:uppercase;color:#047857;">KELOMPOK KERJA GURU PENDIDIKAN AGAMA ISLAM (KKG PAI)</div>
                    <div style="font-size:14px;font-weight:900;text-transform:uppercase;color:#0f172a;margin:2px 0;">PANITIA PELAKSANA PEKAN KETERAMPILAN DAN SENI PAI (PENTAS PAI)</div>
                    <div style="font-size:12px;font-weight:800;text-transform:uppercase;color:#1e293b;">TINGKAT SD KECAMATAN ${kecamatan.uppercase()} TAHUN $tahun</div>
                    <div style="font-size:10px;color:#475569;margin-top:2px;">Sekretariat: KKG PAI Kecamatan $kecamatan • Lokasi: ${skConfig.lokasiLomba}</div>
                </div>
            </div>
        """.trimIndent()

        val daftarCabang = if (cabangDipilih == "All") {
            kategoriLomba.map { DefaultData.normalisasiNamaLomba(it.nama) }
        } else {
            listOf(DefaultData.normalisasiNamaLomba(cabangDipilih))
        }
        val labelPredikat = listOf("Juara I", "Juara II", "Juara III", "Harapan I", "Harapan II", "Harapan III")

        return when (mode) {
            JuaraDocMode.SK_LENGKAP -> {
                val sortedJU = rekapSekolah.sortedWith(
                    compareByDescending<RekapSekolahItem> { it.totalPoin }
                        .thenByDescending { it.j1 }
                        .thenByDescending { it.j2 }
                        .thenByDescending { it.j3 }
                        .thenByDescending { it.totalSkorAkumulasi }
                )
                val juaraUmum = sortedJU.firstOrNull { it.totalPoin > 0 || it.totalSkorAkumulasi > 0 }
                val tablesSb = StringBuilder()
                daftarCabang.forEachIndexed { idx, cabang ->
                    val top6 = pesertaList.filter {
                        DefaultData.normalisasiNamaLomba(it.lomba) == cabang && it.skorAkhir > 0
                    }.sortedByDescending { it.skorAkhir }.take(6)
                    tablesSb.append("<div style='margin-bottom:14px;page-break-inside:avoid;'>")
                    tablesSb.append("<div style='background:#064e3b;color:#fff;padding:6px 10px;font-weight:800;font-size:11px;'>${idx + 1}. CABANG LOMBA : $cabang</div>")
                    tablesSb.append("<table><thead><tr><th>Peringkat</th><th>No. Tampil</th><th>Nama Peserta / Regu</th><th>Asal Sekolah</th><th>Juri 1</th><th>Juri 2</th><th>Skor Akhir</th></tr></thead><tbody>")
                    if (top6.isEmpty()) {
                        tablesSb.append("<tr><td colspan='7' style='text-align:center;color:#94a3b8;'>Belum ada perolehan nilai.</td></tr>")
                    } else {
                        top6.forEachIndexed { i, p ->
                            tablesSb.append(
                                "<tr>" +
                                    "<td style='text-align:center;font-weight:bold;'>${labelPredikat.getOrElse(i) { "Peringkat ${i + 1}" }}</td>" +
                                    "<td style='text-align:center;font-weight:bold;'>${DefaultData.formatDuaDigit(p.noPeserta)}</td>" +
                                    "<td style='font-weight:bold;'>${p.nama}</td>" +
                                    "<td>${p.sekolah}</td>" +
                                    "<td style='text-align:center;'>${if (p.skorJ1 > 0) DefaultData.formatScore(p.skorJ1) else "-"}</td>" +
                                    "<td style='text-align:center;'>${if (p.skorJ2 > 0) DefaultData.formatScore(p.skorJ2) else "-"}</td>" +
                                    "<td style='text-align:center;font-weight:bold;color:#047857;'>${DefaultData.formatScore(p.skorAkhir)}</td>" +
                                "</tr>"
                            )
                        }
                    }
                    tablesSb.append("</tbody></table></div>")
                }

                val ttdHtml = """
                    <div style="margin-top:24px;font-size:11px;page-break-inside:avoid;">
                        <div style="text-align:right;margin-bottom:10px;">
                            Ditetapkan di : <b>${skConfig.kotaTetap}</b><br/>
                            Pada tanggal : <b>${skConfig.tanggalSK}</b>
                        </div>
                        <table style="border:none;">
                            <tr>
                                <td style="border:none;text-align:center;width:50%;">
                                    Ketua Panitia,<br/><br/><br/><br/>
                                    <u><b>${skConfig.ketuaPanitia}</b></u><br/>NIP. ${skConfig.nipKetua}
                                </td>
                                <td style="border:none;text-align:center;width:50%;">
                                    Sekretaris Panitia,<br/><br/><br/><br/>
                                    <u><b>${skConfig.sekretarisPanitia}</b></u><br/>NIP. ${skConfig.nipSekretaris}
                                </td>
                            </tr>
                        </table>
                        <div style="text-align:center;margin-top:18px;">
                            Mengetahui &amp; Mengesahkan,<br/>Ketua KKG PAI Kecamatan $kecamatan<br/><br/><br/><br/>
                            <u><b>${skConfig.ketuaKKG}</b></u><br/>NIP. ${skConfig.nipKetuaKKG}
                        </div>
                    </div>
                """.trimIndent()

                """
                $kopSurat
                <div style="text-align:center;margin-bottom:14px;">
                    <div style="font-size:14px;font-weight:900;text-decoration:underline;">SURAT KEPUTUSAN</div>
                    <div style="font-size:12px;font-weight:800;">PANITIA PELAKSANA PENTAS PAI TINGKAT SD KECAMATAN ${kecamatan.uppercase()}</div>
                    <div style="font-size:11px;font-weight:700;">NOMOR : ${skConfig.nomorSK}</div>
                    <div style="font-size:11px;font-weight:800;margin-top:8px;">TENTANG PENETAPAN PESERTA TERBAIK &amp; JUARA UMUM PENTAS PAI TAHUN $tahun</div>
                </div>
                <p style="font-size:11px;text-align:justify;"><b>MEMUTUSKAN:</b> Menetapkan nama-nama peserta didik/regu sebagaimana tercantum dalam lampiran Surat Keputusan ini sebagai Juara I, II, III dan Harapan I, II, III pada setiap cabang perlombaan PENTAS PAI Tingkat SD Kecamatan $kecamatan Tahun $tahun${if (juaraUmum != null) ", serta menetapkan <b>${juaraUmum.sekolah}</b> sebagai Juara Umum (${juaraUmum.totalPoin} poin)" else ""}.</p>
                $tablesSb
                $ttdHtml
                """.trimIndent()
            }

            JuaraDocMode.REKAP_SEKOLAH -> {
                val headers = kategoriLomba.joinToString("") { "<th>${DefaultData.normalisasiNamaLomba(it.nama)}</th>" }
                val rows = rekapSekolah.mapIndexed { idx, s ->
                    val cells = kategoriLomba.joinToString("") { k ->
                        val h = s.hasilCabang[DefaultData.normalisasiNamaLomba(k.nama)]
                        "<td style='text-align:center;'>${if (h != null && h.ada) DefaultData.formatScore(h.skor) else "-"}</td>"
                    }
                    "<tr><td style='text-align:center;'>${idx + 1}</td><td style='font-weight:bold;'>${s.sekolah}</td>$cells<td style='text-align:center;font-weight:bold;color:#047857;'>${DefaultData.formatScore(s.totalSkorAkumulasi)}</td></tr>"
                }.joinToString("")
                "$kopSurat<h3 style='text-align:center;'>REKAPITULASI HASIL PERLOMBAAN PER SEKOLAH</h3><table><thead><tr><th>No</th><th>Sekolah</th>$headers<th>Total</th></tr></thead><tbody>$rows</tbody></table>"
            }

            JuaraDocMode.JUARA_UMUM -> {
                val sorted = rekapSekolah.sortedWith(
                    compareByDescending<RekapSekolahItem> { it.totalPoin }
                        .thenByDescending { it.totalSkorAkumulasi }
                )
                val rows = sorted.mapIndexed { idx, s ->
                    "<tr><td style='text-align:center;font-weight:bold;'>${idx + 1}</td><td style='font-weight:bold;'>${s.sekolah}</td><td style='text-align:center;'>${s.j1}</td><td style='text-align:center;'>${s.j2}</td><td style='text-align:center;'>${s.j3}</td><td style='text-align:center;font-weight:bold;color:#047857;'>${s.totalPoin}</td></tr>"
                }.joinToString("")
                "$kopSurat<h3 style='text-align:center;'>KLASEMEN JUARA UMUM KONTINGEN SEKOLAH</h3><table><thead><tr><th>No</th><th>Nama Sekolah</th><th>Juara 1</th><th>Juara 2</th><th>Juara 3</th><th>Total Poin</th></tr></thead><tbody>$rows</tbody></table>"
            }

            JuaraDocMode.DAFTAR_JUARA -> {
                val sb = StringBuilder("$kopSurat<h3 style='text-align:center;'>DAFTAR PEMENANG PER CABANG LOMBA</h3>")
                daftarCabang.forEach { cabang ->
                    val top6 = pesertaList.filter {
                        DefaultData.normalisasiNamaLomba(it.lomba) == cabang && it.skorAkhir > 0
                    }.sortedByDescending { it.skorAkhir }.take(6)
                    sb.append("<div style='margin-bottom:14px;'><div style='background:#0f172a;color:#fff;padding:6px 10px;font-weight:bold;font-size:11px;'>CABANG : ${cabang.uppercase()}</div>")
                    sb.append("<table><thead><tr><th>Predikat</th><th>No Tampil</th><th>Nama Peserta</th><th>Asal Sekolah</th><th>Nilai</th></tr></thead><tbody>")
                    if (top6.isEmpty()) {
                        sb.append("<tr><td colspan='5' style='text-align:center;color:#94a3b8;'>Belum ada nilai.</td></tr>")
                    } else {
                        top6.forEachIndexed { i, p ->
                            sb.append("<tr><td style='text-align:center;font-weight:bold;'>Juara ${i + 1}</td><td style='text-align:center;'>${DefaultData.formatDuaDigit(p.noPeserta)}</td><td style='font-weight:bold;'>${p.nama}</td><td>${p.sekolah}</td><td style='text-align:center;font-weight:bold;color:#047857;'>${DefaultData.formatScore(p.skorAkhir)}</td></tr>")
                        }
                    }
                    sb.append("</tbody></table></div>")
                }
                sb.toString()
            }
        }
    }
}
