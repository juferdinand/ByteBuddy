package de.gaming.discord.bytebuddy.commands.games.scrapper.steam

import de.gaming.discord.bytebuddy.commands.games.scrapper.entity.StoreScrapperResult
import de.gaming.discord.bytebuddy.commands.games.scrapper.util.AgeVerificationUtil
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class SteamStoreScrapper {

    companion object {
        fun scrap(gameUrl: String): StoreScrapperResult {


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
            AgeVerificationUtil.steam(webDriver)
            WebDriverWait(webDriver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(By.id("appHubAppName"))
            )
            val title = webDriver.findElement(By.id("appHubAppName")).text
            val imageUrl =
                webDriver.findElement(By.className("game_header_image_full")).getAttribute("src")
            val isGameAlreadyAvailable =
                findElement(By.className("game_area_comingsoon"), webDriver) == null
            val price =
                findPrice(isGameAlreadyAvailable, webDriver).replace("Free To Play", "0.00").replace("Free", "0.00")
                    .replace("€", "").replace(",", ".")
            var releaseDateText: String? = null
            if (!isGameAlreadyAvailable) {
                val divComingSoon = webDriver.findElement(By.className("game_area_comingsoon"))
                releaseDateText = divComingSoon.findElement(By.className("content"))
                    .findElement(By.tagName("h1")).text.replace("Planned Release Date: ", "")
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
            if (!isGameAlreadyAvailable && findElement(
                    By.ByClassName("game_area_purchase_game_wrapper"),
                    webDriver
                ) == null
            ) {
                return "-1.00"
            }

            return (findElement(By.className("game_purchase_price"), webDriver)
                ?: findElement(By.className("discount_original_price"), webDriver)
                ?: "-1.00")
        }

        private fun findElement(by: By, webDriver: ChromeDriver): String? {
            return try {
                webDriver.findElement(by).text
            } catch (e: Exception) {
                null
            }
        }
    }
}