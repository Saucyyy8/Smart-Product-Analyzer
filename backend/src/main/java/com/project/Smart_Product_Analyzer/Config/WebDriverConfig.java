package com.project.Smart_Product_Analyzer.Config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
public class WebDriverConfig {

    @Bean(destroyMethod = "quit")
    @org.springframework.context.annotation.Scope("prototype")
    public WebDriver webDriver() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();

        // High-Quality User Agents for Rotation
        String[] userAgents = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:123.0) Gecko/20100101 Firefox/123.0",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0"
        };

        String randomUserAgent = userAgents[(int) (Math.random() * userAgents.length)];

        // Core Headless & Performance
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        // STEALTH: Hide Automation Flags
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
        options.setExperimentalOption("useAutomationExtension", false);

        // STEALTH: User Agent & Headers
        options.addArguments("user-agent=" + randomUserAgent);
        options.addArguments("--lang=en-US");
        options.addArguments("--accept-lang=en-US,en;q=0.9");

        // Disable potentially flagging features
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-images"); // Save bandwidth, but can sometimes trigger bot checks? Usually fine.

        // return new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), options);
        return new ChromeDriver(options);
    }
}
