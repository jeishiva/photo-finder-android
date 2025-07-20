package com.experiment.facedetector.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.experiment.facedetector.R
import com.experiment.facedetector.common.LogManager

@Composable
fun StatusMessage(
    isLoading: Boolean,
    errorMessage: String?,
    message: String?
) {
    LogManager.d(
        "StatusMessage",
        "isLoading: $isLoading, errorMessage: $errorMessage, message: $message"
    )
    when {
        isLoading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message ?: stringResource(R.string.loading),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        !errorMessage.isNullOrBlank() -> {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        !message.isNullOrBlank() -> {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        else -> {
            // Do nothing — you can omit this else entirely
        }
    }
}