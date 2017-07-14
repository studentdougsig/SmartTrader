package com.stocktrak;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.stocktrak.transactional.AccountCash;
import com.stocktrak.transactional.HoldingInfo;
import com.stocktrak.transactional.Transaction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Queue;


/**
 * Created by Douglas on 2/13/2015.
 */
public class StockTrackerProcess extends Thread {
    private Queue<Transaction> transactionQueue = AnalysisProcess.transactionQueue;
    private AccountCash accountCash = AnalysisProcess.accountCash;
    private ReadyToTradeListener readyToTradeListener;
    private ReadyToUpdatePortfolioListener readyToUpdatePortfolioListener;
    private WebDriver driver;

    public StockTrackerProcess(ReadyToTradeListener readyToTradeListener, ReadyToUpdatePortfolioListener readyToUpdatePortfolioListener) {
        this.readyToTradeListener = readyToTradeListener;
        this.readyToUpdatePortfolioListener = readyToUpdatePortfolioListener;
    }

    private static final String LOG_TAG = "StockTrackerProcess";
    private static final String USERNAME = "qcampbell3";
    private static final String PASSWORD = "Dresser5";

    @Override
    public void run() {
        super.run();
        driver = new FirefoxDriver();
        login();
        if(updatePortfolioValue() && updateHoldings()) {
            readyToTradeListener.readyToTrade();
        }
        boolean stop = false;
        while(!stop) {
            while (!transactionQueue.isEmpty()) {
                try {
                    Transaction transaction = transactionQueue.remove();
                    makeTransaction(transaction);
                } catch (ElementNotFoundException e) {
                    log(e);
                }
            }
            try {
                sleep(1000);
            } catch(InterruptedException e) {
                log(e);
                stop = true;
            }
        }
        driver.close();
        driver.quit();
    }

    private boolean login() {
        try {
            driver.navigate().to("http://stocktrak.com");
            WebDriverWait wait = new WebDriverWait(driver, 4000);
            wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("Login1_Login"))));
            WebElement usernameField = driver.findElement(By.id("Login1_UserName"));
            WebElement passwordField = driver.findElement(By.id("Login1_Password"));
            WebElement submitLogin = driver.findElement(By.id("Login1_Login"));
            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            submitLogin.click();
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    private boolean logout() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 4000);
            wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("Header1_btnLogout"))));
            WebElement logoutButton = driver.findElement(By.id("Header1_btnLogout"));
            logoutButton.click();
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    private void refreshCookie() {
        logout();
        login();
    }

    public boolean updatePortfolioValue() throws ElementNotFoundException {
        String url = "http://stocktrak.com/private/account/portfolio.aspx";
        if(!driver.getCurrentUrl().equals(url)) {
            driver.navigate().to(url);
        }
        Double portfolioValue = null;
        String portfolioValueXPath = "/html/body/form[@id='form1']" +
                "/div[@id='wrapper']/div[@class='inner-wrapper']" +
                "/div[@class='content bg-pageContent']/div[@class='index']/div[@class='left-col']" +
                "/div[@class='introduction-box']/div[@class='content-left']/div[@class='snapshot']" +
                "/table[@class='data']/tbody/tr[2]/td[2]";
        try {
            WebDriverWait wait = new WebDriverWait(driver, 4000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(portfolioValueXPath)));
            portfolioValue = Double.parseDouble(driver.findElement(By.xpath(portfolioValueXPath))
                    .getText().replace("$", "").replace(",", ""));
        } catch(Exception e) {
            log(e);
        }
        log("portfolioValue="+portfolioValue);
        AnalysisProcess.accountCash.setCurrentCash(portfolioValue);
        AnalysisProcess.accountCash.setExpectedCash(portfolioValue);
        return portfolioValue != null;
    }

    public boolean updateHoldings() throws ElementNotFoundException {
        boolean success = true;
        String url = "http://stocktrak.com/private/account/openpositions.aspx";
        if(!driver.getCurrentUrl().equals(url)) {
            driver.navigate().to(url);
        }
        WebDriverWait wait = new WebDriverWait(driver, 4000);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tOpenPositions")));
        } catch(Exception e) {
            return false;
        }
        String xPathToBody = "//table[@id='tOpenPositions']/tbody";
        boolean hasMore = true;
        int i = 2;
        while(hasMore) {
            try {
                String currentXPath = xPathToBody + "/tr[" + i + "]";
                int qty = Integer.parseInt(driver.findElement(By.xpath(currentXPath + "/td[5]")).getText());
                String priceString = driver.findElement(By.xpath(currentXPath + "/td[8]")).getText();

                double pricePaid = Double.parseDouble(priceString);
                double totalSale = qty * pricePaid;
                String symbol = driver.findElement(By.xpath(currentXPath + "/td[2]")).getText();
                AnalysisProcess.holdings.put(symbol, new HoldingInfo(totalSale, qty, pricePaid));
                log(AnalysisProcess.holdings);
            } catch(Exception e) {
                hasMore = false;
                log("no more");
            }
            i++;
        }
        return success;
    }

    private void makeTransaction(Transaction transaction) throws ElementNotFoundException {
        if(transaction.getType().equals(Transaction.Type.REFRESH)) {
            refreshCookie();
            return;
        }
        driver.navigate().to("http://stocktrak.com/private/trading/equities.aspx");

        WebDriverWait wait = new WebDriverWait(driver, 4000);
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("ContentPlaceHolder1_Equities_tbSymbol"))));

        WebElement action = driver.findElement(By.id("ContentPlaceHolder1_Equities_ddlOrderSides"));
        action.sendKeys(transaction.getType().toString());

        WebElement symbolField = driver.findElement(By.id("ContentPlaceHolder1_Equities_tbSymbol"));
        symbolField.sendKeys(transaction.getSymbol());
        symbolField.sendKeys("\t");
        wait = new WebDriverWait(driver, 4000);
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("ContentPlaceHolder1_Equities_pPriceResult"))));

        WebElement quantityField = driver.findElement(By.id("ContentPlaceHolder1_Equities_tbQuantity"));
        quantityField.sendKeys(""+transaction.getQuantity());

        WebElement previewOrderButton = driver.findElement(By.id("ContentPlaceHolder1_Equities_btnPreviewOrder"));
        previewOrderButton.click();
        wait = new WebDriverWait(driver, 4000);
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("ContentPlaceHolder1_Equities_btnPlaceOrder"))));

        Double transactionDollarAmount = Double.parseDouble(
                driver.findElement(By.xpath("/html/body/form[@id='form1']" +
                    "/div[@id='wrapper']/div[@class='inner-wrapper']" +
                    "/div[@class='content bg-pageContent']/div[@class='index']/div[@class='left-col']" +
                    "/div[@class='introduction-box']/div[2]/div[@id='ContentPlaceHolder1_Equities_TradePanel']" +
                    "/div[@id='ContentPlaceHolder1_Equities_UpdatePanel1']/table[@class='data']/tbody/tr[2]/td[7]"))
                .getText().replace(",", ""));
        WebElement placeOrderButton = driver.findElement(By.id("ContentPlaceHolder1_Equities_btnPlaceOrder"));
        placeOrderButton.click();

        if(transaction.getType().equals(Transaction.Type.BUY)) {
            accountCash.increaseCurrentBy(transactionDollarAmount);
        } else if(transaction.getType().equals(Transaction.Type.SELL)) {
            accountCash.increaseCurrentBy(transactionDollarAmount);
        }
    }

    public void log(Object str) {
        System.out.println(LOG_TAG + ": " + (str != null ? str.toString() : null));
    }
}
