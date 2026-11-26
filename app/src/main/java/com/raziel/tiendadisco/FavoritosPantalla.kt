package com.raziel.tiendadisco

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
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
fun FavoritosPantalla(
    correoActual: String,
    onRegresar: () -> Unit = {},
    onNavegar: (Int) -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { BaseDatos(context) }

    BackHandler(onBack = onRegresar)

    var version by remember { mutableIntStateOf(0) } // sube cada vez que cambian los favoritos
    var mostrarSelector by remember { mutableStateOf(false) }

    // SELECT: favoritos del usuario
    val favoritos = remember(version) { db.obtenerFavoritos(correoActual) }

    val msgAgregado = stringResource(R.string.favorito_agregado)
    val msgQuitado = stringResource(R.string.favorito_quitado)

    fun aviso(texto: String) {
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
    }

    // DIÁLOGO: elegir qué disco agregar (INSERT). Muestra todo el catálogo menos los que ya son favoritos
    if (mostrarSelector) {
        val idsFavoritos = favoritos.map { it.id }.toSet()
        val disponibles = db.obtenerDiscos().filter { it.id !in idsFavoritos }

        AlertDialog(
            onDismissRequest = { mostrarSelector = false },
            title = { Text(stringResource(R.string.elegir_disco)) },
            text = {
                if (disponibles.isEmpty()) {
                    Text(stringResource(R.string.todos_agregados))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(disponibles, key = { it.id }) { disco ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        db.agregarFavorito(correoActual, disco.id)
                                        version++
                                        mostrarSelector = false
                                        aviso(msgAgregado)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ImagenDisco(
                                    nombreArchivo = disco.imagen,
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(60.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = disco.nombre,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = disco.artista,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarSelector = false }) {
                    Text(stringResource(R.string.boton_cancelar))
                }
            }
        )
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
                text = stringResource(R.string.favoritos_titulo),
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

        HorizontalDivider(color = colores.outlineVariant)

        // LISTA DE FAVORITOS (solo portada + botón Quitar)
        if (favoritos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.sin_favoritos),
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
                items(favoritos, key = { it.id }) { disco ->
                    Row(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImagenDisco(
                            nombreArchivo = disco.imagen,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(150.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // DELETE
                        Button(
                            onClick = {
                                db.quitarFavorito(correoActual, disco.id)
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
                                text = stringResource(R.string.boton_quitar),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // BOTÓN AGREGAR +
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { mostrarSelector = true },
                modifier = Modifier.height(60.dp),
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colores.tertiary,
                    contentColor = colores.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 36.dp)
            ) {
                Text(
                    text = stringResource(R.string.boton_agregar),
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.desc_agregar)
                )
            }
        }

        BarraNavegacion(seleccionado = 3, onNavegar = onNavegar)
    }
}