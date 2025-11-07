# Java Banking Simulation

This is a simple command-line banking application written in Java. It serves as a practical example of Object-Oriented Programming (OOP), common design patterns, and basic file-based persistence.

## Features

- **Create Account**: Create a new bank account for a customer with an initial deposit.
- **Deposit**: Add funds to an existing account.
- **Withdraw**: Remove funds from an existing account, checking for sufficient balance.
- **Transfer**: Move funds from one account to another, with rollback logic for failed transfers.
- **View Balance**: Check the current balance of any account.
- **View History**: See a complete transaction log for a specific account.

## How to Run

You can compile and run this project from any standard Java Development Kit (JDK) on the command line.

1. Compile all Java files:

```bash
javac *.java
```

2. Run the main application:

```bash
java BankingSimulation
```

You will then see the main menu and can interact with the banking system.

## Project Structure

- `BankingSimulation.java`: The main class with the `main` method. Handles the user menu and console interaction.
- `Bank.java`: The central "gateway" for all banking operations. Manages accounts and coordinates transactions. (Singleton)
- `Account.java`: Represents a single bank account, holding its balance and transaction history.
- `Transaction.java`: An immutable class representing a single transaction (deposit, withdrawal, etc.).
- `TransactionType.java`: An `enum` defining the different types of transactions.
- `AccountRepository.java`: An interface (DAO) defining the contract for saving and loading accounts.
- `FileAccountRepository.java`: A concrete implementation of the repository that saves account data to files.
- `TransactionLogger.java`: A global logger for bank-wide audit events. (Singleton)