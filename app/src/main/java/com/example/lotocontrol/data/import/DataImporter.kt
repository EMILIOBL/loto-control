package com.example.lotocontrol.data.import

import android.content.Context
import android.net.Uri
import com.example.lotocontrol.data.models.Client
import com.example.lotocontrol.data.models.LotterySettings
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class ImportResult {
    data class Success(
        val clients: List<Client>,
        val settings: LotterySettings
    ) : ImportResult()
    
    data class Error(val message: String) : ImportResult()
}

class DataImporter {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    fun importFromExcel(context: Context, uri: Uri): ImportResult {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val workbook = WorkbookFactory.create(stream)
                val sheet = workbook.getSheetAt(0)

                var drawDate = LocalDate.now()
                var ticketPrice = 0.0
                val clients = mutableListOf<Client>()

                // Skip header row
                for (rowIndex in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    
                    // Read cells
                    val name = row.getCell(0)?.stringCellValue ?: continue
                    val dateStr = row.getCell(1)?.stringCellValue
                    val price = row.getCell(2)?.numericCellValue ?: 0.0
                    val ticketsDelivered = row.getCell(3)?.numericCellValue?.toInt() ?: 0
                    val previousDebt = row.getCell(4)?.numericCellValue ?: 0.0

                    // Update settings from first valid row
                    if (rowIndex == 1) {
                        drawDate = if (dateStr != null) {
                            LocalDate.parse(dateStr, DATE_FORMATTER)
                        } else {
                            LocalDate.now()
                        }
                        ticketPrice = price
                    }

                    // Create client
                    clients.add(
                        Client(
                            name = name,
                            ticketsDelivered = ticketsDelivered,
                            previousDebt = previousDebt
                        )
                    )
                }

                workbook.close()

                ImportResult.Success(
                    clients = clients,
                    settings = LotterySettings(
                        drawDate = drawDate,
                        ticketPrice = ticketPrice
                    )
                )
            } ?: ImportResult.Error("No se pudo abrir el archivo")
        } catch (e: Exception) {
            ImportResult.Error("Error al importar datos: ${e.message}")
        }
    }

    fun validateExcelFormat(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val workbook = WorkbookFactory.create(stream)
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)

                // Validate header columns
                val expectedHeaders = listOf(
                    "Cliente",
                    "Fecha Sorteo",
                    "Precio Décimo",
                    "Décimos Entregados",
                    "Deuda Anterior"
                )

                val hasValidHeaders = expectedHeaders.withIndex().all { (index, expectedHeader) ->
                    headerRow?.getCell(index)?.stringCellValue == expectedHeader
                }

                workbook.close()
                hasValidHeaders
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
