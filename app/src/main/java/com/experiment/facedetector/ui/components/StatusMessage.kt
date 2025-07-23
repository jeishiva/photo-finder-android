package com.experiment.facedetector.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = message ?: stringResource(R.string.loading),
                    color = Color.White,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        !errorMessage.isNullOrBlank() -> {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        !message.isNullOrBlank() -> {
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        else -> {
            // Do nothing — you can omit this else entirely
        }
    }
}