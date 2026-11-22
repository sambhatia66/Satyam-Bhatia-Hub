package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.R

sealed interface BiometricAuthResult {
    data object Success : BiometricAuthResult
    data object Cancelled : BiometricAuthResult
    data object NotEnrolled : BiometricAuthResult
    data object Unavailable : BiometricAuthResult
    data class Error(val message: String) : BiometricAuthResult
}

object BiometricAuthManager {
    private const val ALLOWED_BIOMETRICS = BIOMETRIC_STRONG or BIOMETRIC_WEAK

    fun authenticateForCard(
        context: Context,
        cardName: String,
        onResult: (BiometricAuthResult) -> Unit
    ) {
        val activity = context.findFragmentActivity()
        if (activity == null) {
            onResult(BiometricAuthResult.Unavailable)
            return
        }

        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(ALLOWED_BIOMETRICS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showPrompt(activity = activity, cardName = cardName, onResult = onResult)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onResult(BiometricAuthResult.NotEnrolled)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                onResult(BiometricAuthResult.Unavailable)
            }

            else -> {
                onResult(BiometricAuthResult.Unavailable)
            }
        }
    }

    private fun showPrompt(
        activity: FragmentActivity,
        cardName: String,
        onResult: (BiometricAuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(BiometricAuthResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> {
                        onResult(BiometricAuthResult.Cancelled)
                    }

                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        onResult(BiometricAuthResult.NotEnrolled)
                    }

                    BiometricPrompt.ERROR_HW_NOT_PRESENT,
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                        onResult(BiometricAuthResult.Unavailable)
                    }

                    else -> {
                        onResult(BiometricAuthResult.Error(errString.toString()))
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // System BiometricPrompt keeps the sheet open and prompts the user to retry.
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_prompt_title))
            .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle, cardName))
            .setNegativeButtonText(activity.getString(R.string.biometric_prompt_negative))
            .setAllowedAuthenticators(ALLOWED_BIOMETRICS)
            .setConfirmationRequired(false)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }

    fun openBiometricEnrollSettings(context: Context) {
        val enrollIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, ALLOWED_BIOMETRICS)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        val fallbackIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        val intentToLaunch = if (enrollIntent.resolveActivity(context.packageManager) != null) {
            enrollIntent
        } else {
            fallbackIntent
        }
        runCatching {
            context.startActivity(intentToLaunch)
        }
    }

    private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
