package com.framework.testcases;

import com.framework.uielements.GoogleHomePage;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * Validates the main logo on google.com.mx.
 */
public class TestGoogleLogo extends BaseTest {

    @Test(groups      = {"smoke"},
          description = "Validate Google main logo is present on google.com.mx")
    public void testGoogleLogoIsDisplayed() {
        log.info(">>> START: testGoogleLogoIsDisplayed");

        GoogleHomePage googlePage = new GoogleHomePage(driver);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(googlePage.isLogoDisplayed(),
                "Google main logo should be visible on the homepage");
        softAssert.assertAll();

        log.info(">>> END:   testGoogleLogoIsDisplayed");
    }
}
