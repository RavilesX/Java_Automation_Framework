# Java Automation Framework

Generic Selenium UI automation framework — Java 11 + TestNG + Maven.
Ported from a Python/pytest equivalent (comments throughout reference the Python counterparts).

## Stack

| Tool | Version | Role |
|------|---------|------|
| Java | 11 | Language |
| Maven | — | Build / dependency management |
| Selenium | 4.18.1 | Browser automation |
| WebDriverManager | 5.7.0 | Driver binary management (no manual installs) |
| TestNG | 7.9.0 | Test runner, fixtures, assertions |
| ExtentReports | 5.1.1 | HTML test reports (dark theme) |
| Log4j2 | 2.22.1 | Logging (file + console) |
| Jackson | 2.16.1 | JSON test data for DDT |

## Project Structure

```
src/test/java/com/framework/
  testcases/
    BaseTest.java           — base class all test cases extend; manages WebDriver lifecycle
  commonaction/
    CommonActions.java      — logger factory (getLogger() infers caller class name)
    DriverActions.java      — Selenium helpers all Page Objects extend
  listeners/
    TestListener.java       — TestNG ITestListener + ISuiteListener; owns ExtentReports lifecycle
  utils/
    JsonDataProvider.java   — reads JSON → Object[][] for @DataProvider DDT
src/test/resources/
  log4j2.xml                — logging config; output → reports/logs/logFile.log
  testdata/                 — JSON files for data-driven tests
templates/
  TemplatePageObject.java   — copy → src/.../uielements/<Name>Page.java
  TemplateTestCase.java     — copy → src/.../testcases/Test<Flow>.java (simple)
  TemplateTestCaseDDT.java  — copy → src/.../testcases/Test<Flow>.java (DDT)
reports/
  report.html               — ExtentReports output (generated at runtime)
  screenshots/              — auto-captured on failure + manual takeScreenshot()
  logs/logFile.log          — Log4j2 file appender output
testng.xml                  — suite config; defines test groups and browser/url params
pom.xml                     — all dependency versions centralized in <properties>
```

## Key Architecture Decisions

### BaseTest
- `@BeforeClass setup()` initializes WebDriver; supports chrome / firefox / edge via WebDriverManager
- Browser and URL come from testng.xml params OR `-Dbrowser=` / `-Durl=` Maven system properties (system props win)
- `ThreadLocal<WebDriver> driverThread` exposes driver to `TestListener` for failure screenshots
- `ThreadLocal<String> currentTestName` (in DriverActions) set per `@BeforeMethod` so `takeScreenshot()` needs no args

### Page Object pattern
- All Page Objects extend `DriverActions`
- Locators: `private static final By NAME_TYPE` (e.g. `LOGIN_BTN`, `EMAIL_TB`)
- Element getters: `private WebElement getLoginBtn() { return waitForElement(LOGIN_BTN); }`
- Actions return next Page Object when they navigate: `return new DashboardPage(driver);`

### TestListener
- `onStart(ISuite)` initializes `ExtentReports` → `reports/report.html`
- `onTestStart` creates `ExtentTest` node; stored in `DriverActions.extentTest` ThreadLocal
- `onTestFailure` auto-captures screenshot and attaches to report

### DDT (Data-Driven Testing)
- JSON files in `src/test/resources/testdata/`
- Format: top-level keys = test case names; values = flat string maps
- `JsonDataProvider.getData("filename.json")` → `Object[][]` for `@DataProvider`
- Test method receives `Map<String, String> data`

### Logging
- `CommonActions.getLogger()` uses stack trace to name logger after calling class
- Usage: `private static final Logger log = CommonActions.getLogger();` in any class
- Log4j2 silences verbose Selenium/WebDriverManager output (WARN level)

## Running Tests

```bash
# Default (chrome, https://www.example.com, as per testng.xml)
mvn test

# Override browser and URL
mvn test -Dbrowser=firefox -Durl=https://yourapp.com

# Headless chrome: uncomment options.addArguments("--headless") in BaseTest.java
```

## Adding a New Test Flow

1. Create Page Object from template:
   - Copy `templates/TemplatePageObject.java` → `src/test/java/com/framework/uielements/<Name>Page.java`
   - Rename class, define `By` locators, implement action methods

2. Create test case:
   - Simple: copy `TemplateTestCase.java` → `testcases/Test<Flow>.java`
   - DDT: copy `TemplateTestCaseDDT.java` + create `testdata/<flow>.json`

3. Register in `testng.xml`:
   ```xml
   <class name="com.framework.testcases.Test<Flow>"/>
   ```

## TestNG Groups
- `smoke` — fast, critical path tests
- `regression` — full suite

## Locator Naming Convention
Suffix indicates element type: `BTN`, `TB` (textbox), `LBL`, `PNL`, `IMG`, `LNK`, `DDL`, `CHK`
