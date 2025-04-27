package rest;

import configs.TestMasterConfigurations;
import io.qameta.allure.*;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test class for Binance WebSocket Trade stream
 */
@Epic("Binance WebSocket Tests")
@Feature("Trade Stream")
public class testWebSocketTrade extends TestMasterConfigurations {
    
    private WebSocketTradeClient tradeClient;
    private String testSymbol = "bnbusdt";
    
    @BeforeTest
    @Step("Setup WebSocket client")
    public void setup() {
        // Create WebSocket client
        tradeClient = new WebSocketTradeClient();
    }
    
    /**
     * Test to subscribe to the Trade stream
     * 
     * This test verifies that we can successfully subscribe to the Trade stream
     * and receive updates for a symbol.
     * 
     * @throws InterruptedException if the thread is interrupted
     */
    @Test(description = "Test subscribing to Trade stream via WebSocket")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Subscribe to Trade stream")
    @Description("This test subscribes to the Trade stream for a symbol and verifies that updates are received.")
    public void testSubscribeToTradeStream() throws InterruptedException {
        // Create a latch to wait for the message
        CountDownLatch latch = new CountDownLatch(1);
        
        // Create atomic references to store the received data
        AtomicBoolean messageReceived = new AtomicBoolean(false);
        AtomicReference<JSONObject> receivedData = new AtomicReference<>();
        
        Allure.step("Subscribing to Trade stream for " + testSymbol, () -> {
            // Subscribe to the Trade stream
            tradeClient.subscribeToTradeStream(testSymbol, data -> {
                // Store the received data
                receivedData.set(data);
                messageReceived.set(true);
                
                // Count down the latch
                latch.countDown();
            });
        });
        
        Allure.step("Waiting for Trade update", () -> {
            // Wait for the message (with timeout)
            boolean received = latch.await(30, TimeUnit.SECONDS);
            
            // Verify that a message was received
            Assert.assertTrue(received, "Should receive a message within the timeout");
            Assert.assertTrue(messageReceived.get(), "Should receive a message");
            
            // Verify the message structure
            JSONObject data = receivedData.get();
            Assert.assertNotNull(data, "Received data should not be null");
            
            // Verify the trade data fields
            Assert.assertTrue(data.containsKey("e"), "Message should contain event type");
            Assert.assertEquals(data.get("e"), "trade", "Event type should be 'trade'");
            Assert.assertTrue(data.containsKey("s"), "Message should contain symbol");
            Assert.assertTrue(data.containsKey("p"), "Message should contain price");
            Assert.assertTrue(data.containsKey("q"), "Message should contain quantity");
            
            // Attach the received data to the Allure report
            Allure.addAttachment("Trade Update", "application/json", data.toJSONString());
        });
    }
    
    @AfterTest
    @Step("Cleanup - Close WebSocket connection")
    public void cleanup() {
        // Close the WebSocket connection
        if (tradeClient != null) {
            tradeClient.close();
        }
    }
}
