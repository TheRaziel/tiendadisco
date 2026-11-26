package com.raziel.tiendadisco

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Disco(
    val id: Int,
    val nombre: String,
    val artista: String,
    val precio: Int,
    val imagen: String   // nombre de un archivo de res/drawable, o ruta de una foto elegida de la galería
)

data class DetalleDisco(
    val descripcion: String = "",
    val produccion: String = "",
    val genero: String = "",
    val duracion: String = "",
    val canciones: String = "",
    val imagenes: String = "",
    val premios: String = "",
    val artistas: String = ""
)

data class Usuario(
    val nombre: String = "",
    val foto: String = ""   // ruta del archivo de la foto de perfil
)

class BaseDatos(private val context: Context) :
    SQLiteOpenHelper(context, "tiendadisco.db", null, 5) {

    override fun onCreate(db: SQLiteDatabase) {
        // TABLA 1: usuarios
        db.execSQL(
            """CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                correo TEXT NOT NULL UNIQUE,
                contrasena TEXT NOT NULL,
                nombre TEXT NOT NULL DEFAULT '',
                foto TEXT NOT NULL DEFAULT ''
            )"""
        )
        // TABLA 2: discos
        db.execSQL(
            """CREATE TABLE discos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                artista TEXT NOT NULL,
                precio INTEGER NOT NULL,
                imagen TEXT NOT NULL DEFAULT ''
            )"""
        )

        // Datos iniciales del catálogo
        val iniciales = listOf(
            Disco(0, "Abbey Road", "The Beatles", 1099, "abbey_road"),
            Disco(0, "Starboy", "The Weeknd", 1075, "starboy"),
            Disco(0, "Random Access Memories", "Daft Punk", 1149, "random_access_memories"),
            Disco(0, "K-12", "Melanie Martinez", 999, "k12")
        )
        iniciales.forEach { d ->
            val valores = ContentValues().apply {
                put("nombre", d.nombre)
                put("artista", d.artista)
                put("precio", d.precio)
                put("imagen", d.imagen)
            }
            db.insert("discos", null, valores)
        }

        // TABLA 3: detalles
        crearTablaDetalles(db)
        sembrarDetalles(db)

        // TABLA 4: carrito
        crearTablaCarrito(db)

        // TABLA 5: favoritos
        crearTablaFavoritos(db)

        // TABLA 6: compras
        crearTablaCompras(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, viejaVersion: Int, nuevaVersion: Int) {
        if (viejaVersion < 2) {
            crearTablaDetalles(db)
            sembrarDetalles(db)
        }
        if (viejaVersion < 3) {
            crearTablaCarrito(db)
        }
        if (viejaVersion < 4) {
            crearTablaFavoritos(db)
        }
        if (viejaVersion < 5) {
            db.execSQL("ALTER TABLE usuarios ADD COLUMN nombre TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN foto TEXT NOT NULL DEFAULT ''")
            crearTablaCompras(db)
        }
    }

    // ---------------- DETALLES ----------------

    private fun crearTablaDetalles(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS detalles (
                disco_id INTEGER PRIMARY KEY,
                descripcion TEXT NOT NULL DEFAULT '',
                produccion TEXT NOT NULL DEFAULT '',
                genero TEXT NOT NULL DEFAULT '',
                duracion TEXT NOT NULL DEFAULT '',
                canciones TEXT NOT NULL DEFAULT '',
                imagenes TEXT NOT NULL DEFAULT '',
                premios TEXT NOT NULL DEFAULT '',
                artistas TEXT NOT NULL DEFAULT ''
            )"""
        )
    }

    // Llena los detalles de los 4 discos iniciales (si existen en la tabla discos)
    private fun sembrarDetalles(db: SQLiteDatabase) {
        val datos = listOf(
            "Abbey Road" to listOf(
                R.string.ab_desc, R.string.ab_prod, R.string.ab_gen, R.string.ab_dur,
                R.string.ab_can, R.string.ab_img, R.string.ab_pre, R.string.ab_art
            ),
            "Starboy" to listOf(
                R.string.sb_desc, R.string.sb_prod, R.string.sb_gen, R.string.sb_dur,
                R.string.sb_can, R.string.sb_img, R.string.sb_pre, R.string.sb_art
            ),
            "Random Access Memories" to listOf(
                R.string.ra_desc, R.string.ra_prod, R.string.ra_gen, R.string.ra_dur,
                R.string.ra_can, R.string.ra_img, R.string.ra_pre, R.string.ra_art
            ),
            "K-12" to listOf(
                R.string.k1_desc, R.string.k1_prod, R.string.k1_gen, R.string.k1_dur,
                R.string.k1_can, R.string.k1_img, R.string.k1_pre, R.string.k1_art
            )
        )

        datos.forEach { (nombre, ids) ->
            val discoId = db.query(
                "discos", arrayOf("id"), "nombre = ?", arrayOf(nombre),
                null, null, null
            ).use { c -> if (c.moveToFirst()) c.getInt(0) else null } ?: return@forEach

            val t = ids.map { context.getString(it) }
            val valores = ContentValues().apply {
                put("disco_id", discoId)
                put("descripcion", t[0])
                put("produccion", t[1])
                put("genero", t[2])
                put("duracion", t[3])
                put("canciones", t[4])
                put("imagenes", t[5])
                put("premios", t[6])
                put("artistas", t[7])
            }
            db.insertWithOnConflict("detalles", null, valores, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    // SELECT: si el disco no tiene detalle, regresa uno vacío
    fun obtenerDetalle(discoId: Int): DetalleDisco {
        return readableDatabase.query(
            "detalles", null, "disco_id = ?", arrayOf(discoId.toString()),
            null, null, null
        ).use { c ->
            if (c.moveToFirst()) {
                DetalleDisco(
                    descripcion = c.getString(c.getColumnIndexOrThrow("descripcion")),
                    produccion = c.getString(c.getColumnIndexOrThrow("produccion")),
                    genero = c.getString(c.getColumnIndexOrThrow("genero")),
                    duracion = c.getString(c.getColumnIndexOrThrow("duracion")),
                    canciones = c.getString(c.getColumnIndexOrThrow("canciones")),
                    imagenes = c.getString(c.getColumnIndexOrThrow("imagenes")),
                    premios = c.getString(c.getColumnIndexOrThrow("premios")),
                    artistas = c.getString(c.getColumnIndexOrThrow("artistas"))
                )
            } else {
                DetalleDisco()
            }
        }
    }

    // INSERT o UPDATE del detalle (descripción y elementos) de un disco
    fun guardarDetalle(discoId: Int, d: DetalleDisco) {
        val valores = ContentValues().apply {
            put("disco_id", discoId)
            put("descripcion", d.descripcion)
            put("produccion", d.produccion)
            put("genero", d.genero)
            put("duracion", d.duracion)
            put("canciones", d.canciones)
            put("imagenes", d.imagenes)
            put("premios", d.premios)
            put("artistas", d.artistas)
        }
        writableDatabase.insertWithOnConflict(
            "detalles", null, valores, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // ---------------- CARRITO ----------------

    private fun crearTablaCarrito(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS carrito (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                correo TEXT NOT NULL,
                disco_id INTEGER NOT NULL,
                UNIQUE(correo, disco_id)
            )"""
        )
    }

    // INSERT (regresa false si el disco ya estaba en el carrito)
    fun agregarAlCarrito(correo: String, discoId: Int): Boolean {
        val valores = ContentValues().apply {
            put("correo", correo)
            put("disco_id", discoId)
        }
        return writableDatabase.insertWithOnConflict(
            "carrito", null, valores, SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    // SELECT con JOIN: los discos que hay en el carrito de ese usuario
    fun obtenerCarrito(correo: String): List<Disco> {
        val lista = mutableListOf<Disco>()
        readableDatabase.rawQuery(
            """SELECT d.id, d.nombre, d.artista, d.precio, d.imagen
               FROM carrito c INNER JOIN discos d ON d.id = c.disco_id
               WHERE c.correo = ?
               ORDER BY c.id ASC""",
            arrayOf(correo)
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(Disco(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3), c.getString(4)))
            }
        }
        return lista
    }

    // SELECT con SUM: total de la compra
    fun totalCarrito(correo: String): Int {
        return readableDatabase.rawQuery(
            """SELECT COALESCE(SUM(d.precio), 0)
               FROM carrito c INNER JOIN discos d ON d.id = c.disco_id
               WHERE c.correo = ?""",
            arrayOf(correo)
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    // DELETE de un disco del carrito
    fun quitarDelCarrito(correo: String, discoId: Int): Boolean {
        return writableDatabase.delete(
            "carrito", "correo = ? AND disco_id = ?", arrayOf(correo, discoId.toString())
        ) > 0
    }

    // DELETE de todo el carrito
    fun vaciarCarrito(correo: String): Boolean {
        return writableDatabase.delete("carrito", "correo = ?", arrayOf(correo)) > 0
    }

    // ---------------- COMPRAS (HISTORIAL) ----------------

    private fun crearTablaCompras(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS compras (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                correo TEXT NOT NULL,
                disco_id INTEGER NOT NULL,
                fecha INTEGER NOT NULL
            )"""
        )
    }

    // INSERT + DELETE en una transacción: pasa el carrito al historial y lo vacía
    fun finalizarCompra(correo: String): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            db.execSQL(
                "INSERT INTO compras (correo, disco_id, fecha) SELECT correo, disco_id, ? FROM carrito WHERE correo = ?",
                arrayOf<Any>(System.currentTimeMillis(), correo)
            )
            db.delete("carrito", "correo = ?", arrayOf(correo))
            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    // SELECT con JOIN: discos comprados (sin repetir), el más reciente primero
    fun obtenerCompras(correo: String): List<Disco> {
        val lista = mutableListOf<Disco>()
        readableDatabase.rawQuery(
            """SELECT d.id, d.nombre, d.artista, d.precio, d.imagen
               FROM compras c INNER JOIN discos d ON d.id = c.disco_id
               WHERE c.correo = ?
               GROUP BY d.id
               ORDER BY MAX(c.id) DESC""",
            arrayOf(correo)
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(Disco(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3), c.getString(4)))
            }
        }
        return lista
    }

    // ---------------- FAVORITOS ----------------

    private fun crearTablaFavoritos(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS favoritos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                correo TEXT NOT NULL,
                disco_id INTEGER NOT NULL,
                UNIQUE(correo, disco_id)
            )"""
        )
    }

    // INSERT (regresa false si ya era favorito)
    fun agregarFavorito(correo: String, discoId: Int): Boolean {
        val valores = ContentValues().apply {
            put("correo", correo)
            put("disco_id", discoId)
        }
        return writableDatabase.insertWithOnConflict(
            "favoritos", null, valores, SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    // SELECT con JOIN: los favoritos de ese usuario
    fun obtenerFavoritos(correo: String): List<Disco> {
        val lista = mutableListOf<Disco>()
        readableDatabase.rawQuery(
            """SELECT d.id, d.nombre, d.artista, d.precio, d.imagen
               FROM favoritos f INNER JOIN discos d ON d.id = f.disco_id
               WHERE f.correo = ?
               ORDER BY f.id ASC""",
            arrayOf(correo)
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(Disco(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3), c.getString(4)))
            }
        }
        return lista
    }

    // SELECT: ¿este disco es favorito de este usuario?
    fun esFavorito(correo: String, discoId: Int): Boolean {
        return readableDatabase.query(
            "favoritos", arrayOf("id"),
            "correo = ? AND disco_id = ?", arrayOf(correo, discoId.toString()),
            null, null, null
        ).use { it.count > 0 }
    }

    // DELETE
    fun quitarFavorito(correo: String, discoId: Int): Boolean {
        return writableDatabase.delete(
            "favoritos", "correo = ? AND disco_id = ?", arrayOf(correo, discoId.toString())
        ) > 0
    }

    // ---------------- USUARIOS ----------------

    // INSERT
    fun registrarUsuario(correo: String, contrasena: String): Boolean {
        val valores = ContentValues().apply {
            put("correo", correo)
            put("contrasena", contrasena)
        }
        return writableDatabase.insert("usuarios", null, valores) != -1L
    }

    // SELECT
    fun iniciarSesion(correo: String, contrasena: String): Boolean {
        return readableDatabase.query(
            "usuarios", arrayOf("id"),
            "correo = ? AND contrasena = ?", arrayOf(correo, contrasena),
            null, null, null
        ).use { it.count > 0 }
    }

    // SELECT: nombre y foto del perfil
    fun obtenerUsuario(correo: String): Usuario {
        return readableDatabase.query(
            "usuarios", arrayOf("nombre", "foto"), "correo = ?", arrayOf(correo),
            null, null, null
        ).use { c ->
            if (c.moveToFirst()) Usuario(c.getString(0), c.getString(1)) else Usuario()
        }
    }

    // UPDATE
    fun cambiarContrasena(correo: String, nueva: String): Boolean {
        val valores = ContentValues().apply { put("contrasena", nueva) }
        return writableDatabase.update("usuarios", valores, "correo = ?", arrayOf(correo)) > 0
    }

    // UPDATE
    fun actualizarNombre(correo: String, nombre: String): Boolean {
        val valores = ContentValues().apply { put("nombre", nombre) }
        return writableDatabase.update("usuarios", valores, "correo = ?", arrayOf(correo)) > 0
    }

    // UPDATE
    fun actualizarFoto(correo: String, ruta: String): Boolean {
        val valores = ContentValues().apply { put("foto", ruta) }
        return writableDatabase.update("usuarios", valores, "correo = ?", arrayOf(correo)) > 0
    }

    // DELETE (también borra su carrito, favoritos y compras)
    fun eliminarUsuario(correo: String): Boolean {
        writableDatabase.delete("carrito", "correo = ?", arrayOf(correo))
        writableDatabase.delete("favoritos", "correo = ?", arrayOf(correo))
        writableDatabase.delete("compras", "correo = ?", arrayOf(correo))
        return writableDatabase.delete("usuarios", "correo = ?", arrayOf(correo)) > 0
    }

    // ---------------- DISCOS ----------------

    // INSERT (regresa el id del disco nuevo, o -1 si falló)
    fun insertarDisco(nombre: String, artista: String, precio: Int, imagen: String): Long {
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("artista", artista)
            put("precio", precio)
            put("imagen", imagen)
        }
        return writableDatabase.insert("discos", null, valores)
    }

    // SELECT (orden: 0 = mejores vendidos, 1 = mayor precio, 2 = menor precio)
    fun obtenerDiscos(orden: Int = 0): List<Disco> {
        val ordenSql = when (orden) {
            1 -> "precio DESC"
            2 -> "precio ASC"
            else -> "id ASC"
        }
        val lista = mutableListOf<Disco>()
        readableDatabase.query("discos", null, null, null, null, null, ordenSql).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Disco(
                        id = c.getInt(c.getColumnIndexOrThrow("id")),
                        nombre = c.getString(c.getColumnIndexOrThrow("nombre")),
                        artista = c.getString(c.getColumnIndexOrThrow("artista")),
                        precio = c.getInt(c.getColumnIndexOrThrow("precio")),
                        imagen = c.getString(c.getColumnIndexOrThrow("imagen"))
                    )
                )
            }
        }
        return lista
    }

    // SELECT de un solo disco
    fun obtenerDisco(id: Int): Disco? {
        return readableDatabase.query(
            "discos", null, "id = ?", arrayOf(id.toString()), null, null, null
        ).use { c ->
            if (c.moveToFirst()) {
                Disco(
                    id = c.getInt(c.getColumnIndexOrThrow("id")),
                    nombre = c.getString(c.getColumnIndexOrThrow("nombre")),
                    artista = c.getString(c.getColumnIndexOrThrow("artista")),
                    precio = c.getInt(c.getColumnIndexOrThrow("precio")),
                    imagen = c.getString(c.getColumnIndexOrThrow("imagen"))
                )
            } else null
        }
    }

    // UPDATE
    fun actualizarDisco(disco: Disco): Boolean {
        val valores = ContentValues().apply {
            put("nombre", disco.nombre)
            put("artista", disco.artista)
            put("precio", disco.precio)
            put("imagen", disco.imagen)
        }
        return writableDatabase.update("discos", valores, "id = ?", arrayOf(disco.id.toString())) > 0
    }

    // DELETE (también borra su detalle, su portada y lo quita de carritos, favoritos y compras)
    fun eliminarDisco(id: Int): Boolean {
        val disco = obtenerDisco(id)
        if (disco != null && disco.imagen.startsWith("/")) {
            java.io.File(disco.imagen).delete()
        }
        writableDatabase.delete("detalles", "disco_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("carrito", "disco_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("favoritos", "disco_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("compras", "disco_id = ?", arrayOf(id.toString()))
        return writableDatabase.delete("discos", "id = ?", arrayOf(id.toString())) > 0
    }
}