package com.raziel.tiendadisco

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetallePantalla(
    discoId: Int,
    correoActual: String,
    onRegresar: () -> Unit = {},
    onNavegar: (Int) -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { BaseDatos(context) }

    // SELECT del disco y de su detalle
    val disco = remember(discoId) { db.obtenerDisco(discoId) }
    val detalle = remember(discoId) { db.obtenerDetalle(discoId) }

    BackHandler(onBack = onRegresar)

    if (disco == null) {
        LaunchedEffect(Unit) { onRegresar() }
        return
    }

    val msgCarrito = stringResource(R.string.carrito_agregado)
    val msgYaEsta = stringResource(R.string.ya_en_carrito)
    val sinInfo = stringResource(R.string.sin_informacion)

    // Índice de la fila abierta (-1 = ninguna)
    var abierto by rememberSaveable { mutableIntStateOf(-1) }

    val elementos = listOf(
        R.string.el_produccion to detalle.produccion,
        R.string.el_genero to detalle.genero,
        R.string.el_duracion to detalle.duracion,
        R.string.el_canciones to detalle.canciones,
        R.string.el_imagenes to detalle.imagenes,
        R.string.el_premios to detalle.premios,
        R.string.el_artistas to detalle.artistas
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colores.surface)
    ) {

        // ENCABEZADO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onRegresar) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.desc_regresar),
                    tint = colores.onSurface
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = stringResource(R.string.desc_perfil),
                    tint = colores.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // CONTENIDO CON SCROLL
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            // PORTADA + DATOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImagenDisco(
                    nombreArchivo = disco.imagen,
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp)
                )

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = disco.nombre,
                        fontSize = 28.sp,
                        color = colores.onSurface,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = disco.artista,
                        fontSize = 20.sp,
                        color = colores.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.precio, disco.precio),
                        fontSize = 18.sp,
                        color = colores.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BOTÓN AGREGAR AL CARRITO (INSERT en la tabla carrito)
            Button(
                onClick = {
                    val texto = if (db.agregarAlCarrito(correoActual, disco.id)) msgCarrito else msgYaEsta
                    Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colores.tertiary,
                    contentColor = colores.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = stringResource(R.string.desc_carrito)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.agregar_carrito),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // DESCRIPCIÓN
            Text(
                text = stringResource(R.string.descripcion),
                fontSize = 22.sp,
                color = colores.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = detalle.descripcion.ifBlank { sinInfo },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colores.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ELEMENTOS (se despliegan al tocarlos)
            Text(
                text = stringResource(R.string.elementos),
                fontSize = 26.sp,
                color = colores.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            elementos.forEachIndexed { indice, (titulo, texto) ->
                val expandido = abierto == indice
                val giro by animateFloatAsState(
                    targetValue = if (expandido) 90f else 0f,
                    label = "giroFlecha"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { abierto = if (expandido) -1 else indice }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(titulo),
                            fontSize = 19.sp,
                            color = colores.onSurface
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = colores.onSurface,
                            modifier = Modifier.rotate(giro)
                        )
                    }

                    AnimatedVisibility(visible = expandido) {
                        Text(
                            text = texto.ifBlank { sinInfo },
                            fontSize = 15.sp,
                            color = colores.onSurface,
                            lineHeight = 22.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    colores.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // BARRA INFERIOR
        BarraNavegacion(seleccionado = 1, onNavegar = onNavegar)
    }
}