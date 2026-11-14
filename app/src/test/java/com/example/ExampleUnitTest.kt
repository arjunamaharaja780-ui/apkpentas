package com.example

import com.example.model.DefaultData
import com.example.model.Peserta
import com.example.util.PrintAndExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formatDuaDigit_formatsNumbersProperly() {
        assertEquals("01", DefaultData.formatDuaDigit("1"))
        assertEquals("09", DefaultData.formatDuaDigit("9"))
        assertEquals("12", DefaultData.formatDuaDigit("12"))
        assertEquals("-", DefaultData.formatDuaDigit("-"))
        assertEquals("-", DefaultData.formatDuaDigit(""))
    }

    @Test
    fun normalisasiNamaLomba_stripsKuotaSuffix() {
        assertEquals("Cerdas Cermat PAI", DefaultData.normalisasiNamaLomba("Cerdas Cermat PAI (3 Peserta)"))
        assertEquals("MTQ Putra", DefaultData.normalisasiNamaLomba("MTQ Putra"))
    }

    @Test
    fun compareNoUrut_sortsNumericOrderWithDashLast() {
        val list = listOf(
            Peserta(id = "1", noPeserta = "-", nama = "A", sekolah = "SDN 3", lomba = "MTQ Putra"),
            Peserta(id = "2", noPeserta = "10", nama = "B", sekolah = "SDN 2", lomba = "MTQ Putra"),
            Peserta(id = "3", noPeserta = "2", nama = "C", sekolah = "SDN 1", lomba = "MTQ Putra")
        ).sortedWith(DefaultData::compareNoUrut)

        assertEquals("3", list[0].id)
        assertEquals("2", list[1].id)
        assertEquals("1", list[2].id)
    }

    @Test
    fun parseCsvPeserta_importsValidRowsAndAssignsSchoolNoUrut() {
        val csv = """
            Nomor Tampil,Nama Peserta,Asal Sekolah,Kategori Lomba
            ,Ahmad Farhan,SDN BANTARGEBANG 1,MTQ Putra
            05,Siti Nurhaliza,SDN CIKIWUL 1,MHQ Putri
        """.trimIndent()
        val parsed = PrintAndExportHelper.parseCsvPeserta(csv, mapOf("SDN BANTARGEBANG 1" to "03"))
        assertEquals(2, parsed.size)
        assertEquals("03", parsed[0].noPeserta)
        assertEquals("Ahmad Farhan", parsed[0].nama)
        assertEquals("05", parsed[1].noPeserta)
        assertTrue(parsed[1].lomba == "MHQ Putri")
    }
}
