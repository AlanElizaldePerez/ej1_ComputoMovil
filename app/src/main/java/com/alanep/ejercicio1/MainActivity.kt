package com.alanep.ejercicio1

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.alanep.ejercicio1.ui.theme.Ejercicio1Theme
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.sqrt

enum class TipoCalculo {
    CUADRATICA,
    IMC,
    TRAPECIO,
    VELOCIDAD
}

enum class Idioma(
    val codigo: String,
    @StringRes val nombre: Int,
    val locale: Locale
) {
    ESPANOL(
        codigo = "es",
        nombre = R.string.language_es,
        locale = Locale.forLanguageTag("es-MX")
    ),

    INGLES(
        codigo = "en",
        nombre = R.string.language_en,
        locale = Locale.forLanguageTag("en-US")
    ),

    RUSO(
        codigo = "ru",
        nombre = R.string.language_ru,
        locale = Locale.forLanguageTag("ru-RU")
    );

    /*
     * Separador decimal del idioma: '.' en español e inglés, ',' en ruso.
     * Sale del propio Locale, así siempre coincide con el de los resultados.
     */
    val separadorDecimal: Char =
        DecimalFormatSymbols.getInstance(locale).decimalSeparator
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

@DrawableRes
private fun formulaDrawable(
    tipo: TipoCalculo,
    idioma: Idioma
): Int = when (tipo) {

    TipoCalculo.CUADRATICA -> when (idioma) {
        Idioma.ESPANOL -> R.drawable.formula_cuadratica
        Idioma.INGLES -> R.drawable.formula_cuadratica
        Idioma.RUSO -> R.drawable.formula_cuadratica
    }

    TipoCalculo.IMC -> when (idioma) {
        Idioma.ESPANOL -> R.drawable.formula_imc
        Idioma.INGLES -> R.drawable.formula_imc_en
        Idioma.RUSO -> R.drawable.formula_imc_ru
    }

    TipoCalculo.TRAPECIO -> when (idioma) {
        Idioma.ESPANOL -> R.drawable.formula_trapecio
        Idioma.INGLES -> R.drawable.formula_trapecio
        Idioma.RUSO -> R.drawable.formula_trapecio_ru
    }

    TipoCalculo.VELOCIDAD -> when (idioma) {
        Idioma.ESPANOL -> R.drawable.formula_rapidez
        Idioma.INGLES -> R.drawable.formula_rapidez_en
        Idioma.RUSO -> R.drawable.formula_rapidez_ru
    }
}

/*
 * Valida la estructura del texto que se está escribiendo.
 *
 * allowSign = true:
 * permite - únicamente al principio.
 *
 * allowSign = false:
 * solamente permite números y punto decimal.
 *
 * Internamente el decimal SIEMPRE es un punto: la coma del ruso se
 * convierte a punto en NumericInputField antes de llegar aquí.
 */
private fun isValidNumericInput(
    value: String,
    allowSign: Boolean
): Boolean {

    if (value.isEmpty()) {
        return true
    }

    val pattern = if (allowSign) {
        Regex(pattern = """[-]?\d*\.?\d*""")
    } else {
        Regex(pattern = """\d*\.?\d*""")
    }

    return pattern.matches(input = value)
}

/*
 * Muestra el número con el separador decimal del idioma.
 *
 * El texto guardado lleva siempre punto ("1.5"); esta transformación solo
 * cambia lo que se ve en pantalla ("1,5" en ruso). Como cambia un carácter
 * por otro, el cursor no se mueve (OffsetMapping.Identity).
 */
private class SeparadorDecimalVisual(
    private val separadorDecimal: Char
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString(
                text = text.text.replace(oldChar = '.', newChar = separadorDecimal)
            ),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

@Composable
private fun NumericInputField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    allowSign: Boolean,
    separadorDecimal: Char,
    @StringRes suffixRes: Int? = null
) {

    val transformacionVisual = remember(key1 = separadorDecimal) {
        SeparadorDecimalVisual(separadorDecimal = separadorDecimal)
    }

    OutlinedTextField(
        value = value,

        onValueChange = { newValue ->

            val normalizado = newValue.replace(
                oldChar = separadorDecimal,
                newChar = '.'
            )

            if (isValidNumericInput(value = normalizado, allowSign = allowSign)) {
                onValueChange(normalizado)
            }
        },

        label = {
            Text(text = stringResource(id = labelRes))
        },

        suffix = if (suffixRes != null) {
            {
                Text(text = stringResource(id = suffixRes))
            }
        } else {
            null
        },

        visualTransformation = transformacionVisual,

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),

        singleLine = true,

        modifier = Modifier
            .fillMaxWidth()
    )
}

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

        val a = leerNumero(context = context, texto = campo1, labelRes = R.string.label_a)
        val b = leerNumero(context = context, texto = campo2, labelRes = R.string.label_b)
        val c = leerNumero(context = context, texto = campo3, labelRes = R.string.label_c)

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

        val x1 = (-b + sqrt(x = discriminante)) / (2 * a)
        val x2 = (-b - sqrt(x = discriminante)) / (2 * a)

        context.getString(R.string.result_quadratic, x1, x2)
    }

    /*
     * IMC
     */
    TipoCalculo.IMC -> {

        val peso = leerNumero(context = context, texto = campo1, labelRes = R.string.label_weight)
        val altura = leerNumero(context = context, texto = campo2, labelRes = R.string.label_height)

        exigirPositivo(context = context, valor = peso, labelRes = R.string.label_weight)
        exigirPositivo(context = context, valor = altura, labelRes = R.string.label_height)

        val imc = peso / (altura * altura)

        context.getString(R.string.result_bmi, imc)
    }

    /*
     * TRAPECIO
     */
    TipoCalculo.TRAPECIO -> {

        val baseMayor = leerNumero(context = context, texto = campo1, labelRes = R.string.label_base_major)
        val baseMenor = leerNumero(context = context, texto = campo2, labelRes = R.string.label_base_minor)
        val altura = leerNumero(context = context, texto = campo3, labelRes = R.string.label_trapezoid_height)

        exigirPositivo(context = context, valor = baseMayor, labelRes = R.string.label_base_major)
        exigirPositivo(context = context, valor = baseMenor, labelRes = R.string.label_base_minor)
        exigirPositivo(context = context, valor = altura, labelRes = R.string.label_trapezoid_height)

        val area = ((baseMayor + baseMenor) * altura) / 2

        context.getString(R.string.result_trapezoid, area)
    }

    /*
     * VELOCIDAD
     */
    TipoCalculo.VELOCIDAD -> {

        val distancia = leerNumero(context = context, texto = campo1, labelRes = R.string.label_distance)
        val tiempo = leerNumero(context = context, texto = campo2, labelRes = R.string.label_time)

        if (distancia < 0) {
            throw IllegalArgumentException(
                context.getString(
                    R.string.error_non_negative,
                    context.getString(R.string.label_distance)
                )
            )
        }

        exigirPositivo(context = context, valor = tiempo, labelRes = R.string.label_time)

        val velocidad = distancia / tiempo

        context.getString(R.string.result_rapidez, velocidad)
    }
}


/*
 * CAMBIO DE IDIOMA
 */
@Composable
private fun ConIdioma(
    codigo: String?,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val (contextoLocal, configuracionLocal) =
        remember(key1 = codigo, key2 = context, key3 = configuration) {

            val codigoSistema = ConfigurationCompat
                .getLocales(configuration)
                .get(0)
                ?.language

            val idioma = Idioma.entries.firstOrNull { it.codigo == codigo }
                ?: Idioma.entries.firstOrNull { it.codigo == codigoSistema }
                ?: Idioma.ESPANOL

            val nueva = Configuration(configuration).apply {
                setLocale(idioma.locale)
            }

            context.createConfigurationContext(nueva) to nueva
        }

    CompositionLocalProvider(
        LocalContext provides contextoLocal,
        LocalConfiguration provides configuracionLocal
    ) {
        content()
    }
}

@Composable
private fun idiomaActual(): Idioma {

    val codigo = ConfigurationCompat
        .getLocales(LocalConfiguration.current)
        .get(0)
        ?.language

    return Idioma.entries.firstOrNull { it.codigo == codigo } ?: Idioma.ESPANOL
}


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val preferencias = getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

        setContent {

            /*
             * rememberSaveable conserva el idioma al girar el dispositivo y
             * SharedPreferences al cerrar y volver a abrir la app.
             */
            var codigoIdioma by rememberSaveable {
                mutableStateOf(value = preferencias.getString(CLAVE_IDIOMA, null))
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
                            modifier = Modifier.padding(paddingValues = innerPadding)
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
            Text(text = stringResource(id = R.string.screen_title))
        },

        actions = {
            BotonIdioma(onIdiomaSeleccionado = onIdiomaSeleccionado)
        },

        // Respeta el notch y las barras del sistema también en horizontal.
        windowInsets = WindowInsets.safeDrawing.only(
            sides = WindowInsetsSides.Top + WindowInsetsSides.Horizontal
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
    var expandido by remember { mutableStateOf(value = false) }

    val idiomaActivo = idiomaActual()

    Box {

        IconButton(onClick = { expandido = true }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_language),
                contentDescription = stringResource(id = R.string.change_language)
            )
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {

            Idioma.entries.forEach { idioma ->

                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = idioma.nombre))
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
    esHorizontal: Boolean =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
) {

    val context = LocalContext.current
    val foco = LocalFocusManager.current

    /*
     * Idioma que se está mostrando. Se usa para:
     *  - elegir la imagen de la fórmula
     *  - saber qué separador decimal se escribe (punto o coma)
     *  - como "llave" del resultado y del error: cuando cambia el idioma se
     *    limpian, porque ya son texto traducido en el idioma anterior.
     */
    val idiomaActivo = idiomaActual()
    val separadorDecimal = idiomaActivo.separadorDecimal

    var seleccion by rememberSaveable {
        mutableStateOf(value = TipoCalculo.CUADRATICA)
    }

    var campo1 by rememberSaveable {
        mutableStateOf(value = "")
    }

    var campo2 by rememberSaveable {
        mutableStateOf(value = "")
    }

    var campo3 by rememberSaveable {
        mutableStateOf(value = "")
    }

    var resultado by rememberSaveable(idiomaActivo) {
        mutableStateOf(value = "")
    }

    var errorMensaje by rememberSaveable(idiomaActivo) {
        mutableStateOf<String?>(value = null)
    }
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
         * (el título está en la barra superior)
         */
        izquierdo = {

            Text(
                text = stringResource(id = R.string.select_calculation)
            )

            Spacer(
                modifier = Modifier.height(height = Dimens.Medio)
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
                    onClick = { seleccionar(tipo = TipoCalculo.CUADRATICA) }
                )

                BotonMenu(
                    textoRes = R.string.calc_imc,
                    seleccionado = seleccion == TipoCalculo.IMC,
                    onClick = { seleccionar(tipo = TipoCalculo.IMC) }
                )
            }

            Spacer(
                modifier = Modifier.height(height = Dimens.Chico)
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
                    onClick = { seleccionar(tipo = TipoCalculo.TRAPECIO) }
                )

                BotonMenu(
                    textoRes = R.string.calc_rapidez,
                    seleccionado = seleccion == TipoCalculo.VELOCIDAD,
                    onClick = { seleccionar(tipo = TipoCalculo.VELOCIDAD) }
                )
            }

            Spacer(
                modifier = Modifier.height(height = Dimens.Medio)
            )

            /*
             * FÓRMULA DINÁMICA
             *
             * La imagen depende del cálculo Y del idioma: al cambiar
             * el idioma se recompone con la imagen del idioma nuevo.
             */
            val imageRes = formulaDrawable(
                tipo = seleccion,
                idioma = idiomaActivo
            )

            Image(
                painter = painterResource(id = imageRes),

                contentDescription =
                    stringResource(
                        id = R.string.formula_content_description
                    ),

                modifier = Modifier.height(height = Dimens.AltoFormula)
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
                verticalArrangement = Arrangement.spacedBy(space = Dimens.Chico)
            ) {

                when (seleccion) {

                    /*
                     * ECUACIÓN CUADRÁTICA
                     *
                     * Los coeficientes pueden ser negativos.
                     */
                    TipoCalculo.CUADRATICA -> {

                        NumericInputField(
                            value = campo1,
                            onValueChange = { campo1 = it },
                            labelRes = R.string.label_a,
                            allowSign = true,
                            separadorDecimal = separadorDecimal
                        )

                        NumericInputField(
                            value = campo2,
                            onValueChange = { campo2 = it },
                            labelRes = R.string.label_b,
                            allowSign = true,
                            separadorDecimal = separadorDecimal
                        )

                        NumericInputField(
                            value = campo3,
                            onValueChange = { campo3 = it },
                            labelRes = R.string.label_c,
                            allowSign = true,
                            separadorDecimal = separadorDecimal
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
                            separadorDecimal = separadorDecimal,
                            suffixRes = R.string.unit_kg
                        )

                        NumericInputField(
                            value = campo2,
                            onValueChange = { campo2 = it },
                            labelRes = R.string.label_height,
                            allowSign = false,
                            separadorDecimal = separadorDecimal,
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
                            separadorDecimal = separadorDecimal,
                            suffixRes = R.string.unit_m
                        )

                        NumericInputField(
                            value = campo2,
                            onValueChange = { campo2 = it },
                            labelRes = R.string.label_base_minor,
                            allowSign = false,
                            separadorDecimal = separadorDecimal,
                            suffixRes = R.string.unit_m
                        )

                        NumericInputField(
                            value = campo3,
                            onValueChange = { campo3 = it },
                            labelRes = R.string.label_trapezoid_height,
                            allowSign = false,
                            separadorDecimal = separadorDecimal,
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
                            separadorDecimal = separadorDecimal,
                            suffixRes = R.string.unit_m
                        )

                        NumericInputField(
                            value = campo2,
                            onValueChange = { campo2 = it },
                            labelRes = R.string.label_time,
                            allowSign = false,
                            separadorDecimal = separadorDecimal,
                            suffixRes = R.string.unit_s
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(height = Dimens.Medio)
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
                    modifier = Modifier.height(height = Dimens.Chico)
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
                            context = context,
                            tipo = seleccion,
                            campo1 = campo1,
                            campo2 = campo2,
                            campo3 = campo3
                        )
                        errorMensaje = null

                    } catch (e: IllegalArgumentException) {

                        errorMensaje = e.message
                        resultado = ""
                    }
                }
            ) {
                Text(
                    text = stringResource(id = R.string.btn_calculate)
                )
            }

            /*
             * RESULTADO
             */
            if (resultado.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(height = Dimens.Medio)
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
                .padding(all = Dimens.Medio),

            horizontalArrangement = Arrangement.spacedBy(space = Dimens.Medio)
        ) {

            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxHeight()
                    .verticalScroll(state = rememberScrollState()),

                horizontalAlignment = Alignment.CenterHorizontally,
                content = izquierdo
            )

            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxHeight()
                    .verticalScroll(state = rememberScrollState()),

                horizontalAlignment = Alignment.CenterHorizontally,
                content = derecho
            )
        }

    } else {

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(all = Dimens.Medio),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            izquierdo()

            Spacer(
                modifier = Modifier.height(height = Dimens.Medio)
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
            Text(text = stringResource(id = textoRes))
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(text = stringResource(id = textoRes))
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