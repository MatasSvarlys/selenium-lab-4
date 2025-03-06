

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class WebShopTests {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://demowebshop.tricentis.com/";
    private static final String PASSWORD = "Password123";
    private String EMAIL;
    
    public static void createNewUser(String email) {
        WebDriver driverSetup = new ChromeDriver();
        try {
            WebDriverWait waitSetup = new WebDriverWait(driverSetup, Duration.ofSeconds(10));
            
            driverSetup.get(BASE_URL);
            
            driverSetup.findElement(By.linkText("Log in")).click();
            
            driverSetup.findElement(By.linkText("Register")).click();
            
            driverSetup.findElement(By.id("gender-male")).click();
            driverSetup.findElement(By.id("FirstName")).sendKeys("John");
            driverSetup.findElement(By.id("LastName")).sendKeys("Doe");
            driverSetup.findElement(By.id("Email")).sendKeys(email);
            driverSetup.findElement(By.id("Password")).sendKeys(PASSWORD);
            driverSetup.findElement(By.id("ConfirmPassword")).sendKeys(PASSWORD);
            
            driverSetup.findElement(By.id("register-button")).click();
            
            //Wait for registration to complete
            waitSetup.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='result' and contains(text(), 'completed')]")));
                
                driverSetup.findElement(By.xpath("//input[@value='Continue']")).click();
                
                System.out.println("User created successfully: " + email);
            } finally {
                driverSetup.quit();
            }
        }
        
        @Before
        public void setUp() {
        this.EMAIL = "user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        createNewUser(this.EMAIL);
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }
    
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    public void testShoppingCartWithDataFile1() throws IOException {
        performShoppingTest("data1.txt");
    }

    
    @Test
    public void testShoppingCartWithDataFile2() throws IOException {
        performShoppingTest("data2.txt");
    }
    
    private void performShoppingTest(String dataFile) throws IOException {
        
        driver.get(BASE_URL);
        
        driver.findElement(By.linkText("Log in")).click();
        
        driver.findElement(By.id("Email")).sendKeys(EMAIL);
        driver.findElement(By.id("Password")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//input[@value='Log in']")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[@class='account' and contains(text(), '" + EMAIL + "')]")));
        
        driver.findElement(By.xpath("//a[contains(text(), 'Digital downloads')]")).click();  
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String productName;
            while ((productName = reader.readLine()) != null) {
                System.out.println("Adding product: " + productName);
                if (!productName.trim().isEmpty()) {
                    // Get all products
                    List<WebElement> products = driver.findElements(
                            By.xpath("//div[@class='item-box']//h2[@class='product-title']/a"));
                    
                    //Check through all products to find the one with the matching name
                    for (WebElement product : products) {
                        if (product.getText().trim().equals(productName.trim())) {
                            product.findElement(
                                    By.xpath("./ancestor::div[@class='item-box']//input[contains(@class, 'button-2') and contains(@class, 'product-box-add-to-cart-button')]"))
                                    .click();
                                    
                            //Wait for confirmation notification to appear
                            wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[@id='bar-notification' and @class='bar-notification success']")));
                        
                            //Wait for confirmation notification to disappear
                            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                                    By.xpath("//div[@id='bar-notification' and @class='bar-notification success']")));
                            
                            break;
                        }
                    }
                }
            }
        }
        
        driver.findElement(By.linkText("Shopping cart")).click();
        
        driver.findElement(By.id("termsofservice")).click();
        driver.findElement(By.id("checkout")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billing-buttons-container")));
        List<WebElement> addressDropdown = driver.findElements(By.id("billing-address-select"));
        if (!addressDropdown.isEmpty() && addressDropdown.get(0).isDisplayed()) {
            //Select the first address option
            Select select = new Select(addressDropdown.get(0));
            select.selectByIndex(1);
        } else {
            //Fill new address
            driver.findElement(By.id("BillingNewAddress_CountryId")).click();
            new Select(driver.findElement(By.id("BillingNewAddress_CountryId"))).selectByVisibleText("United States");
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("#BillingNewAddress_StateProvinceId option"), 1));
            driver.findElement(By.id("BillingNewAddress_StateProvinceId")).click();
            new Select(driver.findElement(By.id("BillingNewAddress_StateProvinceId"))).selectByIndex(1);
            driver.findElement(By.id("BillingNewAddress_City")).sendKeys("Test City");
            driver.findElement(By.id("BillingNewAddress_Address1")).sendKeys("123 Test St");
            driver.findElement(By.id("BillingNewAddress_ZipPostalCode")).sendKeys("12345");
            driver.findElement(By.id("BillingNewAddress_PhoneNumber")).sendKeys("1234567890");
        }
        
        driver.findElement(
            By.xpath("//div[@id='billing-buttons-container']/input[@value='Continue']"))
            .click();
        
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='payment-method-buttons-container']/input[@value='Continue']")));
        driver.findElement(By.xpath("//div[@id='payment-method-buttons-container']/input[@value='Continue']")).click();
        
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='payment-info-buttons-container']/input[@value='Continue']")));
        driver.findElement(By.xpath("//div[@id='payment-info-buttons-container']/input[@value='Continue']")).click();
        
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='confirm-order-buttons-container']/input[@value='Confirm']")));
        driver.findElement(By.xpath("//div[@id='confirm-order-buttons-container']/input[@value='Confirm']")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='section order-completed']//div[@class='title']/strong[contains(text(), 'Your order has been successfully processed!')]")));
        
        System.out.println("Test with " + dataFile + " completed successfully");
    }
}