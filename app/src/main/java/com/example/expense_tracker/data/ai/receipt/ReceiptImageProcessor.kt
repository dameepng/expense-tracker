package com.example.expense_tracker.data.ai.receipt

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
        // Gallery providers may omit MIME for content:// URIs; decoding and
        // re-encoding as JPEG is still safe for the supported image formats.
        val mime = (declaredMime ?: "image/jpeg")
            .takeIf { it in SUPPORTED_MIME || it.startsWith("image/") }
            ?: throw ReceiptImageException(if (declaredMime == null) ReceiptImageError.INVALID_URI else ReceiptImageError.UNSUPPORTED_MIME)
        val sourceBytes = try {
            resolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        } ?: throw ReceiptImageException(ReceiptImageError.INVALID_URI)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw ReceiptImageException(ReceiptImageError.DECODE_FAILED)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) }
        val bitmap = BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.size, options)
            ?: throw ReceiptImageException(ReceiptImageError.DECODE_FAILED)
        val oriented = bitmap
        try {
            val output = ByteArrayOutputStream()
            if (!oriented.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                throw ReceiptImageException(ReceiptImageError.COMPRESSION_FAILED)
            }
            var bytes = output.toByteArray()
            if (bytes.size > MAX_BASE64_CHARS * 3 / 4) {
                output.reset()
                if (!oriented.compress(Bitmap.CompressFormat.JPEG, JPEG_FALLBACK_QUALITY, output)) {
                    throw ReceiptImageException(ReceiptImageError.COMPRESSION_FAILED)
                }
                bytes = output.toByteArray()
            }
            val encoded = Base64.getEncoder().encodeToString(bytes)
            if (encoded.length > MAX_BASE64_CHARS) {
                throw ReceiptImageException(ReceiptImageError.TOO_LARGE)
            }
            ProcessedReceiptImage(JPEG_MIME, encoded, oriented.width, oriented.height)
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

    private companion object {
        const val JPEG_MIME = "image/jpeg"
        val SUPPORTED_MIME = setOf(JPEG_MIME, "image/png", "image/webp")
        const val MAX_DIMENSION = 1600
        const val JPEG_QUALITY = 82
        const val JPEG_FALLBACK_QUALITY = 60
        const val MAX_BASE64_CHARS = 240_000
    }
}
