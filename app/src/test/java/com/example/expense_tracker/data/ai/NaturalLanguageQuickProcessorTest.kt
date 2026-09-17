package com.example.expense_tracker.data.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NaturalLanguageQuickProcessorTest {

    @Test
    fun processAndSave_withEmptyText_returnsFailure() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = NaturalLanguageQuickProcessor.processAndSave(context, "   ")
        assertTrue("Expected failure for blank text", result.isFailure)
    }
}
