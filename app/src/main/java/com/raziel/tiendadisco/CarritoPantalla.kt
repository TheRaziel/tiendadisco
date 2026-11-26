package com.raziel.tiendadisco

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CarritoPantalla(
    correoActual: String,
    onRegresar: () -> Unit = {},
    onNavegar: (Int) -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { BaseDatos(context) }

    BackHandler(onBack = onRegresar)

    var version by remember { mutableIntStateOf(0) } // sube cada vez que cambia el carrito

    // SELECT: lista y total salen de la base de datos
    val discos = remember(version) { db.obtenerCarrito(correoActual) }
    val total = remember(version) { db.totalCarrito(correoActual) }

    val msgQuitado = stringResource(R.string.quitado_carrito)
    val msgCompra = stringResource(R.string.compra_ok)

    fun aviso(texto: String) {
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
    }

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

            Text(
                text = stringResource(R.string.carrito_titulo),
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = colores.onSurface
            )

            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = stringResource(R.string.desc_perfil),
                    tint = colores.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // LISTA DEL CARRITO
        if (discos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.carrito_vacio),
                    fontSize = 18.sp,
                    color = colores.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(discos, key = { it.id }) { disco ->
                    Column(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ImagenDisco(
                                nombreArchivo = disco.imagen,
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(120.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // DELETE
                            Button(
                                onClick = {
                                    db.quitarDelCarrito(correoActual, disco.id)
                                    version++
                                    aviso(msgQuitado)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(55.dp),
                                shape = RoundedCornerShape(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colores.tertiary,
                                    contentColor = colores.onSurface
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.boton_eliminar_item),
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = disco.nombre,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colores.onSurface
                        )

                        Text(
                            text = disco.artista,
                            fontSize = 17.sp,
                            color = colores.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.precio, disco.precio),
                            fontSize = 17.sp,
                            color = colores.tertiary
                        )
                    }
                }
            }
        }

        // TOTAL Y FINALIZAR COMPRA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.total_compra, total),
                fontSize = 18.sp,
                color = colores.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    db.finalizarCompra(correoActual)
                    version++
                    aviso(msgCompra)
                },
                enabled = discos.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colores.inverseSurface,
                    contentColor = colores.inverseOnSurface
                )
            ) {
                Text(
                    text = stringResource(R.string.finalizar_compra),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        BarraNavegacion(seleccionado = 2, onNavegar = onNavegar)
    }
}