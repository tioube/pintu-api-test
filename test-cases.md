# Binance WebSocket API Test Cases

This document provides a comprehensive list of test cases for the Binance WebSocket API integration.

## Table of Contents

1. [User Data Stream Tests](#user-data-stream-tests)
2. [Order Book Stream Tests](#order-book-stream-tests)
3. [Trade Stream Tests](#trade-stream-tests)

---

## User Data Stream Tests

### TC-456: Subscribe to User Data Stream
**Issue:** BINANCE-123  
**Owner:** QA Team  
**Lead:** Automation Team  
**Severity:** CRITICAL

#### Description
This test verifies that we can successfully subscribe to the User Data stream and receive updates.

#### Prerequisites
- Valid API credentials
- Active Binance account

#### Test Steps
1. Create a listen key for the User Data stream
2. Extend the validity of the listen key
3. Subscribe to the User Data stream
4. Wait for the WebSocket connection to establish
5. Send a subscription message to the websocket with the following payload:
   ```json
   {
     "method": "SUBSCRIBE",
     "params": [
       "btcusdt@aggTrade",
       "btcusdt@depth"
     ],
     "id": 1
   }
   ```
6. Collect and process all received messages for 30 seconds
7. Close the listen key
8. Close the WebSocket connection

#### Expected Results
- Listen key is successfully created
- Listen key is successfully extended
- WebSocket connection is established
- Subscription message is sent successfully
- At least one message is received from the User Data stream
- Each message contains the expected event type field ("e")
- Listen key is successfully closed
- WebSocket connection is successfully closed

---

### TC-456-1: Create Listen Key
**Issue:** BINANCE-123  
**Owner:** QA Team  
**Lead:** Automation Team  
**Severity:** CRITICAL

#### Description
This test verifies that we can successfully create a listen key for the User Data stream.

#### Test Steps
1. Send a POST request to create a listen key
2. Verify the response

#### Expected Results
- Listen key is successfully created
- Listen key is not null or empty

---

### TC-456-2: Extend Listen Key
**Issue:** BINANCE-123  
**Owner:** QA Team  
**Lead:** Automation Team  
**Severity:** NORMAL

#### Description
This test verifies that we can successfully extend the validity of a listen key.

#### Test Steps
1. Create a listen key
2. Send a PUT request to extend the listen key
3. Verify the response

#### Expected Results
- Listen key is successfully extended
- No exceptions are thrown

---

### TC-456-4: Close Listen Key
**Issue:** BINANCE-123  
**Owner:** QA Team  
**Lead:** Automation Team  
**Severity:** NORMAL

#### Description
This test verifies that we can successfully close a listen key.

#### Test Steps
1. Create a listen key
2. Subscribe to the User Data stream
3. Close the listen key
4. Verify the response

#### Expected Results
- Listen key is successfully closed
- No exceptions are thrown

---

## Order Book Stream Tests

### TC-457: Subscribe to Order Book Stream
**Issue:** BINANCE-124  
**Owner:** QA Team  
**Lead:** Automation Team  
**Severity:** CRITICAL

#### Description
This test verifies that we can successfully subscribe to the Order Book stream and receive updates for a symbol.

#### Prerequisites
- Active Binance market data

#### Test Steps
1. Create a WebSocket connection to the Order Book stream for a specific symbol (bnbusdt)
2. Wait for the WebSocket connection to establish
3. Collect and process all received messages for 30 seconds
4. Close the WebSocket connection

#### Expected Results
- WebSocket connection is established
- At least one message is received from the Order Book stream
- Each message contains bids ("b") and asks ("a") fields
- Bids and asks are valid arrays
- WebSocket connection is successfully closed

---

## Trade Stream Tests

### TC-458: Subscribe to Trade Stream
**Issue:** BINANCE-125  
**Owner:** QA Team  
**Lead:** Automation Team  
**Severity:** CRITICAL

#### Description
This test verifies that we can successfully subscribe to the Trade stream and receive updates for a symbol.

#### Prerequisites
- Active Binance market data

#### Test Steps
1. Create a WebSocket connection to the Trade stream for a specific symbol (bnbusdt)
2. Wait for the WebSocket connection to establish
3. Collect and process all received messages for 30 seconds
4. Close the WebSocket connection

#### Expected Results
- WebSocket connection is established
- At least one message is received from the Trade stream
- Each message contains the expected event type field ("e") with value "trade"
- Each message contains symbol ("s"), price ("p"), and quantity ("q") fields
- WebSocket connection is successfully closed

---

## Test Environment

- **Base URL:** https://testnet.binance.vision
- **WebSocket URL:** wss://stream.binance.com:9443/ws/
- **Test Symbol:** bnbusdt

## Test Data

- **API Key:** Stored in Globals.getSpotApiKey()
- **Test Duration:** 30 seconds per WebSocket test

## Notes

1. All tests use a timeout of 30 seconds to collect messages
2. Tests verify both connection establishment and message content
3. All received messages are logged and attached to the Allure report
4. A summary of the total number of messages received is included in the report