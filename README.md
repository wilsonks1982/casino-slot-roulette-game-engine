# European Class III Roulette Game Engine

## Architecture Type: Microservice with Real-Time Gaming Backend
A Spring Boot-based gaming service that implements a European roulette game engine with real-time WebSocket communication, Akka actor-based concurrency, and MongoDB persistence for game history tracking.

## Key Features

* ✅ Multi-table Support - Concurrent games on different tableIds
* ✅ Real-time WebSocket - Sub-100ms message delivery
* ✅ European Roulette - Accurate 37-number wheel (0-36)
* ✅ Hot/Cold Analytics - Dynamic number frequency tracking
* ✅ Group Statistics - Distribution across sections (Tiers, Voisins, Orphelins, Zero)
* ✅ Concurrent Players - Akka-based actor model for scalability
* ✅ Regulatory Compliance - Class III European gaming standards
* ✅ Persistence - Complete game history in MongoDB
* ✅ Docker Ready - Production-deployable containerization

## Technology Stack
* Framework	Spring Boot	3.4.5
* JDK	Java	21
* Concurrency	Akka Actor (Classic)	2.6.20
* Real-time Communication	WebSocket	STOMP Protocol
* Database	MongoDB	(via Spring Data MongoDB)
* Build Tool	Maven	3.x
* ORM/Mapping	Spring Data MongoDB	-
* Code Generation	Lombok	1.18.30
* Security	Spring Security	6.x
* Templating	Thymeleaf	-
* Documentation	SpringDoc OpenAPI	2.2.0
* Testing	JUnit 5, Mockito	-
* Deployment	Docker	Multi-stage Build


## Core Components

### WebSocket Communication

`Endpoint: ws://api.egmserver.com:9090/websocket/game?egmId={egmId}&uid={uid}&tableId={tableId}
Connection Parameters:
egmId: Electronic Gaming Machine ID
uid: Unique User ID
tableId: Table identifier for multi-table support`

### Message Types (Request/Response)

#### Server-to-Client Messages:

1. INITIAL_DATA - Initial connection payload (table info, balance, game state)
2. ROUND_CREATED - New round initialization
3. BETTING_OPENED - Betting phase begins with countdown
4. BETTING_CLOSED - No more bets accepted
5. BALL_RELEASED - Physical ball release (animation trigger)
6. WHEEL_SPINNING - Wheel in motion
7. RESULT_DECLARED - Winning number + statistics (hot/cold numbers)
8. PAYOUT_COMPLETED - Payout calculated and balance updated
9. BALANCE_UPDATED - Wallet balance change notification
10. BET_PLACED - Bet confirmation with new balance
11. BET_REJECTED - Bet failure with reason
12. BET_CANCELLED - Cancellation confirmation

#### Client-to-Server Messages:

1. PLACE_BET - Player places bets with stake and bet list
2. BET_CANCELLED - Cancel previous bet
3. HEARTBEAT - Keep-alive signal every 30 seconds


### Betting Types Supported

#### Inside Bets:
* Straight Up (Single Number) - 35:1 payout
* Split Bet (2 numbers) - 17:1 payout
* Street Bet (3 numbers) - 11:1 payout
* Corner Bet (4 numbers) - 8:1 payout
* Line Bet (6 numbers) - 5:1 payout

#### Outside Bets:
* Dozen (1-12, 13-24, 25-36) - 2:1 payout
* Column (1st, 2nd, 3rd) - 2:1 payout
* Red/Black - 1:1 payout
* Odd/Even - 1:1 payout
* High/Low (1-18, 19-36) - 1:1 payout

#### Call Bets (European):

* Voisins (Neighbors of 0)
* Tiers (Opposite sector to Voisins)
* Orphelins
* Zero Bet

### Game Flow State Machine 
```
[IDLE]
↓
[ROUND_CREATED] → Initialize round, reset player bets
↓
[BETTING_OPENED] → Start betting timer (60 seconds)
├─ Players place/cancel bets
├─ Balance updates in real-time
↓
[BETTING_CLOSED] → Lock bets, prevent new placements
↓
[BALL_RELEASED] → Trigger animations
↓
[WHEEL_SPINNING] → Execute spin algorithm
↓
[RESULT_DECLARED] → Announce winning number + attributes
├─ Distribute payouts
├─ Update statistics
├─ Calculate hot/cold numbers
↓
[PAYOUT_COMPLETED] → Update balance, send confirmation
↓
[IDLE] → Await next round
```
## Performance Considerations

* Stateless WebSocket Handler for horizontal scaling
* Actor system isolates state per table and player
* MongoDB aggregation pipelines for statistics computation
* Caching layer for hot/cold number calculations
* Connection pooling for database access