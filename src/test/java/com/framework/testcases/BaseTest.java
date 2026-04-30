package com.framework.testcases;

import com.framework.commonaction.CommonActions;
import com.framework.commonaction.DriverActions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.*;

import java.lang.reflect.Method;

/**
 * Clase base para todos los test cases del framework.
 *
 * Gestiona el ciclo de vida del WebDriver (inicialización, navegación y cierre)
 * y expone el driver y el logger a las clases hijas.
 *
 * Equivalente a conftest.py de Pytest:
 *   - @BeforeClass  ↔ fixture setup(scope="class")
 *   - @AfterClass   ↔ driver.close() al salir del yield
 *   - @BeforeMethod ↔ hook pytest_runtest_call (setea el nombre del test activo)
 *   - @Parameters   ↔ pytest_addoption (--browser, --url)
 *
 * Los test cases concretos deben extender esta clase.
 */
public class BaseTest {

    /** Driver disponible para todas las subclases y el TestListener. */
    protected WebDriver driver;

    protected static final Logger log = CommonActions.getLogger();

    /**
     * ThreadLocal que expone el driver al TestListener para capturas de screenshot
     * en caso de fallo. Equivalente al acceso a item.instance.driver en conftest.py.
     */
    public static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    private static final String[] SUPPORTED_BROWSERS = {"chrome", "firefox", "edge"};

    /**
     * Inicializa el WebDriver según el browser indicado, navega a la URL base
     * y maximiza la ventana.
     *
     * Los valores pueden venir de testng.xml (parámetros) o de system properties
     * pasados por Maven: mvn test -Dbrowser=firefox -Durl=https://www.miapp.com
     *
     * @param browser Nombre del navegador (chrome | firefox | edge). Por defecto: chrome.
     * @param url     URL base de la aplicación bajo prueba.
     */
    @Parameters({"browser", "url"})
    @BeforeClass(alwaysRun = true)
    public void setup(@Optional("chrome") String browser,
                      @Optional("https://www.example.com") String url) {

        // System props win only when explicitly set via -Dbrowser=... / -Durl=...
        // Maven sets them to "" when omitted, so blank = not provided
        String sysBrowser      = System.getProperty("browser");
        String sysUrl          = System.getProperty("url");
        String resolvedBrowser = (sysBrowser != null && !sysBrowser.isBlank()) ? sysBrowser : browser;
        String resolvedUrl     = (sysUrl     != null && !sysUrl.isBlank())     ? sysUrl     : url;

        log.info("Iniciando WebDriver: browser=" + resolvedBrowser + ", url=" + resolvedUrl);

        switch (resolvedBrowser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                // options.addArguments("--headless");  // descomenta para ejecución sin UI
                driver = new ChromeDriver(options);
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                break;
            default:
                throw new IllegalArgumentException(
                        "Browser '" + resolvedBrowser + "' no soportado. "
                        + "Opciones válidas: chrome, firefox, edge");
        }

        driver.get(resolvedUrl);
        driver.manage().window().maximize();
        driverThread.set(driver);
    }

    /**
     * Registra el nombre del test activo en un ThreadLocal antes de cada método.
     * Equivalente al hook pytest_runtest_call que setea pytest.current_test.
     * Permite que DriverActions.takeScreenshot() use el nombre sin recibir parámetros.
     *
     * @param method Método de test que se está por ejecutar.
     */
    @BeforeMethod(alwaysRun = true)
    public void setCurrentTestName(Method method) {
        DriverActions.currentTestName.set(method.getName());
    }

    /**
     * Limpia el ThreadLocal del nombre de test al finalizar cada método.
     */
    @AfterMethod(alwaysRun = true)
    public void clearCurrentTestName() {
        DriverActions.currentTestName.remove();
    }

    /**
     * Cierra el navegador al finalizar todos los tests de la clase.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            log.info("Cerrando WebDriver");
            driver.quit();
            driverThread.remove();
        }
    }
}
