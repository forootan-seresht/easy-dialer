package app.arteh.easydialer.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.createBitmap
import app.arteh.easydialer.contacts.ContactRP

object Holder {
    val colors = listOf(
        Color(0xFFFF76C3),
        Color(0xFF9467FF),
        Color(0xFF2492FF),
        Color(0xFFD8226C),
        Color(0xFFFF9B51),
        Color(0xFF00B7B5),
        Color(0xFF5CB855)
    )
    val contactRP = ContactRP()

    fun loadBitmapUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        // Handle hardware bitmaps by copying to software memory if necessary
        val srcBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
        else {
            bitmap
        }

        val output = createBitmap(srcBitmap.width, srcBitmap.height)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
        }

        val rect = Rect(0, 0, srcBitmap.width, srcBitmap.height)
        val radius = (srcBitmap.width / 2).toFloat()

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(radius, radius, radius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(srcBitmap, rect, rect, paint)

        return output
    }
}