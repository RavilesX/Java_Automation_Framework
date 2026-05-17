package com.framework.listeners;

import com.framework.commonaction.DriverActions;
import com.framework.report.ReportRenderer;
import com.framework.report.RunData;
import com.framework.report.TestResultData;
import com.framework.testcases.BaseTest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listener de TestNG que recolecta resultados de la suite y renderiza
 * el reporte HTML custom al finalizar.
 *
 * Hooks:
 *   onStart(ISuite)        — inicializa el listado de resultados
 *   onTestStart            — crea TestResultData, lo registra en DriverActions.currentTest
 *   onTestSuccess/Failure  — setea status, error, duración
 *   onFinish(ISuite)       — invoca ReportRenderer.render()
 *
 * Registrado en testng.xml bajo <listeners>.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter STAMP_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy · HH:mm");

    private final List<TestResultData> results = new ArrayList<>();
    private final AtomicInteger idSeq = new AtomicInteger(0);
    private final RunData run = new RunData();

    // ----------------------------------------------------------------
    // ISuiteListener
    // ----------------------------------------------------------------

    @Override
    public void onStart(ISuite suite) {
        run.meta.suiteName  = suite.getName();
        run.meta.executedAt = LocalDateTime.now().format(STAMP_FMT);
        run.meta.framework  = "Selenium 4 · TestNG · Java 11";
        run.meta.buildId    = "build #" + System.currentTimeMillis() / 1000 % 100000;
        run.meta.envLabel   = "QA · Web";
        run.meta.version    = "v1.0.0";
        run.meta.year       = String.valueOf(Year.now().getValue());

        run.meta.environment.put("Framework",  "Selenium 4");
        run.meta.environment.put("Runner",     "TestNG · Java 11");
        run.meta.environment.put("Ambiente",   "QA");
        run.meta.environment.put("Sistema",    System.getProperty("os.name") + " · " + System.getProperty("os.arch"));
        run.meta.environment.put("Java",       System.getProperty("java.version"));
        run.meta.environment.put("User",       System.getProperty("user.name"));
        run.meta.environment.put("Inicio",     LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        run.meta.environment.put("Suite",      suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        run.tests = results;
        run.meta.environment.put("Fin",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        new ReportRenderer().render(run);
    }

    // ----------------------------------------------------------------
    // ITestListener
    // ----------------------------------------------------------------

    @Override
    public void onTestStart(ITestResult result) {
        TestResultData t = new TestResultData();
        t.id          = idSeq.incrementAndGet();
        t.className   = result.getTestClass().getRealClass().getSimpleName();
        String desc   = result.getMethod().getDescription();
        t.name        = (desc != null && !desc.isEmpty()) ? desc : result.getMethod().getMethodName();
        t.time        = LocalDateTime.now().format(TIME_FMT);
        t.startMillis = System.currentTimeMillis();
        t.status      = "skip"; // default; se sobreescribe en success/failure
        t.tags        = new ArrayList<>(Arrays.asList(result.getMethod().getGroups()));

        DriverActions.currentTest.set(t);
        results.add(t);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TestResultData t = DriverActions.currentTest.get();
        if (t == null) return;
        t.status = "pass";
        t.dur    = (System.currentTimeMillis() - t.startMillis) / 1000.0;
        DriverActions.logEvent("pass", "Test pasado correctamente");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TestResultData t = DriverActions.currentTest.get();
        if (t == null) return;
        t.status = "fail";
        t.dur    = (System.currentTimeMillis() - t.startMillis) / 1000.0;

        Throwable thr = result.getThrowable();
        if (thr != null) {
            StringWriter sw = new StringWriter();
            thr.printStackTrace(new PrintWriter(sw));
            t.err = sw.toString();
            DriverActions.logEvent("fail", escapeHtml(thr.getMessage()));
        }

        // Captura screenshot automático en fallo
        WebDriver driver = BaseTest.driverThread.get();
        if (driver != null) {
            captureFailureScreenshot(driver, t, result.getMethod().getMethodName());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TestResultData t = DriverActions.currentTest.get();
        if (t == null) return;
        t.status = "skip";
        t.dur    = 0.0;
        String msg = result.getThrowable() != null
                ? result.getThrowable().getMessage()
                : "Test omitido";
        DriverActions.logEvent("info", "Skipped: " + escapeHtml(msg));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // No aplica.
    }

    // ----------------------------------------------------------------
    // helpers
    // ----------------------------------------------------------------

    private void captureFailureScreenshot(WebDriver driver, TestResultData t, String methodName) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename  = methodName + "_" + timestamp + "_FAIL.png";
            Path absolutePath = Paths.get("reports", "screenshots", filename);
            Files.createDirectories(absolutePath.getParent());
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(absolutePath, screenshot);

            // Ruta relativa al HTML (que vive en reports/report.html).
            t.screenshotPath = "screenshots/" + filename;
        } catch (IOException e) {
            DriverActions.logEvent("info", "No se pudo capturar screenshot: " + escapeHtml(e.getMessage()));
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
