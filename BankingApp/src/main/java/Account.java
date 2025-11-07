import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account.
 * Holds customer info, balance, and a log of transactions.
 */
public class Account {
    private final String accountId;
    private final String customerName;
    private double balance;
    private final List<Transaction> transactionLog;

    public Account(String accountId, String customerName, double initialBalance) {
        this.accountId = accountId;
        this.customerName = customerName;
        this.balance = 0; // Balance is set by the initial transaction
        this.transactionLog = new ArrayList<>();

        if (initialBalance > 0) {
            deposit(initialBalance, "Initial Deposit");
        } else if (initialBalance < 0) {
            System.err.println("Initial balance cannot be negative. Setting to 0.");
        }
    }

    /**
     * Special constructor for loading an account from persistence.
     */
    public Account(String accountId, String customerName, double balance, List<Transaction> transactions) {
        this.accountId = accountId;
        this.customerName = customerName;
        this.balance = balance;
        this.transactionLog = new ArrayList<>(transactions);
    }

    /**
     * Deposits a positive amount into the account.
     *
     * @param amount      The amount to deposit (must be > 0).
     * @param description A description of the transaction.
     * @return true if successful, false otherwise.
     */
    public synchronized boolean deposit(double amount, String description) {
        if (amount <= 0) {
            System.err.println("Deposit amount must be positive.");
            return false;
        }
        this.balance += amount;
        addTransaction(new Transaction(TransactionType.DEPOSIT, amount, description));
        return true;
    }

    /**
     * Withdraws a positive amount from the account.
     *
     * @param amount      The amount to withdraw (must be > 0).
     * @param description A description of the transaction.
     * @return true if successful, false if funds are insufficient.
     */
    public synchronized boolean withdraw(double amount, String description) {
        if (amount <= 0) {
            System.err.println("Withdrawal amount must be positive.");
            return false;
        }
        if (this.balance < amount) {
            System.err.println("Insufficient funds for withdrawal.");
            return false;
        }
        this.balance -= amount;
        addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, description));
        return true;
    }

    /**
     * A private helper to add transactions to the log.
     */
    private void addTransaction(Transaction transaction) {
        this.transactionLog.add(transaction);
    }

    public void printTransactionHistory() {
        System.out.println("\n--- Transaction History for Account: " + accountId + " ---");
        if (transactionLog.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction tx : transactionLog) {
                System.out.println(tx);
            }
        }
        System.out.printf("--- Current Balance: R%.2f ---%n", balance);
    }

    // --- Getters ---
    public String getAccountId() {
        return accountId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactionLog() {
        // Return a copy to prevent external modification
        return new ArrayList<>(transactionLog);
    }
}