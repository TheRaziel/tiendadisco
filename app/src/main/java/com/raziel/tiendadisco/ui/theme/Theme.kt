package com.raziel.tiendadisco.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaTiendaDiscos = lightColorScheme(
    primary = Rosa,
    onPrimary = RosaOscuro,
    primaryContainer = CirculoLogo,
    onPrimaryContainer = RosaOscuro,
    secondary = TextoSecundario,
    secondaryContainer = GrisFiltro,
    onSecondaryContainer = Color.Black,
    tertiary = Rojo,
    background = FondoClaro,
    onBackground = TextoPrincipal,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = GrisClaro,
    onSurfaceVariant = Color.Gray,
    outlineVariant = GrisDivisor,
    inverseSurface = AzulOscuro,
    inverseOnSurface = Color.White
)

@Composable
fun TiendaDiscoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaTiendaDiscos,
        typography = Typography,
        content = content
    )
}