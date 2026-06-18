package com.wildace.roulette.config;

import com.mongodb.client.MongoClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.wildace.roulette.domain.documents.Transaction;
import com.wildace.roulette.repositories.TransactionRepository;
import com.wildace.roulette.domain.documents.User;
import com.wildace.roulette.repositories.UserRepository;
import com.wildace.roulette.domain.documents.BetLimitsDocument;
import com.wildace.roulette.domain.documents.CoinDocument;
import com.wildace.roulette.domain.documents.PayoutDocument;
import com.wildace.roulette.domain.documents.SpinResult;
import com.wildace.roulette.repositories.BetLimitsRepository;
import com.wildace.roulette.repositories.CoinRepository;
import com.wildace.roulette.repositories.PayoutRepository;
import com.wildace.roulette.repositories.SpinResultRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;



@Configuration
@Profile("dev")
public class DevDataConfiguration {

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        // Create a no-op MongoTemplate that won't be used in dev mode
        // This satisfies the dependency injection requirement
        return new MongoTemplate(Mockito.mock(MongoClient.class), "dev-database") {
            // Override methods if needed to prevent actual MongoDB calls

        };
    }
    @Bean
    public CoinRepository coinRepository() {
        return new InMemoryCoinRepository();
    }

    @Bean
    public PayoutRepository payoutRepository() {
        return new InMemoryPayoutRepository();
    }

    @Bean
    public BetLimitsRepository betLimitsRepository() {
        return new InMemoryBetLimitsRepository();
    }

    @Bean
    public SpinResultRepository spinResultRepository() {
        return new InMemorySpinResultRepository();
    }

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    public TransactionRepository transactionRepository() {
        return new InMemoryTransactionRepository();
    }

    // ==================== InMemoryTransactionRepository ====================
    private static class InMemoryTransactionRepository implements TransactionRepository {
        private final Map<String, Transaction> storage = new ConcurrentHashMap<>();

        public InMemoryTransactionRepository() {
            initializeMockData();
        }

        private void initializeMockData() {
            // Transaction 1 - Deposit by USER123
            Transaction trans1 = new Transaction();
            trans1.setId("trans001");
            trans1.setEgmId("EGM001");
            trans1.setUid("USER123");
            trans1.setTransId("20260205100000001");
            trans1.setTransType("Deposit");
            trans1.setTransBy("Uadmin");
            trans1.setDepositAmount(5000.0);
            trans1.setWithdrawAmount(0.0);
            trans1.setPrevCredit(5000.0);
            trans1.setThenCredit(10000.0);
            trans1.setTransStartTime("2026-02-05 10:00:00");
            trans1.setTransEndTime("2026-02-05 10:00:15");
            storage.put(trans1.getId(), trans1);

            // Transaction 2 - Withdraw by USER123
            Transaction trans2 = new Transaction();
            trans2.setId("trans002");
            trans2.setEgmId("EGM001");
            trans2.setUid("USER123");
            trans2.setTransId("20260205110000002");
            trans2.setTransType("Withdraw");
            trans2.setTransBy("Uadmin");
            trans2.setDepositAmount(0.0);
            trans2.setWithdrawAmount(2000.0);
            trans2.setPrevCredit(11250.0);
            trans2.setThenCredit(9250.0);
            trans2.setTransStartTime("2026-02-05 11:00:00");
            trans2.setTransEndTime("2026-02-05 11:00:20");
            storage.put(trans2.getId(), trans2);

            // Transaction 3 - Deposit by USER456
            Transaction trans3 = new Transaction();
            trans3.setId("trans003");
            trans3.setEgmId("EGM001");
            trans3.setUid("USER456");
            trans3.setTransId("20260205093000003");
            trans3.setTransType("Deposit");
            trans3.setTransBy("Uadmin");
            trans3.setDepositAmount(5000.0);
            trans3.setWithdrawAmount(0.0);
            trans3.setPrevCredit(0.0);
            trans3.setThenCredit(5000.0);
            trans3.setTransStartTime("2026-02-05 09:30:00");
            trans3.setTransEndTime("2026-02-05 09:30:12");
            storage.put(trans3.getId(), trans3);

            // Transaction 4 - Large deposit by VIP001
            Transaction trans4 = new Transaction();
            trans4.setId("trans004");
            trans4.setEgmId("EGM001");
            trans4.setUid("VIP001");
            trans4.setTransId("20260205080000004");
            trans4.setTransType("Deposit");
            trans4.setTransBy("Uadmin");
            trans4.setDepositAmount(50000.0);
            trans4.setWithdrawAmount(0.0);
            trans4.setPrevCredit(0.0);
            trans4.setThenCredit(50000.0);
            trans4.setTransStartTime("2026-02-05 08:00:00");
            trans4.setTransEndTime("2026-02-05 08:00:18");
            storage.put(trans4.getId(), trans4);
        }

        @Override
        public <S extends Transaction> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID().toString());
            }
            storage.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public <S extends Transaction> List<S> saveAll(Iterable<S> entities) {
            List<S> result = new ArrayList<>();
            entities.forEach(entity -> result.add(save(entity)));
            return result;
        }

        @Override
        public Optional<Transaction> findById(String id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean existsById(String id) {
            return storage.containsKey(id);
        }

        @Override
        public List<Transaction> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<Transaction> findAllById(Iterable<String> ids) {
            List<Transaction> result = new ArrayList<>();
            ids.forEach(id -> findById(id).ifPresent(result::add));
            return result;
        }

        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public void deleteById(String id) {
            storage.remove(id);
        }

        @Override
        public void delete(Transaction entity) {
            storage.remove(entity.getId());
        }

        @Override
        public void deleteAllById(Iterable<? extends String> ids) {
            ids.forEach(storage::remove);
        }

        @Override
        public void deleteAll(Iterable<? extends Transaction> entities) {
            entities.forEach(entity -> storage.remove(entity.getId()));
        }

        @Override
        public void deleteAll() {
            storage.clear();
            initializeMockData();
        }

        @Override
        public List<Transaction> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<Transaction> findAll(Pageable pageable) {
            throw new UnsupportedOperationException("Pagination not supported in dev mode");
        }

        @Override
        public <S extends Transaction> S insert(S entity) {
            return save(entity);
        }

        @Override
        public <S extends Transaction> List<S> insert(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public <S extends Transaction> Optional<S> findOne(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends Transaction> List<S> findAll(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends Transaction> List<S> findAll(Example<S> example, Sort sort) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends Transaction> Page<S> findAll(Example<S> example, Pageable pageable) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends Transaction> long count(Example<S> example) {
            return count();
        }

        @Override
        public <S extends Transaction> boolean exists(Example<S> example) {
            return !storage.isEmpty();
        }

        @Override
        public <S extends Transaction, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            throw new UnsupportedOperationException("FluentQuery not supported in dev mode");
        }

        // Custom query methods - add if TransactionRepository has them
        // Example:
        // @Override
        // public List<Transaction> findByUid(String uid) {
        //     return storage.values().stream()
        //         .filter(trans -> trans.getUid().equals(uid))
        //         .collect(Collectors.toList());
        // }
        //
        // @Override
        // public List<Transaction> findByTransType(String transType) {
        //     return storage.values().stream()
        //         .filter(trans -> trans.getTransType().equals(transType))
        //         .collect(Collectors.toList());
        // }
    }

    // Add this inner class at the end of DevDataConfiguration
// ==================== InMemoryUserRepository ====================
    private static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> storage = new ConcurrentHashMap<>();

        public InMemoryUserRepository() {
            initializeMockData();
        }

        private void initializeMockData() {
            // Create mock user 1 - Active player
            User user1 = new User();
            user1.setId(1001);
            user1.setUid("USER123");
            user1.setRole("PLAYER");
            user1.setNickname("aceplayer");
            user1.setFirstName("John");
            user1.setLastName("Doe");
            user1.setWallet(10000.0);
            user1.setPin(1234);
            user1.setIsPlaying(true);
            user1.setCreatedAt("2025-05-15 14:30:00");
            user1.setUpdatedAt("2026-02-05 10:00:00");
            storage.put(user1.getUid(), user1);

            // Create mock user 2 - Inactive player
            User user2 = new User();
            user2.setId(1002);
            user2.setUid("USER456");
            user2.setRole("PLAYER");
            user2.setNickname("luckystar");
            user2.setFirstName("Jane");
            user2.setLastName("Smith");
            user2.setWallet(5000.0);
            user2.setPin(5678);
            user2.setIsPlaying(false);
            user2.setCreatedAt("2025-06-01 09:15:00");
            user2.setUpdatedAt("2026-02-05 09:30:00");
            storage.put(user2.getUid(), user2);

            // Create mock user 3 - VIP player
            User user3 = new User();
            user3.setId(1003);
            user3.setUid("VIP001");
            user3.setRole("VIP");
            user3.setNickname("highroller");
            user3.setFirstName("Michael");
            user3.setLastName("Johnson");
            user3.setWallet(50000.0);
            user3.setPin(9999);
            user3.setIsPlaying(true);
            user3.setCreatedAt("2025-01-10 12:00:00");
            user3.setUpdatedAt("2026-02-05 11:00:00");
            storage.put(user3.getUid(), user3);
        }

        @Override
        public <S extends User> S save(S entity) {
            if (entity.getUid() == null) {
                entity.setUid(UUID.randomUUID().toString());
            }
            if (entity.getUpdatedAt() == null) {
                entity.setUpdatedAt(java.time.LocalDateTime.now().toString());
            }
            storage.put(entity.getUid(), entity);
            return entity;
        }

        @Override
        public <S extends User> List<S> saveAll(Iterable<S> entities) {
            List<S> result = new ArrayList<>();
            entities.forEach(entity -> result.add(save(entity)));
            return result;
        }

        @Override
        public Optional<User> findById(String id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean existsById(String id) {
            return storage.containsKey(id);
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public User findOneAndUpdate(String id, User updatedUser) {
            User existingUser = storage.get(id);
            if (existingUser != null) {
                // Update fields
                existingUser.setRole(updatedUser.getRole());
                existingUser.setNickname(updatedUser.getNickname());
                existingUser.setFirstName(updatedUser.getFirstName());
                existingUser.setLastName(updatedUser.getLastName());
                existingUser.setWallet(updatedUser.getWallet());
                existingUser.setPin(updatedUser.getPin());
                existingUser.setIsPlaying(updatedUser.getIsPlaying());
                existingUser.setUpdatedAt(java.time.LocalDateTime.now().toString());
                storage.put(id, existingUser);
                return existingUser;
            }
            return null;
        }

        @Override
        public User findOne(String uid) {
            return storage.get(uid);
        }

        @Override
        public User updateOne(String uid, User updatedUser) {
            return findOneAndUpdate(uid, updatedUser);
        }

        @Override
        public List<User> findAllById(Iterable<String> ids) {
            List<User> result = new ArrayList<>();
            ids.forEach(id -> findById(id).ifPresent(result::add));
            return result;
        }

        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public void deleteById(String id) {
            storage.remove(id);
        }

        @Override
        public void delete(User entity) {
            storage.remove(entity.getUid());
        }

        @Override
        public void deleteAllById(Iterable<? extends String> ids) {
            ids.forEach(storage::remove);
        }

        @Override
        public void deleteAll(Iterable<? extends User> entities) {
            entities.forEach(entity -> storage.remove(entity.getUid()));
        }

        @Override
        public void deleteAll() {
            storage.clear();
            initializeMockData();
        }

        @Override
        public List<User> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<User> findAll(Pageable pageable) {
            throw new UnsupportedOperationException("Pagination not supported in dev mode");
        }

        @Override
        public <S extends User> S insert(S entity) {
            return save(entity);
        }

        @Override
        public <S extends User> List<S> insert(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public <S extends User> Optional<S> findOne(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends User> List<S> findAll(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends User> long count(Example<S> example) {
            return count();
        }

        @Override
        public <S extends User> boolean exists(Example<S> example) {
            return !storage.isEmpty();
        }

        @Override
        public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            throw new UnsupportedOperationException("FluentQuery not supported in dev mode");
        }

        // Custom query methods - implement if UserRepository has them
        // Add implementations for any custom finder methods in your UserRepository interface
        // For example:
        // @Override
        // public Optional<User> findByUid(String uid) {
        //     return storage.values().stream()
        //         .filter(user -> user.getUid().equals(uid))
        //         .findFirst();
        // }
    }
    // ==================== InMemorySpinResultRepository ====================
    private static class InMemorySpinResultRepository implements SpinResultRepository {
        private final Map<String, SpinResult> storage = new ConcurrentHashMap<>();

        public InMemorySpinResultRepository() {
            initializeMockData();
        }

        private void initializeMockData() {
            // Create mock spin result 1 - Winning spin
            SpinResult spin1 = new SpinResult();
            spin1.setId("1");
            spin1.setEgmId("EGM001");
            spin1.setUid("USER123");
            spin1.setWallet(10000.0);
            spin1.setSpinNumber("SPIN001");
            spin1.setNumber(17); // Winning number
            spin1.setOldCredit(10000.0);
            spin1.setBetAmount(500.0);
            spin1.setWinAmount(1750.0); // Won on split bet (17:1)
            spin1.setNewCredit(11250.0);
            spin1.setSpinStart("2026-02-05T10:30:00");
            spin1.setSpinEnd("2026-02-05T10:30:45");

            // Bets placed
            SpinResult.Bet bet1 = new SpinResult.Bet();
            bet1.setBetIndex(17); // Straight up on 17
            bet1.setBetAmount(100.0);

            SpinResult.Bet bet2 = new SpinResult.Bet();
            bet2.setBetIndex(150); // Split bet including 17
            bet2.setBetAmount(200.0);

            SpinResult.Bet bet3 = new SpinResult.Bet();
            bet3.setBetIndex(200); // Red (losing)
            bet3.setBetAmount(200.0);

            spin1.setBetsList(Arrays.asList(bet1, bet2, bet3));

            // Won bets
            SpinResult.WonBet won1 = new SpinResult.WonBet();
            won1.setBetIndex(17);
            won1.setBetAmount(100.0);
            won1.setWinAmount(3500.0); // 35:1 payout
            won1.setName("Straight Up on 17");

            SpinResult.WonBet won2 = new SpinResult.WonBet();
            won2.setBetIndex(150);
            won2.setBetAmount(200.0);
            won2.setWinAmount(3400.0); // 17:1 payout
            won2.setName("Split 14/17");

            spin1.setWonBetsList(Arrays.asList(won1, won2));

            storage.put("1", spin1);

            // Create mock spin result 2 - Losing spin
            SpinResult spin2 = new SpinResult();
            spin2.setId("2");
            spin2.setEgmId("EGM001");
            spin2.setUid("USER123");
            spin2.setWallet(11250.0);
            spin2.setSpinNumber("SPIN002");
            spin2.setNumber(0); // Zero - most bets lose
            spin2.setOldCredit(11250.0);
            spin2.setBetAmount(300.0);
            spin2.setWinAmount(0.0);
            spin2.setNewCredit(10950.0);
            spin2.setSpinStart("2026-02-05T10:32:00");
            spin2.setSpinEnd("2026-02-05T10:32:45");

            SpinResult.Bet bet4 = new SpinResult.Bet();
            bet4.setBetIndex(100); // Black
            bet4.setBetAmount(100.0);

            SpinResult.Bet bet5 = new SpinResult.Bet();
            bet5.setBetIndex(101); // Even
            bet5.setBetAmount(100.0);

            SpinResult.Bet bet6 = new SpinResult.Bet();
            bet6.setBetIndex(25); // Straight on 25
            bet6.setBetAmount(100.0);

            spin2.setBetsList(Arrays.asList(bet4, bet5, bet6));
            spin2.setWonBetsList(Collections.emptyList()); // No wins

            storage.put("2", spin2);

            // Create mock spin result 3 - Small win on outside bet
            SpinResult spin3 = new SpinResult();
            spin3.setId("3");
            spin3.setEgmId("EGM001");
            spin3.setUid("USER456");
            spin3.setWallet(5000.0);
            spin3.setSpinNumber("SPIN003");
            spin3.setNumber(32); // Red, Even, High
            spin3.setOldCredit(5000.0);
            spin3.setBetAmount(600.0);
            spin3.setWinAmount(600.0); // Even money on red
            spin3.setNewCredit(5000.0);
            spin3.setSpinStart("2026-02-05T10:35:00");
            spin3.setSpinEnd("2026-02-05T10:35:45");

            SpinResult.Bet bet7 = new SpinResult.Bet();
            bet7.setBetIndex(200); // Red
            bet7.setBetAmount(300.0);

            SpinResult.Bet bet8 = new SpinResult.Bet();
            bet8.setBetIndex(101); // Even
            bet8.setBetAmount(200.0);

            SpinResult.Bet bet9 = new SpinResult.Bet();
            bet9.setBetIndex(100); // Black (losing)
            bet9.setBetAmount(100.0);

            spin3.setBetsList(Arrays.asList(bet7, bet8, bet9));

            SpinResult.WonBet won3 = new SpinResult.WonBet();
            won3.setBetIndex(200);
            won3.setBetAmount(300.0);
            won3.setWinAmount(300.0); // 1:1 payout
            won3.setName("Red");

            SpinResult.WonBet won4 = new SpinResult.WonBet();
            won4.setBetIndex(101);
            won4.setBetAmount(200.0);
            won4.setWinAmount(200.0); // 1:1 payout
            won4.setName("Even");

            spin3.setWonBetsList(Arrays.asList(won3, won4));

            storage.put("3", spin3);
        }

        @Override
        public <S extends SpinResult> S save(S entity) {
            if (entity.getId() == null) entity.setId(UUID.randomUUID().toString());
            storage.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public <S extends SpinResult> List<S> saveAll(Iterable<S> entities) {
            List<S> result = new ArrayList<>();
            entities.forEach(entity -> result.add(save(entity)));
            return result;
        }

        @Override
        public Optional<SpinResult> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }



        @Override
        public List<SpinResult> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<SpinResult> findAllById(Iterable<Integer> integers) {
            List<SpinResult> result = new ArrayList<>();
            integers.forEach(id -> {
                storage.values().stream()
                        .filter(spin -> spin.getId().equals(String.valueOf(id)))
                        .findFirst()
                        .ifPresent(result::add);
            });
            return result;
        }


        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public void deleteById(Integer integer) {

        }


        @Override
        public void delete(SpinResult entity) {
            storage.remove(entity.getId());
        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {

        }

        @Override
        public void deleteAll(Iterable<? extends SpinResult> entities) {
            entities.forEach(entity -> storage.remove(entity.getId()));
        }

        @Override
        public void deleteAll() {
            storage.clear();
            initializeMockData();
        }

        @Override
        public List<SpinResult> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<SpinResult> findAll(Pageable pageable) {
            throw new UnsupportedOperationException("Pagination not supported in dev mode");
        }

        @Override
        public <S extends SpinResult> S insert(S entity) {
            return save(entity);
        }

        @Override
        public <S extends SpinResult> List<S> insert(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public <S extends SpinResult> Optional<S> findOne(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends SpinResult> List<S> findAll(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends SpinResult> List<S> findAll(Example<S> example, Sort sort) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends SpinResult> Page<S> findAll(Example<S> example, Pageable pageable) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends SpinResult> long count(Example<S> example) {
            return count();
        }

        @Override
        public <S extends SpinResult> boolean exists(Example<S> example) {
            return !storage.isEmpty();
        }

        @Override
        public <S extends SpinResult, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            throw new UnsupportedOperationException("FluentQuery not supported in dev mode");
        }

        @Override
        public List<SpinResult> findAllByUid(String uid) {
            List<SpinResult> result = new ArrayList<>();
            storage.values().stream()
                    .filter(spin -> spin.getUid().equals(uid))
                    .forEach(result::add);
            return result;
        }

        @Override
        public List<SpinResult> findAllByEgmId(String egmId) {
            List<SpinResult> result = new ArrayList<>();
            storage.values().stream()
                    .filter(spin -> spin.getEgmId().equals(egmId))
                    .forEach(result::add);
            return result;
        }
    }

    // ==================== InMemoryCoinRepository ====================
    private static class InMemoryCoinRepository implements CoinRepository {
        private final Map<String, CoinDocument> storage = new ConcurrentHashMap<>();

        public InMemoryCoinRepository() {
            initializeMockData();
        }

        private void initializeMockData() {
            List<CoinDocument.Coin> mockCoins = Arrays.asList(
                    new CoinDocument.Coin(1, 10, "blue", "INR"),
                    new CoinDocument.Coin(2, 50, "green", "INR"),
                    new CoinDocument.Coin(3, 100, "red", "INR"),
                    new CoinDocument.Coin(4, 500, "purple", "INR"),
                    new CoinDocument.Coin(5, 1000, "gold", "INR")
            );
            storage.put("1", new CoinDocument("1", mockCoins));
        }

        @Override
        public <S extends CoinDocument> S save(S entity) {
            if (entity.getId() == null) entity.setId(UUID.randomUUID().toString());
            storage.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public <S extends CoinDocument> List<S> saveAll(Iterable<S> entities) {
            List<S> result = new ArrayList<>();
            entities.forEach(entity -> result.add(save(entity)));
            return result;
        }

        @Override
        public Optional<CoinDocument> findById(String id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean existsById(String id) {
            return storage.containsKey(id);
        }

        @Override
        public List<CoinDocument> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<CoinDocument> findAllById(Iterable<String> ids) {
            List<CoinDocument> result = new ArrayList<>();
            ids.forEach(id -> findById(id).ifPresent(result::add));
            return result;
        }

        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public void deleteById(String id) {
            storage.remove(id);
        }

        @Override
        public void delete(CoinDocument entity) {
            storage.remove(entity.getId());
        }

        @Override
        public void deleteAllById(Iterable<? extends String> ids) {
            ids.forEach(storage::remove);
        }

        @Override
        public void deleteAll(Iterable<? extends CoinDocument> entities) {
            entities.forEach(entity -> storage.remove(entity.getId()));
        }

        @Override
        public void deleteAll() {
            storage.clear();
            initializeMockData();
        }

        @Override
        public List<CoinDocument> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<CoinDocument> findAll(Pageable pageable) {
            throw new UnsupportedOperationException("Pagination not supported in dev mode");
        }

        @Override
        public <S extends CoinDocument> S insert(S entity) {
            return save(entity);
        }

        @Override
        public <S extends CoinDocument> List<S> insert(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public <S extends CoinDocument> Optional<S> findOne(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends CoinDocument> List<S> findAll(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends CoinDocument> List<S> findAll(Example<S> example, Sort sort) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends CoinDocument> Page<S> findAll(Example<S> example, Pageable pageable) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends CoinDocument> long count(Example<S> example) {
            return count();
        }

        @Override
        public <S extends CoinDocument> boolean exists(Example<S> example) {
            return !storage.isEmpty();
        }

        @Override
        public <S extends CoinDocument, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            throw new UnsupportedOperationException("FluentQuery not supported in dev mode");
        }
    }

    // ==================== InMemoryPayoutRepository ====================
    private static class InMemoryPayoutRepository implements PayoutRepository {
        private final Map<String, PayoutDocument> storage = new ConcurrentHashMap<>();

        public InMemoryPayoutRepository() {
            initializeMockData();
        }

        private void initializeMockData() {
            List<PayoutDocument.Payout> mockPayouts = Arrays.asList(
                    new PayoutDocument.Payout(1, "Straight Up", 35),
                    new PayoutDocument.Payout(2, "Split", 17),
                    new PayoutDocument.Payout(3, "Street", 11),
                    new PayoutDocument.Payout(4, "Corner", 8),
                    new PayoutDocument.Payout(5, "Line", 5),
                    new PayoutDocument.Payout(6, "Column", 2),
                    new PayoutDocument.Payout(7, "Dozen", 2),
                    new PayoutDocument.Payout(8, "Red/Black", 1),
                    new PayoutDocument.Payout(9, "Even/Odd", 1),
                    new PayoutDocument.Payout(10, "High/Low", 1)
            );
            storage.put("1", new PayoutDocument("1", mockPayouts));
        }

        @Override
        public <S extends PayoutDocument> S save(S entity) {
            if (entity.getId() == null) entity.setId(UUID.randomUUID().toString());
            storage.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public <S extends PayoutDocument> List<S> saveAll(Iterable<S> entities) {
            List<S> result = new ArrayList<>();
            entities.forEach(entity -> result.add(save(entity)));
            return result;
        }

        @Override
        public Optional<PayoutDocument> findById(String id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean existsById(String id) {
            return storage.containsKey(id);
        }

        @Override
        public List<PayoutDocument> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<PayoutDocument> findAllById(Iterable<String> ids) {
            List<PayoutDocument> result = new ArrayList<>();
            ids.forEach(id -> findById(id).ifPresent(result::add));
            return result;
        }

        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public void deleteById(String id) {
            storage.remove(id);
        }

        @Override
        public void delete(PayoutDocument entity) {
            storage.remove(entity.getId());
        }

        @Override
        public void deleteAllById(Iterable<? extends String> ids) {
            ids.forEach(storage::remove);
        }

        @Override
        public void deleteAll(Iterable<? extends PayoutDocument> entities) {
            entities.forEach(entity -> storage.remove(entity.getId()));
        }

        @Override
        public void deleteAll() {
            storage.clear();
            initializeMockData();
        }

        @Override
        public List<PayoutDocument> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<PayoutDocument> findAll(Pageable pageable) {
            throw new UnsupportedOperationException("Pagination not supported in dev mode");
        }

        @Override
        public <S extends PayoutDocument> S insert(S entity) {
            return save(entity);
        }

        @Override
        public <S extends PayoutDocument> List<S> insert(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public <S extends PayoutDocument> Optional<S> findOne(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends PayoutDocument> List<S> findAll(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends PayoutDocument> List<S> findAll(Example<S> example, Sort sort) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends PayoutDocument> Page<S> findAll(Example<S> example, Pageable pageable) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends PayoutDocument> long count(Example<S> example) {
            return count();
        }

        @Override
        public <S extends PayoutDocument> boolean exists(Example<S> example) {
            return !storage.isEmpty();
        }

        @Override
        public <S extends PayoutDocument, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            throw new UnsupportedOperationException("FluentQuery not supported in dev mode");
        }
    }

    // ==================== InMemoryBetLimitsRepository ====================
    private static class InMemoryBetLimitsRepository implements BetLimitsRepository {
        private final Map<String, BetLimitsDocument> storage = new ConcurrentHashMap<>();

        public InMemoryBetLimitsRepository() {
            initializeMockData();
        }

        private void initializeMockData() {
            // Create Inside Bets category
            BetLimitsDocument.Category insideBets = new BetLimitsDocument.Category();
            insideBets.setCategory("Inside Bets");
            insideBets.setLimits(Arrays.asList(
                    createLimit(1, "Min Straight Up Bet", 10),
                    createLimit(2, "Max Straight Up Bet", 1000),
                    createLimit(3, "Min Split Bet", 10),
                    createLimit(4, "Max Split Bet", 2000),
                    createLimit(5, "Min Street Bet", 10),
                    createLimit(6, "Max Street Bet", 3000),
                    createLimit(7, "Min Corner Bet", 10),
                    createLimit(8, "Max Corner Bet", 4000)
            ));

            // Create Outside Bets category
            BetLimitsDocument.Category outsideBets = new BetLimitsDocument.Category();
            outsideBets.setCategory("Outside Bets");
            outsideBets.setLimits(Arrays.asList(
                    createLimit(9, "Min Red/Black Bet", 50),
                    createLimit(10, "Max Red/Black Bet", 10000),
                    createLimit(11, "Min Even/Odd Bet", 50),
                    createLimit(12, "Max Even/Odd Bet", 10000),
                    createLimit(13, "Min High/Low Bet", 50),
                    createLimit(14, "Max High/Low Bet", 10000),
                    createLimit(15, "Min Dozen Bet", 50),
                    createLimit(16, "Max Dozen Bet", 5000),
                    createLimit(17, "Min Column Bet", 50),
                    createLimit(18, "Max Column Bet", 5000)
            ));

            BetLimitsDocument betLimits = new BetLimitsDocument();
            betLimits.setId("1");
            betLimits.setLimits(Arrays.asList(insideBets, outsideBets));

            storage.put("1", betLimits);
        }

        private BetLimitsDocument.Limit createLimit(int id, String name, int amount) {
            BetLimitsDocument.Limit limit = new BetLimitsDocument.Limit();
            limit.setId(id);
            limit.setName(name);
            limit.setAmount(amount);
            return limit;
        }

        @Override
        public <S extends BetLimitsDocument> S save(S entity) {
            if (entity.getId() == null) entity.setId(UUID.randomUUID().toString());
            storage.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public <S extends BetLimitsDocument> List<S> saveAll(Iterable<S> entities) {
            List<S> result = new ArrayList<>();
            entities.forEach(entity -> result.add(save(entity)));
            return result;
        }

        @Override
        public Optional<BetLimitsDocument> findById(String id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean existsById(String id) {
            return storage.containsKey(id);
        }

        @Override
        public List<BetLimitsDocument> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<BetLimitsDocument> findAllById(Iterable<String> ids) {
            List<BetLimitsDocument> result = new ArrayList<>();
            ids.forEach(id -> findById(id).ifPresent(result::add));
            return result;
        }

        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public void deleteById(String id) {
            storage.remove(id);
        }

        @Override
        public void delete(BetLimitsDocument entity) {
            storage.remove(entity.getId());
        }

        @Override
        public void deleteAllById(Iterable<? extends String> ids) {
            ids.forEach(storage::remove);
        }

        @Override
        public void deleteAll(Iterable<? extends BetLimitsDocument> entities) {
            entities.forEach(entity -> storage.remove(entity.getId()));
        }

        @Override
        public void deleteAll() {
            storage.clear();
            initializeMockData();
        }

        @Override
        public List<BetLimitsDocument> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<BetLimitsDocument> findAll(Pageable pageable) {
            throw new UnsupportedOperationException("Pagination not supported in dev mode");
        }

        @Override
        public <S extends BetLimitsDocument> S insert(S entity) {
            return save(entity);
        }

        @Override
        public <S extends BetLimitsDocument> List<S> insert(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public <S extends BetLimitsDocument> Optional<S> findOne(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends BetLimitsDocument> List<S> findAll(Example<S> example) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends BetLimitsDocument> List<S> findAll(Example<S> example, Sort sort) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends BetLimitsDocument> Page<S> findAll(Example<S> example, Pageable pageable) {
            throw new UnsupportedOperationException("Example queries not supported in dev mode");
        }

        @Override
        public <S extends BetLimitsDocument> long count(Example<S> example) {
            return count();
        }

        @Override
        public <S extends BetLimitsDocument> boolean exists(Example<S> example) {
            return !storage.isEmpty();
        }

        @Override
        public <S extends BetLimitsDocument, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            throw new UnsupportedOperationException("FluentQuery not supported in dev mode");
        }

        @Override
        public List<BetLimitsDocument> findByLimitsCategory(String category) {
            List<BetLimitsDocument> result = new ArrayList<>();
            storage.values().forEach(doc -> {
                // Check if this document has the category
                boolean hasCategory = doc.getLimits().stream()
                        .anyMatch(cat -> cat.getCategory().equals(category));
                if (hasCategory) {
                    result.add(doc);
                }
            });
            return result;
        }

        @Override
        public List<BetLimitsDocument> findByLimitsLimitsName(String limitName) {
            List<BetLimitsDocument> result = new ArrayList<>();
            storage.values().forEach(doc -> {
                // Check if this document has a limit with the given name
                boolean hasLimit = doc.getLimits().stream()
                        .flatMap(cat -> cat.getLimits().stream())
                        .anyMatch(limit -> limit.getName().equals(limitName));
                if (hasLimit) {
                    result.add(doc);
                }
            });
            return result;
        }
    }
}