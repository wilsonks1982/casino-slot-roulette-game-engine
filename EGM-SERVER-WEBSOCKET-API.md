# WebSocket API for Roulette Game

#### Change History
- **2026-02-20**: **Wilson K Sam**: V1.0 version of the WebSocket API documentation for the Roulette Game.
- **2026-02-23**: **Wilson K Sam**: V1.1 Connection Parameter tableId Added
- **2026-02-26**: **Wilson K Sam**: V1.2 Added hot/cold numbers, group, statistics, lastNumbers  to RESULT_DECLARED message

## WebSocket Endpoint
- **Endpoint URL**: `ws://api.egmserver.com:9090/websocket/game?egmId={egmId}&uid={uid}&tableId={tableId}`

## Example
```properties
//For Table 1
ws://api.egmserver.com:9090/websocket/game?egmId=123&uid=456&tableId=1

//For Table 2
ws://api.egmserver.com:9090/websocket/game?egmId=123&uid=456&tableId=2

```

## Connection Parameters
- **egmId**: The unique identifier for the Electronic Gaming Machine (EGM) that the player is interacting with.
- **uid**: The unique identifier for the user/player.
- **tableId**: The unique identifier for the roulette table that the player is joining.


## Message Format
All messages sent and received through the WebSocket connection will be in JSON format. Each message will contain a `type` field to indicate the type of message, and a `payload` field to contain the relevant information.

### Example Message Structure
```json
{
  "type": "ROUND_CREATED",
  "messageId": "17715855141",
  "timestamp": "2026-02-20T16:36:23.858401+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "tableId": "1"
  }
}
```

## Message Types
### 1. INITIAL_DATA

- **Description**: This message is sent by the server immediately after a successful connection is established. It contains the initial data required for the player to start interacting with the game.
- **Payload**:
  - `tableInfo`: Information about the roulette table, including the table ID, name, and wheel type.
  - `payout`: The payout ratios for different types of bets.
  - `tableLimit`: The betting limits for the table, including chip denominations and minimum/maximum bet amounts.
  - `session`: Information about the player's session, including session ID, EGM ID, user ID, protocol version, and heartbeat interval.
  - `wallet`: The player's current wallet balance and currency.
  - `gameState`: The current state of the game, including round ID, status, and seconds remaining for betting.
  - `currentBets`: The player's current bets, including total stake and individual bet details.
  - `history`: Historical data about hot numbers, cold numbers, statistics, group distributions, and last winning numbers.

### Example Message Structure
```json
{
  "type": "INITIAL_DATA",
  "messageId": "17715855130",
  "timestamp": "2026-02-20T16:36:12.766867+05:30[Asia/Kolkata]",
  "payload": {
    "tableInfo": {
      "tableId": "1",
      "tableName": "Roulette Table 1",
      "wheelType": "European"
    },
    "payout": {
      "straightUpBet": 35,
      "splitBet": 17,
      "streetBet": 11,
      "cornerBet": 8,
      "basketBet": 6,
      "lineBet": 5,
      "columnBet": 2,
      "dozenBet": 2,
      "outsideBet": 1
    },
    "tableLimit": {
      "chips": [
        {
          "id": "1",
          "color": "red",
          "value": 10,
          "img": "chip_red.png",
          "defaultChip": true
        },
        {
          "id": "2",
          "color": "blue",
          "value": 50,
          "img": "chip_blue.png",
          "defaultChip": false
        },
        {
          "id": "3",
          "color": "green",
          "value": 100,
          "img": "chip_green.png",
          "defaultChip": false
        },
        {
          "id": "4",
          "color": "black",
          "value": 500,
          "img": "chip_black.png",
          "defaultChip": false
        },
        {
          "id": "5",
          "color": "purple",
          "value": 1000,
          "img": "chip_purple.png",
          "defaultChip": false
        }
      ],
      "minBet": 10,
      "maxBet": 100000,
      "minSideBet": 0,
      "maxSideBet": 0,
      "minStraightUpBet": 10,
      "maxStraightUpBet": 25000,
      "minSplitBet": 10,
      "maxSplitBet": 50000,
      "minStreetBet": 10,
      "maxStreetBet": 75000,
      "minCornerBet": 10,
      "maxCornerBet": 100000,
      "minLineBet": 100,
      "maxLineBet": 150000,
      "minDozenColumnBet": 100,
      "maxDozenColumnBet": 300000,
      "minOutsideBet": 100,
      "maxOutsideBet": 500000
    },
    "session": {
      "sessionId": "e3ce3627-d01d-d40d-6ac8-ddeeb2faa14a",
      "egmId": "123",
      "uid": "456",
      "protocolVersion": "1.0",
      "heartbeatInterval": 30
    },
    "wallet": {
      "balance": 1000,
      "currency": "INR"
    },
    "gameState": {
      "roundId": "1771585513",
      "status": "BETTING_CLOSED",
      "secondsRemaining": -1
    },
    "currentBets": {
      "totalStake": 0,
      "bets": []
    },
    "history": {
      "hotNumbers": {
        "hotNumbers": [
          16,
          17,
          20,
          25,
          32
        ]
      },
      "coldNumbers": {
        "coldNumbers": [
          1,
          2,
          3,
          4,
          5
        ]
      },
      "statistics": {
        "statistics": [
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 1,
            "percentage": 100
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 1,
            "percentage": 100
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 1,
            "percentage": 100
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 1,
            "percentage": 100
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          },
          {
            "count": 0,
            "percentage": 0
          }
        ]
      },
      "group": {
        "groupFirstLine": 0,
        "groupSecondLine": 50,
        "groupThirdLine": 50,
        "group1to12": 0,
        "group13to24": 50,
        "group25to36": 50,
        "groupBlack": 50,
        "groupRed": 50,
        "groupOdd": 50,
        "groupEven": 50,
        "group1to18": 25,
        "group19to36": 75
      },
      "lastNumbers": {
        "lastNumbers": [
          {
            "winningNumber": 17,
            "roundId": "1771585512"
          },
          {
            "winningNumber": 20,
            "roundId": "1771585511"
          },
          {
            "winningNumber": 32,
            "roundId": "1771585510"
          },
          {
            "winningNumber": 25,
            "roundId": "1771585509"
          }
        ]
      }
    }
  }
}
```
### 2. ROUND_CREATED
- **Description**: This message is sent by the server when a new round of the roulette game is created.
- **Payload**:
  - `roundId`: The unique identifier for the newly created round.
  - `tableId`: The identifier for the table where the round is taking place.

### Example Message Structure
```json
{
  "type": "ROUND_CREATED",
  "messageId": "17715855141",
  "timestamp": "2026-02-20T16:36:23.858401+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "tableId": "table-1"
  }
}
```

### 3. BETTING_OPENED
- **Description**: This message is sent by the server when the betting phase for a round is opened, allowing players to place their bets.
- **Payload**:
  - `roundId`: The unique identifier for the round that has opened for betting.
  - `secondsRemaining`: The number of seconds remaining for players to place their bets before the betting phase closes.

#### Example Message Structure
```json
{
  "type": "BETTING_OPENED",
  "messageId": "17715855142",
  "timestamp": "2026-02-20T16:36:30.123456+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "secondsRemaining": 60
  }
}
```

### 4. BETTING_CLOSED
- **Description**: This message is sent by the server when the betting phase for a round is closed, indicating that no more bets can be placed for that round.
- **Payload**:
  - `roundId`: The unique identifier for the round that has closed for betting.

#### Example Message Structure
```json
{
  "type": "BETTING_CLOSED",
  "messageId": "17715855143",
  "timestamp": "2026-02-20T16:37:30.123456+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514"
  }
}
```

### 5. BALL_RELEASED
- **Description**: This message is sent by the server when the ball is released on the roulette wheel, indicating that the outcome of the round is being determined.
- **Payload**:
  - `roundId`: The unique identifier for the round in which the ball has been released.

#### Example Message Structure
```json
{
  "type": "BALL_RELEASED",
  "messageId": "17715855144",
  "timestamp": "2026-02-20T16:37:45.123456+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514"
  }
}
```

### 6. WHEEL_SPINNING
- **Description**: This message is sent by the server when the roulette wheel is spinning, indicating that the outcome of the round is being determined and players should wait for the result.
- **Payload**:
  - `roundId`: The unique identifier for the round in which the wheel is spinning.

#### Example Message Structure
```json
{
  "type": "WHEEL_SPINNING",
  "messageId": "17715855145",
  "timestamp": "2026-02-20T16:38:00.123456+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514"
  }
}
```

### 7. RESULT_DECLARED
- **Description**: This message is sent by the server when the result of a round is declared, providing the winning number and associated attributes.
- **Payload**:
  - `roundId`: The unique identifier for the round for which the result is declared.
  - `winningNumber`: The number that won in the round.
  - `color`: The color of the winning number (e.g., RED, BLACK, GREEN).
  - `parity`: The parity of the winning number (e.g., ODD, EVEN).
  - `dozen`: The dozen category of the winning number (e.g., 1 for 1-12, 2 for 13-24, 3 for 25-36).
  - `column`: The column category of the winning number (e.g., 1 for first column, 2 for second column, 3 for third column).
  - `coldNumbers`: A list of cold numbers at the time of result declaration, which can be used for historical reference and player insights.
  - `hotNumbers`: A list of hot numbers at the time of result declaration, which can be used for historical reference and player insights.
  - `statistics`: A list of statistics for all numbers at the time of result declaration, including count and percentage for each number, which can be used for historical reference and player insights.
  - `group`: The group distribution percentages for various categories (e.g., first line, second line, third line, 1-12, 13-24, 25-36, black, red, odd, even, 1-18, 19-36) at the time of result declaration, which can be used for historical reference and player insights.
  - `lastNumbers`: A list of the last winning numbers along with their corresponding round IDs at the time of result declaration, which can be used for historical reference and player insights.


#### Example Message Structure
```json
{
  "type": "RESULT_DECLARED",
  "messageId": "17715855146",
  "timestamp": "2026-02-20T16:36:50.861916+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "winningNumber": 17,
    "color": "BLACK",
    "parity": "ODD",
    "dozen": 2,
    "column": 2,
    "hotNumbers": {
      "hotNumbers": [
        16,
        17,
        20,
        25,
        32
      ]
    },
    "coldNumbers": {
      "coldNumbers": [
        1,
        2,
        3,
        4,
        5
      ]
    },
    "statistics": {
      "statistics": [
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 1,
          "percentage": 100
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 1,
          "percentage": 100
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 1,
          "percentage": 100
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 1,
          "percentage": 100
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        },
        {
          "count": 0,
          "percentage": 0
        }
      ]
    },
    "group": {
      "groupFirstLine": 0,
      "groupSecondLine": 50,
      "groupThirdLine": 50,
      "group1to12": 0,
      "group13to24": 50,
      "group25to36": 50,
      "groupBlack": 50,
      "groupRed": 50,
      "groupOdd": 50,
      "groupEven": 50,
      "group1to18": 25,
      "group19to36": 75
    },
    "lastNumbers": {
      "lastNumbers": [
        {
          "winningNumber": 17,
          "roundId": "1771585512"
        },
        {
          "winningNumber": 20,
          "roundId": "1771585511"
        },
        {
          "winningNumber": 32,
          "roundId": "1771585510"
        },
        {
          "winningNumber": 25,
          "roundId": "1771585509"
        }
      ]
    }
  }
}
```


### 8. PAYOUT_COMPLETED
- **Description**: This message is sent by the server when the payout for a round is completed, providing details about the total stake, total payout, and net result for the player.
- **Payload**:
  - `roundId`: The unique identifier for the round for which the payout is completed.
  - `totalStake`: The total amount of money the player staked in the round.
  - `totalPayout`: The total amount of money the player won in the round.
  - `netResult`: The net result for the player, calculated as total payout minus total stake.

#### Example Message Structure
```json
{
  "type": "PAYOUT_COMPLETED",
  "messageId": "17715855147",
  "timestamp": "2026-02-20T16:36:53.861935+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "totalStake": 100,
    "totalPayout": 800,
    "netResult": 700
  }
}
```

### 9. PLACE_BET
- **Description**: This message is sent by the client to the server when the player wants to place a bet during the betting phase of a round.
- **Payload**:
  - `roundId`: The unique identifier for the round in which the bet is being placed.
  - `stake`: The total amount of money the player is betting.
  - `betsList`: A list of individual bets, where each bet includes a `betIndex` (indicating the type of bet) and a `betAmount` (indicating the amount of money placed on that bet).


#### Example Message Structure
```json
{
  "type": "PLACE_BET",
  "payload": {
    "roundId": "1771585514",
    "stake": 100,
    "betsList": [
      {
        "betIndex": 117,
        "betAmount": 50
      },
      {
        "betIndex": 127,
        "betAmount": 50
      }
    ]
  }
}
```

### 10. HEARTBEAT
- **Description**: This message is sent by the client to the server at regular intervals (as specified in the session information) to keep the WebSocket connection alive and prevent timeouts.
- **Payload**:
  - `egmId`: The unique identifier for the Electronic Gaming Machine (EGM) that the player is interacting with.
  - `uid`: The unique identifier for the user/player.


#### Example Message Structure
```json
{
  "type": "HEARTBEAT",
  "payload": {
    "egmId": "123",
    "uid": "456"
  }
}
```

### 11. BET_PLACED
- **Description**: This message is sent by the server to the client to confirm that a bet has been successfully placed during the betting phase of a round.
- **Payload**:
  - `roundId`: The unique identifier for the round in which the bet was placed.
  - `betId`: A unique identifier for the bet that was placed, which can be used for tracking and reference purposes.
  - `stake`: The total amount of money the player is betting.
  - `betsList`: A list of individual bets that were successfully placed, where each bet includes a `betIndex` (indicating the type of bet) and a `betAmount` (indicating the amount of money placed on that bet).
  - `newBalance`: The player's updated wallet balance after placing the bet.
  
#### Example Message Structure
```json
{
  "type": "BET_PLACED",
  "messageId": "17715855150",
  "timestamp": "2026-02-20T16:37:05.861950+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "betId": "bet-12345-1771585514",
    "stake": 100,
    "betsList": [
      {
        "betIndex": 117,
        "betAmount": 50
      },
      {
        "betIndex": 127,
        "betAmount": 50
      }
    ],
    "newBalance": 900
  }
}
```

### 12. BET_REJECTED
- **Description**: This message is sent by the server to the client to indicate that a bet has been rejected during the betting phase of a round, along with the reason for rejection.
- **Payload**:
  - `roundId`: The unique identifier for the round in which the bet was attempted.
  - `betId`: A unique identifier for the bet that was attempted, which can be used for tracking and reference purposes.
  - `stake`: The total amount of money the player attempted to bet.
  - `betsList`: A list of individual bets that were attempted, where each bet includes a `betIndex` (indicating the type of bet) and a `betAmount` (indicating the amount of money attempted to be placed on that bet).
  - `reason`: A string indicating the reason for bet rejection (e.g., "Insufficient Balance", "Betting Closed", "Invalid Bet Type").
  - `newBalance`: The player's current wallet balance at the time of bet rejection.
- #### Example Message Structure
```json
{
  "type": "BET_REJECTED",
  "messageId": "17715855151",
  "timestamp": "2026-02-20T16:37:10.861955+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "betId": "bet-12345-1771585514",
    "stake": 100,
    "betsList": [
      {
        "betIndex": 117,
        "betAmount": 50
      },
      {
        "betIndex": 127,
        "betAmount": 50
      }
    ],
    "reason": "Server Error: Unable to process the bet at this time. Please try again later.",
    "newBalance": 1000
  }
}
```


### 13. BET_CANCELLED
- **Description**: This message is sent by the client to the server when the player wants to cancel a previously placed bet during the betting phase of a round.
- **Payload**:
  - `roundId`: The unique identifier for the round in which the bet was placed.
  - `betId`: The unique identifier for the bet that the player wants to cancel.
  - `stake`: The total amount of money that was staked on the bet being cancelled.
  - `betsList`: A list of individual bets that are being cancelled, where each bet includes a `betIndex` (indicating the type of bet) and a `betAmount` (indicating the amount of money that was placed on that bet).
  - `newBalance`: The player's updated wallet balance after cancelling the bet.
  - `reason`: A string indicating the reason for bet cancellation (e.g., "Player Request", "Betting Closed", "Round Ended").

#### Example Message Structure
```json
{
  "type": "BET_CANCELLED",
  "messageId": "17715855152",
  "timestamp": "2026-02-20T16:37:15.861960+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "betId": "bet-12345-1771585514",
    "stake": 100,
    "betsList": [
      {
        "betIndex": 117,
        "betAmount": 50
      },
      {
        "betIndex": 127,
        "betAmount": 50
      }
    ],
    "newBalance": 1000,
    "reason": "Game Ended: The round has ended and the bet has been cancelled. Your stake has been refunded."
  }
}
```


### 14. BALANCE_UPDATED

- **Description**: This message is sent by the server to the client whenever there is an update to the player's wallet balance, such as after placing a bet, winning a round, or receiving a payout.
- **Payload**:
  - `roundId`: The unique identifier for the round associated with the balance update (if applicable).
  - `newBalance`: The player's updated wallet balance after the change.
  - `reason`: A string indicating the reason for the balance update (e.g., "Bet Placed", "Round Won", "Payout Received", "Bet Cancelled").

#### Example Message Structure
```json
{
  "type": "BALANCE_UPDATED",
  "messageId": "17715855153",
  "timestamp": "2026-02-20T16:37:20.861965+05:30[Asia/Kolkata]",
  "payload": {
    "roundId": "1771585514",
    "newBalance": 900,
    "reason": "Bet Placed: You have placed a bet of 100 INR. Your new balance is 900 INR."
  }
}
```