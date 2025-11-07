import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Main class to run the banking simulation.
 * This class contains the user interface (command-line menu)
 * and the main method to start the application.
 */
public class BankingSimulation {

    public static void main(String[] args) {
        // Ensure data directories exist
        try {
            Files.createDirectories(Paths.get(FileAccountRepository.DATA_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
            return;
        }

        Bank bank = Bank.getInstance();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("Welcome to the Java Banking Simulation!");

        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Create New Account");
            System.out.println("2. Deposit Funds");
            System.out.println("3. Withdraw Funds");
            System.out.println("4. Transfer Funds");
            System.out.println("5. View Account Balance");
            System.out.println("6. View Account Transaction History");
            System.out.println("7. Exit");
            System.out.print("Please choose an option: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        createAccount(scanner, bank);
                        break;
                    case "2":
                        performDeposit(scanner, bank);
                        break;
                    case "3":
                        performWithdrawal(scanner, bank);
                        break;
                    case "4":
                        performTransfer(scanner, bank);
                        break;
                    case "5":
                        viewBalance(scanner, bank);
                        break;
                    case "6":
                        viewHistory(scanner, bank);
                        break;
                    case "7":
                        running = false;
                        System.out.println("Thank you for using James' banking app. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("[Error] An unexpected operation failed: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void createAccount(Scanner scanner, Bank bank) {
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        double initialDeposit = 0;
        boolean validAmount = false;
        while (!validAmount) {
            System.out.print("Enter initial deposit amount (must be 0 or more): ");
            try {
                initialDeposit = Double.parseDouble(scanner.nextLine());
                if (initialDeposit < 0) {
                    System.out.println("Initial deposit cannot be negative.");
                } else {
                    validAmount = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        }

        Account newAccount = bank.createAccount(name, initialDeposit);
        System.out.println("Account created successfully!");
        System.out.println("Your new Account ID is: " + newAccount.getAccountId());
    }

    private static void performDeposit(Scanner scanner, Bank bank) {
        System.out.print("Enter account ID: ");
        String accountId = scanner.nextLine();
        System.out.print("Enter amount to deposit: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            if (bank.deposit(accountId, amount)) {
                System.out.println("Deposit successful.");
                viewBalance(accountId, bank);
            } else {
                System.out.println("Deposit failed. Please check the account ID and amount.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
        }
    }

    private static void performWithdrawal(Scanner scanner, Bank bank) {
        System.out.print("Enter account ID: ");
        String accountId = scanner.nextLine();
        System.out.print("Enter amount to withdraw: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            if (bank.withdraw(accountId, amount)) {
                System.out.println("Withdrawal successful.");
                viewBalance(accountId, bank);
            } else {
                System.out.println("Withdrawal failed. Check account ID or ensure sufficient funds.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
        }
    }

    private static void performTransfer(Scanner scanner, Bank bank) {
        System.out.print("Enter YOUR account ID (from): ");
        String fromId = scanner.nextLine();
        System.out.print("Enter RECIPIENT'S account ID (to): ");
        String toId = scanner.nextLine();
        System.out.print("Enter amount to transfer: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            if (bank.transfer(fromId, toId, amount)) {
                System.out.println("Transfer successful.");
                viewBalance(fromId, bank);
            } else {
                System.out.println("Transfer failed. Check account IDs or ensure sufficient funds.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
        }
    }

    private static void viewBalance(Scanner scanner, Bank bank) {
        System.out.print("Enter account ID: ");
        String accountId = scanner.nextLine();
        viewBalance(accountId, bank);
    }

    private static void viewBalance(String accountId, Bank bank) {
        Account acc = bank.getAccount(accountId);
        if (acc != null) {
            System.out.printf("Account: %s, Customer: %s, Balance: R%.2f%n",
                    acc.getAccountId(), acc.getCustomerName(), acc.getBalance());
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void viewHistory(Scanner scanner, Bank bank) {
        System.out.print("Enter account ID: ");
        String accountId = scanner.nextLine();
        Account acc = bank.getAccount(accountId);
        if (acc != null) {
            acc.printTransactionHistory();
        } else {
            System.out.println("Account not found.");
        }
    }
}