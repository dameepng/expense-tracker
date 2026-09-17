package com.example.expense_tracker.startup.tasks

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.startup.IdempotentStartupTask
import com.example.expense_tracker.startup.StartupPhase
import kotlinx.coroutines.flow.first

/**
 * Initializes and guards the application language setting.
 *
 * Checks existing system/AppCompat locales before calling [AppCompatDelegate.setApplicationLocales],
 * avoiding redundant disk operations, configuration change loops, or activity recreations.
 */
class LocaleGuardStartupTask(
    private val userPreferencesRepository: UserPreferencesRepository
) : IdempotentStartupTask() {

    override val id: String = "locale_guard"
    override val phase: StartupPhase = StartupPhase.CRITICAL
    override val timeoutMs: Long = 600L

    override suspend fun execute() {
        val savedLanguage = userPreferencesRepository.languageFlow.first()
        val targetLocaleTag = if (savedLanguage == "English") "en" else "id"
        val currentLocales = AppCompatDelegate.getApplicationLocales()

        if (currentLocales.toLanguageTags() != targetLocaleTag) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(targetLocaleTag)
            )
        }
    }
}
