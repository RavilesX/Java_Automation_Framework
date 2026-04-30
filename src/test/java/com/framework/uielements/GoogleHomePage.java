package com.framework.uielements;

import com.framework.commonaction.CommonActions;
import com.framework.commonaction.DriverActions;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for Google homepage (google.com.mx).
 */
public class GoogleHomePage extends DriverActions {

    private static final Logger log = CommonActions.getLogger();

    // Main logo: <img alt="Google"> present in both standard logo and Doodles
    private static final By LOGO_IMG = By.className("lnXdpd");
    // cssSelector("img[alt='Google']");

    public GoogleHomePage(WebDriver driver) {
        super(driver);
    }

    private WebElement getLogoImg() { return waitForElement(LOGO_IMG); }

    /**
     * Validates the main Google logo is present and displayed.
     *
     * @return true if logo element exists and is visible.
     */
    public boolean isLogoDisplayed() {
        try {
            WebElement logo = getLogoImg();
            highlightElement(logo, "yellow", 3);
            log.info("Google logo found: " + logo.getAttribute("src"));
            return logo.isDisplayed();
        } catch (Exception e) {
            log.error("Google logo not found: " + e.getMessage());
            return false;
        }
    }
}
