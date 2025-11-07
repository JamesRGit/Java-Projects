import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single transaction.
 * This is an immutable object.
 */
public class Transaction {
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String description;

    // Formatter for persisting and displaying timestamps
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Transaction(TransactionType type, double amount, String description) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    /**
     * Private constructor for loading transactions from persistence.
     */
    private Transaction(TransactionType type, double amount, LocalDateTime timestamp, String description) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    /**
     * Formats the transaction as a string for display.
     */
    @Override
    public String toString() {
        return String.format("[%s] %-12s | Amount: R%-10.2f | Details: %s",
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                type,
                amount,
                description
        );
    }

    /**
     * Formats the transaction as a line for file persistence.
     * Uses a simple comma-separated format.
     */
    public String toPersistString() {
        // Format: TYPE,AMOUNT,TIMESTAMP,DESCRIPTION
        String cleanDescription = description.replace(",", ";");
        return String.format("%s,%f,%s,%s",
                type.name(),
                amount,
                timestamp.format(ISO_FORMATTER),
                cleanDescription
        );
    }

    /**
     * Static factory method to create a Transaction object from a persisted string.
     */
    public static Transaction fromPersistString(String line) {
        try {
            String[] parts = line.split(",", 4);
            TransactionType type = TransactionType.valueOf(parts[0]);
            double amount = Double.parseDouble(parts[1]);
            LocalDateTime timestamp = LocalDateTime.parse(parts[2], ISO_FORMATTER);
            String description = parts[3];
            return new Transaction(type, amount, timestamp, description);
        } catch (Exception e) {
            System.err.println("Failed to parse transaction line: " + line);
            return null;
        }
    }
}