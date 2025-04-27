package rest;

import configs.TestMasterConfigurations;
import io.qameta.allure.*;
import org.json.simple.JSONArray;
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
 * Test class for Binance WebSocket Order Book stream
 */
@Epic("Binance WebSocket Tests")
@Feature("Order Book Stream")
public class testWebSocketDepth extends TestMasterConfigurations {
    
    private WebSocketDepthClient depthClient;
    private String testSymbol = "bnbusdt";
    
    @BeforeTest
    @Step("Setup WebSocket client")
    public void setup() {
        // Create WebSocket client
        depthClient = new WebSocketDepthClient();
    }
    
    /**
     * Test to subscribe to the Order Book stream
     * 
     * This test verifies that we can successfully subscribe to the Order Book stream
     * and receive updates for a symbol.
     * 
     * @throws InterruptedException if the thread is interrupted
     */
    @Test(description = "Test subscribing to Order Book stream via WebSocket")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Subscribe to Order Book stream")
    @Description("This test subscribes to the Order Book stream for a symbol and verifies that updates are received.")
    public void testSubscribeToOrderBookStream() throws InterruptedException {
        // Create a latch to wait for the message
        CountDownLatch latch = new CountDownLatch(1);
        
        // Create atomic references to store the received data
        AtomicBoolean messageReceived = new AtomicBoolean(false);
        AtomicReference<JSONObject> receivedData = new AtomicReference<>();
        
        Allure.step("Subscribing to Order Book stream for " + testSymbol, () -> {
            // Subscribe to the Order Book stream
            depthClient.subscribeToOrderBookStream(testSymbol, data -> {
                // Store the received data
                receivedData.set(data);
                messageReceived.set(true);
                
                // Count down the latch
                latch.countDown();
            });
        });
        
        Allure.step("Waiting for Order Book update", () -> {
            // Wait for the message (with timeout)
            boolean received = latch.await(30, TimeUnit.SECONDS);
            
            // Verify that a message was received
            Assert.assertTrue(received, "Should receive a message within the timeout");
            Assert.assertTrue(messageReceived.get(), "Should receive a message");
            
            // Verify the message structure
            JSONObject data = receivedData.get();
            Assert.assertNotNull(data, "Received data should not be null");
            Assert.assertTrue(data.containsKey("bids"), "Message should contain bids");
            Assert.assertTrue(data.containsKey("asks"), "Message should contain asks");
            
            // Verify that bids and asks are arrays
            JSONArray bids = (JSONArray) data.get("bids");
            JSONArray asks = (JSONArray) data.get("asks");
            Assert.assertNotNull(bids, "Bids should not be null");
            Assert.assertNotNull(asks, "Asks should not be null");
            
            // Attach the received data to the Allure report
            Allure.addAttachment("Order Book Update", "application/json", data.toJSONString());
        });
    }
    
    @AfterTest
    @Step("Cleanup - Close WebSocket connection")
    public void cleanup() {
        // Close the WebSocket connection
        if (depthClient != null) {
            depthClient.close();
        }
    }
}