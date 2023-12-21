package de.gaming.discord.bytebuddy.commands.games.scrapper.epic

import de.gaming.discord.bytebuddy.commands.games.scrapper.entity.StoreScrapperResult
import de.gaming.discord.bytebuddy.commands.games.scrapper.util.AgeVerificationUtil
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions

class EpicgamesStoreScrapper {
    companion object {
        fun scrap(gameUrl: String): StoreScrapperResult {

            WebDriverManager.chromedriver().setup()

            val options = ChromeOptions()
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36");
            // WebDriver-Flag entfernen
            options.setExperimentalOption("excludeSwitches", Array(1) { "enable-automation" })
            options.addArguments("--disable-blink-features=AutomationControlled")
            options.addArguments("--verbose")
            options.addArguments("--disable-gpu")
            options.addArguments("--no-sandbox")
            options.addArguments("--disable-dev-shm-usage")
            options.addArguments("--lang=de")
            options.addArguments("--whitelisted-ips")
            options.addArguments("--headless")

            val service = ChromeDriverService.Builder()
                .usingAnyFreePort()
                .build()

            WebDriverManager.chromedriver().setup()

            val webDriver = ChromeDriver(service, options)
            webDriver.get(gameUrl)
            webDriver.manage().window().size = Dimension(1920, 1080)
            AgeVerificationUtil.epicgames(webDriver)
            val title = webDriver.findElement(By.className("css-1mzagbj")).text
            val imageUrl =
                webDriver.findElement(By.className("css-7i770w")).getAttribute("src")
            val isGameAlreadyAvailable =
                findElement(By.className("css-1rcvlh3"), webDriver) == null
            val price =
                findPrice(isGameAlreadyAvailable, webDriver).replace("Gratis", "0.00")
                    .replace("€", "").replace(",", ".").replace(" ", "")
            var releaseDateText: String? = null
            if (!isGameAlreadyAvailable) {
                val divElement = webDriver.findElements(By.className("css-10mlqmn")).first {
                    val text = it.findElement(By.className("css-1o0y1dn")).findElement(By.tagName("span")).text
                    text.equals("Verfügbar", true) || text.equals("Veröffentlichungsdatum")
                }
                releaseDateText = divElement.findElement(By.className("css-btns76")).text
            }
            webDriver.close()
            webDriver.quit()
            return StoreScrapperResult(
                title,
                price.toBigDecimal(),
                imageUrl,
                releaseDateText
            )
        }

        private fun findPrice(isGameAlreadyAvailable: Boolean, webDriver: ChromeDriver): String {
            if (!isGameAlreadyAvailable) {
                return "-1.00"
            }
            val divElement = webDriver.findElements(By.className("css-119zqif"))?.first {
                it.text.contains("€") || it.text.contains("Gratis")
            }
            return (divElement?.text
                ?: findElement(By.className("css-4jky3p"), webDriver)?.text ?: "-1.00")
        }

        private fun findElement(by: By, webDriver: ChromeDriver): WebElement? {
            return try {
                webDriver.findElement(by)
            } catch (e: Exception) {
                null
            }
        }
    }
}