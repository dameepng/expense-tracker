package com.example.expense_tracker.ui.onboarding

import com.example.expense_tracker.R
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OnboardingResourceTest {

    @Test
    fun `onboarding string resources exist and are valid`() {
        assertNotEquals(0, R.string.onboarding_title)
        assertNotEquals(0, R.string.onboarding_subtitle)
        assertNotEquals(0, R.string.onboarding_start_button)
    }
}
