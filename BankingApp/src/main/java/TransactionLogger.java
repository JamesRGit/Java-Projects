import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global audit logger using the Singleton pattern.
 * Logs major bank-wide events to a single file.
 */
public class TransactionLogger {
    private static final TransactionLogger instance = new TransactionLogger();
    private static final String LOG_FILE = "bank_audit.log";
    private PrintWriter writer;

    private TransactionLogger() {
        try {
            // Open file in append mode
            this.writer = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("Failed to initialize TransactionLogger: " + e.getMessage());
        }
    }

    public static TransactionLogger getInstance() {
        return instance;
    }

    public void log(String message) {
        if (writer != null) {
            String logEntry = String.format("[%s] %s",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    message
            );
            writer.println(logEntry);
        }
    }
}