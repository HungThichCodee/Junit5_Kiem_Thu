import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Demo Kiểm thử YouTube với JUnit 5 - Stable Version")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class YoutubeTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setup() {

        WebDriverManager.chromedriver().setup();

        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @BeforeEach
    void goToHome() {
        driver.get("https://www.youtube.com");
        wait.until(ExpectedConditions.titleContains("YouTube"));
    }

    // ==============================
    // NHÓM 1: GIAO DIỆN
    // ==============================

    @Test
    @Order(1)
    @DisplayName("T1-T5: Kiểm tra các phần tử chính")
    void testCoreElements() {

        assertAll("Trang chủ YouTube",
                () -> assertTrue(driver.getTitle().contains("YouTube")),

                () -> assertTrue(wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ytd-topbar-logo-renderer"))
                ).isDisplayed()),

                () -> assertTrue(wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.name("search_query"))
                ).isDisplayed()),

                () -> assertTrue(wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button[aria-label='Search']"))
                ).isEnabled()),

                () -> assertNotNull(driver.findElement(By.id("guide-button")))
        );
    }

    // ==============================
    // NHÓM 2: SEARCH PARAMETERIZED
    // ==============================

    @ParameterizedTest
    @Order(2)
    @ValueSource(strings = {
            "JUnit 5 tutorial",
            "Java Spring Boot",
            "Selenium 4",
            "HUTECH",
            "Music 2026"
    })
    @DisplayName("T6-T10: Test tìm kiếm nhiều từ khóa")
    void testSearchFunction(String keyword) {

        WebElement searchBox = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("search_query"))
        );

        searchBox.clear();
        searchBox.sendKeys(keyword);

        WebElement searchButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label='Search']"))
        );

        searchButton.click();

        wait.until(ExpectedConditions.titleContains(keyword));

        assertTrue(driver.getTitle().toLowerCase()
                        .contains(keyword.toLowerCase()),
                "Không tìm thấy từ khóa: " + keyword);
    }

    // ==============================
    // NHÓM 3: ĐIỀU HƯỚNG
    // ==============================

    @Test
    @Order(3)
    @DisplayName("T11-T15: Kiểm tra điều hướng Shorts")
    void testNavigation() {

        WebElement shortsMenu = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[@title='Shorts']"))
        );

        shortsMenu.click();

        wait.until(ExpectedConditions.urlContains("shorts"));

        assertTrue(driver.getCurrentUrl().contains("shorts"));
    }

    // ==============================
    // NHÓM 4: VIDEO
    // ==============================

    @Test
    @Order(4)
    @DisplayName("T16-T20: Kiểm tra mở video")
    void testVideoInteraction() {

        // ===== T16: Search trước =====
        WebElement searchBox = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("search_query"))
        );
        searchBox.sendKeys("Selenium tutorial");
        searchBox.sendKeys(Keys.ENTER);

        // ===== T17: Đợi video load =====
        WebElement firstVideo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//a[@id='video-title'])[1]"))
        );

        firstVideo.click();

        // ===== T18: Đợi vào trang watch =====
        wait.until(ExpectedConditions.urlContains("watch"));

        // Hover player để hiện controls
        WebElement player = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("movie_player"))
        );

        Actions actions = new Actions(driver);
        actions.moveToElement(player).perform();

        // Scroll xuống để load comment
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0,800)");

        // ===== T19-T20: Assert 5 điều kiện ổn định =====
        assertAll("Trang Video",

                // 1. URL chứa watch
                () -> assertTrue(driver.getCurrentUrl().contains("watch")),

                // 2. Player tồn tại
                () -> assertTrue(wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("movie_player"))
                ).isDisplayed()),

                // 3. Title video tồn tại (selector chính xác)
                () -> assertTrue(wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//ytd-watch-metadata//h1//yt-formatted-string"))
                ).isDisplayed()),

                // 4. Subscribe button tồn tại
                () -> assertTrue(wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("ytd-subscribe-button-renderer"))
                ).isDisplayed()),

                // 5. Comment section tồn tại
                () -> assertTrue(wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.id("comments"))
                ).isDisplayed())
        );
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}