package rest;

import io.qameta.allure.Step;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class WebScoketTradeClient {

    private static final String WS_BASE_URL = "wss://stream.binance.com:9443/ws/";
    private OkHttpClient client;
    private WebSocket webSocket;
    private final JSONParser jsonParser = new JSONParser();

    /**
     * Constructor that initializes the OkHttp client
     */
    public WebScoketTradeClient() {
        // Initialize OkHttp client
        client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Subscribe to the Trade stream for a symbol
     *
     * @param symbol Trading pair symbol (e.g., bnbusdt)
     * @param callback Callback to handle the received data
     */
    @Step("Subscribe to Trade stream for {symbol}")
    public void subscribeToTradeStream(String symbol, Consumer<JSONObject> callback) {
        // Create the WebSocket URL
        String url = WS_BASE_URL + symbol.toLowerCase() + "@trade";

        // Create the WebSocket request
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Create the WebSocket listener
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                System.out.println("WebSocket connection opened: " + url);
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

        System.out.println("Subscribed to Trade stream: " + url);
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
    }
}
