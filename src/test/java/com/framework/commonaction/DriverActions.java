package com.framework.commonaction;

import com.aventstack.extentreports.ExtentTest;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase base que encapsula acciones genéricas de Selenium reutilizables
 * por todos los Page Objects del framework.
 *
 * Provee utilidades para esperas explícitas, scroll, screenshots y
 * resaltado visual de elementos durante la ejecución de pruebas.
 *
 * Equivalente a commonaction/driver_actions.py del framework Python.
 * Todos los Page Objects deben extender esta clase.
 */
public class DriverActions {

    protected WebDriver driver;
    protected static final Logger log = CommonActions.getLogger();

    /**
     * Nombre del test activo, establecido por BaseTest antes de cada método de test.
     * Equivalente al pytest.current_test que se setea en el hook pytest_runtest_call.
     * Permite que takeScreenshot() use el nombre correcto sin recibir parámetros.
     */
    public static final ThreadLocal<String> currentTestName = new ThreadLocal<>();

    /**
     * Instancia de ExtentTest activa, establecida por TestListener en onTestStart.
     * Permite que takeScreenshot() adjunte la imagen al reporte sin acoplamiento.
     */
    public static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    private static final int DEFAULT_WAIT_SECONDS  = 5;
    private static final int URL_CHANGE_WAIT_SECONDS = 10;

    public DriverActions(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Espera hasta que un elemento sea clickeable antes de devolverlo.
     * Equivalente a wait_for_element(By, locator) del framework Python.
     *
     * @param locator Estrategia + valor de localización (e.g. By.xpath("...")).
     * @return WebElement una vez que está clickeable.
     */
    public WebElement waitForElement(By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_SECONDS));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Espera hasta que la URL del navegador cambie respecto a la proporcionada.
     * Equivalente a wait_for_url_change(current_url) del framework Python.
     * Reemplaza el uso de time.sleep() para navegaciones.
     *
     * @param currentUrl URL actual antes de la acción que dispara la navegación.
     */
    public void waitForUrlChange(String currentUrl) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(URL_CHANGE_WAIT_SECONDS));
        wait.until(d -> !d.getCurrentUrl().equals(currentUrl));
    }

    /**
     * Captura un screenshot, lo guarda en reports/screenshots/ y lo adjunta al reporte HTML.
     * El nombre del test se lee automáticamente del ThreadLocal currentTestName.
     * Equivalente a take_screenshot() del framework Python.
     *
     * @return Ruta relativa al archivo de screenshot generado.
     */
    public String takeScreenshot() {
        String testName = currentTestName.get() != null ? currentTestName.get() : "manual_screenshot";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));

        // Obtiene el método que llamó a takeScreenshot (o a highlightElement, que llama a este)
        // getStackTrace()[0]=getStackTrace, [1]=takeScreenshot, [2]=llamador directo
        String callerMethod = Thread.currentThread().getStackTrace()[2].getMethodName();

        Path screenshotPath = Paths.get("reports", "screenshots",
                testName + "_" + timestamp + " - " + callerMethod + ".png");

        try {
            Files.createDirectories(screenshotPath.getParent());
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), screenshotPath, StandardCopyOption.REPLACE_EXISTING);

            ExtentTest test = extentTest.get();
            if (test != null) {
                test.addScreenCaptureFromPath(screenshotPath.toString());
            }
        } catch (IOException e) {
            log.error("Error al guardar screenshot: " + e.getMessage());
        }

        return screenshotPath.toString();
    }

    /**
     * Resalta visualmente un elemento en el navegador cambiando temporalmente su borde
     * via JavaScript, luego restaura el estilo original y captura un screenshot.
     * Equivalente a highlight_element(element, color, border) del framework Python.
     *
     * @param element WebElement a resaltar.
     * @param color   Color del borde (e.g. "yellow", "red").
     * @param border  Grosor del borde en píxeles.
     */
    public void highlightElement(WebElement element, String color, int border) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String originalStyle = element.getAttribute("style");
        js.executeScript("arguments[0].setAttribute('style', arguments[1]);",
                element, "border: " + border + "px solid " + color + ";");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originalStyle);
        takeScreenshot();
    }

    /**
     * Realiza scroll hasta el final de la página esperando que cargue contenido dinámico
     * (lazy load). Equivalente a scroll_page() del framework Python.
     */
    public void scrollPage() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long lastHeight;
        do {
            lastHeight = (Long) js.executeScript("return document.body.scrollHeight");
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        } while ((Long) js.executeScript("return document.body.scrollHeight") > lastHeight);
    }
}
