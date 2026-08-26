package com.electoral.personeros.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CleanBlueLightColorScheme = lightColorScheme(
    primary = PrimaryCleanBlue,
    onPrimary = OnPrimaryCleanBlue,
    primaryContainer = PrimaryLightCleanBlue,
    onPrimaryContainer = PrimaryDarkCleanBlue,
    secondary = SecondaryCleanBlue,
    onSecondary = OnSecondaryCleanBlue,
    secondaryContainer = PrimaryLightCleanBlue,
    onSecondaryContainer = PrimaryDarkCleanBlue,
    tertiary = PrimaryLightCleanBlue,
    onTertiary = PrimaryDarkCleanBlue,
    background = BackgroundCleanBlue,
    onBackground = OnBackgroundCleanBlue,
    surface = SurfaceCleanBlue,
    onSurface = OnSurfaceCleanBlue,
    surfaceVariant = TextFieldBackground,
    onSurfaceVariant = OnSurfaceCleanBlue,
    error = ErrorCleanBlue,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = PrimaryCleanBlue,
    outlineVariant = PrimaryLightCleanBlue
)

private val CleanBlueDarkColorScheme = darkColorScheme(
    primary = PrimaryLightCleanBlue,
    onPrimary = PrimaryDarkCleanBlue,
    primaryContainer = PrimaryDarkCleanBlue,
    onPrimaryContainer = PrimaryLightCleanBlue,
    secondary = SecondaryCleanBlue,
    onSecondary = Color.White,
    secondaryContainer = PrimaryDarkCleanBlue,
    onSecondaryContainer = PrimaryLightCleanBlue,
    tertiary = PrimaryLightCleanBlue,
    onTertiary = PrimaryDarkCleanBlue,
    background = Color(0xFF0A1929),
    onBackground = Color(0xFFE3F2FD),
    surface = Color(0xFF102031),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1B3045),
    onSurfaceVariant = Color(0xFFB0BEC5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = PrimaryLightCleanBlue,
    outlineVariant = PrimaryCleanBlue
)

@Composable
fun CleanBlueColorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CleanBlueDarkColorScheme
        else -> CleanBlueLightColorScheme
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
