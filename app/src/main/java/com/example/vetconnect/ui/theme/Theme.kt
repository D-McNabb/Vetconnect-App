package com.example.vetconnect.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = OnPrimary,
    primaryContainer = Indigo500,
    onPrimaryContainer = OnPrimary,
    secondary = Teal500,
    onSecondary = OnSecondary,
    secondaryContainer = Teal400,
    onSecondaryContainer = OnSecondary,
    tertiary = Teal600,
    onTertiary = OnSecondary,
    tertiaryContainer = Teal400,
    onTertiaryContainer = OnSecondary,
    error = Red500,
    onError = OnPrimary,
    errorContainer = Red600,
    onErrorContainer = OnPrimary,
    background = LightGray,
    onBackground = OnBackground,
    surface = SurfaceLight,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Gray300,
    outlineVariant = Gray200,
    scrim = Gray900.copy(alpha = 0.32f),
    inverseSurface = SurfaceDark,
    inverseOnSurface = OnPrimary,
    inversePrimary = Indigo500,
    surfaceDim = Gray100,
    surfaceBright = White,
    surfaceContainerLowest = White,
    surfaceContainerLow = Gray50,
    surfaceContainer = Gray100,
    surfaceContainerHigh = Gray200,
    surfaceContainerHighest = Gray300,
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = OnPrimary,
    primaryContainer = Indigo600,
    onPrimaryContainer = OnPrimary,
    secondary = Teal400,
    onSecondary = OnSecondary,
    secondaryContainer = Teal500,
    onSecondaryContainer = OnSecondary,
    tertiary = Teal400,
    onTertiary = OnSecondary,
    tertiaryContainer = Teal500,
    onTertiaryContainer = OnSecondary,
    error = Red500,
    onError = OnPrimary,
    errorContainer = Red600,
    onErrorContainer = OnPrimary,
    background = Gray900,
    onBackground = OnPrimary,
    surface = SurfaceDark,
    onSurface = OnPrimary,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    outline = Gray600,
    outlineVariant = Gray700,
    scrim = Gray900.copy(alpha = 0.32f),
    inverseSurface = SurfaceLight,
    inverseOnSurface = OnSurface,
    inversePrimary = Indigo400,
    surfaceDim = Gray900,
    surfaceBright = Gray800,
    surfaceContainerLowest = Gray900,
    surfaceContainerLow = Gray800,
    surfaceContainer = Gray700,
    surfaceContainerHigh = Gray600,
    surfaceContainerHighest = Gray500,
)

@Composable
fun VetConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}