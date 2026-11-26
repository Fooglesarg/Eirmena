package com.byfooglesframes.eirmena

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byfooglesframes.eirmena.avisos.Planificador
import com.byfooglesframes.eirmena.datos.Ajustes
import com.byfooglesframes.eirmena.datos.Almacen
import com.byfooglesframes.eirmena.datos.Bloqueo
import com.byfooglesframes.eirmena.datos.ContrasenaIncorrecta
import com.byfooglesframes.eirmena.datos.Curiosidades
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Embarazo
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.datos.FinEmbarazo
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.FormatoInvalido
import com.byfooglesframes.eirmena.datos.Json
import com.byfooglesframes.eirmena.datos.RegistroDia
import com.byfooglesframes.eirmena.datos.Respaldo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Un respaldo ya leído que espera que la persona elija reemplazar o combinar. */
data class ImportacionPendiente(val datos: Datos, val dias: Int)

class EirmenaViewModel(app: Application) : AndroidViewModel(app) {
    private val almacen = Almacen(app)
    val bloqueo = Bloqueo(app)

    private val _datos = MutableStateFlow(Datos())
    val datos: StateFlow<Datos> = _datos.asStateFlow()

    private val _cargado = MutableStateFlow(false)
    val cargado: StateFlow<Boolean> = _cargado.asStateFlow()

    private val _bloqueada = MutableStateFlow(bloqueo.tienePin)
    val bloqueada: StateFlow<Boolean> = _bloqueada.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _trabajando = MutableStateFlow(false)
    val trabajando: StateFlow<Boolean> = _trabajando.asStateFlow()

    private val _importacion = MutableStateFlow<ImportacionPendiente?>(null)
    val importacion: StateFlow<ImportacionPendiente?> = _importacion.asStateFlow()

    /** Archivo cifrado que espera la contraseña para importarse. */
    private val _pideContrasena = MutableStateFlow<String?>(null)
    val pideContrasena: StateFlow<String?> = _pideContrasena.asStateFlow()

    init {
        viewModelScope.launch {
            _datos.value = withContext(Dispatchers.IO) {
                try { almacen.cargar() } catch (e: Exception) {
                    _mensaje.value = "No se pudieron leer los datos guardados."
                    Datos()
                }
            }
            _cargado.value = true
            if (Curiosidades.porId(_datos.value.ajustes.proximaCarta) == null) {
                cambiarAjustes { it.copy(proximaCarta = Curiosidades.sortear(null).id) }
            }
            reprogramar(_datos.value)
        }
        // Si una notificación cambió los datos (p. ej. "Ya la tomé"), se recargan.
        viewModelScope.launch { Almacen.cambios.drop(1).collect { recargar() } }
    }

    /** Vuelve a leer el archivo: lo usa la app al volver a primer plano. */
    fun recargar() {
        viewModelScope.launch {
            val leidos = withContext(Dispatchers.IO) { try { almacen.cargar() } catch (e: Exception) { null } }
            if (leidos != null) _datos.value = leidos
        }
    }

    private fun reprogramar(datos: Datos) {
        val app = getApplication<Application>()
        viewModelScope.launch(Dispatchers.Default) { try { Planificador.programar(app, datos) } catch (_: Exception) { } }
    }

    private fun cambiar(transformar: (Datos) -> Datos) {
        val nuevos = transformar(_datos.value)
        _datos.value = nuevos
        viewModelScope.launch(Dispatchers.IO) {
            try { almacen.guardar(nuevos) } catch (e: Exception) { _mensaje.value = "No se pudo guardar el cambio." }
        }
        reprogramar(nuevos)
    }

    fun mensajeVisto() { _mensaje.value = null }
    fun avisar(texto: String) { _mensaje.value = texto }

    // ---- Bienvenida y ajustes ----

    fun terminarBienvenida(ciclo: Int, periodo: Int, ultimoInicio: LocalDate?, etapa: EtapaVida = EtapaVida.CICLO) = cambiar { d ->
        val embarazo = if (etapa == EtapaVida.EMBARAZO && ultimoInicio != null) Embarazo(ultimoInicio) else null
        var nuevos = d.copy(ajustes = d.ajustes.copy(
            cicloHabitual = ciclo, periodoHabitual = periodo, bienvenidaHecha = true,
            etapa = if (etapa == EtapaVida.EMBARAZO && embarazo == null) EtapaVida.CICLO else etapa, embarazo = embarazo,
        ))
        if (ultimoInicio != null) nuevos = marcarPeriodo(nuevos, ultimoInicio, periodo, LocalDate.now())
        nuevos
    }

    fun cambiarAjustes(transformar: (Ajustes) -> Ajustes) = cambiar { it.copy(ajustes = transformar(it.ajustes)) }

    // ---- Etapas ----

    fun cambiarEtapa(etapa: EtapaVida) = cambiarAjustes { it.copy(etapa = etapa) }

    /** [fum]: primer día de la última menstruación. */
    fun empezarEmbarazo(fum: LocalDate) = cambiarAjustes { it.copy(etapa = EtapaVida.EMBARAZO, embarazo = Embarazo(fum)) }

    fun terminarEmbarazo(fin: LocalDate, resultado: FinEmbarazo) = cambiarAjustes {
        it.copy(etapa = EtapaVida.CICLO, embarazo = it.embarazo?.copy(fin = fin, resultado = resultado))
    }

    /** Marca una carta de "¿Sabías que…?" como descubierta. */
    fun descubrir(id: String) {
        cambiarAjustes {
            val vistas = if (id in it.curiosidades) it.curiosidades else it.curiosidades + id
            // Con la última carta se gana el trofeo (una sola vez).
            val trofeo = it.trofeo ?: if (com.byfooglesframes.eirmena.datos.Curiosidades.completa(vistas)) LocalDate.now() else null
            // Se sortea ya la carta de la próxima visita (puede salir una repetida).
            it.copy(curiosidades = vistas, trofeo = trofeo, proximaCarta = Curiosidades.sortear(id).id)
        }
    }

    fun alertaVista(id: String) = cambiarAjustes { it.copy(alertasVistas = it.alertasVistas + (id to LocalDate.now())) }

    // ---- Registro ----

    fun pastillaTomada(fecha: LocalDate, tomada: Boolean) {
        cambiar { it.conDia(it.dia(fecha).copy(pastillaTomada = tomada)) }
        if (tomada) Planificador.quitarNotificacion(getApplication(), com.byfooglesframes.eirmena.avisos.TipoAviso.PASTILLA)
    }

    fun guardarDia(registro: RegistroDia) = cambiar { it.conDia(registro) }

    /** Marca el inicio de un período y completa los días siguientes según la duración habitual. */
    fun empezoPeriodo(fecha: LocalDate) = cambiar { d ->
        marcarPeriodo(d, fecha, d.ajustes.periodoHabitual, LocalDate.now())
    }

    private fun marcarPeriodo(d: Datos, desde: LocalDate, dias: Int, hoy: LocalDate): Datos {
        var r = d
        for (i in 0 until dias) {
            val f = desde.plusDays(i.toLong())
            if (f > hoy) break
            val actual = r.dias[f] ?: RegistroDia(f)
            if (actual.flujo == null || actual.flujo == Flujo.MANCHADO) {
                r = r.conDia(actual.copy(flujo = if (i == 0 || i == 1) Flujo.MEDIO else Flujo.LEVE))
            }
        }
        return r
    }

    // ---- Bloqueo ----

    fun bloquear() { if (bloqueo.tienePin) _bloqueada.value = true }

    fun desbloquearConHuella() { _bloqueada.value = false }

    suspend fun probarPin(pin: String): Boolean {
        val ok = withContext(Dispatchers.Default) { bloqueo.comprobar(pin) }
        if (ok) _bloqueada.value = false
        return ok
    }

    suspend fun ponerPin(pin: String) = withContext(Dispatchers.Default) { bloqueo.ponerPin(pin) }

    fun quitarPin() = bloqueo.quitarPin()

    /** Olvidé el PIN: no hay recuperación posible, se borra todo para poder importar un respaldo. */
    fun borrarTodo() {
        almacen.borrarTodo()
        bloqueo.quitarPin()
        _datos.value = Datos()
        _bloqueada.value = false
        reprogramar(Datos())
    }

    // ---- Respaldo ----

    fun exportar(uri: Uri, contrasena: CharArray) {
        val datos = _datos.value
        viewModelScope.launch {
            _trabajando.value = true
            try {
                withContext(Dispatchers.Default) {
                    val archivo = Respaldo.cifrar(Json.aJson(datos, BuildConfig.VERSION_NAME), contrasena)
                    getApplication<Application>().contentResolver.openOutputStream(uri, "wt")!!.use {
                        it.write(archivo.toByteArray(Charsets.UTF_8))
                    }
                }
                _mensaje.value = "Respaldo guardado. Guardalo en un lugar seguro junto con la contraseña."
            } catch (e: Exception) {
                _mensaje.value = "No se pudo guardar el respaldo."
            } finally {
                contrasena.fill(' ')
                _trabajando.value = false
            }
        }
    }

    fun leerRespaldo(uri: Uri) {
        viewModelScope.launch {
            try {
                val texto = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)!!.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    }
                }
                if (Respaldo.estaCifrado(texto)) _pideContrasena.value = texto
                else prepararImportacion(Json.desdeJson(texto))
            } catch (e: FormatoInvalido) {
                _mensaje.value = e.message
            } catch (e: Exception) {
                _mensaje.value = "No se pudo abrir el archivo."
            }
        }
    }

    suspend fun descifrarRespaldo(contrasena: CharArray): String? {
        val archivo = _pideContrasena.value ?: return null
        _trabajando.value = true
        return try {
            val datos = withContext(Dispatchers.Default) { Json.desdeJson(Respaldo.descifrar(archivo, contrasena)) }
            _pideContrasena.value = null
            prepararImportacion(datos)
            null
        } catch (e: ContrasenaIncorrecta) {
            "La contraseña no es correcta."
        } catch (e: FormatoInvalido) {
            _pideContrasena.value = null
            e.message
        } finally {
            contrasena.fill(' ')
            _trabajando.value = false
        }
    }

    fun cancelarContrasena() { _pideContrasena.value = null }

    private fun prepararImportacion(datos: Datos) {
        if (_datos.value.dias.isEmpty()) aplicarImportacion(datos, combinar = false)
        else _importacion.value = ImportacionPendiente(datos, datos.dias.size)
    }

    fun confirmarImportacion(combinar: Boolean) {
        _importacion.value?.let { aplicarImportacion(it.datos, combinar) }
        _importacion.value = null
    }

    fun cancelarImportacion() { _importacion.value = null }

    private fun aplicarImportacion(importados: Datos, combinar: Boolean) {
        cambiar { actual ->
            val base = if (combinar) actual.combinarCon(importados) else importados
            base.copy(ajustes = base.ajustes.copy(bienvenidaHecha = true))
        }
        _mensaje.value = "Historial importado: ${importados.dias.size} días."
    }
}
