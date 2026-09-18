package com.example.expense_tracker.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileStaticDataTest {

    @Test
    fun `defaultFaqItems contains valid resource identifiers`() {
        val faqItems = defaultFaqItems()

        assertEquals(7, faqItems.size)
        faqItems.forEach { item ->
            assertNotEquals(0, item.questionResId)
            assertNotEquals(0, item.answerResId)
        }
    }

    @Test
    fun `defaultPrivacySections contains valid resource identifiers`() {
        val sections = defaultPrivacySections()

        assertEquals(7, sections.size)
        sections.forEach { section ->
            assertNotEquals(0, section.titleResId)
            assertNotEquals(0, section.contentResId)
        }
    }

    @Test
    fun `faq and privacy items have unique questions and titles`() {
        val faqQuestions = defaultFaqItems().map { it.questionResId }
        assertEquals(faqQuestions.size, faqQuestions.distinct().size)

        val privacyTitles = defaultPrivacySections().map { it.titleResId }
        assertEquals(privacyTitles.size, privacyTitles.distinct().size)
    }
}
