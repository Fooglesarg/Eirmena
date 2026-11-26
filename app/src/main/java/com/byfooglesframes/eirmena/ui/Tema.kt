package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byfooglesframes.eirmena.R

// Paleta "calma y cálida": terracota, rosa empolvado y crema.
object Colores {
    val Crema = Color(0xFFFBF4EC)
    val CremaOscura = Color(0xFFF3E6D8)
    val Tarjeta = Color(0xFFFFFBF7)
    val Terracota = Color(0xFFC8694F)
    val TerracotaOscura = Color(0xFF9E4A35)
    val Rosa = Color(0xFFE8B9AE)
    val RosaClaro = Color(0xFFF6DED7)
    val Vino = Color(0xFF6E3036)
    val Salvia = Color(0xFF8FA98A)
    val SalviaClara = Color(0xFFDCE6D6)
    val Texto = Color(0xFF3A2925)
    val TextoSuave = Color(0xFF7D6760)
    val Linea = Color(0xFFE6D5C7)
    val Oro = Color(0xFFD9A441)
    val OroClaro = Color(0xFFF3D58A)
    val OroOscuro = Color(0xFFA8752A)
}

@OptIn(ExperimentalTextApi::class)
private fun outfit(peso: Int) = Font(
    R.font.outfit,
    weight = FontWeight(peso),
    variationSettings = FontVariation.Settings(FontVariation.weight(peso)),
)

val Outfit = FontFamily(outfit(300), outfit(400), outfit(500), outfit(600), outfit(700))
val Comfortaa = FontFamily(Font(R.font.comfortaa_bold, FontWeight.Bold))

private val tipografia = Typography(
    displayLarge = TextStyle(fontFamily = Comfortaa, fontWeight = FontWeight.Bold, fontSize = 44.sp),
    headlineMedium = TextStyle(fontFamily = Comfortaa, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = Comfortaa, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Medium, fontSize = 11.sp),
)

@Composable
fun TemaEirmena(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Colores.Terracota,
            onPrimary = Color.White,
            primaryContainer = Colores.RosaClaro,
            onPrimaryContainer = Colores.Vino,
            secondary = Colores.Vino,
            secondaryContainer = Colores.RosaClaro,
            onSecondaryContainer = Colores.Vino,
            tertiary = Colores.Salvia,
            background = Colores.Crema,
            onBackground = Colores.Texto,
            surface = Colores.Crema,
            onSurface = Colores.Texto,
            surfaceVariant = Colores.CremaOscura,
            onSurfaceVariant = Colores.TextoSuave,
            surfaceContainer = Colores.Tarjeta,
            surfaceContainerLow = Colores.Tarjeta,
            surfaceContainerHigh = Colores.Tarjeta,
            surfaceContainerHighest = Colores.CremaOscura,
            outline = Colores.Linea,
            outlineVariant = Colores.Linea,
            error = Color(0xFFB3261E),
        ),
        typography = tipografia,
        content = contenido,
    )
}

/** Tarjeta clara de bordes redondeados que se usa en todas las pantallas. */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    color: Color = Colores.Tarjeta,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = color,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = contenido)
    }
}

@Composable
fun Firma(modifier: Modifier = Modifier) {
    Text(
        "Desarrollado por ByFooglesFrames",
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = Colores.TextoSuave,
    )
}
