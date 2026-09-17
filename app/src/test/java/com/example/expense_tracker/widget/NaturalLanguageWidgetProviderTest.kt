package com.example.expense_tracker.widget

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class NaturalLanguageWidgetProviderTest {

    private lateinit var context: Context
    private lateinit var provider: NaturalLanguageWidgetProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        provider = NaturalLanguageWidgetProvider()
    }

    @Test
    fun provider_initializationAndConstants() {
        assertNotNull(provider)
        assertEquals("extra_auto_speech", NaturalLanguageWidgetProvider.EXTRA_AUTO_SPEECH)
    }

    @Test
    fun updateAllWidgets_doesNotCrashWhenNoWidgets() {
        NaturalLanguageWidgetProvider.updateAllWidgets(context)
    }
}
