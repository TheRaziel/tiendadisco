package com.raziel.tiendadisco

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

// seleccionado: 0 inicio, 1 catálogo, 2 carrito, 3 favoritos, 4 cuenta
@Composable
fun BarraNavegacion(
    seleccionado: Int,
    onNavegar: (Int) -> Unit
) {
    val colores = MaterialTheme.colorScheme

    val iconos = listOf(
        Icons.Default.Home to R.string.desc_inicio,
        Icons.Default.DateRange to R.string.desc_calendario,
        Icons.Default.ShoppingCart to R.string.desc_carrito,
        Icons.Default.Favorite to R.string.desc_favoritos,
        Icons.Default.Person to R.string.desc_cuenta
    )

    HorizontalDivider(color = colores.outlineVariant)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconos.forEachIndexed { indice, (icono, descripcion) ->
            IconButton(onClick = { onNavegar(indice) }) {
                Icon(
                    imageVector = icono,
                    contentDescription = stringResource(descripcion),
                    modifier = Modifier.size(27.dp),
                    tint = if (indice == seleccionado)
                        colores.onSurface
                    else
                        colores.outlineVariant
                )
            }
        }
    }
}