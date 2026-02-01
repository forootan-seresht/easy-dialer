package app.arteh.easydialer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("No AppTypography provided")
}

val MaterialTheme.appTypography: AppTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTypography.current

private val DarkColorScheme = darkColorScheme(
    primary = Primary_dark,
    secondary = Accent_dark,
    background = background_dark,
    surface = surface_dark,
    onBackground = BodyDark
)

private val LightColorScheme = lightColorScheme(
    primary = Primary_light,
    secondary = Accent_light,
    background = background_light,
    surface = surface_light,
    onBackground = BodyLight
)

@Composable
fun EasyDialer(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = if (darkTheme) DarkAppTypography else LightAppTypography

    CompositionLocalProvider(LocalAppTypography provides typography) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography, // optional base typography
            content = content
        )
    }
}