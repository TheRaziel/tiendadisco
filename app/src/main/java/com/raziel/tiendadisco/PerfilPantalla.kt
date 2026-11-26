package com.raziel.tiendadisco

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun PerfilPantalla(
    correoActual: String,
    onRegresar: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onNavegar: (Int) -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { BaseDatos(context) }

    BackHandler(onBack = onRegresar)

    var version by remember { mutableIntStateOf(0) }
    var mostrarDialogoNombre by remember { mutableStateOf(false) }

    // SELECT: datos del usuario, historial y foto
    val usuario = remember(version) { db.obtenerUsuario(correoActual) }
    val compras = remember(version) { db.obtenerCompras(correoActual) }
    val foto = remember(usuario.foto) { cargarFoto(usuario.foto) }

    val nombreMostrado = usuario.nombre.ifBlank { correoActual.substringBefore("@") }

    val msgFotoError = stringResource(R.string.foto_error)
    val msgNombre = stringResource(R.string.nombre_guardado)
    val msgFavAgregado = stringResource(R.string.favorito_agregado)
    val msgFavQuitado = stringResource(R.string.favorito_quitado)

    fun aviso(texto: String) {
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
    }

    // Selector de fotos del sistema (no necesita permisos)
    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = guardarFoto(context, uri)
            if (ruta == null) {
                aviso(msgFotoError)
            } else {
                // borra la foto anterior y guarda la nueva (UPDATE)
                if (usuario.foto.isNotBlank()) File(usuario.foto).delete()
                db.actualizarFoto(correoActual, ruta)
                version++
            }
        }
    }

    // DIÁLOGO: EDITAR NOMBRE (UPDATE)
    if (mostrarDialogoNombre) {
        var nombreTemp by remember { mutableStateOf(usuario.nombre) }
        AlertDialog(
            onDismissRequest = { mostrarDialogoNombre = false },
            title = { Text(stringResource(R.string.editar_nombre)) },
            text = {
                OutlinedTextField(
                    value = nombreTemp,
                    onValueChange = { nombreTemp = it },
                    label = { Text(stringResource(R.string.nombre_label)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.actualizarNombre(correoActual, nombreTemp.trim())
                        version++
                        mostrarDialogoNombre = false
                        aviso(msgNombre)
                    }
                ) {
                    Text(stringResource(R.string.boton_guardar))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNombre = false }) {
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
                text = stringResource(R.string.perfil_titulo),
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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // FOTO + NOMBRE + CORREO
            item {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .clickable {
                            selectorFoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (foto != null) {
                        Image(
                            bitmap = foto,
                            contentDescription = stringResource(R.string.desc_foto_perfil),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.desc_foto_perfil),
                            tint = colores.onSurface,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                TextButton(
                    onClick = {
                        selectorFoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text(
                        text = stringResource(R.string.cambiar_foto),
                        color = colores.secondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = nombreMostrado,
                    fontSize = 26.sp,
                    color = colores.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = correoActual,
                    fontSize = 16.sp,
                    color = colores.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(onClick = { mostrarDialogoNombre = true }) {
                    Text(
                        text = stringResource(R.string.editar_nombre),
                        color = colores.secondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.historial_compras),
                    fontSize = 20.sp,
                    color = colores.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // HISTORIAL DE COMPRAS
            if (compras.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.sin_compras),
                        fontSize = 16.sp,
                        color = colores.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(compras, key = { it.id }) { disco ->
                    val esFavorito = remember(version) { db.esFavorito(correoActual, disco.id) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ImagenDisco(
                                nombreArchivo = disco.imagen,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(110.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // INSERT / DELETE en favoritos
                            Button(
                                onClick = {
                                    if (esFavorito) {
                                        db.quitarFavorito(correoActual, disco.id)
                                        aviso(msgFavQuitado)
                                    } else {
                                        db.agregarFavorito(correoActual, disco.id)
                                        aviso(msgFavAgregado)
                                    }
                                    version++
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(55.dp),
                                shape = RoundedCornerShape(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colores.tertiary,
                                    contentColor = colores.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        if (esFavorito) R.string.quitar_favoritos
                                        else R.string.agregar_favoritos
                                    ),
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = disco.nombre,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colores.onSurface
                        )

                        Text(
                            text = disco.artista,
                            fontSize = 16.sp,
                            color = colores.onSurface
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // CERRAR SESIÓN
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(60.dp),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colores.tertiary,
                contentColor = colores.onSurface
            )
        ) {
            Text(
                text = stringResource(R.string.cerrar_sesion),
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }

        BarraNavegacion(seleccionado = 4, onNavegar = onNavegar)
    }
}

// Copia la imagen elegida al almacenamiento interno de la app y regresa su ruta
private fun guardarFoto(context: Context, uri: Uri): String? {
    return try {
        val archivo = File(context.filesDir, "perfil_${System.currentTimeMillis()}.jpg")
        val entrada = context.contentResolver.openInputStream(uri) ?: return null
        entrada.use { origen ->
            archivo.outputStream().use { destino -> origen.copyTo(destino) }
        }
        archivo.absolutePath
    } catch (e: Exception) {
        null
    }
}

// Lee la foto guardada, reduciéndola para no gastar memoria de más
private fun cargarFoto(ruta: String): ImageBitmap? {
    if (ruta.isBlank()) return null
    return try {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, limites)
        var muestra = 1
        while (limites.outWidth / muestra > 800 || limites.outHeight / muestra > 800) {
            muestra *= 2
        }
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
        BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}