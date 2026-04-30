// ==============================================================================
// TEMPLATE: Test Case con Data-Driven Testing (DDT)
// ==============================================================================
// Instrucciones de uso:
//   1. Copia a src/test/java/com/framework/testcases/Test<NombreFlujo>.java
//   2. Ajusta el package y renombra la clase.
//   3. Importa el Page Object de entrada (primera página del flujo).
//   4. Crea el JSON de datos en src/test/resources/testdata/<flujo>.json
//      (ver formato al final de este archivo).
//   5. Actualiza el nombre del archivo en @DataProvider y los campos del Map
//      en la firma del método de test.
//
// Equivalente a:
//   @ddt + @file_data de Python  →  @DataProvider + JsonDataProvider.getData()
//   softest.TestCase             →  SoftAssert de TestNG
//   class_setup fixture          →  instanciación del Page Object dentro del test
//
// Formato del JSON esperado en src/test/resources/testdata/<flujo>.json:
// {
//   "test1": { "param1": "val1", "param2": "val2", "start": ">>> START", "end": ">>> END" },
//   "test2": { "param1": "valA", "param2": "valB", "start": ">>> START", "end": ">>> END" }
// }
// ==============================================================================

package com.framework.testcases; // ajustar package

import com.framework.utils.JsonDataProvider;
// import com.framework.uielements.<NombrePaginaEntrada>Page;  // ajustar import
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;

/**
 * <Describe brevemente el flujo de negocio que cubre este conjunto de pruebas>.
 *
 * Extiende BaseTest para heredar la inicialización del WebDriver,
 * el logger y el manejo automático del nombre de test para screenshots.
 */
public class TemplateTestCaseDDT extends BaseTest {

    /**
     * Proveedor de datos: lee el JSON de testdata/ y retorna una fila por caso de prueba.
     * Cada fila es un Map&lt;String, String&gt; con los campos del objeto JSON.
     */
    @DataProvider(name = "testData")
    public Object[][] testData() {
        return JsonDataProvider.getData("<flujo>.json"); // ajustar nombre del archivo
    }

    /**
     * <Describe qué verifica este test y cuáles son las condiciones de éxito>.
     *
     * Datos de prueba: testdata/<flujo>.json
     *
     * @param data Map con los campos del caso de prueba leídos desde el JSON.
     */
    @Test(dataProvider = "testData",
          groups       = {"regression"},       // ajustar: smoke | regression
          description  = "<Describe el flujo>")
    public void test<NombreFlujo>(Map<String, String> data) {
        String param1 = data.get("param1");
        String param2 = data.get("param2");
        String start  = data.get("start");
        String end    = data.get("end");

        // ------------------------------------------------------------------
        // Inicio del test
        // ------------------------------------------------------------------
        log.info(start + " param1:" + param1 + ", param2:" + param2);

        // ------------------------------------------------------------------
        // Instanciar el Page Object de entrada
        // (equivalente al class_setup fixture en el framework Python)
        // ------------------------------------------------------------------
        // <NombrePaginaEntrada>Page entryPage = new <NombrePaginaEntrada>Page(driver);

        // ------------------------------------------------------------------
        // Paso 1 — <describe la acción>
        // ------------------------------------------------------------------
        // <NombreSiguientePagina>Page nextPage = entryPage.someAction(param1);

        // ------------------------------------------------------------------
        // Paso 2 — <describe la acción>
        // ------------------------------------------------------------------
        // nextPage.anotherAction(param2);

        // ------------------------------------------------------------------
        // Validación con SoftAssert
        // (equivalente a self.soft_assert + self.assert_all() de softest)
        // ------------------------------------------------------------------
        // SoftAssert softAssert = new SoftAssert();
        // boolean result = nextPage.someValidation();
        // softAssert.assertTrue(result, "Descripción del assert");
        // softAssert.assertAll();  // reporta todos los fallos juntos al final

        // ------------------------------------------------------------------
        // Fin del test
        // ------------------------------------------------------------------
        log.info(end + " param1:" + param1 + ", param2:" + param2);
    }
}
