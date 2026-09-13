package com.example.expense_tracker.data.ai.receipt

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Base64

data class ProcessedReceiptImage(val mediaType: String, val base64: String, val width: Int, val height: Int)

enum class ReceiptImageError { INVALID_URI, UNSUPPORTED_MIME, DECODE_FAILED, TOO_LARGE, COMPRESSION_FAILED }

class ReceiptImageException(val reason: ReceiptImageError) : Exception(reason.name)

class ReceiptImageProcessor(
    private val resolver: ContentResolver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun process(uri: Uri): ProcessedReceiptImage = withContext(ioDispatcher) {
        val declaredMime = resolver.getType(uri)?.lowercase()
        val mime = (declaredMime ?: uri.toString().substringAfterLast('.', "").let { "image/$it" })
            .takeIf { it in SUPPORTED_MIME }
            ?: throw ReceiptImageException(if (declaredMime == null) ReceiptImageError.INVALID_URI else ReceiptImageError.UNSUPPORTED_MIME)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw ReceiptImageException(ReceiptImageError.INVALID_URI)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw ReceiptImageException(ReceiptImageError.DECODE_FAILED)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) }
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw ReceiptImageException(ReceiptImageError.DECODE_FAILED)
        val oriented = resolver.openInputStream(uri)?.use { input ->
            val orientation = ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            orient(bitmap, orientation)
        } ?: bitmap
        try {
            val output = ByteArrayOutputStream()
            if (!oriented.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) throw ReceiptImageException(ReceiptImageError.COMPRESSION_FAILED)
            val bytes = output.toByteArray()
            val encoded = Base64.getEncoder().encodeToString(bytes)
            if (encoded.length > MAX_BASE64_CHARS) throw ReceiptImageException(ReceiptImageError.TOO_LARGE)
            ProcessedReceiptImage("image/jpeg", encoded, oriented.width, oriented.height)
        } finally {
            if (oriented !== bitmap) oriented.recycle()
            bitmap.recycle()
        }
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (width / sample > MAX_DIMENSION || height / sample > MAX_DIMENSION) sample *= 2
        return sample
    }

    private fun orient(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private companion object {
        val SUPPORTED_MIME = setOf("image/jpeg", "image/png", "image/webp")
        const val MAX_DIMENSION = 1600
        const val JPEG_QUALITY = 82
        const val MAX_BASE64_CHARS = 240_000
    }
}
