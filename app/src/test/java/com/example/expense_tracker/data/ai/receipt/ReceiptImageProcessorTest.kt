package com.example.expense_tracker.data.ai.receipt

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ReceiptImageProcessorTest {
    @Test fun validImageProducesJpegBase64AndBoundedDimensions() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.cacheDir, "receipt-test.png")
        Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
        val result = ReceiptImageProcessor(context.contentResolver).process(Uri.fromFile(file))
        assertEquals("image/jpeg", result.mediaType)
        assertTrue(result.base64.isNotEmpty()); assertEquals(40, result.width); assertEquals(20, result.height)
        file.delete()
    }

    @Test fun invalidUriReturnsDomainError() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val error = runCatching { ReceiptImageProcessor(context.contentResolver).process(Uri.parse("content://missing/receipt")) }.exceptionOrNull()
        assertTrue(error is ReceiptImageException)
        assertEquals(ReceiptImageError.INVALID_URI, (error as ReceiptImageException).reason)
    }
}
