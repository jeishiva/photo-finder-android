package com.experiment.facedetector.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * The main theme composable for the Android Face Detector app.
 *
 * @param darkTheme Whether to use dark theme colors. Defaults to the system theme.
 * @param dynamicColor Whether to use dynamic color theming (Android 12+). Defaults to true.
 * @param content The content to be displayed within this theme.
 */
@Composable
fun AndroidFaceDetectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = colorScheme(context, darkTheme, dynamicColor),
        typography = MaterialTheme.typography,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBackgroundBrush())
            ) {
                content()
            }
        })
}

@Composable
fun colorScheme(
    context: android.content.Context, darkTheme: Boolean, dynamicColor: Boolean
): ColorScheme {
    return when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> {
            DarkColorScheme
        }

        else -> {
            LightColorScheme
        }
    }
}

@Composable
private fun screenBackgroundBrush(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            GradientStartMildGrey, GradientEndMildBlack
        )
    )
}


