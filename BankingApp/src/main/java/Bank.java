import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Bank class, using the Singleton pattern.
 * This is the central facade for all banking operations.
 * It manages the in-memory account cache and coordinates
 * with the persistence layer (AccountRepository).
 */
public class Bank {
    private static final Bank instance = new Bank();
    private final Map<String, Account> accounts; // In-memory cache
    private final AccountRepository repository;
    private final TransactionLogger logger;

    private Bank() {
        this.repository = new FileAccountRepository();
        this.logger = TransactionLogger.getInstance();
        this.accounts = new HashMap<>();

        // Eagerly load all accounts from persistence on startup
        loadAllAccounts();
    }

    private void loadAllAccounts() {
        List<String> accountIds = repository.getAllAccountIds();
        System.out.println("Loading " + accountIds.size() + " account(s) from persistence...");
        for (String id : accountIds) {
            Account acc = repository.loadAccount(id);
            if (acc != null) {
                this.accounts.put(id, acc);
            }
        }
        System.out.println("...Loading complete.");
    }

    public static Bank getInstance() {
        return instance;
    }

    public Account createAccount(String customerName, double initialDeposit) {
        // Generate a unique account ID
        String accountId = "ACCT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Account account = new Account(accountId, customerName, initialDeposit);

        // Persist and cache
        repository.saveAccount(account);
        accounts.put(accountId, account);

        logger.log("Created Account: " + accountId + " for " + customerName + " with initial deposit R" + initialDeposit);
        return account;
    }

    /**
     * Gets an account. First checks cache, then tries to load from persistence.
     *
     * @param accountId The ID of the account.
     * @return The Account, or null if not found.
     */
    public Account getAccount(String accountId) {
        // Try cache first
        Account acc = accounts.get(accountId);
        if (acc == null) {
            // If not in cache, try loading from repository
            acc = repository.loadAccount(accountId);
            if (acc != null) {
                // Add to cache if found
                accounts.put(accountId, acc);
            }
        }
        return acc;
    }

    public boolean deposit(String accountId, double amount) {
        Account acc = getAccount(accountId);
        if (acc == null) {
            System.err.println("Deposit failed: Account " + accountId + " not found.");
            return false;
        }

        if (acc.deposit(amount, "Customer Deposit")) {
            repository.saveAccount(acc); // Persist changes
            logger.log("Deposit: R" + amount + " to Account: " + accountId);
            return true;
        }
        return false;
    }

    public boolean withdraw(String accountId, double amount) {
        Account acc = getAccount(accountId);
        if (acc == null) {
            System.err.println("Withdrawal failed: Account " + accountId + " not found.");
            return false;
        }

        if (acc.withdraw(amount, "Customer Withdrawal")) {
            repository.saveAccount(acc); // Persist changes
            logger.log("Withdrawal: R" + amount + " from Account: " + accountId);
            return true;
        }
        return false;
    }

    public synchronized boolean transfer(String fromAccountId, String toAccountId, double amount) {
        if (fromAccountId.equals(toAccountId)) {
            System.err.println("Transfer failed: Cannot transfer to the same account.");
            return false;
        }

        Account fromAcc = getAccount(fromAccountId);
        Account toAcc = getAccount(toAccountId);

        if (fromAcc == null) {
            System.err.println("Transfer failed: 'From' account " + fromAccountId + " not found.");
            return false;
        }
        if (toAcc == null) {
            System.err.println("Transfer failed: 'To' account " + toAccountId + " not found.");
            return false;
        }

        // Perform the withdrawal from the 'from' account
        if (fromAcc.withdraw(amount, "Transfer to " + toAccountId)) {
            // If withdrawal is successful, perform the deposit
            if (toAcc.deposit(amount, "Transfer from " + fromAccountId)) {

                // Both operations succeeded, persist both accounts
                repository.saveAccount(fromAcc);
                repository.saveAccount(toAcc);

                logger.log("Transfer: R" + amount + " from " + fromAccountId + " to " + toAccountId);
                return true;
            } else {
                // This should ideally not fail, but if it does, we must roll back.
                // This is a simple rollback. Real systems use database transactions.
                System.err.println("CRITICAL ERROR: Deposit failed after withdrawal. Rolling back withdrawal.");
                fromAcc.deposit(amount, "ROLLBACK: Failed transfer to " + toAccountId);
                logger.log("CRITICAL: Rollback processed for " + fromAccountId + " due to failed transfer deposit.");
                return false;
            }
        }

        // Withdrawal failed (insufficient funds)
        return false;
    }
}