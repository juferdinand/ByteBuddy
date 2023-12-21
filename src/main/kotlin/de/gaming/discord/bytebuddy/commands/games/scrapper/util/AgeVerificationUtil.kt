package de.gaming.discord.bytebuddy.commands.games.scrapper.util

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration
import java.util.concurrent.TimeUnit

class AgeVerificationUtil {

    companion object {

        fun steam(webDriver: ChromeDriver) {
            try {
                // Überprüfen, ob das Altersüberprüfungsformular vorhanden ist
                val ageGate = webDriver.findElement(By.className("agegate_birthday_selector"))
                if (ageGate != null) {
                    // Wählen Sie einen Tag, Monat und Jahr aus den Dropdown-Menüs
                    val ageDay = Select(webDriver.findElement(By.id("ageDay")))
                    ageDay.selectByValue("1")
                    val ageMonth = Select(webDriver.findElement(By.id("ageMonth")))
                    ageMonth.selectByValue("January")
                    val ageYear = Select(webDriver.findElement(By.id("ageYear")))
                    ageYear.selectByValue("1900")

                    // Klicken Sie auf die Schaltfläche "Eingeben"
                    val ageSubmit =
                        webDriver.findElement(By.id("view_product_page_btn"))
                    ageSubmit.click()

                    // Warten Sie, bis die Seite geladen ist
                    WebDriverWait(webDriver, Duration.ofSeconds(10)).until(
                        ExpectedConditions.invisibilityOfElementLocated(By.id("agegate_birthday_selector"))
                    )
                }
            } catch (e: NoSuchElementException) {
                // Das Altersüberprüfungsformular ist nicht vorhanden, nichts zu tun
            }
        }

        fun epicgames(webDriver: ChromeDriver) {
            try {
                // Überprüfen, ob das Altersüberprüfungsformular vorhanden ist
                Thread.sleep(TimeUnit.SECONDS.toMillis(5))

                // Warte, bis die Seite geladen ist (optional: WebDriverWait verwenden)
                for ((x, selectElement) in webDriver.findElements(By.className("css-6jt4kr"))
                    .withIndex()) {
                    when (x) {
                        0, 1 -> selectDropdownValue(
                            selectElement,
                            "01"
                        )
                        2 -> selectDropdownValue(
                            selectElement,
                            "1900"
                        )
                    }
                }
                // Ersetze "Jahr" mit einem spezifischen Wert

                // Weitere Aktionen ...
                webDriver.findElement(By.className("css-1a6we1t")).click()
                // Warten Sie, bis die Seite geladen ist
                WebDriverWait(webDriver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("css-1tjsjc"))
                )
            } catch (e: NoSuchElementException) {
                // Das Altersüberprüfungsformular ist nicht vorhanden, nichts zu tun
            }
        }

        private fun selectDropdownValue(selectElement: WebElement, value: String) {

            selectElement.click() // Öffne das Dropdown-Menü

            // Wähle einen Wert aus dem Dropdown-Menü

            val option =
                selectElement.findElements(By.className("css-23anny"))
                    .first { it.text == value }
            option.click()
        }
    }
}