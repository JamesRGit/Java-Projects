# Java Library Management System (Console App)

This is a simple, console-based Library Management System written in Java. It demonstrates core Object-Oriented Programming (OOP) principles, exception handling, and data persistence using Java's built-in serialization.

The entire application is contained within a single (`LibraryManagementSystem.java`) file for ease of compilation and execution.

## Features

**Book Management (CRUD):**

- Add a new book.
- View details of a specific book (by ISBN).
- Update an existing book's details.
- Delete a book (if not currently on loan).
- List all books in the library.

**Member Management (CRUD):**

- Add a new library member.
- View a member's details.
- Update a member's name.
- Delete a member (if they have no active loans).
- List all members.

**Loan Management:**

- Check out a book to a member.
- Return a book.
- List all active loans, showing both book and member details.

**Data Persistence:**

- All library data (books, members, loans) is automatically saved to a (`library.dat`) file upon exiting the program.
- Data is automatically loaded from (`library.dat`) when the program starts.

**Exception Handling:**

- Uses custom exceptions ((`ItemNotFoundException, DuplicateItemException, LoanException`)) to handle business logic errors gracefully (e.g., trying to add a duplicate book, loaning a book that's already out).

## How to Compile and Run

1. Ensure you have a Java Development Kit (JDK) installed (version 8 or higher).
2. Save the code as (`LibraryManagementSystem.java`).
3. Open a terminal or command prompt and navigate to the directory containing the file.
4. **Compile:**

```javac LibraryManagementSystem.java```

**Run:**

```java LibraryManagementSystem```

6. The program will start, and a (`library.dat`) file will be created in the same directory to store data.

## Project Structure (Single File)

The (`LibraryManagementSystem.java`) file is internally organized into several classes to demonstrate separation of concerns:

**(`LibraryManagementSystem`) (public class):**
- Contains the (`main`) method.
- Handles all console UI (menus, user input).
- Manages the main application loop.
- Orchestrates calls to the (`Library`) service.
- Manages saving ((`saveLibrary()`)) and loading ((`loadLibrary()`)) data.

(`Library`) **(package-private class):**
- The core "service" class that holds the business logic.
- Contains the lists for (`books`), (`members`), and (`loans`).
- Provides methods for all CRUD and loan operations (e.g., (`addBook, checkoutBook`).
- Implements (`Serializable`) so the entire state can be saved.

**Data Models (package-private classes):**
- (`Book`): Represents a book (ISBN, title, author, availability).
- (`Member`): Represents a library member (ID, name).
- (`Loan`): Represents an active loan (member ID, ISBN, checkout date).
- All models implement (`Serializable`).

**Custom Exceptions (package-private classes):**
- (`ItemNotFoundException`)
- (`DuplicateItemException`)
- (`LoanException`)
