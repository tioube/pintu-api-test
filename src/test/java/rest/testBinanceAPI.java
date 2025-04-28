package rest;

import configs.TestMasterConfigurations;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Binance REST API Tests")
@Feature("Binance API")
public class testBinanceAPI extends TestMasterConfigurations {
    
    private String testSymbol = "BNBUSDT";

    @Test(description = "Test fetching order book via Binance API")
    @Severity(SeverityLevel.NORMAL)
    @Story("Fetch Order Book")
    @Description("This test fetches the order book for a symbol and verifies the response structure.")
    public void testFetchOrderBook() {
        Allure.step("Fetching order book for " + testSymbol, () -> {
            // Fetch order book with limit of 5 entries
            Response response = OrderClient.fetchOrderBook(testSymbol, 5);
            
            // Verify the response
            Assert.assertEquals(response.getStatusCode(), 200, "Order book should be fetched successfully");
            
            // Verify the response structure
            Assert.assertTrue(response.jsonPath().getList("bids").size() <= 5, "Should have at most 5 bid entries");
            Assert.assertTrue(response.jsonPath().getList("asks").size() <= 5, "Should have at most 5 ask entries");
            
            // Attach order book details to the Allure report
            Allure.addAttachment("Order Book Details", "application/json", response.asString());
        });
    }

    @Test(description = "Test fetching open orders via Binance API")
    @Severity(SeverityLevel.NORMAL)
    @Story("Fetch Open Orders")
    @Description("This test fetches open orders for a symbol and verifies the response.")
    public void testFetchOpenOrders() {
        Allure.step("Fetching open orders for " + testSymbol, () -> {
            // Fetch open orders
            Response response = OrderClient.fetchOpenOrders(testSymbol);
            
            // Verify the response
            Assert.assertEquals(response.getStatusCode(), 200, "Open orders should be fetched successfully");
            
            // Attach open orders details to the Allure report
            Allure.addAttachment("Open Orders Details", "application/json", response.asString());
        });
    }

    @Test(description = "Test fetching trade history via Binance API")
    @Severity(SeverityLevel.NORMAL)
    @Story("Fetch Trade History")
    @Description("This test fetches trade history for a symbol and verifies the response.")
    public void testFetchTradeHistory() {
        Allure.step("Fetching trade history for " + testSymbol, () -> {
            // Fetch trade history with limit of 10 entries
            Response response = OrderClient.fetchTradeHistory(testSymbol, 10);
            
            // Verify the response
            Assert.assertEquals(response.getStatusCode(), 200, "Trade history should be fetched successfully");
            
            // Attach trade history details to the Allure report
            Allure.addAttachment("Trade History Details", "application/json", response.asString());
        });
    }

    @Test(description = "Test fetching account balance via Binance API")
    @Severity(SeverityLevel.NORMAL)
    @Story("Fetch Account Balance")
    @Description("This test fetches account balance and verifies the response.")
    public void testFetchAccountBalance() {
        Allure.step("Fetching account balance", () -> {
            // Fetch account balance
            Response response = OrderClient.fetchAccountBalance();
            
            // Verify the response
            Assert.assertEquals(response.getStatusCode(), 200, "Account balance should be fetched successfully");
            
            // Verify the response structure
            Assert.assertNotNull(response.jsonPath().getList("balances"), "Response should contain balances");
            
            // Attach account balance details to the Allure report
            Allure.addAttachment("Account Balance Details", "application/json", response.asString());
        });
    }
}