package rest;

import configs.TestMasterConfigurations;
import io.qameta.allure.*;
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
    @Issue("BINANCE-125")
    @TmsLink("TC-458")
    @Owner("QA Team")
    @Lead("Automation Team")
    public void testSubscribeToTradeStream() throws InterruptedException {
        // Create a latch to wait for the test completion
        CountDownLatch testCompletionLatch = new CountDownLatch(1);
        
        // Create a list to store all received messages
        List<JSONObject> receivedMessages = Collections.synchronizedList(new ArrayList<>());
        
        Allure.step("Subscribing to Trade stream for " + testSymbol, () -> {
            // Subscribe to the Trade stream
            tradeClient.subscribeToTradeStream(testSymbol, data -> {
                // Add the received data to our list
                receivedMessages.add(data);
                System.out.println("Received trade message: " + data);
                
                // Note: We don't count down the latch here as we want to collect all messages
            });
        });
        
        // Wait for some time to collect messages (we'll use a timeout instead of waiting for a specific event)
        Allure.step("Collecting trade messages for 30 seconds", () -> {
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
        
        Allure.step("Processing collected trade messages", () -> {
            // Verify that at least one message was received
            Assert.assertFalse(receivedMessages.isEmpty(), "Should receive at least one trade message");
            
            // Process all received messages
            for (int i = 0; i < receivedMessages.size(); i++) {
                JSONObject data = receivedMessages.get(i);
                Assert.assertNotNull(data, "Received data should not be null");
                
                // Verify the trade data fields for each message
                Assert.assertTrue(data.containsKey("e"), "Message should contain event type");
                Assert.assertEquals(data.get("e"), "trade", "Event type should be 'trade'");
                Assert.assertTrue(data.containsKey("s"), "Message should contain symbol");
                Assert.assertTrue(data.containsKey("p"), "Message should contain price");
                Assert.assertTrue(data.containsKey("q"), "Message should contain quantity");
                
                // Attach each message to the Allure report
                Allure.addAttachment("Trade Update " + (i + 1), "application/json", data.toJSONString());
            }
            
            // Add a summary of the number of messages received
            Allure.addAttachment("Trade Summary", "text/plain",
                "Received " + receivedMessages.size() + " trade updates for symbol " + testSymbol);
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
