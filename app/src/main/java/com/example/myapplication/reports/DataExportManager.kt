package com.example.myapplication.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.myapplication.data.models.Message
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExportManager {

    fun exportToCsv(
        context: Context,
        roomId: String,
        messages: List<Message>
    ): File? {
        return try {
            val fileName = "VoxBridge_${roomId}_${System.currentTimeMillis()}.csv"
            val dir = context.getExternalFilesDir(null) ?: context.cacheDir
            val file = File(dir, fileName)

            file.bufferedWriter().use { writer ->
                writer.write("Sequence,Message ID,Timestamp,Sender Name,Source Language,Original Text,Translated Text\n")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                messages.forEachIndexed { index, msg ->
                    val formattedTime = dateFormat.format(Date(msg.timestamp))
                    val cleanSender = msg.senderName.replace(",", " ")
                    val cleanOriginal = msg.originalText.replace(",", " ").replace("\n", " ")
                    val cleanTranslation = (msg.translations["en"] ?: msg.originalText).replace(",", " ").replace("\n", " ")

                    writer.write("${index + 1},${msg.id},$formattedTime,$cleanSender,${msg.sourceLanguage},\"$cleanOriginal\",\"$cleanTranslation\"\n")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToPdf(
        context: Context,
        roomId: String,
        messages: List<Message>,
        analytics: AnalyticsData
    ): File? {
        return try {
            val fileName = "VoxBridge_Report_${roomId}_${System.currentTimeMillis()}.pdf"
            val dir = context.getExternalFilesDir(null) ?: context.cacheDir
            val file = File(dir, fileName)

            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
            val page = pdfDoc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                textSize = 20f
                isFakeBoldText = true
            }

            val textPaint = Paint().apply {
                textSize = 12f
            }

            var yPos = 40f
            canvas.drawText("VoxBridge AI — Conversation & Analytics Report", 40f, yPos, titlePaint)

            yPos += 30f
            canvas.drawText("Room ID: $roomId", 40f, yPos, textPaint)
            yPos += 20f
            canvas.drawText("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", 40f, yPos, textPaint)

            yPos += 30f
            canvas.drawText("--- Session Summary ---", 40f, yPos, titlePaint)
            yPos += 20f
            canvas.drawText("Total Messages: ${analytics.totalMessages}", 40f, yPos, textPaint)
            yPos += 20f
            canvas.drawText("Total Translated Words: ${analytics.translatedWordsCount}", 40f, yPos, textPaint)
            yPos += 20f
            canvas.drawText("Total Participants: ${analytics.totalParticipants}", 40f, yPos, textPaint)

            yPos += 30f
            canvas.drawText("--- Message Log Snippet ---", 40f, yPos, titlePaint)
            yPos += 20f

            messages.take(15).forEach { msg ->
                val line = "[${msg.sourceLanguage.uppercase()}] ${msg.senderName}: ${msg.originalText}"
                canvas.drawText(line.take(75), 40f, yPos, textPaint)
                yPos += 18f
            }

            pdfDoc.finishPage(page)
            FileOutputStream(file).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
