package com.alanep.ejercicio1

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.alanep.ejercicio1.ui.theme.Ejercicio1Theme
import java.util.Locale
import kotlin.math.sqrt

enum class TipoCalculo {
    CUADRATICA,
    IMC,
    TRAPECIO,
    VELOCIDAD
}

/*
 * Idiomas disponibles en el menú.
 *
 * Para agregar uno nuevo:
 *  1) crea res/values-XX/strings.xml con las traducciones
 *  2) agrega aquí una entrada (código del idioma + nombre)
 *  3) agrega su nombre en res/values/languages.xml
 *
 * El nombre se escribe en su propio idioma (Español, English, Русский)
 * y no se traduce, por eso está marcado translatable="false".
 */
enum class Idioma(
    val codigo: String,
    @StringRes val nombre: Int
) {
    ESPANOL(codigo= "es", nombre= R.string.language_es),
    INGLES(codigo= "en", nombre= R.string.language_en),
    RUSO(codigo= "ru", nombre= R.string.language_ru)
}

/*
 * Dónde se guarda el idioma elegido para recordarlo
 * la próxima vez que se abra la app.
 */
private const val PREFERENCIAS = "ajustes"
private const val CLAVE_IDIOMA = "idioma"

/*
 * Medidas reutilizables. Se declaran una sola vez para no repetir
 * números sueltos (16.dp, 8.dp...) por todo el código.
 */
private object Dimens {
    val Chico = 8.dp
    val Medio = 16.dp
    val AltoFormula = 100.dp
}

/*
 * Valida la estructura del texto que se está escribiendo.
 *
 * allowSign = true:
 * permite + y - únicamente al principio.
 *
 * allowSign = false:
 * solamente permite números y punto decimal.
 */
private fun isValidNumericInput(
    value: String,
    allowSign: Boolean
): Boolean {

    if (value.isEmpty()) {
        return true
    }

    val pattern = if (allowSign) {
        Regex(pattern= """[+-]?\d*\.?\d*""")
    } else {
        Regex(pattern= """\d*\.?\d*""")
    }

    return pattern.matches(input= value)
}

@Composable
private fun NumericInputField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    allowSign: Boolean,
    @StringRes suffixRes: Int? = null
) {
    OutlinedTextField(
        value = value,

        onValueChange = { newValue ->
            if (isValidNumericInput(newValue, allowSign)) {
                onValueChange(newValue)
            }
        },

        label = {
            Text(stringResource(id= labelRes))
        },

        suffix = if (suffixRes != null) {
            {
                Text(stringResource(id= suffixRes))
            }
        } else {
            null
        },

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),

        singleLine = true,

        modifier = Modifier
            .fillMaxWidth()
    )
}


/*
 * LÓGICA DE CÁLCULO
 *
 * Estas funciones no dibujan nada: reciben los textos de los campos y
 * devuelven el resultado ya traducido, o lanzan una IllegalArgumentException
 * cuyo mensaje también viene de strings.xml.
 */

/*
 * Convierte el texto de un campo a Double.
 * Si está vacío o no es un número, lanza el error correspondiente.
 */
private fun leerNumero(
    context: Context,
    texto: String,
    @StringRes labelRes: Int
): Double {

    val numero = texto.toDoubleOrNull()?.takeIf { it.isFinite() }

    if (numero == null) {
        val mensajeRes = if (texto.isBlank()) {
            R.string.error_required
        } else {
            R.string.error_invalid_number
        }

        throw IllegalArgumentException(
            context.getString(mensajeRes, context.getString(labelRes))
        )
    }

    return numero
}

/*
 * Lanza un error si el valor no es mayor que 0.
 */
private fun exigirPositivo(
    context: Context,
    valor: Double,
    @StringRes labelRes: Int
) {
    if (valor <= 0) {
        throw IllegalArgumentException(
            context.getString(
                R.string.error_positive,
                context.getString(labelRes)
            )
        )
    }
}

private fun calcular(
    context: Context,
    tipo: TipoCalculo,
    campo1: String,
    campo2: String,
    campo3: String
): String = when (tipo) {

    /*
     * ECUACIÓN CUADRÁTICA
     */
    TipoCalculo.CUADRATICA -> {

        val a = leerNumero(context, campo1, R.string.label_a)
        val b = leerNumero(context, campo2, R.string.label_b)
        val c = leerNumero(context, campo3, R.string.label_c)

        if (a == 0.0) {
            throw IllegalArgumentException(
                context.getString(R.string.error_zero_a)
            )
        }

        val discriminante = (b * b) - (4 * a * c)

        if (discriminante < 0) {
            throw IllegalArgumentException(
                context.getString(R.string.error_discriminant)
            )
        }

        val x1 = (-b + sqrt(discriminante)) / (2 * a)
        val x2 = (-b - sqrt(discriminante)) / (2 * a)

        context.getString(R.string.result_quadratic, x1, x2)
    }

    /*
     * IMC
     */
    TipoCalculo.IMC -> {

        val peso = leerNumero(context, campo1, R.string.label_weight)
        val altura = leerNumero(context, campo2, R.string.label_height)

        exigirPositivo(context, peso, R.string.label_weight)
        exigirPositivo(context, altura, R.string.label_height)

        val imc = peso / (altura * altura)

        context.getString(R.string.result_bmi, imc)
    }

    /*
     * TRAPECIO
     */
    TipoCalculo.TRAPECIO -> {

        val baseMayor = leerNumero(context, texto= campo1, labelRes= R.string.label_base_major)
        val baseMenor = leerNumero(context, texto= campo2, labelRes= R.string.label_base_minor)
        val altura = leerNumero(context, texto= campo3, labelRes= R.string.label_trapezoid_height)

        exigirPositivo(context, valor= baseMayor, labelRes= R.string.label_base_major)
        exigirPositivo(context, valor= baseMenor, labelRes= R.string.label_base_minor)
        exigirPositivo(context, valor= altura, labelRes= R.string.label_trapezoid_height)

        val area = ((baseMayor + baseMenor) * altura) / 2

        context.getString(R.string.result_trapezoid, area)
    }

    /*
     * VELOCIDAD
     */
    TipoCalculo.VELOCIDAD -> {

        val distancia = leerNumero(context, campo1, R.string.label_distance)
        val tiempo = leerNumero(context, campo2, R.string.label_time)

        if (distancia < 0) {
            throw IllegalArgumentException(
                context.getString(
                    R.string.error_non_negative,
                    context.getString(R.string.label_distance)
                )
            )
        }

        exigirPositivo(context, tiempo, R.string.label_time)

        val velocidad = distancia / tiempo

        context.getString(R.string.result_rapidez, velocidad)
    }
}


/*
 * CAMBIO DE IDIOMA
 *
 * Cambia el idioma de TODA la interfaz sin reiniciar la Activity:
 * crea un Context con otro Locale y se lo entrega a Compose, así
 * stringResource() y context.getString() leen values-en, values-ru, etc.
 *
 * codigo = null significa "usar el idioma del sistema".
 *
 * (AppCompatDelegate.setApplicationLocales() no sirve aquí: exige
 * AppCompatActivity y un tema AppCompat, y MainActivity es ComponentActivity.)
 */
@Composable
private fun ConIdioma(
    codigo: String?,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val (contextoLocal, configuracionLocal) =
        remember(key1= codigo, key2= context, key3= configuration) {

            if (codigo == null) {

                context to configuration

            } else {

                /*
                 * Si el idioma elegido ya es el del sistema se conserva su
                 * Locale completo (por ejemplo es-MX), así los números siguen
                 * mostrándose con punto decimal.
                 */
                val localeSistema = ConfigurationCompat
                    .getLocales(configuration)
                    .get(0)

                val locale = localeSistema
                    ?.takeIf { it.language == codigo }
                    ?: Locale.forLanguageTag(codigo)

                val nueva = Configuration(configuration).apply {
                    setLocale(locale)
                }

                context.createConfigurationContext(nueva) to nueva
            }
        }

    CompositionLocalProvider(
        LocalContext provides contextoLocal,
        LocalConfiguration provides configuracionLocal
    ) {
        content()
    }
}

/*
 * Código del idioma que se está mostrando ahora ("es", "en", "ru"...).
 */
@Composable
private fun codigoIdiomaActual(): String? =
    ConfigurationCompat
        .getLocales(LocalConfiguration.current)
        .get(0)
        ?.language


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val preferencias = getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

        setContent {

            /*
             * Idioma elegido por el usuario (null = el del sistema).
             *
             * rememberSaveable lo conserva al girar el dispositivo y
             * SharedPreferences al cerrar y volver a abrir la app.
             */
            var codigoIdioma by rememberSaveable {
                mutableStateOf(preferencias.getString(CLAVE_IDIOMA, null))
            }

            Ejercicio1Theme {

                ConIdioma(codigo = codigoIdioma) {

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),

                        topBar = {
                            BarraSuperior(
                                onIdiomaSeleccionado = { idioma ->
                                    codigoIdioma = idioma.codigo

                                    preferencias.edit()
                                        .putString(CLAVE_IDIOMA, idioma.codigo)
                                        .apply()
                                }
                            )
                        },

                        /*
                         * safeDrawing = barras del sistema + notch + teclado.
                         * En horizontal evita que el recorte de la pantalla
                         * tape el contenido.
                         */
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->

                        CalculadoraScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}


/*
 * BARRA SUPERIOR: título a la izquierda y botón de idioma a la derecha.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BarraSuperior(
    onIdiomaSeleccionado: (Idioma) -> Unit
) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.screen_title))
        },

        actions = {
            BotonIdioma(onIdiomaSeleccionado = onIdiomaSeleccionado)
        },

        // Respeta el notch y las barras del sistema también en horizontal.
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        )
    )
}

/*
 * Botón con el icono del globo. Al tocarlo abre un menú con los idiomas
 * disponibles; el que se está usando aparece marcado.
 */
@Composable
private fun BotonIdioma(
    onIdiomaSeleccionado: (Idioma) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    // Si el idioma del sistema no está en la lista se usa el de values/ (español).
    val codigoActual = codigoIdiomaActual()
    val idiomaActivo = Idioma.entries.firstOrNull { it.codigo == codigoActual }
        ?: Idioma.ESPANOL

    Box {

        IconButton(onClick = { expandido = true }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_language),
                contentDescription = stringResource(R.string.change_language)
            )
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {

            Idioma.entries.forEach { idioma ->

                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(idioma.nombre))
                    },

                    leadingIcon = {
                        RadioButton(
                            selected = idioma == idiomaActivo,
                            onClick = null
                        )
                    },

                    onClick = {
                        expandido = false
                        onIdiomaSeleccionado(idioma)
                    }
                )
            }
        }
    }
}


@Composable
fun CalculadoraScreen(
    modifier: Modifier = Modifier,

    /*
     * Por defecto se calcula con la orientación real del dispositivo.
     * Se puede forzar (true / false) para probarlo en las vistas previas.
     */
    esHorizontal: Boolean =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
) {

    val context = LocalContext.current
    val foco = LocalFocusManager.current

    /*
     * Idioma que se está mostrando. Se usa como "llave" del resultado y del
     * error: cuando cambia el idioma se limpian, porque ya son texto traducido
     * en el idioma anterior. Al girar el dispositivo el idioma no cambia,
     * así que ahí no se pierden.
     */
    val idioma = codigoIdiomaActual()

    /*
     * rememberSaveable permite conservar estos valores
     * cuando Android recrea la Activity, por ejemplo
     * al girar el dispositivo.
     */
    var seleccion by rememberSaveable {
        mutableStateOf(TipoCalculo.CUADRATICA)
    }

    var campo1 by rememberSaveable {
        mutableStateOf("")
    }

    var campo2 by rememberSaveable {
        mutableStateOf("")
    }

    var campo3 by rememberSaveable {
        mutableStateOf("")
    }

    var resultado by rememberSaveable(idioma) {
        mutableStateOf("")
    }

    var errorMensaje by rememberSaveable(idioma) {
        mutableStateOf<String?>(null)
    }

    /*
     * Cambia de cálculo y deja la pantalla limpia.
     * Este bloque antes estaba repetido en cada botón del menú.
     */
    fun seleccionar(tipo: TipoCalculo) {
        seleccion = tipo
        errorMensaje = null
        resultado = ""
        campo1 = ""
        campo2 = ""
        campo3 = ""
    }

    PanelesAdaptativos(
        esHorizontal = esHorizontal,
        modifier = modifier,

        /*
         * PANEL IZQUIERDO: menú y fórmula
         * (el título ahora está en la barra superior)
         */
        izquierdo = {

            Text(
                text = stringResource(R.string.select_calculation)
            )

            Spacer(
                modifier = Modifier.height(Dimens.Medio)
            )

            /*
             * PRIMERA FILA DEL MENÚ
             */
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {

                BotonMenu(
                    textoRes = R.string.calc_cuadratica,
                    seleccionado = seleccion == TipoCalculo.CUADRATICA,
                    onClick = { seleccionar(TipoCalculo.CUADRATICA) }
                )

                BotonMenu(
                    textoRes = R.string.calc_imc,
                    seleccionado = seleccion == TipoCalculo.IMC,
                    onClick = { seleccionar(TipoCalculo.IMC) }
                )
            }

            Spacer(
                modifier = Modifier.height(Dimens.Chico)
            )

            /*
             * SEGUNDA FILA DEL MENÚ
             */
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {

                BotonMenu(
                    textoRes = R.string.calc_trapecio,
                    seleccionado = seleccion == TipoCalculo.TRAPECIO,
                    onClick = { seleccionar(TipoCalculo.TRAPECIO) }
                )

                BotonMenu(
                    textoRes = R.string.calc_rapidez,
                    seleccionado = seleccion == TipoCalculo.VELOCIDAD,
                    onClick = { seleccionar(TipoCalculo.VELOCIDAD) }
                )
            }

            Spacer(
                modifier = Modifier.height(Dimens.Medio)
            )

            /*
             * FÓRMULA DINÁMICA
             */
            val imageRes = when (seleccion) {

                TipoCalculo.CUADRATICA ->
                    R.drawable.formula_cuadratica

                TipoCalculo.IMC ->
                    R.drawable.formula_imc

                TipoCalculo.TRAPECIO ->
                    R.drawable.formula_trapecio

                TipoCalculo.VELOCIDAD ->
                    R.drawable.formula_rapidez
            }

            Image(
                painter = painterResource(id = imageRes),

                contentDescription =
                    stringResource(
                        R.string.formula_content_description
                    ),

                modifier = Modifier.height(Dimens.AltoFormula)
            )
        },

        /*
         * PANEL DERECHO: formulario, error, botón y resultado
         */
        derecho = {

            /*
             * FORMULARIO DINÁMICO
             *
             * spacedBy separa los campos sin necesidad de
             * poner un Spacer entre cada uno.
             */
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.Chico)
            ) {

                when (seleccion) {

                    /*
                     * ECUACIÓN CUADRÁTICA
                     *
                     * Los coeficientes pueden ser positivos
                     * o negativos.
                     */
                    TipoCalculo.CUADRATICA -> {

                        NumericInputField(
                            value = campo1,
                            onValueChange = { campo1 = it },
                            labelRes = R.string.label_a,
                            allowSign = true
                        )

                        NumericInputField(
                            value = campo2,
                            onValueChange = { campo2 = it },
                            labelRes = R.string.label_b,
                            allowSign = true
                        )

                        NumericInputField(
                            value = campo3,
                            onValueChange = { campo3 = it },
                            labelRes = R.string.label_c,
                            allowSign = true
                        )
                    }

                    /*
                     * IMC
                     */
                    TipoCalculo.IMC -> {

                        NumericInputField(
                            value = campo1,
                            onValueChange = { campo1 = it },
                            labelRes = R.string.label_weight,
                            allowSign = false,
                            suffixRes = R.string.unit_kg
                        )

                        NumericInputField(
                            value = campo2,
                            onValueChange = { campo2 = it },
                            labelRes = R.string.label_height,
                            allowSign = false,
                            suffixRes = R.string.unit_m
                        )
                    }

                    /*
                     * TRAPECIO
                     */
                    TipoCalculo.TRAPECIO -> {

                        NumericInputField(
                            value = campo1,
                            onValueChange = { campo1 = it },
                            labelRes = R.string.label_base_major,
                            allowSign = false,
                            suffixRes = R.string.unit_m
                        )

                        NumericInputField(
                            value = campo2,
                            onValueChange = { campo2 = it },
                            labelRes = R.string.label_base_minor,
                            allowSign = false,
                            suffixRes = R.string.unit_m
                        )

                        NumericInputField(
                            value = campo3,
                            onValueChange = { campo3 = it },
                            labelRes = R.string.label_trapezoid_height,
                            allowSign = false,
                            suffixRes = R.string.unit_m
                        )
                    }

                    /*
                     * VELOCIDAD
                     */
                    TipoCalculo.VELOCIDAD -> {

                        NumericInputField(
                            value = campo1,
                            onValueChange = { campo1 = it },
                            labelRes = R.string.label_distance,
                            allowSign = false,
                            suffixRes = R.string.unit_m
                        )

                        NumericInputField(
                            value = campo2,
                            onValueChange = { campo2 = it },
                            labelRes = R.string.label_time,
                            allowSign = false,
                            suffixRes = R.string.unit_s
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(Dimens.Medio)
            )

            /*
             * MENSAJE DE ERROR
             */
            errorMensaje?.let { mensaje ->

                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(Dimens.Chico)
                )
            }

            /*
             * BOTÓN CALCULAR
             */
            Button(
                onClick = {

                    // Cierra el teclado para ver el resultado.
                    foco.clearFocus()

                    try {
                        resultado = calcular(
                            context,
                            seleccion,
                            campo1,
                            campo2,
                            campo3
                        )
                        errorMensaje = null

                    } catch (e: IllegalArgumentException) {

                        errorMensaje = e.message
                        resultado = ""
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.btn_calculate)
                )
            }

            /*
             * RESULTADO
             */
            if (resultado.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(Dimens.Medio)
                )

                Text(
                    text = resultado,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    )
}


/*
 * Vertical: una sola columna con scroll.
 * Horizontal: dos paneles lado a lado, cada uno con su propio scroll.
 *
 * Sin el scroll, en horizontal el contenido no cabe en la altura
 * de la pantalla y se corta.
 */
@Composable
private fun PanelesAdaptativos(
    esHorizontal: Boolean,
    izquierdo: @Composable ColumnScope.() -> Unit,
    derecho: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {

    if (esHorizontal) {

        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(Dimens.Medio),

            horizontalArrangement = Arrangement.spacedBy(Dimens.Medio)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),

                horizontalAlignment = Alignment.CenterHorizontally,
                content = izquierdo
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),

                horizontalAlignment = Alignment.CenterHorizontally,
                content = derecho
            )
        }

    } else {

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.Medio),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            izquierdo()

            Spacer(
                modifier = Modifier.height(Dimens.Medio)
            )

            derecho()
        }
    }
}


/*
 * Botón del menú: el cálculo activo se ve relleno
 * y los demás solo con borde.
 */
@Composable
private fun BotonMenu(
    @StringRes textoRes: Int,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    if (seleccionado) {
        Button(onClick = onClick) {
            Text(text = stringResource(textoRes))
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(text = stringResource(textoRes))
        }
    }
}


@Preview(
    name = "Vertical",
    showBackground = true,
    widthDp = 360,
    heightDp = 740
)
@Composable
fun CalculadoraPreview() {
    Ejercicio1Theme {
        CalculadoraScreen(esHorizontal = false)
    }
}

@Preview(
    name = "Horizontal",
    showBackground = true,
    widthDp = 800,
    heightDp = 360
)
@Composable
fun CalculadoraHorizontalPreview() {
    Ejercicio1Theme {
        CalculadoraScreen(esHorizontal = true)
    }
}