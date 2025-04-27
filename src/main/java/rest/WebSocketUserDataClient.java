package rest;

import commons.Globals;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.restassured.RestAssured.given;

/**
 * Client for interacting with Binance WebSocket User Data stream
 */
public class WebSocketUserDataClient {
    
    private static final String REST_BASE_URL = "https://testnet.binance.vision";
    private static final String WS_BASE_URL = "wss://stream.binance.com:9443/ws/";
    private OkHttpClient client;
    private WebSocket webSocket;
    private final JSONParser jsonParser = new JSONParser();
    private String listenKey;
    private CountDownLatch connectionLatch;
    
    /**
     * Constructor that initializes the OkHttp client
     */
    public WebSocketUserDataClient() {
        // Initialize OkHttp client
        client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        connectionLatch = new CountDownLatch(1);
    }
    
    /**
     * Create a listen key for the User Data stream
     * 
     * @return The listen key
     */
    @Step("Create listen key")
    public String createListenKey() {
        // Create the URL
        String url = REST_BASE_URL + "/api/v3/userDataStream";
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Make the request
        Response response = given()
                .headers(headers)
                .when()
                .post(url);
        
        // Check if the request was successful
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to create listen key: " + response.getStatusCode() + " " + response.getBody().asString());
        }
        
        // Extract the listen key
        listenKey = response.jsonPath().getString("listenKey");
        
        System.out.println("Created listen key: " + listenKey);
        
        return listenKey;
    }
    
    /**
     * Extend the validity of a listen key
     * 
     * @param listenKey The listen key to extend
     */
    @Step("Extend listen key: {listenKey}")
    public void extendListenKey(String listenKey) {
        // Create the URL
        String url = REST_BASE_URL + "/api/v3/userDataStream";
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Create parameters
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("listenKey", listenKey);
        
        // Make the request
        Response response = given()
                .headers(headers)
                .queryParams(params)
                .when()
                .put(url);
        
        // Check if the request was successful
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to extend listen key: " + response.getStatusCode() + " " + response.getBody().asString());
        }
        
        System.out.println("Extended listen key: " + listenKey);
    }
    
    /**
     * Close a listen key
     * 
     * @param listenKey The listen key to close
     */
    @Step("Close listen key: {listenKey}")
    public void closeListenKey(String listenKey) {
        // Create the URL
        String url = REST_BASE_URL + "/api/v3/userDataStream";
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Create parameters
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("listenKey", listenKey);
        
        // Make the request
        Response response = given()
                .headers(headers)
                .queryParams(params)
                .when()
                .delete(url);
        
        // Check if the request was successful
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to close listen key: " + response.getStatusCode() + " " + response.getBody().asString());
        }
        
        System.out.println("Closed listen key: " + listenKey);
    }
    
    /**
     * Subscribe to the User Data stream
     * 
     * @param callback Callback to handle the received data
     */
    @Step("Subscribe to User Data stream")
    public void subscribeToUserDataStream(Consumer<JSONObject> callback) {
        // Create a listen key if not already created
        if (listenKey == null) {
            createListenKey();
        }
        
        // Create the WebSocket URL
        String url = WS_BASE_URL + listenKey;
        
        // Create the WebSocket request
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        // Create the WebSocket listener
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                System.out.println("WebSocket connection opened: " + url);
                System.out.println("WebSocket response code: " + response.code());
                System.out.println("WebSocket response message: " + response.message());
                
                // Signal that the connection is established
                connectionLatch.countDown();
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    // Parse the message
                    JSONObject json = (JSONObject) jsonParser.parse(text);
                    
                    // Log the message
                    System.out.println("Received message: " + json);
                    
                    // Call the callback
                    callback.accept(json);
                } catch (ParseException e) {
                    System.err.println("Error parsing message: " + e.getMessage());
                }
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                System.err.println("WebSocket failure: " + t.getMessage());
            }
            
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("WebSocket closing: " + reason);
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("WebSocket closed: " + reason);
            }
        };
        
        // Connect to the WebSocket
        webSocket = client.newWebSocket(request, listener);
        
        System.out.println("Subscribed to User Data stream: " + url);
    }
    
    /**
     * Wait for the WebSocket connection to be established
     *
     * @param timeout The maximum time to wait
     * @param unit The time unit of the timeout
     * @return true if the connection was established within the timeout, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @Step("Wait for WebSocket connection")
    public boolean waitForConnection(long timeout, TimeUnit unit) throws InterruptedException {
        return connectionLatch.await(timeout, unit);
    }
    
    /**
     * Send a message to the WebSocket
     *
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    @Step("Send message to WebSocket")
    public boolean sendMessage(String message) {
        if (webSocket != null) {
            boolean sent = webSocket.send(message);
            if (sent) {
                System.out.println("Message sent: " + message);
            } else {
                System.err.println("Failed to send message: " + message);
            }
            return sent;
        } else {
            System.err.println("Cannot send message: WebSocket is not connected");
            return false;
        }
    }
    
    /**
     * Close the WebSocket connection
     */
    @Step("Close WebSocket connection")
    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
            System.out.println("WebSocket connection closed");
        }
        
        if (listenKey != null) {
            closeListenKey(listenKey);
            listenKey = null;
        }
        
        // Reset the connection latch for future connections
        connectionLatch = new CountDownLatch(1);
    }
}