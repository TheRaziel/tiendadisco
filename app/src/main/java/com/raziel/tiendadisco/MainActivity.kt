package com.raziel.tiendadisco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.raziel.tiendadisco.ui.theme.TiendaDiscoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TiendaDiscoTheme {

                var pantallaActual by rememberSaveable { mutableStateOf("inicio") }
                var correoActual by rememberSaveable { mutableStateOf("") }
                var discoActualId by rememberSaveable { mutableIntStateOf(0) }

                // Barra inferior: 0 inicio, 1 catálogo, 2 carrito, 3 favoritos, 4 perfil
                val navegar: (Int) -> Unit = { indice ->
                    when (indice) {
                        0 -> pantallaActual = "home"
                        1 -> pantallaActual = "catalogo"
                        2 -> pantallaActual = "carrito"
                        3 -> pantallaActual = "favoritos"
                        4 -> pantallaActual = "perfil"
                    }
                }

                val abrirDetalle: (Disco) -> Unit = { disco ->
                    discoActualId = disco.id
                    pantallaActual = "detalle"
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (pantallaActual) {
                        // Login
                        "inicio" -> PrimeraPantalla(
                            onIniciarSesion = { correo ->
                                correoActual = correo
                                pantallaActual = "home"
                            }
                        )

                        "home" -> HomePantalla(
                            correoActual = correoActual,
                            onAlbumClick = abrirDetalle,
                            onNavegar = navegar
                        )

                        "catalogo" -> SegundaPantalla(
                            correoActual = correoActual,
                            onRegresar = { pantallaActual = "home" },
                            onCuentaEliminada = { pantallaActual = "inicio" },
                            onAlbumClick = abrirDetalle,
                            onNavegar = navegar
                        )

                        "detalle" -> DetallePantalla(
                            discoId = discoActualId,
                            correoActual = correoActual,
                            onRegresar = { pantallaActual = "home" },
                            onNavegar = navegar
                        )

                        "carrito" -> CarritoPantalla(
                            correoActual = correoActual,
                            onRegresar = { pantallaActual = "home" },
                            onNavegar = navegar
                        )

                        "favoritos" -> FavoritosPantalla(
                            correoActual = correoActual,
                            onRegresar = { pantallaActual = "home" },
                            onNavegar = navegar
                        )

                        "perfil" -> PerfilPantalla(
                            correoActual = correoActual,
                            onRegresar = { pantallaActual = "home" },
                            onCerrarSesion = {
                                correoActual = ""
                                pantallaActual = "inicio"
                            },
                            onNavegar = navegar
                        )
                    }
                }
            }
        }
    }
}