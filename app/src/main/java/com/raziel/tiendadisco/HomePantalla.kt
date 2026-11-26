package com.raziel.tiendadisco

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun HomePantalla(
    correoActual: String,
    onAlbumClick: (Disco) -> Unit = {},
    onNavegar: (Int) -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { BaseDatos(context) }

    // El botón atrás no hace nada aquí (Inicio es la pantalla principal)
    BackHandler { }

    // SELECT: todo sale de la base de datos
    val usuario = remember { db.obtenerUsuario(correoActual) }
    val nombre = usuario.nombre
        .ifBlank { correoActual.substringBefore("@") }
        .substringBefore(" ")

    val todos = remember { db.obtenerDiscos(0) }
    val destacado = remember(todos) {
        if (todos.isEmpty()) null
        else todos[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % todos.size]
    }
    val novedades = remember(todos) { todos.reversed().take(6) }
    val baratos = remember { db.obtenerDiscos(2).take(6) }
    val favoritos = remember { db.obtenerFavoritos(correoActual) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colores.surface)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {

            // SALUDO
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)) {
                Text(
                    text = stringResource(R.string.home_saludo, nombre),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colores.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.eslogan),
                    fontSize = 16.sp,
                    color = colores.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DISCO DESTACADO
            if (destacado != null) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .background(colores.primary, RoundedCornerShape(24.dp))
                        .clickable { onAlbumClick(destacado) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImagenDisco(
                        nombreArchivo = destacado.imagen,
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = stringResource(R.string.home_destacado),
                            fontSize = 13.sp,
                            color = colores.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = destacado.nombre,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colores.onPrimary,
                            lineHeight = 26.sp
                        )
                        Text(
                            text = destacado.artista,
                            fontSize = 16.sp,
                            color = colores.onPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.precio, destacado.precio),
                            fontSize = 16.sp,
                            color = colores.onPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onAlbumClick(destacado) },
                            shape = RoundedCornerShape(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colores.tertiary,
                                contentColor = colores.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ver_disco),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilaDiscos(R.string.home_novedades, novedades, onAlbumClick)
            FilaDiscos(R.string.home_baratos, baratos, onAlbumClick)
            FilaDiscos(R.string.home_favoritos, favoritos, onAlbumClick)

            Spacer(modifier = Modifier.height(12.dp))
        }

        BarraNavegacion(seleccionado = 0, onNavegar = onNavegar)
    }
}

// Franja horizontal con título; no se dibuja si la lista está vacía
@Composable
private fun FilaDiscos(
    titulo: Int,
    discos: List<Disco>,
    onAlbumClick: (Disco) -> Unit
) {
    if (discos.isEmpty()) return
    val colores = MaterialTheme.colorScheme

    Text(
        text = stringResource(titulo),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = colores.onSurface,
        modifier = Modifier.padding(horizontal = 20.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(discos, key = { it.id }) { disco ->
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onAlbumClick(disco) }
            ) {
                ImagenDisco(
                    nombreArchivo = disco.imagen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = disco.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colores.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = disco.artista,
                    fontSize = 13.sp,
                    color = colores.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.precio, disco.precio),
                    fontSize = 14.sp,
                    color = colores.tertiary
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}
