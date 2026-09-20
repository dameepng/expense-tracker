package com.example.expense_tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.ui.theme.ExpenseTrackerTheme

private const val LOCK_ICON_DESCRIPTION = "Locked"
private const val APP_LOCKED_TEXT = "Aplikasi Terkunci"
private const val UNLOCK_BUTTON_TEXT = "Buka Kunci"

/**
 * Full-screen lock overlay displayed when biometric lock is active and user is not authenticated.
 */
@Composable
fun BiometricLockScreen(
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = LOCK_ICON_DESCRIPTION,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = APP_LOCKED_TEXT,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onUnlockClick) {
                    Text(UNLOCK_BUTTON_TEXT)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BiometricLockScreenPreview() {
    ExpenseTrackerTheme {
        BiometricLockScreen(onUnlockClick = {})
    }
}
