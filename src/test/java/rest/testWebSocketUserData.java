package rest;

import configs.TestMasterConfigurations;
import io.qameta.allure.*;
import io.restassured.response.Response;
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
 * Test class for Binance WebSocket User Data stream
 */
@Epic("Binance WebSocket Tests")
@Feature("User Data Stream")
public class testWebSocketUserData extends TestMasterConfigurations {
    
    private WebSocketUserDataClient userDataClient;
    private String listenKey;
    
    @BeforeTest
    @Step("Setup WebSocket client")
    public void setup() {
        // Create WebSocket client
        userDataClient = new WebSocketUserDataClient();
    }
    
    /**
     * Test to create a listen key for the User Data stream
     * 
     * This test verifies that we can successfully create a listen key.
     */
    @Test(description = "Test creating a listen key for User Data stream")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create listen key")
    @Description("This test creates a listen key for the User Data stream.")
    public void testCreateListenKey() {
        Allure.step("Creating listen key", () -> {
            // Create a listen key
            listenKey = userDataClient.createListenKey();
            
            // Verify that a listen key was created
            Assert.assertNotNull(listenKey, "Listen key should not be null");
            Assert.assertFalse(listenKey.isEmpty(), "Listen key should not be empty");
            
            // Attach the listen key to the Allure report
            Allure.addAttachment("Listen Key", "text/plain", listenKey);
        });
    }
    
    /**
     * Test to extend the validity of a listen key
     * 
     * This test verifies that we can successfully extend the validity of a listen key.
     */
    @Test(description = "Test extending a listen key", dependsOnMethods = "testCreateListenKey")
    @Severity(SeverityLevel.NORMAL)
    @Story("Extend listen key")
    @Description("This test extends the validity of a listen key.")
    public void testExtendListenKey() {
        Allure.step("Extending listen key: " + listenKey, () -> {
            // Extend the listen key
            userDataClient.extendListenKey(listenKey);
            
            // No assertion needed, the method will throw an exception if it fails
        });
    }
    
    /**
     * Test to subscribe to the User Data stream
     * 
     * This test verifies that we can successfully subscribe to the User Data stream
     * and receive updates.
     * 
     * @throws InterruptedException if the thread is interrupted
     */
    @Test(description = "Test subscribing to User Data stream", dependsOnMethods = "testExtendListenKey")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Subscribe to User Data stream")
    @Description("This test subscribes to the User Data stream and verifies that updates are received.")
    @Issue("BINANCE-123")
    @TmsLink("TC-456")
    @Owner("QA Team")
    @Lead("Automation Team")
    public void testSubscribeToUserDataStream() throws InterruptedException {
        // Create a latch to wait for the test completion
        CountDownLatch testCompletionLatch = new CountDownLatch(1);
        
        // Create a list to store all received messages
        List<JSONObject> receivedMessages = Collections.synchronizedList(new ArrayList<>());
        
        Allure.step("Subscribing to User Data stream", () -> {
            // Subscribe to the User Data stream
            userDataClient.subscribeToUserDataStream(data -> {
                // Add the received data to our list
                receivedMessages.add(data);
                System.out.println("Received message: " + data);
                
                // Note: We don't count down the latch here as we want to collect all messages
            });
        });
        // Wait for the WebSocket connection to establish
        boolean connected = userDataClient.waitForConnection(30, TimeUnit.SECONDS);
        Assert.assertTrue(connected, "WebSocket connection should be established within the timeout");
        
        // Send a subscription message to the websocket
        Allure.step("Sending subscription message to websocket", () -> {
            System.out.println("Sending subscription message to websocket...");
            
            // Create the subscription message
            String subscriptionMessage = "{\n" +
                "  \"method\": \"SUBSCRIBE\",\n" +
                "  \"params\": [\n" +
                "    \"btcusdt@aggTrade\",\n" +
                "    \"btcusdt@depth\"\n" +
                "  ],\n" +
                "  \"id\": 1\n" +
                "}";
            
            // Send the message to the websocket
            userDataClient.sendMessage(subscriptionMessage);
            
            System.out.println("Subscription message sent: " + subscriptionMessage);
        });
        
        // Wait for some time to collect messages (we'll use a timeout instead of waiting for a specific event)
        Allure.step("Collecting messages for 5 seconds", () -> {
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
        
        Allure.step("Processing collected messages", () -> {
            // Verify that at least one message was received
            Assert.assertFalse(receivedMessages.isEmpty(), "Should receive at least one message");
            
            // Process all received messages
            for (int i = 0; i < receivedMessages.size(); i++) {
                JSONObject data = receivedMessages.get(i);
                Assert.assertNotNull(data, "Received data should not be null");
                
                // Attach each message to the Allure report
                Allure.addAttachment("User Data Update " + (i + 1), "application/json", data.toJSONString());
                
                // Add a summary of the number of messages received
                Allure.addAttachment("User Data Summary", "text/plain",
                    "Received " + receivedMessages.size() + " user data updates");
            }
        });

    }
    
    /**
     * Test to close a listen key
     * 
     * This test verifies that we can successfully close a listen key.
     */
    @Test(description = "Test closing a listen key", dependsOnMethods = "testSubscribeToUserDataStream")
    @Severity(SeverityLevel.NORMAL)
    @Story("Close listen key")
    @Description("This test closes a listen key.")
    public void testCloseListenKey() {
        Allure.step("Closing listen key: " + listenKey, () -> {
            // Close the listen key
            userDataClient.closeListenKey(listenKey);
            
            // No assertion needed, the method will throw an exception if it fails
        });
    }

//    @AfterTest
//    @Step("Cleanup - Close WebSocket connection")
//    public void cleanup() {
//        // Close the WebSocket connection
//        if (userDataClient != null) {
//            userDataClient.close();
//        }
//    }
}