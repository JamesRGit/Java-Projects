import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File-based implementation of the AccountRepository (DAO).
 * Saves each account to its own file.
 *
 * File Format:
 * line 1: customerName
 * line 2: balance
 * line 3: ---Transactions---
 * line 4...N: [Transaction.toPersistString()]
 */
public class FileAccountRepository implements AccountRepository {
    public static final String DATA_DIR = "accounts_data";
    private static final String FILE_EXT = ".acc";
    private static final String TX_SEPARATOR = "---Transactions---";

    @Override
    public void saveAccount(Account account) {
        Path path = Paths.get(DATA_DIR, account.getAccountId() + FILE_EXT);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            // Write basic info
            writer.write(account.getCustomerName());
            writer.newLine();
            writer.write(String.valueOf(account.getBalance()));
            writer.newLine();

            // Write transactions
            writer.write(TX_SEPARATOR);
            writer.newLine();
            for (Transaction tx : account.getTransactionLog()) {
                writer.write(tx.toPersistString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save account " + account.getAccountId() + ": " + e.getMessage());
        }
    }

    @Override
    public Account loadAccount(String accountId) {
        Path path = Paths.get(DATA_DIR, accountId + FILE_EXT);
        if (!Files.exists(path)) {
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String customerName = reader.readLine();
            double balance = Double.parseDouble(reader.readLine());
            String separator = reader.readLine();

            if (customerName == null || separator == null || !separator.equals(TX_SEPARATOR)) {
                throw new IOException("Invalid account file format.");
            }

            List<Transaction> transactions = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                Transaction tx = Transaction.fromPersistString(line);
                if (tx != null) {
                    transactions.add(tx);
                }
            }

            // Use the special constructor to load data
            return new Account(accountId, customerName, balance, transactions);

        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load account " + accountId + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> getAllAccountIds() {
        try {
            return Files.list(Paths.get(DATA_DIR))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(FILE_EXT))
                    .map(name -> name.substring(0, name.length() - FILE_EXT.length()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Failed to list account files: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}