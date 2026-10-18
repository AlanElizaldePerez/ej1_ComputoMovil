Cómputo Móvil 2027-1



Ejercicio de programación 1



Grupo: 02



Profesor: ING. Amaury Hector Perea Matsumura



Alumno: Alan Elizalde Pérez





Aplicación Android desarrollada en Kotlin con Jetpack Compose. Permite resolver ecuaciones de segundo grado, índice de masa corporal, 

rapidez y área del Pentágono; también permite la selección de los idiomas español, inglés, ruso y el cambio entre tema claro y oscuro.



Funcionalidades

Calculadoras disponibles



\- Ecuación cuadrática: calcula las raíces reales de una ecuación de segundo grado. Valida que el coeficiente a no sea cero y que el 

discriminante sea mayor o igual que cero. Para este caso no se realizaron ajustes en unidades, porque son número escalares, ni en 

la imagen de la formula porque se utiliza de esta manera en español, inglés y ruso.



\- Índice de Masa Corporal (IMC): calcula el IMC a partir del peso y la estatura. Utiliza la formula con unidades del sistema métrico 

en español y ruso, para el idioma inglés uso el sistema imperial.



\- Área del trapecio: calcula el área a partir de la base mayor, la base menor y la altura. No se realizaron ajustes en las formulas, 

pero para el idioma inglés se utilizan pies en lugar de metros y para el ruso muestra su formula como la estudian normalmente en Rusia.



\- Rapidez: calcula la rapidez dividiendo la distancia entre el tiempo. No se realizaron ajustes en las formulas, pero para el idioma 

inglés se utilizan pies en lugar de metros.



Idiomas y separadores decimales

La interfaz dispone de tres idiomas:

\- Español (México): utiliza punto como separador decimal.



\- English (United States): utiliza punto como separador decimal.



\- Русский (Rusia): utiliza coma como separador decimal.



Los valores decimales se normalizan internamente con punto para facilitar su validación y cálculo. La aplicación adapta su 

representación visual al separador del idioma seleccionado.



Sistemas de unidades

El sistema de unidades se determina según el idioma seleccionado en la implementación actual:

Idioma	Sistema de unidades

Español	Métrico

Inglés (Estados Unidos)	Imperial

Ruso	Métrico



En el cálculo del IMC se emplea el factor 1.0 para el sistema métrico y 703.0 para el sistema imperial. Los textos y símbolos de las 

unidades deben estar definidos en los recursos de cada idioma para que coincidan con el sistema utilizado.



Nota: Las unidades están asociadas al idioma; no hay forma de cambiar unicamente las unidades.



Tema claro y oscuro

\- Permite alternar entre los temas claro y oscuro desde la barra superior.



\- Utiliza las imágenes de fondo tema\_c para el tema claro y tema\_o para el oscuro.



\- Anima la transición entre fondos mediante Crossfade.



\- Cambia también el esquema de colores de Material 3 y utiliza paneles translúcidos para mostrar el contenido sobre el fondo.



\- Si todavía no se ha guardado una preferencia, utiliza el tema del sistema. Una vez que el usuario elige un tema, la selección se 

guarda para las siguientes ejecuciones.



Validaciones y experiencia de uso

\- Comprueba que los campos requeridos contengan valores numéricos válidos.



\- Permite coeficientes negativos en la ecuación cuadrática y evita valores negativos en los campos donde no están permitidos.



\- Valida restricciones específicas de cada operación, como valores positivos para el peso, la estatura, las bases, la altura y el tiempo.



\- Muestra mensajes de error y resultados mediante recursos de texto.



\- Conserva el contenido de los campos al cambiar la orientación del dispositivo.



\- Limpia los campos al cambiar entre sistemas de unidades; por ejemplo si se estaba utilizan el idioma español y se cambia al idioma 

inglés se limpian los campos porque utilizan formulas diferentes, no se hace una conversión.



\- Adapta la disposición de la interfaz a orientación vertical u horizontal. En horizontal presenta los paneles lado a lado; en vertical 

los coloca en una sola columna con desplazamiento.



Tecnologías

\- Kotlin como lenguaje de programación.



\- Jetpack Compose para construir la interfaz.



\- Material 3 para los componentes y el esquema de colores.



\- Android Resources (strings.xml, imágenes y otros recursos) para textos e imágenes localizados.



\- SharedPreferences para guardar las preferencias de idioma y tema.

