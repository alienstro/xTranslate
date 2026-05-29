package com.xtranslate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkInk,
        onPrimary = DarkPaper,
        secondary = WarmAccent,
        background = DarkPaper,
        onBackground = DarkInk,
        surface = DarkSurface,
        onSurface = DarkInk,
        surfaceVariant = WarmInk,
        onSurfaceVariant = WarmPanel,
        outline = WarmMuted,
)

private val LightColorScheme =
    lightColorScheme(
        primary = WarmInk,
        onPrimary = WarmSurface,
        secondary = WarmAccent,
        onSecondary = WarmSurface,
        background = WarmPaper,
        onBackground = WarmInk,
        surface = WarmSurface,
        onSurface = WarmInk,
        surfaceVariant = WarmPanel,
        onSurfaceVariant = WarmMuted,
        outline = WarmLine,
)

@Composable
fun XTranslateAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
