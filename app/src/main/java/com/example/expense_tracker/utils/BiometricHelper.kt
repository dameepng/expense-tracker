package com.example.expense_tracker.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    private const val ALLOWED_AUTHENTICATORS = 
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Autentikasi Diperlukan",
        subtitle: String = "Gunakan sidik jari atau wajah untuk masuk",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val availabilityError = checkBiometricAvailability(biometricManager)
        if (availabilityError != null) {
            onError(availabilityError)
            return
        }

        val biometricPrompt = createBiometricPrompt(activity, onSuccess, onError)
        val promptInfo = createPromptInfo(title, subtitle)

        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkBiometricAvailability(biometricManager: BiometricManager): String? {
        return when (biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> null
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Hardware biometrik tidak tersedia"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware biometrik sedang tidak bisa digunakan"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Anda belum mendaftarkan biometrik/PIN di perangkat ini"
            else -> "Fitur biometrik tidak didukung"
        }
    }

    private fun createBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        return BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            }
        )
    }

    private fun createPromptInfo(title: String, subtitle: String): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()
    }
}
