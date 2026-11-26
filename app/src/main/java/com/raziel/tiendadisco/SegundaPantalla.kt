package com.raziel.tiendadisco

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun SegundaPantalla(
    correoActual: String,
    onRegresar: () -> Unit = {},
    onCuentaEliminada: () -> Unit = {},
    onAlbumClick: (Disco) -> Unit = {},
    onNavegar: (Int) -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { BaseDatos(context) }

    val filtros = listOf(
        R.string.filtro_mejores,
        R.string.filtro_mayor_precio,
        R.string.filtro_menor_precio
    )

    var filtroSeleccionado by rememberSaveable { mutableIntStateOf(0) }
    var mostrarMas by rememberSaveable { mutableStateOf(false) }
    var version by remember { mutableIntStateOf(0) } // sube cada vez que cambia la tabla
    var mostrarFormulario by remember { mutableStateOf(false) }
    var discoEditando by remember { mutableStateOf<Disco?>(null) }
    var mostrarCuenta by remember { mutableStateOf(false) }

    // SELECT: la lista sale de la base de datos y se recarga al cambiar filtro o datos
    val productosOrdenados = remember(filtroSeleccionado, version) {
        db.obtenerDiscos(filtroSeleccionado)
    }
    val listaVisible = if (mostrarMas) productosOrdenados else productosOrdenados.take(4)

    val msgGuardado = stringResource(R.string.disco_guardado)
    val msgEliminado = stringResource(R.string.disco_eliminado)
    val msgInvalido = stringResource(R.string.dato_invalido)
    val msgCuentaEliminada = stringResource(R.string.cuenta_eliminada)

    fun aviso(texto: String) {
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
    }

    // DIÁLOGO: AGREGAR (INSERT) / EDITAR (UPDATE)
    if (mostrarFormulario) {
        val detalleInicial = remember(discoEditando) {
            discoEditando?.let { db.obtenerDetalle(it.id) } ?: DetalleDisco()
        }

        DialogoDisco(
            disco = discoEditando,
            detalleInicial = detalleInicial,
            onGuardar = { nombre, artista, precioTexto, imagen, detalle ->
                val precio = precioTexto.toIntOrNull()
                if (nombre.isBlank() || artista.isBlank() || precio == null) {
                    aviso(msgInvalido)
                } else {
                    val editando = discoEditando
                    if (editando == null) {
                        val nuevoId = db.insertarDisco(nombre, artista, precio, imagen)
                        if (nuevoId > 0) db.guardarDetalle(nuevoId.toInt(), detalle)
                    } else {
                        // si cambió la portada y la anterior era una foto elegida, se borra el archivo
                        if (editando.imagen.startsWith("/") && editando.imagen != imagen) {
                            File(editando.imagen).delete()
                        }
                        db.actualizarDisco(Disco(editando.id, nombre, artista, precio, imagen))
                        db.guardarDetalle(editando.id, detalle)
                    }
                    version++
                    mostrarFormulario = false
                    discoEditando = null
                    aviso(msgGuardado)
                }
            },
            onCancelar = {
                mostrarFormulario = false
                discoEditando = null
            }
        )
    }

    // DIÁLOGO: MI CUENTA (DELETE de usuarios)
    if (mostrarCuenta) {
        AlertDialog(
            onDismissRequest = { mostrarCuenta = false },
            title = { Text(stringResource(R.string.dialogo_cuenta_titulo)) },
            text = { Text(stringResource(R.string.dialogo_cuenta_texto, correoActual)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.eliminarUsuario(correoActual)
                        mostrarCuenta = false
                        aviso(msgCuentaEliminada)
                        onCuentaEliminada()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.boton_eliminar_cuenta),
                        color = colores.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCuenta = false }) {
                    Text(stringResource(R.string.boton_cerrar))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

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
                    text = stringResource(R.string.catalogo),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = colores.onSurface
                )

                IconButton(onClick = { mostrarCuenta = true }) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.desc_perfil),
                        tint = colores.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // FILTROS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtros.forEachIndexed { indice, filtro ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                color = if (filtroSeleccionado == indice)
                                    colores.tertiary
                                else
                                    colores.secondaryContainer,
                                shape = RoundedCornerShape(30.dp)
                            )
                            .clickable { filtroSeleccionado = indice },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(filtro),
                            fontSize = 14.sp,
                            color = if (filtroSeleccionado == indice)
                                colores.onTertiary
                            else
                                colores.onSecondaryContainer
                        )
                    }
                }
            }

            // LISTA DE DISCOS
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(listaVisible, key = { it.id }) { disco ->
                    Row(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { onAlbumClick(disco) }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImagenDisco(
                            nombreArchivo = disco.imagen,
                            modifier = Modifier
                                .weight(1f)
                                .height(125.dp)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1.35f)) {
                            Text(
                                text = disco.nombre,
                                fontSize = 25.sp,
                                color = colores.onSurface,
                                lineHeight = 32.sp
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = disco.artista,
                                fontSize = 18.sp,
                                color = colores.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = stringResource(R.string.precio, disco.precio),
                                fontSize = 18.sp,
                                color = colores.tertiary
                            )

                            Row {
                                // UPDATE
                                IconButton(
                                    onClick = {
                                        discoEditando = disco
                                        mostrarFormulario = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.desc_editar),
                                        tint = colores.onSurface
                                    )
                                }
                                // DELETE
                                IconButton(
                                    onClick = {
                                        db.eliminarDisco(disco.id)
                                        version++
                                        aviso(msgEliminado)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.desc_eliminar),
                                        tint = colores.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                if (mostrarMas) R.string.ver_menos else R.string.ver_mas
                            ),
                            fontSize = 19.sp,
                            color = colores.tertiary,
                            modifier = Modifier
                                .clickable { mostrarMas = !mostrarMas }
                                .padding(15.dp)
                        )
                    }
                }
            }

            // BARRA INFERIOR
            BarraNavegacion(seleccionado = 1, onNavegar = onNavegar)
        }

        // BOTÓN + (INSERT)
        FloatingActionButton(
            onClick = {
                discoEditando = null
                mostrarFormulario = true
            },
            containerColor = colores.tertiary,
            contentColor = colores.onTertiary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.desc_agregar)
            )
        }
    }
}

@Composable
private fun DialogoDisco(
    disco: Disco?,
    detalleInicial: DetalleDisco,
    onGuardar: (String, String, String, String, DetalleDisco) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf(disco?.nombre ?: "") }
    var artista by remember { mutableStateOf(disco?.artista ?: "") }
    var precio by remember { mutableStateOf(disco?.precio?.toString() ?: "") }
    var imagen by remember { mutableStateOf(disco?.imagen ?: "") }

    var descripcion by remember { mutableStateOf(detalleInicial.descripcion) }
    var produccion by remember { mutableStateOf(detalleInicial.produccion) }
    var genero by remember { mutableStateOf(detalleInicial.genero) }
    var duracion by remember { mutableStateOf(detalleInicial.duracion) }
    var canciones by remember { mutableStateOf(detalleInicial.canciones) }
    var imagenes by remember { mutableStateOf(detalleInicial.imagenes) }
    var premios by remember { mutableStateOf(detalleInicial.premios) }
    var artistas by remember { mutableStateOf(detalleInicial.artistas) }

    val msgImagenError = stringResource(R.string.imagen_error)

    // Selector de fotos del sistema (no necesita permisos)
    val selectorImagen = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = guardarPortada(context, uri)
            if (ruta == null) {
                Toast.makeText(context, msgImagenError, Toast.LENGTH_SHORT).show()
            } else {
                // si había elegido otra foto en este mismo formulario y no es la original, la borra
                if (imagen.startsWith("/") && imagen != disco?.imagen) File(imagen).delete()
                imagen = ruta
            }
        }
    }

    // Al cancelar, si se eligió una foto nueva que no se guardó, se borra el archivo
    val cancelar = {
        if (imagen.startsWith("/") && imagen != disco?.imagen) File(imagen).delete()
        onCancelar()
    }

    AlertDialog(
        onDismissRequest = cancelar,
        title = {
            Text(
                stringResource(
                    if (disco == null) R.string.titulo_agregar_disco
                    else R.string.titulo_editar_disco
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // PORTADA
                ImagenDisco(
                    nombreArchivo = imagen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            selectorImagen.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text(
                            stringResource(
                                if (imagen.isBlank()) R.string.elegir_imagen
                                else R.string.cambiar_imagen
                            )
                        )
                    }
                    if (imagen.isNotBlank()) {
                        TextButton(
                            onClick = {
                                if (imagen.startsWith("/") && imagen != disco?.imagen) File(imagen).delete()
                                imagen = ""
                            }
                        ) {
                            Text(stringResource(R.string.quitar_imagen))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // DATOS DEL CATÁLOGO
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.campo_nombre)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = artista,
                    onValueChange = { artista = it },
                    label = { Text(stringResource(R.string.campo_artista)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.campo_precio)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.seccion_info),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // INFORMACIÓN (tabla detalles)
                CampoTexto(descripcion, { descripcion = it }, R.string.descripcion, 3)
                CampoTexto(produccion, { produccion = it }, R.string.el_produccion)
                CampoTexto(genero, { genero = it }, R.string.el_genero)
                CampoTexto(duracion, { duracion = it }, R.string.el_duracion)
                CampoTexto(
                    canciones, { canciones = it }, R.string.el_canciones, 4,
                    ayuda = R.string.ayuda_canciones
                )
                CampoTexto(imagenes, { imagenes = it }, R.string.el_imagenes)
                CampoTexto(premios, { premios = it }, R.string.el_premios)
                CampoTexto(artistas, { artistas = it }, R.string.el_artistas)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onGuardar(
                        nombre.trim(), artista.trim(), precio, imagen,
                        DetalleDisco(
                            descripcion = descripcion.trim(),
                            produccion = produccion.trim(),
                            genero = genero.trim(),
                            duracion = duracion.trim(),
                            canciones = canciones.trim(),
                            imagenes = imagenes.trim(),
                            premios = premios.trim(),
                            artistas = artistas.trim()
                        )
                    )
                }
            ) {
                Text(stringResource(R.string.boton_guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = cancelar) {
                Text(stringResource(R.string.boton_cancelar))
            }
        }
    )
}

@Composable
private fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: Int,
    lineas: Int = 1,
    ayuda: Int? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(stringResource(etiqueta)) },
        supportingText = ayuda?.let { { Text(stringResource(it)) } },
        singleLine = lineas == 1,
        minLines = lineas,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

// Copia la imagen elegida al almacenamiento interno de la app y regresa su ruta
private fun guardarPortada(context: Context, uri: Uri): String? {
    return try {
        val archivo = File(context.filesDir, "portada_${System.currentTimeMillis()}.jpg")
        val entrada = context.contentResolver.openInputStream(uri) ?: return null
        entrada.use { origen ->
            archivo.outputStream().use { destino -> origen.copyTo(destino) }
        }
        archivo.absolutePath
    } catch (e: Exception) {
        null
    }
}

// Lee una portada guardada, reduciéndola para no gastar memoria de más
private fun cargarPortada(ruta: String): ImageBitmap? {
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

// Muestra la portada: foto elegida de la galería (ruta), imagen de drawable (nombre) o un recuadro gris.
// Es pública porque también la usan las demás pantallas.
@Composable
fun ImagenDisco(
    nombreArchivo: String,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val context = LocalContext.current

    val bitmap = remember(nombreArchivo) {
        if (nombreArchivo.startsWith("/")) cargarPortada(nombreArchivo) else null
    }
    val recursoId = remember(nombreArchivo) {
        if (nombreArchivo.isBlank() || nombreArchivo.startsWith("/")) 0
        else context.resources.getIdentifier(nombreArchivo, "drawable", context.packageName)
    }

    when {
        bitmap != null -> Image(
            bitmap = bitmap,
            contentDescription = stringResource(R.string.imagen_disco),
            contentScale = ContentScale.Fit,
            modifier = modifier
        )

        recursoId != 0 -> Image(
            painter = painterResource(recursoId),
            contentDescription = stringResource(R.string.imagen_disco),
            contentScale = ContentScale.Fit,
            modifier = modifier
        )

        else -> Box(
            modifier = modifier.background(
                colores.surfaceVariant,
                RoundedCornerShape(8.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.imagen_disco),
                color = colores.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}