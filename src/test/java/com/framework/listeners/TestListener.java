package com.framework.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.framework.commonaction.DriverActions;
import com.framework.testcases.BaseTest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Listener de TestNG que gestiona el ciclo de vida de ExtentReports y captura
 * screenshots automáticos ante fallos de test.
 *
 * Equivalente a los hooks de conftest.py del framework Python:
 *   - onStart(ISuite)        ↔ pytest_configure (inicializa el reporte)
 *   - onFinish(ISuite)       ↔ pytest_sessionfinish (escribe el reporte final)
 *   - onTestStart            ↔ pytest_runtest_call (crea el nodo de test en el reporte)
 *   - onTestFailure          ↔ pytest_runtest_makereport (screenshot en fallo)
 *
 * Registrado en testng.xml bajo <listeners>.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static ExtentReports extent;

    // ----------------------------------------------------------------
    // Ciclo de vida de la suite (ISuiteListener)
    // ----------------------------------------------------------------

    @Override
    public void onStart(ISuite suite) {
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/report.html");
        spark.config().setDocumentTitle("Reporte de Automatización - Framework Selenium Java");
        spark.config().setReportName("Automation Report");
        spark.config().setTheme(Theme.DARK);

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Framework", "Selenium 4 + TestNG + Java 11");
        extent.setSystemInfo("Ambiente",  "QA");
        extent.setSystemInfo("Fecha",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Override
    public void onFinish(ISuite suite) {
        if (extent != null) {
            extent.flush();
        }
    }

    // ----------------------------------------------------------------
    // Ciclo de vida de cada test (ITestListener)
    // ----------------------------------------------------------------

    @Override
    public void onTestStart(ITestResult result) {
        String methodName   = result.getMethod().getMethodName();
        String description  = result.getMethod().getDescription();
        String displayName  = (description != null && !description.isEmpty()) ? description : methodName;

        ExtentTest test = extent.createTest(displayName);
        DriverActions.extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = DriverActions.extentTest.get();
        if (test != null) {
            test.pass("Test pasado correctamente");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = DriverActions.extentTest.get();
        if (test == null) return;

        test.fail(result.getThrowable());

        // Captura screenshot automático en fallo, igual que el hook
        // pytest_runtest_makereport de conftest.py
        WebDriver driver = BaseTest.driverThread.get();
        if (driver != null) {
            try {
                String testName = result.getMethod().getMethodName();
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path screenshotPath = Paths.get("reports", "screenshots",
                        testName + "_" + timestamp + "_FAIL.png");
                Files.createDirectories(screenshotPath.getParent());
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Files.write(screenshotPath, screenshot);
                test.addScreenCaptureFromPath(screenshotPath.toString());
            } catch (IOException e) {
                test.warning("No se pudo capturar screenshot en fallo: " + e.getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = DriverActions.extentTest.get();
        if (test != null) {
            test.skip(result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "Test omitido");
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // No aplica en este framework
    }
}
