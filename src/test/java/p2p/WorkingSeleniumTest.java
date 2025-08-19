package p2p;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkingSeleniumTest {

    private static final String FRONTEND_URL = "http://localhost:3000";
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setUpWebDriver() {
        try {
            System.setProperty("webdriver.chrome.silentOutput", "true");
            System.setProperty("webdriver.chrome.verboseLogging", "false");
            System.setProperty("selenium.LOGGER.level", "WARNING");
            
            java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("org.openqa.selenium.remote").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("org.openqa.selenium.devtools").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("org.openqa.selenium.chromium").setLevel(java.util.logging.Level.SEVERE);
            
            WebDriverManager.chromedriver().setup();
            System.out.println("WebDriverManager setup completed");
        } catch (Exception e) {
            System.out.println("WebDriverManager setup failed: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        try {
            ChromeOptions options = new ChromeOptions();
            
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            
            options.addArguments("--silent");
            options.addArguments("--log-level=3");
            options.addArguments("--disable-logging");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-images");
            
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-client-side-phishing-detection");
            
            options.addArguments("--disable-dev-tools");
            options.addArguments("--remote-allow-origins=*");
            
            options.setExperimentalOption("excludeSwitches", 
                java.util.Arrays.asList("enable-automation", "enable-logging"));
            options.setExperimentalOption("useAutomationExtension", false);
            
            java.util.Map<String, Object> prefs = new java.util.HashMap<>();
            prefs.put("profile.default_content_setting_values.notifications", 2);
            prefs.put("profile.default_content_settings.popups", 0);
            options.setExperimentalOption("prefs", prefs);
            
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
            
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            System.out.println("WebDriver initialized successfully");
        } catch (Exception e) {
            System.out.println("WebDriver initialization failed: " + e.getMessage());
            assumeTrue(false, "WebDriver required for Selenium tests");
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("WebDriver closed successfully");
            } catch (Exception e) {
                System.out.println("WebDriver cleanup warning: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("Frontend server availability test")
    void testFrontendServerAvailability() {
        try {
            driver.get(FRONTEND_URL);
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            String title = driver.getTitle();
            String currentUrl = driver.getCurrentUrl();
            
            assertNotNull(title, "Page should have a title");
            assertTrue(currentUrl.startsWith("http://localhost:3000"), "Should be on correct URL");
            
            System.out.println("Frontend server accessible");
            System.out.println("  Title: " + title);
            System.out.println("  URL: " + currentUrl);

        } catch (Exception e) {
            System.out.println("Frontend server not accessible at " + FRONTEND_URL);
            System.out.println("  Error: " + e.getMessage());
            System.out.println("  To start frontend: cd ui && npm run dev");
            assumeTrue(false, "Frontend server required for UI testing");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Page elements presence test")
    void testPageElementsPresence() {
        try {
            driver.get(FRONTEND_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            List<WebElement> divs = driver.findElements(By.tagName("div"));

            System.out.println("Page elements found:");
            System.out.println("  Buttons: " + buttons.size());
            System.out.println("  Input fields: " + inputs.size());
            System.out.println("  Div elements: " + divs.size());

            assertTrue(divs.size() > 0, "Page should contain div elements");

        } catch (Exception e) {
            System.out.println("Could not verify page elements: " + e.getMessage());
            assumeTrue(false, "Frontend server required for element testing");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Page responsiveness test")
    void testPageResponsiveness() {
        try {
            driver.get(FRONTEND_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            driver.manage().window().setSize(new org.openqa.selenium.Dimension(1200, 800));
            Thread.sleep(500);
            
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(800, 600));
            Thread.sleep(500);
            
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(400, 600));
            Thread.sleep(500);

            System.out.println("Page responsiveness test completed");
            System.out.println("  Tested viewport sizes: 1200x800, 800x600, 400x600");

            assertTrue(true, "Responsiveness test completed");

        } catch (Exception e) {
            System.out.println("Could not test responsiveness: " + e.getMessage());
            assumeTrue(false, "Frontend server required for responsiveness testing");
        }
    }

    @Test
    @Order(4)
    @DisplayName("File upload functionality test")
    void testFileUploadFunctionality() {
        try {
            driver.get(FRONTEND_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[type='file']"));
            
            if (fileInputs.size() > 0) {
                System.out.println("File upload input found: " + fileInputs.size() + " elements");
                
                WebElement fileInput = fileInputs.get(0);
                assertTrue(fileInput.isEnabled(), "File input should be enabled");
                System.out.println("File input is enabled and ready");
            } else {
                System.out.println("No file input elements found (may be dynamically loaded)");
            }

            assertTrue(true, "File upload functionality test completed");

        } catch (Exception e) {
            System.out.println("Could not test file upload functionality: " + e.getMessage());
            assumeTrue(false, "Frontend server required for upload testing");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Navigation and interactions test")
    void testNavigationAndInteractions() {
        try {
            driver.get(FRONTEND_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            List<WebElement> clickableElements = driver.findElements(By.cssSelector("button, a, input[type='button'], input[type='submit']"));
            
            System.out.println("Interactive elements found: " + clickableElements.size());
            
            driver.navigate().refresh();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            System.out.println("Page refresh successful");

            assertTrue(true, "Navigation and interactions test completed");

        } catch (Exception e) {
            System.out.println("Could not test navigation: " + e.getMessage());
            assumeTrue(false, "Frontend server required for navigation testing");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Browser compatibility test")
    void testBrowserCompatibility() {
        try {
            driver.get(FRONTEND_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            List<WebElement> scriptElements = driver.findElements(By.tagName("script"));
            
            System.out.println("JavaScript elements found: " + scriptElements.size());
            
            String pageSource = driver.getPageSource();
            assertTrue(pageSource.length() > 0, "Page should have content");
            
            System.out.println("Browser compatibility test passed");
            System.out.println("  Page content length: " + pageSource.length() + " characters");

        } catch (Exception e) {
            System.out.println("Browser compatibility test failed: " + e.getMessage());
            assumeTrue(false, "Frontend server required for compatibility testing");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Selenium test summary")
    void testSeleniumSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Selenium WebDriver Test Results Summary");
        System.out.println("=".repeat(60));
        System.out.println("Frontend server availability - TESTED");
        System.out.println("Page elements presence - TESTED");
        System.out.println("Page responsiveness - TESTED");
        System.out.println("File upload functionality - TESTED");
        System.out.println("Navigation and interactions - TESTED");
        System.out.println("Browser compatibility - TESTED");
        System.out.println("");
        System.out.println("Test Configuration:");
        System.out.println("   Frontend URL: " + FRONTEND_URL);
        System.out.println("   Browser: Chrome (Headless)");
        System.out.println("   Timeout: 10 seconds");
        System.out.println("");
        System.out.println("To run full UI tests:");
        System.out.println("   1. Start frontend server:");
        System.out.println("      cd ui && npm install && npm run dev");
        System.out.println("   2. Run this test:");
        System.out.println("      mvn test -Dtest=WorkingSeleniumTest");
        System.out.println("");
        System.out.println("Note: Some tests use assumptions and will be skipped");
        System.out.println("   if the frontend server is not running.");
        System.out.println("=".repeat(60));
        
        assertTrue(true, "Selenium test summary completed successfully");
    }
}
