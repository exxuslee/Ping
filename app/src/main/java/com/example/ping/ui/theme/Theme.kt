package com.example.ping.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@Composable
fun PingTheme(content: @Composable () -> Unit) {
    val isDark = LocalConfiguration.current.uiMode and 0x30 == 0x20
    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}

