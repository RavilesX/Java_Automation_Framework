// ==============================================================================
// TEMPLATE: Page Object
// ==============================================================================
// Instrucciones de uso:
//   1. Copia a src/test/java/com/framework/uielements/<NombrePagina>Page.java
//      Ejemplo: LoginPage.java, ResultsPage.java, CheckoutPage.java
//   2. Ajusta el package al package de destino.
//   3. Renombra la clase: public class <NombrePagina>Page extends DriverActions
//   4. Define los locators como constantes By (ver sección Locators).
//   5. Crea un getter privado por locator (ver sección WebElements).
//   6. Implementa la lógica de negocio de la página (ver sección Methods).
//   7. Los métodos que navegan a otra página deben retornar la instancia
//      de esa página: return new <NombreSiguientePagina>Page(driver);
// ==============================================================================

package com.framework.uielements; // ajustar package

import com.framework.commonaction.CommonActions;
import com.framework.commonaction.DriverActions;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
// import org.openqa.selenium.Keys;  // descomenta si necesitas enviar teclas especiales

/**
 * Page Object que representa <describe brevemente la página>.
 *
 * Extiende DriverActions para heredar: waitForElement, waitForUrlChange,
 * highlightElement, takeScreenshot y scrollPage.
 */
public class TemplatePage extends DriverActions {

    private static final Logger log = CommonActions.getLogger();

    // ------------------------------------------------------------------
    // Locators
    // Constantes estáticas finales usando By.<estrategia>("<valor>").
    // Convención de nombres: NOMBRE_DESCRIPTIVO + tipo de elemento en mayúsculas.
    //   Tipos: BTN (button), TB (textbox), LBL (label), PNL (panel),
    //          IMG (image), LNK (link), DDL (dropdown), CHK (checkbox)
    // ------------------------------------------------------------------
    private static final By EXAMPLE_BTN   = By.xpath("//button[contains(@id,'example-id')]");
    private static final By EXAMPLE_INPUT = By.xpath("//input[contains(@placeholder,'example')]");
    private static final By EXAMPLE_LBL   = By.xpath("//span[contains(@class,'example-class')]");
    // private static final By EXAMPLE_CSS = By.cssSelector("#parent > div.child");

    public TemplatePage(WebDriver driver) {
        super(driver);
    }

    // ------------------------------------------------------------------
    // WebElements
    // Un getter privado por locator; delegan la espera explícita a waitForElement.
    // Convención: get<NombreDescriptivo><Tipo>()
    // ------------------------------------------------------------------
    private WebElement getExampleBtn()    { return waitForElement(EXAMPLE_BTN); }
    private WebElement getExampleInputTb(){ return waitForElement(EXAMPLE_INPUT); }
    private WebElement getExampleLbl()    { return waitForElement(EXAMPLE_LBL); }

    // ------------------------------------------------------------------
    // Methods
    // Cada método representa una acción completa del usuario en esta página.
    // Si la acción navega a otra página, retorna la instancia de esa página.
    // ------------------------------------------------------------------

    /**
     * <Describe qué hace esta acción y cuándo usarla>.
     *
     * @param value <Describe el parámetro>.
     * @return Instancia de la siguiente página, o void si no hay navegación.
     */
    public void exampleAction(String value) {  // cambiar tipo de retorno si navega
        log.info("Performing example action with: " + value);

        getExampleInputTb().click();
        getExampleInputTb().clear();
        getExampleInputTb().sendKeys(value);
        highlightElement(getExampleInputTb(), "yellow", 3);

        String currentUrl = driver.getCurrentUrl();
        getExampleBtn().click();
        waitForUrlChange(currentUrl);

        // Si navega a otra página:
        // return new <NombreSiguientePagina>Page(driver);
    }

    /**
     * <Describe qué valida este método>.
     *
     * @return true si la condición esperada se cumple, false en caso contrario.
     */
    public boolean exampleValidation() {
        try {
            WebElement element = getExampleLbl();
            highlightElement(element, "yellow", 3);
            log.info("Element found: " + element.getText());
            return true;
        } catch (Exception e) {
            log.info("Element not found");
            return false;
        }
    }
}
