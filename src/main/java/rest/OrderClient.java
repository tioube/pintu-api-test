package rest;

import commons.Globals;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static io.restassured.RestAssured.given;

/**
 * Client for interacting with Binance Order API
 * Based on the official Binance signature examples
 */
public class OrderClient {
    
    private static final String BASE_URL = Globals.getSpotRestUrl(); // Using testnet as specified by the user
    
    /**
     * Place a market order on Binance
     * 
     * @param symbol Trading pair symbol (e.g., BNBUSDT)
     * @param side Order side (BUY or SELL)
     * @param quantity Order quantity
     * @return The response from the API
     */
    @Step("Place market order: {0} {1} {2}")
    public static Response placeMarketOrder(String symbol, String side, String quantity) {
        // Create the URL
        String url = BASE_URL + "/api/v3/order";
        
        // Create timestamp
        long timestamp = System.currentTimeMillis();
        
        // Create parameters map (using TreeMap to ensure consistent ordering)
        Map<String, String> params = new TreeMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("type", "MARKET");
        params.put("quantity", quantity);
        params.put("timestamp", String.valueOf(timestamp));
        
        // Create query string
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // Generate signature
        String signature = generateSignature(queryString.toString(), Globals.getSpotSecretKey());
        
        // Add signature to query string
        String fullQueryString = queryString + "&signature=" + signature;
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Log request details
        System.out.println("Request URL: " + url + "?" + fullQueryString);
        System.out.println("Request Headers: " + headers);
        
        // Make the request
        Response response = given()
                .headers(headers)
                .when()
                .post(url + "?" + fullQueryString);
        
        // Log response details
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        
        return response;
    }
    
    /**
     * Get order status from Binance
     * 
     * @param symbol Trading pair symbol
     * @param orderId Order ID
     * @return The response from the API
     */
    @Step("Get order status: {0} orderId={1}")
    public static Response getOrderStatus(String symbol, long orderId) {
        // Create the URL
        String url = BASE_URL + "/api/v3/order";
        
        // Create timestamp
        long timestamp = System.currentTimeMillis();
        
        // Create parameters map (using TreeMap to ensure consistent ordering)
        Map<String, String> params = new TreeMap<>();
        params.put("symbol", symbol);
        params.put("orderId", String.valueOf(orderId));
        params.put("timestamp", String.valueOf(timestamp));
        
        // Create query string
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // Generate signature
        String signature = generateSignature(queryString.toString(), Globals.getSpotSecretKey());
        
        // Add signature to query string
        String fullQueryString = queryString + "&signature=" + signature;
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Log request details
        System.out.println("Request URL: " + url + "?" + fullQueryString);
        System.out.println("Request Headers: " + headers);
        
        // Make the request
        Response response = given()
                .headers(headers)
                .when()
                .get(url + "?" + fullQueryString);
        
        // Log response details
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        
        return response;
    }
    
    /**
     * Cancel an order on Binance
     * 
     * @param symbol Trading pair symbol
     * @param orderId Order ID
     * @return The response from the API
     */
    @Step("Cancel order: {0} orderId={1}")
    public static Response cancelOrder(String symbol, long orderId) {
        // Create the URL
        String url = BASE_URL + "/api/v3/order";
        
        // Create timestamp
        long timestamp = System.currentTimeMillis();
        
        // Create parameters map (using TreeMap to ensure consistent ordering)
        Map<String, String> params = new TreeMap<>();
        params.put("symbol", symbol);
        params.put("orderId", String.valueOf(orderId));
        params.put("timestamp", String.valueOf(timestamp));
        
        // Create query string
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // Generate signature
        String signature = generateSignature(queryString.toString(), Globals.getSpotSecretKey());
        
        // Add signature to query string
        String fullQueryString = queryString + "&signature=" + signature;
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Log request details
        System.out.println("Request URL: " + url + "?" + fullQueryString);
        System.out.println("Request Headers: " + headers);
        
        // Make the request
        Response response = given()
                .headers(headers)
                .when()
                .delete(url + "?" + fullQueryString);
        
        // Log response details
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        
        return response;
    }
    
    /**
     * Fetch Order Book from Binance
     * 
     * @param symbol Trading pair symbol
     * @param limit Number of entries to return (optional)
     * @return The response from the API
     */
    @Step("Fetch Order Book: {0} limit={1}")
    public static Response fetchOrderBook(String symbol, Integer limit) {
        // Create the URL
        String url = BASE_URL + "/api/v3/depth";
        
        // Create parameters map
        Map<String, String> params = new TreeMap<>();
        params.put("symbol", symbol);
        if (limit != null) {
            params.put("limit", String.valueOf(limit));
        }
        
        // Create query string
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // Log request details
        System.out.println("Request URL: " + url + "?" + queryString);
        
        // Make the request
        Response response = given()
                .when()
                .get(url + "?" + queryString);
        
        // Log response details
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        
        return response;
    }
    
    /**
     * Fetch Open Orders from Binance
     * 
     * @param symbol Trading pair symbol (optional)
     * @return The response from the API
     */
    @Step("Fetch Open Orders: {0}")
    public static Response fetchOpenOrders(String symbol) {
        // Create the URL
        String url = BASE_URL + "/api/v3/openOrders";
        
        // Create timestamp
        long timestamp = System.currentTimeMillis();
        
        // Create parameters map (using TreeMap to ensure consistent ordering)
        Map<String, String> params = new TreeMap<>();
        if (symbol != null && !symbol.isEmpty()) {
            params.put("symbol", symbol);
        }
        params.put("timestamp", String.valueOf(timestamp));
        
        // Create query string
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // Generate signature
        String signature = generateSignature(queryString.toString(), Globals.getSpotSecretKey());
        
        // Add signature to query string
        String fullQueryString = queryString + "&signature=" + signature;
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Log request details
        System.out.println("Request URL: " + url + "?" + fullQueryString);
        System.out.println("Request Headers: " + headers);
        
        // Make the request
        Response response = given()
                .headers(headers)
                .when()
                .get(url + "?" + fullQueryString);
        
        // Log response details
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        
        return response;
    }
    
    /**
     * Fetch Trade History from Binance
     * 
     * @param symbol Trading pair symbol
     * @param limit Number of entries to return (optional)
     * @return The response from the API
     */
    @Step("Fetch Trade History: {0} limit={1}")
    public static Response fetchTradeHistory(String symbol, Integer limit) {
        // Create the URL
        String url = BASE_URL + "/api/v3/myTrades";
        
        // Create timestamp
        long timestamp = System.currentTimeMillis();
        
        // Create parameters map (using TreeMap to ensure consistent ordering)
        Map<String, String> params = new TreeMap<>();
        params.put("symbol", symbol);
        if (limit != null) {
            params.put("limit", String.valueOf(limit));
        }
        params.put("timestamp", String.valueOf(timestamp));
        
        // Create query string
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // Generate signature
        String signature = generateSignature(queryString.toString(), Globals.getSpotSecretKey());
        
        // Add signature to query string
        String fullQueryString = queryString + "&signature=" + signature;
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Log request details
        System.out.println("Request URL: " + url + "?" + fullQueryString);
        System.out.println("Request Headers: " + headers);
        
        // Make the request
        Response response = given()
                .headers(headers)
                .when()
                .get(url + "?" + fullQueryString);
        
        // Log response details
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        
        return response;
    }
    
    /**
     * Fetch Account Balance from Binance
     * 
     * @return The response from the API
     */
    @Step("Fetch Account Balance")
    public static Response fetchAccountBalance() {
        // Create the URL
        String url = BASE_URL + "/api/v3/account";
        
        // Create timestamp
        long timestamp = System.currentTimeMillis();
        
        // Create parameters map (using TreeMap to ensure consistent ordering)
        Map<String, String> params = new TreeMap<>();
        params.put("timestamp", String.valueOf(timestamp));
        
        // Create query string
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // Generate signature
        String signature = generateSignature(queryString.toString(), Globals.getSpotSecretKey());
        
        // Add signature to query string
        String fullQueryString = queryString + "&signature=" + signature;
        
        // Create headers
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("X-MBX-APIKEY", Globals.getSpotApiKey());
        
        // Log request details
        System.out.println("Request URL: " + url + "?" + fullQueryString);
        System.out.println("Request Headers: " + headers);
        
        // Make the request
        Response response = given()
                .headers(headers)
                .when()
                .get(url + "?" + fullQueryString);
        
        // Log response details
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        
        return response;
    }
    
    /**
     * Generate a signature for the request using HMAC-SHA256
     * Based on the official Binance signature examples
     * 
     * @param queryString The query string to sign
     * @param secretKey The secret key to use for signing
     * @return The signature
     */
    private static String generateSignature(String queryString, String secretKey) {
        try {
            // Log the query string and secret key for debugging
            System.out.println("Query string to sign: " + queryString);
            System.out.println("Secret key length: " + secretKey.length());
            
            // Create an HMAC-SHA256 key from the secret
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            hmac.init(keySpec);
            
            // Generate the HMAC-SHA256 hash
            byte[] hmacBytes = hmac.doFinal(queryString.getBytes());
            
            // Convert to hex string
            String signature = Hex.encodeHexString(hmacBytes);
            
            System.out.println("Generated signature: " + signature);
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Error generating signature: " + e.getMessage(), e);
        }
    }
}