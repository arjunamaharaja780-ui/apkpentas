package com.example.data

import com.example.model.AkunJuri
import com.example.model.AksesItem
import com.example.model.DefaultData
import com.example.model.KategoriLomba
import com.example.model.KriteriaItem
import com.example.model.LombaPenilaianConfig
import com.example.model.Peserta
import com.example.model.SkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class ServerSnapshot(
    val aksesList: List<AksesItem>,
    val juriList: List<AkunJuri>,
    val pesertaList: List<Peserta>,
    val noUrutMap: Map<String, String>,
    val daftarSekolah: List<String>,
    val kategoriLomba: List<KategoriLomba>,
    val namaKecamatan: String,
    val tahunPelaksanaan: String,
    val runningText: String,
    val appLogoDataUrl: String,
    val skConfig: SkConfig,
    val konfigurasiPenilaian: Map<String, LombaPenilaianConfig>
)

class SupabaseRepository(
    initialUrl: String = DefaultData.DEFAULT_SUPABASE_URL,
    initialKey: String = DefaultData.DEFAULT_SUPABASE_ANON_KEY
) {
    @Volatile
    var baseUrl: String = initialUrl.trimEnd('/')
        private set

    @Volatile
    var anonKey: String = initialKey
        private set

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    fun updateCredentials(url: String, key: String) {
        baseUrl = url.trim().trimEnd('/')
        anonKey = key.trim()
    }

    private fun buildRestUrl(table: String, query: String = ""): String {
        val base = "$baseUrl/rest/v1/$table"
        return if (query.isBlank()) base else "$base?$query"
    }

    private fun encodeEq(value: String): String = URLEncoder.encode(value, "UTF-8")

    private fun executeGet(url: String, overrideKey: String? = null): JSONArray {
        val keyToUse = overrideKey ?: anonKey
        val req = Request.Builder()
            .url(url)
            .addHeader("apikey", keyToUse)
            .addHeader("Authorization", "Bearer $keyToUse")
            .addHeader("Accept", "application/json")
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: "[]"
            if (!resp.isSuccessful) {
                throw IllegalStateException("HTTP ${resp.code}: ${extractErrorMessage(body)}")
            }
            return if (body.trim().startsWith("[")) JSONArray(body) else JSONArray()
        }
    }

    private fun executePost(
        url: String,
        payload: String,
        prefer: String = "return=minimal"
    ) {
        val req = Request.Builder()
            .url(url)
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", prefer)
            .post(payload.toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                throw IllegalStateException("HTTP ${resp.code}: ${extractErrorMessage(body)}")
            }
        }
    }

    private fun executePatch(url: String, payload: String) {
        val req = Request.Builder()
            .url(url)
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .patch(payload.toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                throw IllegalStateException("HTTP ${resp.code}: ${extractErrorMessage(body)}")
            }
        }
    }

    private fun executeDelete(url: String) {
        val req = Request.Builder()
            .url(url)
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .delete()
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                throw IllegalStateException("HTTP ${resp.code}: ${extractErrorMessage(body)}")
            }
        }
    }

    private fun extractErrorMessage(raw: String): String {
        return try {
            val obj = JSONObject(raw)
            obj.optString("message").ifBlank { obj.optString("error").ifBlank { raw } }
        } catch (_: Exception) {
            raw.take(200)
        }
    }

    suspend fun testConnection(testUrl: String, testKey: String): Pair<Long, Int> = withContext(Dispatchers.IO) {
        val cleanUrl = testUrl.trim().trimEnd('/')
        val cleanKey = testKey.trim()
        val start = System.currentTimeMillis()
        // Validate akses endpoint
        executeGet("$cleanUrl/rest/v1/akses?select=role&limit=1", cleanKey)
        val pesertaArr = executeGet("$cleanUrl/rest/v1/peserta?select=id", cleanKey)
        val latency = System.currentTimeMillis() - start
        latency to pesertaArr.length()
    }

    suspend fun fetchAllData(): ServerSnapshot = withContext(Dispatchers.IO) {
        val aksesArr = executeGet(buildRestUrl("akses", "select=*"))
        val juriArr = executeGet(buildRestUrl("akun_juri", "select=*"))
        val noUrutArr = executeGet(buildRestUrl("no_urut_sekolah", "select=*"))
        val pesertaArr = executeGet(buildRestUrl("peserta", "select=*"))

        var tahun = DefaultData.DEFAULT_TAHUN
        var kecamatan = DefaultData.DEFAULT_KECAMATAN
        var runningText = ""
        var daftarSekolah = DefaultData.DEFAULT_SEKOLAH
        var kategoriLomba = DefaultData.DEFAULT_KATEGORI
        var skConfig = SkConfig()
        var logoDataUrl = ""
        val konfigurasiPenilaian = mutableMapOf<String, LombaPenilaianConfig>()
        val normalAkses = mutableListOf<AksesItem>()

        for (i in 0 until aksesArr.length()) {
            val row = aksesArr.getJSONObject(i)
            val role = row.optString("role").trim()
            val password = row.optString("password")
            val keterangan = row.optString("keterangan")
            when (role.lowercase()) {
                "tahun_pelaksanaan" -> {
                    val v = if (keterangan.isNotBlank() && keterangan != "-") keterangan else password
                    if (v.isNotBlank()) tahun = v
                }
                "nama_kecamatan" -> {
                    val v = if (keterangan.isNotBlank() && keterangan != "-") keterangan else password
                    if (v.isNotBlank()) kecamatan = v
                }
                "running_text" -> {
                    val v = if (keterangan.isNotBlank() && keterangan != "-") keterangan else password
                    if (v.isNotBlank()) runningText = v
                }
                "daftar_sekolah" -> {
                    if (keterangan.isNotBlank()) {
                        try {
                            val arr = JSONArray(keterangan)
                            if (arr.length() > 0) {
                                daftarSekolah = (0 until arr.length()).map { arr.getString(it) }
                            }
                        } catch (_: Exception) {}
                    }
                }
                "kategori_lomba" -> {
                    if (keterangan.isNotBlank()) {
                        try {
                            val arr = JSONArray(keterangan)
                            if (arr.length() > 0) {
                                kategoriLomba = (0 until arr.length()).map { idx ->
                                    val k = arr.getJSONObject(idx)
                                    val nama = DefaultData.normalisasiNamaLomba(k.optString("nama"))
                                    KategoriLomba(
                                        id = k.optString("id").ifBlank { "lomba-$idx" },
                                        nama = nama,
                                        icon = k.optString("icon").ifBlank { "fa-trophy" },
                                        desc = k.optString("desc").ifBlank { "Cabang perlombaan PENTAS PAI." },
                                        maxPeserta = k.optInt("maxPeserta", 0).takeIf { it > 0 }
                                            ?: DefaultData.getMaxPesertaCabang(nama, DefaultData.DEFAULT_KATEGORI)
                                    )
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
                "sk_config" -> {
                    if (keterangan.isNotBlank()) {
                        try {
                            val obj = JSONObject(keterangan)
                            skConfig = SkConfig(
                                nomorSK = obj.optString("nomorSK", skConfig.nomorSK),
                                kotaTetap = obj.optString("kotaTetap", skConfig.kotaTetap),
                                tanggalSK = obj.optString("tanggalSK", skConfig.tanggalSK),
                                lokasiLomba = obj.optString("lokasiLomba", skConfig.lokasiLomba),
                                ketuaPanitia = obj.optString("ketuaPanitia", skConfig.ketuaPanitia),
                                nipKetua = obj.optString("nipKetua", skConfig.nipKetua),
                                sekretarisPanitia = obj.optString("sekretarisPanitia", skConfig.sekretarisPanitia),
                                nipSekretaris = obj.optString("nipSekretaris", skConfig.nipSekretaris),
                                ketuaKKG = obj.optString("ketuaKKG", skConfig.ketuaKKG),
                                nipKetuaKKG = obj.optString("nipKetuaKKG", skConfig.nipKetuaKKG)
                            )
                        } catch (_: Exception) {}
                    }
                }
                "konfigurasi_penilaian" -> {
                    if (keterangan.isNotBlank()) {
                        try {
                            val obj = JSONObject(keterangan)
                            val keys = obj.keys()
                            while (keys.hasNext()) {
                                val lombaKey = keys.next()
                                val cfgObj = obj.optJSONObject(lombaKey) ?: continue
                                val kArr = cfgObj.optJSONArray("kriteria") ?: JSONArray()
                                val kList = (0 until kArr.length()).map { idx ->
                                    val kItem = kArr.getJSONObject(idx)
                                    KriteriaItem(
                                        id = kItem.optString("id").ifBlank { "kr_${idx + 1}" },
                                        label = kItem.optString("label", "Kriteria ${idx + 1}"),
                                        min = kItem.optDouble("min", 0.0),
                                        max = kItem.optDouble("max", 100.0),
                                        juriTarget = kItem.optString("juriTarget", "all").ifBlank { "all" }
                                    )
                                }
                                konfigurasiPenilaian[DefaultData.normalisasiNamaLomba(lombaKey)] = LombaPenilaianConfig(
                                    jumlahJuri = cfgObj.optInt("jumlahJuri", 2).coerceIn(1, 3),
                                    modeDuaJuri = cfgObj.optString("modeDuaJuri", "semua").ifBlank { "semua" },
                                    kriteria = kList
                                )
                            }
                        } catch (_: Exception) {}
                    }
                }
                "app_logo" -> {
                    if (keterangan.startsWith("data:image")) {
                        logoDataUrl = keterangan
                    }
                }
                else -> {
                    if (role.isNotBlank() && !DefaultData.RESERVED_ROLES.contains(role.lowercase())) {
                        normalAkses.add(AksesItem(role, password, keterangan))
                    }
                }
            }
        }

        val finalAkses = if (normalAkses.isNotEmpty()) normalAkses else DefaultData.DEFAULT_AKSES
        val finalRunningText = runningText.ifBlank { DefaultData.defaultRunningText(kecamatan, tahun) }

        val juriList = (0 until juriArr.length()).map { idx ->
            val j = juriArr.getJSONObject(idx)
            AkunJuri(
                id = j.optString("id"),
                nama = j.optString("nama"),
                lomba = DefaultData.normalisasiNamaLomba(j.optString("lomba")),
                juriKe = j.optInt("juri_ke", 1).coerceAtLeast(1),
                password = j.optString("password")
            )
        }

        val noUrutMap = mutableMapOf<String, String>()
        for (i in 0 until noUrutArr.length()) {
            val r = noUrutArr.getJSONObject(i)
            val sek = r.optString("sekolah")
            val no = DefaultData.formatDuaDigit(r.optString("no_urut"))
            if (sek.isNotBlank()) noUrutMap[sek] = no
        }

        val pesertaList = (0 until pesertaArr.length()).map { idx ->
            val p = pesertaArr.getJSONObject(idx)
            val rawRincian = parseRincianSkor(p.opt("rincian_skor"))
            val s1 = p.optDouble("skor_j1", 0.0).let { if (it.isNaN()) 0.0 else it }
            val s2 = p.optDouble("skor_j2", 0.0).let { if (it.isNaN()) 0.0 else it }
            val s3 = rawRincian.second
            val sAkhir = p.optDouble("skor_akhir", 0.0).let { if (it.isNaN()) 0.0 else it }

            Peserta(
                id = p.optString("id"),
                noPeserta = DefaultData.formatDuaDigit(p.optString("no_peserta")),
                nama = p.optString("nama"),
                sekolah = p.optString("sekolah"),
                lomba = DefaultData.normalisasiNamaLomba(p.optString("lomba")),
                skorAkhir = sAkhir,
                skorJ1 = s1,
                skorJ2 = s2,
                skorJ3 = s3,
                rincianKriteria = rawRincian.first
            )
        }

        ServerSnapshot(
            aksesList = finalAkses,
            juriList = juriList,
            pesertaList = pesertaList,
            noUrutMap = noUrutMap,
            daftarSekolah = daftarSekolah,
            kategoriLomba = kategoriLomba,
            namaKecamatan = kecamatan,
            tahunPelaksanaan = tahun,
            runningText = finalRunningText,
            appLogoDataUrl = logoDataUrl,
            skConfig = skConfig,
            konfigurasiPenilaian = konfigurasiPenilaian
        )
    }

    private fun parseRincianSkor(raw: Any?): Pair<Map<Int, Map<String, Double>>, Double> {
        val map = mutableMapOf<Int, Map<String, Double>>()
        var skorJ3 = 0.0
        if (raw == null || raw == JSONObject.NULL) return map to 0.0
        var obj: JSONObject? = when (raw) {
            is JSONObject -> raw
            is String -> {
                val trimmed = raw.trim()
                if (trimmed.isBlank()) null
                else try {
                    var parsed = JSONObject(trimmed)
                    if (parsed.length() == 0 && trimmed.startsWith("\"")) {
                        parsed = JSONObject(JSONObject("{\"v\":$trimmed}").getString("v"))
                    }
                    parsed
                } catch (_: Exception) {
                    null
                }
            }
            else -> null
        }
        if (obj == null) return map to 0.0
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (k == "_skor_j3") {
                skorJ3 = obj.optDouble(k, 0.0).let { if (it.isNaN()) 0.0 else it }
                continue
            }
            val juriNum = when {
                k.toIntOrNull() != null -> k.toInt()
                k.startsWith("juri", ignoreCase = true) -> k.drop(4).toIntOrNull()
                k.startsWith("J", ignoreCase = true) -> k.drop(1).toIntOrNull()
                else -> null
            } ?: continue
            val subObj = obj.optJSONObject(k) ?: continue
            val subMap = mutableMapOf<String, Double>()
            val subKeys = subObj.keys()
            while (subKeys.hasNext()) {
                val sk = subKeys.next()
                val v = subObj.optDouble(sk, Double.NaN)
                if (!v.isNaN()) subMap[sk] = v
            }
            map[juriNum] = subMap
        }
        return map to skorJ3
    }

    private fun serializeRincianSkor(rincian: Map<Int, Map<String, Double>>, skorJ3: Double = 0.0): JSONObject {
        val root = JSONObject()
        rincian.forEach { (juriIdx, mapKriteria) ->
            val sub = JSONObject()
            mapKriteria.forEach { (kId, nilai) ->
                sub.put(kId, nilai)
            }
            root.put(juriIdx.toString(), sub)
        }
        if (skorJ3 > 0.0) {
            root.put("_skor_j3", skorJ3)
        }
        return root
    }

    suspend fun upsertAksesRow(role: String, password: String, keterangan: String) = withContext(Dispatchers.IO) {
        val arr = JSONArray().put(
            JSONObject().apply {
                put("role", role)
                put("password", password)
                put("keterangan", keterangan)
            }
        )
        executePost(
            buildRestUrl("akses", "on_conflict=role"),
            arr.toString(),
            prefer = "resolution=merge-duplicates,return=minimal"
        )
    }

    suspend fun deleteAksesRow(role: String) = withContext(Dispatchers.IO) {
        executeDelete(buildRestUrl("akses", "role=eq.${encodeEq(role)}"))
    }

    suspend fun insertPeserta(list: List<Peserta>) = withContext(Dispatchers.IO) {
        if (list.isEmpty()) return@withContext
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("no_peserta", p.noPeserta)
                    put("nama", p.nama)
                    put("sekolah", p.sekolah)
                    put("lomba", p.lomba)
                    put("skor_akhir", p.skorAkhir)
                    put("skor_j1", p.skorJ1)
                    put("skor_j2", p.skorJ2)
                    put("rincian_skor", serializeRincianSkor(p.rincianKriteria, p.skorJ3))
                }
            )
        }
        executePost(buildRestUrl("peserta"), arr.toString())
    }

    suspend fun updatePesertaInfo(id: String, noPeserta: String, nama: String, sekolah: String) = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("no_peserta", noPeserta)
            put("nama", nama)
            put("sekolah", sekolah)
        }
        executePatch(buildRestUrl("peserta", "id=eq.${encodeEq(id)}"), body.toString())
    }

    suspend fun updatePesertaScore(
        id: String,
        skorJ1: Double,
        skorJ2: Double,
        skorJ3: Double,
        skorAkhir: Double,
        rincian: Map<Int, Map<String, Double>>
    ) = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("skor_j1", skorJ1)
            put("skor_j2", skorJ2)
            put("skor_akhir", skorAkhir)
            put("rincian_skor", serializeRincianSkor(rincian, skorJ3))
        }
        executePatch(buildRestUrl("peserta", "id=eq.${encodeEq(id)}"), body.toString())
    }

    suspend fun deletePeserta(id: String) = withContext(Dispatchers.IO) {
        executeDelete(buildRestUrl("peserta", "id=eq.${encodeEq(id)}"))
    }

    suspend fun deletePesertaByLomba(lomba: String) = withContext(Dispatchers.IO) {
        executeDelete(buildRestUrl("peserta", "lomba=eq.${encodeEq(lomba)}"))
    }

    suspend fun upsertNoUrutAndSyncPeserta(noUrutMap: Map<String, String>) = withContext(Dispatchers.IO) {
        val arr = JSONArray()
        noUrutMap.forEach { (sekolah, noUrut) ->
            arr.put(
                JSONObject().apply {
                    put("sekolah", sekolah)
                    put("no_urut", noUrut)
                }
            )
        }
        if (arr.length() > 0) {
            executePost(
                buildRestUrl("no_urut_sekolah", "on_conflict=sekolah"),
                arr.toString(),
                prefer = "resolution=merge-duplicates,return=minimal"
            )
        }
        noUrutMap.forEach { (sekolah, noUrut) ->
            if (noUrut.isNotBlank() && noUrut != "-") {
                val body = JSONObject().put("no_peserta", noUrut)
                executePatch(buildRestUrl("peserta", "sekolah=eq.${encodeEq(sekolah)}"), body.toString())
            }
        }
    }

    suspend fun insertJuri(juri: AkunJuri) = withContext(Dispatchers.IO) {
        val arr = JSONArray().put(
            JSONObject().apply {
                put("id", juri.id)
                put("nama", juri.nama)
                put("lomba", juri.lomba)
                put("juri_ke", juri.juriKe)
                put("password", juri.password)
            }
        )
        executePost(buildRestUrl("akun_juri"), arr.toString())
    }

    suspend fun deleteJuri(id: String) = withContext(Dispatchers.IO) {
        executeDelete(buildRestUrl("akun_juri", "id=eq.${encodeEq(id)}"))
    }

    suspend fun resetAllScores() = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("skor_akhir", 0)
            put("skor_j1", 0)
            put("skor_j2", 0)
            put("rincian_skor", JSONObject())
        }
        executePatch(buildRestUrl("peserta", "id=neq.NONE"), body.toString())
    }

    suspend fun deleteAllPeserta() = withContext(Dispatchers.IO) {
        executeDelete(buildRestUrl("peserta", "id=neq.NONE"))
    }

    suspend fun deleteTotalAllData() = withContext(Dispatchers.IO) {
        executeDelete(buildRestUrl("peserta", "id=neq.NONE"))
        executeDelete(buildRestUrl("no_urut_sekolah", "sekolah=neq.NONE"))
        executeDelete(buildRestUrl("akun_juri", "id=neq.NONE"))
    }

    suspend fun exportBackupJson(kecamatan: String, tahun: String): String = withContext(Dispatchers.IO) {
        val aksesArr = executeGet(buildRestUrl("akses", "select=*"))
        val juriArr = executeGet(buildRestUrl("akun_juri", "select=*"))
        val pesertaArr = executeGet(buildRestUrl("peserta", "select=*"))
        val noUrutArr = executeGet(buildRestUrl("no_urut_sekolah", "select=*"))

        JSONObject().apply {
            put("version", "2.8")
            put("timestamp", java.time.Instant.now().toString())
            put("kecamatan", kecamatan)
            put("tahun", tahun)
            put("akses", aksesArr)
            put("akun_juri", juriArr)
            put("peserta", pesertaArr)
            put("no_urut_sekolah", noUrutArr)
        }.toString(2)
    }

    suspend fun restoreBackupJson(rawJson: String) = withContext(Dispatchers.IO) {
        val backup = JSONObject(rawJson)
        if (!backup.has("peserta")) {
            throw IllegalArgumentException("Berkas JSON cadangan tidak sesuai struktur.")
        }
        val aksesArr = backup.optJSONArray("akses")
        if (aksesArr != null && aksesArr.length() > 0) {
            executePost(
                buildRestUrl("akses", "on_conflict=role"),
                aksesArr.toString(),
                prefer = "resolution=merge-duplicates,return=minimal"
            )
        }
        val juriArr = backup.optJSONArray("akun_juri")
        if (juriArr != null && juriArr.length() > 0) {
            executeDelete(buildRestUrl("akun_juri", "id=neq.NONE"))
            executePost(buildRestUrl("akun_juri"), juriArr.toString())
        }
        val pesertaArr = backup.optJSONArray("peserta")
        if (pesertaArr != null && pesertaArr.length() > 0) {
            executeDelete(buildRestUrl("peserta", "id=neq.NONE"))
            executePost(buildRestUrl("peserta"), pesertaArr.toString())
        }
        val noUrutArr = backup.optJSONArray("no_urut_sekolah")
        if (noUrutArr != null && noUrutArr.length() > 0) {
            executePost(
                buildRestUrl("no_urut_sekolah", "on_conflict=sekolah"),
                noUrutArr.toString(),
                prefer = "resolution=merge-duplicates,return=minimal"
            )
        }
    }

    suspend fun saveSkConfig(config: SkConfig) = upsertAksesRow(
        role = "sk_config",
        password = "SK",
        keterangan = JSONObject().apply {
            put("nomorSK", config.nomorSK)
            put("kotaTetap", config.kotaTetap)
            put("tanggalSK", config.tanggalSK)
            put("lokasiLomba", config.lokasiLomba)
            put("ketuaPanitia", config.ketuaPanitia)
            put("nipKetua", config.nipKetua)
            put("sekretarisPanitia", config.sekretarisPanitia)
            put("nipSekretaris", config.nipSekretaris)
            put("ketuaKKG", config.ketuaKKG)
            put("nipKetuaKKG", config.nipKetuaKKG)
        }.toString()
    )

    suspend fun saveKonfigurasiPenilaian(map: Map<String, LombaPenilaianConfig>) = upsertAksesRow(
        role = "konfigurasi_penilaian",
        password = "CFG",
        keterangan = JSONObject().apply {
            map.forEach { (lomba, cfg) ->
                put(lomba, JSONObject().apply {
                    put("jumlahJuri", cfg.jumlahJuri)
                    put("modeDuaJuri", cfg.modeDuaJuri)
                    val kArr = JSONArray()
                    cfg.kriteria.forEach { k ->
                        kArr.put(JSONObject().apply {
                            put("id", k.id)
                            put("label", k.label)
                            put("min", k.min)
                            put("max", k.max)
                            put("juriTarget", k.juriTarget)
                        })
                    }
                    put("kriteria", kArr)
                })
            }
        }.toString()
    )

    suspend fun saveDaftarSekolah(list: List<String>) = upsertAksesRow(
        role = "daftar_sekolah",
        password = "SEKOLAH",
        keterangan = JSONArray(list).toString()
    )

    suspend fun saveKategoriLomba(list: List<KategoriLomba>) = upsertAksesRow(
        role = "kategori_lomba",
        password = "LOMBA",
        keterangan = JSONArray().apply {
            list.forEach { k ->
                put(JSONObject().apply {
                    put("id", k.id)
                    put("nama", k.nama)
                    put("icon", k.icon)
                    put("desc", k.desc)
                    put("maxPeserta", k.maxPeserta)
                })
            }
        }.toString()
    )
}
