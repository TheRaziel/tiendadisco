package com.raziel.tiendadisco

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrimeraPantalla(
    onIniciarSesion: (String) -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { BaseDatos(context) }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var dialogo by remember { mutableIntStateOf(0) } // 0 = ninguno, 1 = registro, 2 = cambiar contraseña

    // Textos para los avisos (se leen aquí porque stringResource no se puede usar dentro de onClick)
    val msgVacios = stringResource(R.string.error_campos_vacios)
    val msgLoginMal = stringResource(R.string.login_incorrecto)
    val msgRegistroOk = stringResource(R.string.registro_ok)
    val msgRegistroError = stringResource(R.string.registro_error)
    val msgContrasenaOk = stringResource(R.string.contrasena_ok)
    val msgNoExiste = stringResource(R.string.correo_no_existe)

    fun aviso(texto: String) {
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
    }

    val coloresCampo = TextFieldDefaults.colors(
        focusedContainerColor = colores.primary,
        unfocusedContainerColor = colores.primary,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )

    // DIÁLOGO: REGISTRO (INSERT)
    if (dialogo == 1) {
        DialogoCredenciales(
            titulo = stringResource(R.string.dialogo_registro_titulo),
            etiquetaContrasena = stringResource(R.string.contrasena_label),
            textoConfirmar = stringResource(R.string.boton_registrar),
            onConfirmar = { c, p ->
                if (c.isBlank() || p.isBlank()) {
                    aviso(msgVacios)
                } else if (db.registrarUsuario(c, p)) {
                    aviso(msgRegistroOk)
                    dialogo = 0
                } else {
                    aviso(msgRegistroError)
                }
            },
            onCancelar = { dialogo = 0 }
        )
    }

    // DIÁLOGO: CAMBIAR CONTRASEÑA (UPDATE)
    if (dialogo == 2) {
        DialogoCredenciales(
            titulo = stringResource(R.string.dialogo_recuperar_titulo),
            etiquetaContrasena = stringResource(R.string.nueva_contrasena_label),
            textoConfirmar = stringResource(R.string.boton_guardar),
            onConfirmar = { c, p ->
                if (c.isBlank() || p.isBlank()) {
                    aviso(msgVacios)
                } else if (db.cambiarContrasena(c, p)) {
                    aviso(msgContrasenaOk)
                    dialogo = 0
                } else {
                    aviso(msgNoExiste)
                }
            },
            onCancelar = { dialogo = 0 }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colores.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(35.dp))

        // LOGO
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(colores.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(115.dp)
                    .clip(CircleShape)
                    .background(colores.onPrimary),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(65.dp)) {
                    drawCircle(
                        color = colores.surface,
                        radius = size.minDimension / 2
                    )
                    drawCircle(
                        color = colores.onPrimary,
                        radius = size.minDimension / 2.7f,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = colores.onPrimary,
                        radius = size.minDimension / 4,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = colores.onPrimary,
                        radius = size.minDimension / 8
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(65.dp))
                    Text(
                        text = stringResource(R.string.logo_texto),
                        color = colores.surface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = stringResource(R.string.eslogan),
            fontSize = 18.sp,
            color = colores.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CORREO
        Text(
            text = stringResource(R.string.correo_label),
            fontSize = 22.sp,
            color = colores.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = correo,
            onValueChange = { correo = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.desc_usuario),
                    tint = colores.onPrimary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(40.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = coloresCampo
        )

        Spacer(modifier = Modifier.height(15.dp))

        // CONTRASEÑA
        Text(
            text = stringResource(R.string.contrasena_label),
            fontSize = 22.sp,
            color = colores.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(40.dp),
            colors = coloresCampo
        )

        Spacer(modifier = Modifier.height(35.dp))

        // BOTÓN INICIAR SESIÓN (SELECT)
        Button(
            onClick = {
                val c = correo.trim()
                if (c.isBlank() || contrasena.isBlank()) {
                    aviso(msgVacios)
                } else if (db.iniciarSesion(c, contrasena)) {
                    onIniciarSesion(c)
                } else {
                    aviso(msgLoginMal)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colores.primary,
                contentColor = colores.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.desc_iniciar_sesion)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.boton_iniciar_sesion),
                fontSize = 21.sp
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        // OLVIDÉ MI CONTRASEÑA
        TextButton(
            onClick = { dialogo = 2 },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = stringResource(R.string.olvidaste_contrasena),
                fontSize = 17.sp,
                color = colores.secondary
            )
        }

        Spacer(modifier = Modifier.height(65.dp))

        // REGISTRO
        Text(
            text = stringResource(R.string.sin_cuenta),
            fontSize = 18.sp,
            color = colores.onBackground
        )

        TextButton(onClick = { dialogo = 1 }) {
            Text(
                text = stringResource(R.string.registrate),
                fontSize = 18.sp,
                color = colores.secondary
            )
        }

        Spacer(modifier = Modifier.height(25.dp))
    }
}

@Composable
private fun DialogoCredenciales(
    titulo: String,
    etiquetaContrasena: String,
    textoConfirmar: String,
    onConfirmar: (String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text(stringResource(R.string.correo_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text(etiquetaContrasena) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(correo.trim(), contrasena) }) {
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(stringResource(R.string.boton_cancelar))
            }
        }
    )
}