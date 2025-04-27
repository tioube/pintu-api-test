package rest;

import configs.TestMasterConfigurations;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

/**
 * Test class for Binance Order API
 */
@Epic("Binance REST API Tests")
@Feature("Order API")
public class testOrderAPI extends TestMasterConfigurations {
    
    private static String testSymbol = "BNBUSDT"; // Using BNBUSDT as per the curl example
    private static Response orderResponse;
    private static long orderId;
    
    /**
     * Test to place a market order on Binance
     * 
     * This test verifies that we can successfully place a market order using the Binance API.
     * It places a market order, verifies the order was created successfully, and then gets the order status.
     */
    @Test(description = "Test placing a market order via Binance API")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Place market order and verify order details")
    @Description("This test places a market order on Binance and verifies the order details.")
    public void testPlaceMarketOrder() {
        // Use a small quantity for testing
        String quantity = "1.0"; // 1.0 BNB as per the curl example
        
        Allure.step("Placing market BUY order for " + testSymbol + ": " + quantity, () -> {
            // Place a market order
            orderResponse = OrderClient.placeMarketOrder(testSymbol, "BUY", quantity);
            
            // Verify the order was created successfully
            Assert.assertEquals(orderResponse.getStatusCode(), 200, "Order should be created successfully");
            
            // Extract the order ID
            orderId = orderResponse.jsonPath().getLong("orderId");
            
            // Attach order details to the Allure report
            Allure.addAttachment("Created Order Details", "application/json", orderResponse.asString());
        });
        
        Allure.step("Verifying order status", () -> {
            // Get the order status
            Response statusResponse = OrderClient.getOrderStatus(testSymbol, orderId);
            
            // Verify the order status
            Assert.assertEquals(statusResponse.getStatusCode(), 200, "Order status should be retrieved successfully");
            Assert.assertEquals(statusResponse.jsonPath().getString("symbol"), testSymbol, "Order symbol should match");
            Assert.assertEquals(statusResponse.jsonPath().getLong("orderId"), orderId, "Order ID should match");
            
            // Attach order status to the Allure report
            Allure.addAttachment("Order Status Details", "application/json", statusResponse.asString());
        });
    }

    public static void orderMaker() {
        // Use a small quantity for testing
        String quantity = "1.0"; // 1.0 BNB as per the curl example

        Allure.step("Placing market BUY order for " + testSymbol + ": " + quantity, () -> {
            // Place a market order
            orderResponse = OrderClient.placeMarketOrder(testSymbol, "SELL", quantity);

            // Verify the order was created successfully
            Assert.assertEquals(orderResponse.getStatusCode(), 200, "Order should be created successfully");

            // Extract the order ID
            orderId = orderResponse.jsonPath().getLong("orderId");

            // Attach order details to the Allure report
            Allure.addAttachment("Created Order Details", "application/json", orderResponse.asString());
        });
    }
    
    @AfterTest
    @Step("Cleanup - Cancel any open orders")
    public void cleanup() {
    }
}