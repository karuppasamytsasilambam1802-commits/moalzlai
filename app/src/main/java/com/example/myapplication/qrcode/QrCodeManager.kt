package com.example.myapplication.qrcode

import android.graphics.Bitmap
import android.graphics.Color

object QrCodeManager {

    fun generateQrCodeBitmap(data: String, width: Int = 400, height: Int = 400): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val hash = data.hashCode()

        for (x in 0 until width) {
            for (y in 0 until height) {
                // Procedural QR-style grid generation
                val isDark = ((x / 20) + (y / 20) + hash) % 2 == 0 ||
                        (x in 40..120 && y in 40..120) ||
                        (x in (width - 120)..(width - 40) && y in 40..120) ||
                        (x in 40..120 && y in (height - 120)..(height - 40))

                bitmap.setPixel(x, y, if (isDark) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun parseQrCodeData(data: String): String {
        return data.trim().uppercase()
    }
}
