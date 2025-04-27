package rest;

import configs.TestMasterConfigurations;
import io.qameta.allure.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    @Issue("BINANCE-124")
    @TmsLink("TC-457")
    @Owner("QA Team")
    @Lead("Automation Team")
    public void testSubscribeToOrderBookStream() throws InterruptedException {
        // Create a latch to wait for the test completion
        CountDownLatch testCompletionLatch = new CountDownLatch(1);
        
        // Create a list to store all received messages
        List<JSONObject> receivedMessages = Collections.synchronizedList(new ArrayList<>());
        
        Allure.step("Subscribing to Order Book stream for " + testSymbol, () -> {
            // Subscribe to the Order Book stream
            depthClient.subscribeToOrderBookStream(testSymbol, data -> {
                // Add the received data to our list
                receivedMessages.add(data);
                System.out.println("Received order book message: " + data);
                
                // Note: We don't count down the latch here as we want to collect all messages
            });
        });
        
        // Wait for some time to collect messages (we'll use a timeout instead of waiting for a specific event)
        Allure.step("Collecting order book messages for 30 seconds", () -> {
            try {
                // Wait for 5 seconds to collect messages
                Thread.sleep(5000);
                
                // Count down the test completion latch to signal we're done collecting messages
                testCompletionLatch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted while waiting for messages", e);
            }
        });
        
        // Wait for the test to complete
        testCompletionLatch.await();
        
        Allure.step("Processing collected order book messages", () -> {
            // Verify that at least one message was received
            Assert.assertFalse(receivedMessages.isEmpty(), "Should receive at least one order book message");
            
            // Process all received messages
            for (int i = 0; i < receivedMessages.size(); i++) {
                JSONObject data = receivedMessages.get(i);
                Assert.assertNotNull(data, "Received data should not be null");
                
                // Verify the message structure for each message
                Assert.assertTrue(data.containsKey("b"), "Message should contain bids");
                Assert.assertTrue(data.containsKey("a"), "Message should contain asks");
                
                // Verify that bids and asks are arrays
                JSONArray bids = (JSONArray) data.get("b");
                JSONArray asks = (JSONArray) data.get("a");
                Assert.assertNotNull(bids, "Bids should not be null");
                Assert.assertNotNull(asks, "Asks should not be null");
                
                // Attach each message to the Allure report
                Allure.addAttachment("Order Book Update " + (i + 1), "application/json", data.toJSONString());
            }
            
            // Add a summary of the number of messages received
            Allure.addAttachment("Order Book Summary", "text/plain",
                "Received " + receivedMessages.size() + " order book updates for symbol " + testSymbol);
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