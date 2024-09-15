package com.kamina.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

// Define the colors for the light and dark themes
private val LightColors = lightColorScheme(
    primary = Color(0xFF6200EA),  // Primary color
    secondary = Color(0xFF03DAC6),  // Secondary color
    background = Color(0xFFF6F6F6),  // Background color
    surface = Color(0xFFFFFFFF),  // Surface color
    onPrimary = Color.White,  // Color for text/icons on primary
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    surface = Color(0xFF1F1F1F),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

// Apply the theme based on system settings (dark/light)
@Composable
fun KaminaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    Box(  // Use Box instead of Surface to avoid overriding background
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF58125D),  // Start color
                        Color(0xFF1F2168)   // End color
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(500f, 500f)  // Adjust to simulate 50-degree angle
                )
            )
    ) {
        content()  // Apply the content over the gradient background
    }
}
