package com.framework.commonaction;

import com.framework.report.TestResultData;
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
 * Todos los Page Objects deben extender esta clase.
 */
public class DriverActions {

    protected WebDriver driver;
    protected static final Logger log = CommonActions.getLogger();

    /**
     * Nombre del test activo, establecido por BaseTest antes de cada método de test.
     * Permite que takeScreenshot() use el nombre correcto sin recibir parámetros.
     */
    public static final ThreadLocal<String> currentTestName = new ThreadLocal<>();

    /**
     * Test activo en ejecución (set por TestListener.onTestStart).
     * Permite que takeScreenshot() adjunte la imagen al reporte y que las
     * acciones registren eventos en el timeline del test.
     */
    public static final ThreadLocal<TestResultData> currentTest = new ThreadLocal<>();

    private static final int DEFAULT_WAIT_SECONDS  = 5;
    private static final int URL_CHANGE_WAIT_SECONDS = 10;

    private static final DateTimeFormatter EVENT_TS_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public DriverActions(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Registra un evento en el timeline del test activo (se mostrará en el
     * reporte HTML al expandir el test).
     *
     * @param severity "info" | "pass" | "fail"
     * @param description texto del evento (HTML permitido en {@code <code>...</code>})
     */
    public static void logEvent(String severity, String description) {
        TestResultData t = currentTest.get();
        if (t == null) return;
        t.events.add(new TestResultData.EventEntry(
                severity,
                LocalDateTime.now().format(EVENT_TS_FMT),
                description));
    }

    /**
     * Espera hasta que un elemento sea clickeable antes de devolverlo.
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
     *
     * @param currentUrl URL actual antes de la acción que dispara la navegación.
     */
    public void waitForUrlChange(String currentUrl) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(URL_CHANGE_WAIT_SECONDS));
        wait.until(d -> !d.getCurrentUrl().equals(currentUrl));
    }

    /**
     * Captura un screenshot, lo guarda en reports/screenshots/ y lo asocia
     * al test activo (será visible al expandir el test en el reporte HTML).
     * El nombre del test se lee automáticamente del ThreadLocal currentTestName.
     *
     * @return Ruta relativa al archivo de screenshot generado.
     */
    public String takeScreenshot() {
        String testName = currentTestName.get() != null ? currentTestName.get() : "manual_screenshot";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));

        String callerMethod = Thread.currentThread().getStackTrace()[2].getMethodName();

        Path screenshotPath = Paths.get("reports", "screenshots",
                testName + "_" + timestamp + " - " + callerMethod + ".png");

        try {
            Files.createDirectories(screenshotPath.getParent());
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), screenshotPath, StandardCopyOption.REPLACE_EXISTING);

            // El reporte vive en reports/report.html, así que la ruta relativa
            // que el navegador necesita es "screenshots/..." (sin el "reports/").
            String relativeForHtml = "screenshots/"
                    + screenshotPath.getFileName().toString();

            TestResultData t = currentTest.get();
            if (t != null) {
                t.screenshotPath = relativeForHtml;
                logEvent("info", "Screenshot: <code>" + relativeForHtml + "</code>");
            }
        } catch (IOException e) {
            log.error("Error al guardar screenshot: " + e.getMessage());
        }

        return screenshotPath.toString();
    }

    /**
     * Resalta visualmente un elemento en el navegador cambiando temporalmente su borde
     * via JavaScript, luego restaura el estilo original y captura un screenshot.
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
     * (lazy load).
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
