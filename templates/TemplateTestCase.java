// ==============================================================================
// TEMPLATE: Test Case simple (sin Data-Driven Testing)
// ==============================================================================
// Instrucciones de uso:
//   1. Copia a src/test/java/com/framework/testcases/Test<NombreFlujo>.java
//   2. Ajusta el package y renombra la clase.
//   3. Importa el Page Object de entrada.
//   4. Agrega un método @Test por cada escenario a cubrir.
//   5. Si necesitas múltiples conjuntos de datos, usa TemplateTestCaseDDT.java.
// ==============================================================================

package com.framework.testcases; // ajustar package

// import com.framework.uielements.<NombrePaginaEntrada>Page;  // ajustar import
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * <Describe brevemente el flujo de negocio que cubre este conjunto de pruebas>.
 *
 * Extiende BaseTest para heredar la inicialización del WebDriver,
 * el logger y el manejo automático del nombre de test para screenshots.
 */
public class TemplateTestCase extends BaseTest {

    // ------------------------------------------------------------------
    // Escenario positivo
    // ------------------------------------------------------------------

    /**
     * <Describe qué verifica este escenario y cuál es el resultado esperado>.
     */
    @Test(groups      = {"smoke"},              // ajustar: smoke | regression
          description = "<Describe el escenario positivo>")
    public void test<NombreEscenarioPositivo>() {
        log.info(">>> START: test<NombreEscenarioPositivo>");

        // Instanciar Page Object de entrada
        // <NombrePaginaEntrada>Page entryPage = new <NombrePaginaEntrada>Page(driver);

        // Paso 1 — <describe la acción>
        // <NombreSiguientePagina>Page nextPage = entryPage.someAction("valor_prueba");

        // Paso 2 — <describe la acción>
        // nextPage.anotherAction();

        // Validación con SoftAssert
        // SoftAssert softAssert = new SoftAssert();
        // boolean result = nextPage.someValidation();
        // softAssert.assertTrue(result, "Debería retornar true");
        // softAssert.assertAll();

        log.info(">>> END:   test<NombreEscenarioPositivo>");
    }

    // ------------------------------------------------------------------
    // Escenario negativo
    // (agrega tantos métodos @Test como escenarios necesites)
    // ------------------------------------------------------------------

    /**
     * <Describe qué verifica este escenario negativo y cuál es el comportamiento esperado>.
     */
    @Test(groups      = {"regression"},
          description = "<Describe el escenario negativo>")
    public void test<NombreEscenarioNegativo>() {
        log.info(">>> START: test<NombreEscenarioNegativo>");

        // <NombrePaginaEntrada>Page entryPage = new <NombrePaginaEntrada>Page(driver);

        // <NombreSiguientePagina>Page page = entryPage.someAction("valor_invalido");

        // SoftAssert softAssert = new SoftAssert();
        // softAssert.assertFalse(page.someValidation(),
        //         "Debería retornar false ante input inválido");
        // softAssert.assertAll();

        log.info(">>> END:   test<NombreEscenarioNegativo>");
    }
}
