package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = PaiEmerald400,
    onPrimary = PaiEmerald950,
    primaryContainer = PaiEmerald800,
    onPrimaryContainer = PaiEmerald100,
    secondary = PaiGold400,
    onSecondary = Slate950,
    secondaryContainer = PaiGold700,
    onSecondaryContainer = PaiGold100,
    tertiary = PaiEmerald200,
    background = DarkBg,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = PaiEmerald200,
    outline = PaiEmerald700,
    outlineVariant = DarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PaiEmerald600,
    onPrimary = PureWhite,
    primaryContainer = PaiEmerald100,
    onPrimaryContainer = PaiEmerald900,
    secondary = PaiGold600,
    onSecondary = PureWhite,
    secondaryContainer = PaiGold100,
    onSecondaryContainer = PaiGold700,
    tertiary = Slate900,
    onTertiary = PureWhite,
    background = IvoryMintBg,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200
)

val PentasShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PentasPaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PentasShapes,
        content = content
    )
}
