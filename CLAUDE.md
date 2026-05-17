# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run full suite (browser/url from testng.xml)
mvn test

# Override at runtime
mvn test -Dbrowser=firefox -Durl=https://app.com

# Run a single test class (Surefire still consumes testng.xml — class must be listed there)
mvn test -Dtest=TestGoogleLogo

# Compile only
mvn test-compile

# Clean build artifacts + reports/screenshots
mvn clean
```

Generated artifacts: `reports/report.html` (ExtentReports), `reports/logs/logFile.log`, `reports/screenshots/`.

## Architecture

This is a Java port of a Python/pytest Selenium framework. Code comments throughout map Java constructs to their Python equivalents (`@BeforeClass` ↔ `fixture(scope="class")`, `@DataProvider` ↔ `@ddt + @file_data`, etc.).

### Lifecycle wiring

`BaseTest` (`com.framework.testcases`) owns the WebDriver lifecycle via TestNG annotations. **All configuration methods use `alwaysRun = true`** — required because `testng.xml` uses `<groups>` filters, which would otherwise skip `@BeforeClass`/`@AfterClass` and leave `driver` null.

The driver is exposed to `TestListener` through `BaseTest.driverThread` (ThreadLocal). The listener consumes it to capture failure screenshots without coupling test code to reporting.

### Cross-class ThreadLocals

Three ThreadLocals stitch independent classes together:
- `BaseTest.driverThread` — driver, set in `@BeforeClass`, read by `TestListener.onTestFailure`
- `DriverActions.currentTestName` — test method name, set in `@BeforeMethod`, read by `takeScreenshot()` to name files
- `DriverActions.extentTest` — current ExtentTest node, set in `TestListener.onTestStart`, read by `takeScreenshot()` to attach images to the report

This keeps Page Objects free of reporting/listener dependencies.

### Page Object contract

All Page Objects extend `DriverActions` (inherits `waitForElement`, `waitForUrlChange`, `highlightElement`, `takeScreenshot`, `scrollPage`). Conventions:
- Locators: `private static final By NAME_TYPE` — suffix is `BTN`, `TB` (textbox), `LBL`, `PNL`, `IMG`, `LNK`, `DDL`, `CHK`
- Element accessors: `private WebElement getX() { return waitForElement(X_LOC); }` — never expose raw `WebElement` to tests
- Navigation methods return the next page: `return new CheckoutPage(driver);`

Template files in `templates/` (`TemplatePageObject.java`, `TemplateTestCase.java`, `TemplateTestCaseDDT.java`) are copy-paste scaffolds — not on the build path.

### Parameter resolution chain

Browser/URL resolution in `BaseTest.setup()`:
1. `-Dbrowser=...` / `-Durl=...` CLI flag (only when non-blank)
2. `testng.xml` `<parameter>` — test-level overrides suite-level
3. `@Optional` default in the method signature

**Do not** restore `<systemPropertyVariables>` in `pom.xml`. Maven resolves unset `${browser}` / `${url}` to empty strings, which TestNG then injects ahead of the testng.xml values. Surefire already forwards CLI `-D` flags to the forked JVM natively.

### Logging

`CommonActions.getLogger()` reads the calling class name from the stack trace, so a single static-init line works everywhere:
```java
private static final Logger log = CommonActions.getLogger();
```
Caveat: a `static final` field declared in a superclass logs under the superclass name regardless of which subclass uses it. Test classes that want their own logger name must redeclare the field.

### DDT (Data-Driven Testing)

`JsonDataProvider.getData("file.json")` reads `src/test/resources/testdata/file.json` and returns `Object[][]` for `@DataProvider`. JSON shape: top-level keys = test case names; values = flat string maps. Each test row arrives in the test method as `Map<String, String>`.

## Adding a new test

1. Page Object → `src/test/java/com/framework/uielements/<Name>Page.java` (copy from `templates/TemplatePageObject.java`)
2. Test class → `src/test/java/com/framework/testcases/Test<Flow>.java` extending `BaseTest`
3. Register the class in `testng.xml` under the appropriate `<test>` group block. Per-test URL override goes in that block: `<parameter name="url" value="..."/>`
4. For DDT, add `src/test/resources/testdata/<flow>.json`

## Known gotchas

- `.gitignore` contains `ClAUDE.MD` (typo, case-sensitive on Linux). Real `CLAUDE.md` is not ignored.
- Headless mode requires uncommenting `options.addArguments("--headless")` in `BaseTest.setup()`.
