import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.InputMismatchException;
import java.util.Optional;
import java.util.Scanner;


//  Custom Exceptions for Business Logic
/**
 * Thrown when an item (Book, Member) is not found.
 */
class ItemNotFoundException extends Exception {
    public ItemNotFoundException(String message) {
        super(message);
    }
}

/**
 * Thrown when attempting to add an item that already exists (e.g., duplicate ISBN).
 */
class DuplicateItemException extends Exception {
    public DuplicateItemException(String message) {
        super(message);
    }
}

/**
 * Thrown when a book-related loan operation is invalid (e.g., already loaned).
 */
class LoanException extends Exception {
    public LoanException(String message) {
        super(message);
    }
}


//  Data Model: Book
class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private String isbn;
    private String title;
    private String author;
    private boolean isAvailable;

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
    }

    // Getters
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return isAvailable; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return "Book [ISBN: " + isbn + ", Title: '" + title + "', Author: '" + author +
                "', Available: " + (isAvailable ? "Yes" : "No") + "]";
    }
}

//  Data Model: Member
class Member implements Serializable {
    private static final long serialVersionUID = 2L;
    private String memberId;
    private String name;

    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }

    // Getters
    public String getMemberId() { return memberId; }
    public String getName() { return name; }

    // Setters
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "Member [ID: " + memberId + ", Name: '" + name + "']";
    }
}

//  Data Model: Loan
class Loan implements Serializable {
    private static final long serialVersionUID = 3L;
    private String memberId;
    private String isbn;
    private LocalDate checkoutDate;

    public Loan(String memberId, String isbn, LocalDate checkoutDate) {
        this.memberId = memberId;
        this.isbn = isbn;
        this.checkoutDate = checkoutDate;
    }

    // Getters
    public String getMemberId() { return memberId; }
    public String getIsbn() { return isbn; }
    public LocalDate getCheckoutDate() { return checkoutDate; }

    @Override
    public String toString() {
        return "Loan [Member ID: " + memberId + ", Book ISBN: " + isbn +
                ", Checkout Date: " + checkoutDate + "]";
    }
}

//  Service Class: Library (Holds data and business logic)
class Library implements Serializable {
    private static final long serialVersionUID = 4L;

    private final List<Book> books;
    private final List<Member> members;
    private final List<Loan> loans;

    public Library() {
        books = new ArrayList<>();
        members = new ArrayList<>();
        loans = new ArrayList<>();
    }

    //Book Management

    public void addBook(Book book) throws DuplicateItemException {
        if (findBookByIsbn(book.getIsbn()).isPresent()) {
            throw new DuplicateItemException("Book with ISBN " + book.getIsbn() + " already exists.");
        }
        books.add(book);
    }

    public Optional<Book> findBookByIsbn(String isbn) {
        return books.stream()
                .filter(book -> book.getIsbn().equalsIgnoreCase(isbn))
                .findFirst();
    }

    public void updateBook(String isbn, String newTitle, String newAuthor) throws ItemNotFoundException {
        Book book = findBookByIsbn(isbn)
                .orElseThrow(() -> new ItemNotFoundException("Book not found with ISBN: " + isbn));
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
    }

    public void deleteBook(String isbn) throws ItemNotFoundException, LoanException {
        Book book = findBookByIsbn(isbn)
                .orElseThrow(() -> new ItemNotFoundException("Book not found with ISBN: " + isbn));

        if (!book.isAvailable()) {
            throw new LoanException("Cannot delete book. It is currently on loan.");
        }

        books.remove(book);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books); // Return a copy
    }

    // Member Management

    public void addMember(Member member) throws DuplicateItemException {
        if (findMemberById(member.getMemberId()).isPresent()) {
            throw new DuplicateItemException("Member with ID " + member.getMemberId() + " already exists.");
        }
        members.add(member);
    }

    public Optional<Member> findMemberById(String memberId) {
        return members.stream()
                .filter(member -> member.getMemberId().equalsIgnoreCase(memberId))
                .findFirst();
    }

    public void updateMember(String memberId, String newName) throws ItemNotFoundException {
        Member member = findMemberById(memberId)
                .orElseThrow(() -> new ItemNotFoundException("Member not found with ID: " + memberId));
        member.setName(newName);
    }

    public void deleteMember(String memberId) throws ItemNotFoundException, LoanException {
        Member member = findMemberById(memberId)
                .orElseThrow(() -> new ItemNotFoundException("Member not found with ID: " + memberId));

        boolean hasLoans = loans.stream().anyMatch(loan -> loan.getMemberId().equals(memberId));
        if (hasLoans) {
            throw new LoanException("Cannot delete member. Member has active loans.");
        }

        members.remove(member);
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members); // Return a copy
    }

    // Loan Management

    public void checkoutBook(String memberId, String isbn) throws ItemNotFoundException, LoanException {
        Book book = findBookByIsbn(isbn)
                .orElseThrow(() -> new ItemNotFoundException("Book not found with ISBN: " + isbn));

        if (!findMemberById(memberId).isPresent()) {
            throw new ItemNotFoundException("Member not found with ID: " + memberId);
        }

        if (!book.isAvailable()) {
            throw new LoanException("Book is already on loan.");
        }

        book.setAvailable(false);
        loans.add(new Loan(memberId, isbn, LocalDate.now()));
    }

    public void returnBook(String isbn) throws ItemNotFoundException, LoanException {
        Book book = findBookByIsbn(isbn)
                .orElseThrow(() -> new ItemNotFoundException("Book not found with ISBN: " + isbn));

        Loan loan = loans.stream()
                .filter(l -> l.getIsbn().equals(isbn))
                .findFirst()
                .orElseThrow(() -> new LoanException("Book is not currently on loan."));

        book.setAvailable(true);
        loans.remove(loan);
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }
}

//  Main Application Class (UI and Persistence)
public class LibraryManagementSystem {

    private Library library;
    private final Scanner scanner;
    private static final String DATA_FILE = "library.dat";

    public LibraryManagementSystem() {
        this.scanner = new Scanner(System.in);
        this.library = loadLibrary();
    }

    public static void main(String[] args) {
        LibraryManagementSystem lms = new LibraryManagementSystem();
        lms.run();
    }

    /**
     * Main application loop.
     */
    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            try {
                int choice = getIntInput();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        manageBooks();
                        break;
                    case 2:
                        manageMembers();
                        break;
                    case 3:
                        manageLoans();
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
        saveLibrary();
        System.out.println("Goodbye! Library data saved.");
        scanner.close();
    }

    // Main Menu
    private void printMainMenu() {
        System.out.println("\n--- Library Management System ---");
        System.out.println("1. Manage Books");
        System.out.println("2. Manage Members");
        System.out.println("3. Manage Loans");
        System.out.println("0. Save and Exit");
        System.out.print("Enter your choice: ");
    }

    // Book Management Sub-Menu
    private void manageBooks() {
        boolean back = false;
        while (!back) {
            printBookMenu();
            try {
                int choice = getIntInput();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1: promptAddBook(); break;
                    case 2: promptViewBook(); break;
                    case 3: promptUpdateBook(); break;
                    case 4: promptDeleteBook(); break;
                    case 5: listAllBooks(); break;
                    case 0: back = true; break;
                    default: System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    private void printBookMenu() {
        System.out.println("\n--- Book Management ---");
        System.out.println("1. Add New Book");
        System.out.println("2. View Book Details");
        System.out.println("3. Update Book");
        System.out.println("4. Delete Book");
        System.out.println("5. List All Books");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
    }

    // Member Management Sub-Menu
    private void manageMembers() {
        boolean back = false;
        while (!back) {
            printMemberMenu();
            try {
                int choice = getIntInput();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1: promptAddMember(); break;
                    case 2: promptViewMember(); break;
                    case 3: promptUpdateMember(); break;
                    case 4: promptDeleteMember(); break;
                    case 5: listAllMembers(); break;
                    case 0: back = true; break;
                    default: System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    private void printMemberMenu() {
        System.out.println("\n--- Member Management ---");
        System.out.println("1. Add New Member");
        System.out.println("2. View Member Details");
        System.out.println("3. Update Member");
        System.out.println("4. Delete Member");
        System.out.println("5. List All Members");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
    }

    // --- Loan Management Sub-Menu ---
    private void manageLoans() {
        boolean back = false;
        while (!back) {
            printLoanMenu();
            try {
                int choice = getIntInput();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1: promptCheckoutBook(); break;
                    case 2: promptReturnBook(); break;
                    case 3: listAllLoans(); break;
                    case 0: back = true; break;
                    default: System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    private void printLoanMenu() {
        System.out.println("\n--- Loan Management ---");
        System.out.println("1. Check Out Book");
        System.out.println("2. Return Book");
        System.out.println("3. List All Loans");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
    }

    // --- Action Methods (Books) ---

    private void promptAddBook() {
        try {
            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine();
            System.out.print("Enter Title: ");
            String title = scanner.nextLine();
            System.out.print("Enter Author: ");
            String author = scanner.nextLine();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                System.out.println("Error: All fields are required.");
                return;
            }

            library.addBook(new Book(isbn, title, author));
            System.out.println("Book added successfully!");
        } catch (DuplicateItemException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void promptViewBook() {
        try {
            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine();
            Book book = library.findBookByIsbn(isbn)
                    .orElseThrow(() -> new ItemNotFoundException("Book not found"));
            System.out.println(book);
        } catch (ItemNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void promptUpdateBook() {
        try {
            System.out.print("Enter ISBN of book to update: ");
            String isbn = scanner.nextLine();
            // Check if book exists first
            Book oldBook = library.findBookByIsbn(isbn)
                    .orElseThrow(() -> new ItemNotFoundException("Book not found"));

            System.out.println("Current details: " + oldBook);
            System.out.print("Enter new Title (or press Enter to keep current): ");
            String newTitle = scanner.nextLine();
            System.out.print("Enter new Author (or press Enter to keep current): ");
            String newAuthor = scanner.nextLine();

            library.updateBook(isbn,
                    newTitle.isEmpty() ? oldBook.getTitle() : newTitle,
                    newAuthor.isEmpty() ? oldBook.getAuthor() : newAuthor);
            System.out.println("Book updated successfully!");
        } catch (ItemNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void promptDeleteBook() {
        try {
            System.out.print("Enter ISBN of book to delete: ");
            String isbn = scanner.nextLine();
            library.deleteBook(isbn);
            System.out.println("Book deleted successfully!");
        } catch (ItemNotFoundException | LoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listAllBooks() {
        List<Book> books = library.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books in the library.");
            return;
        }
        System.out.println("\n--- All Books ---");
        books.forEach(System.out::println);
    }

    // --- Action Methods (Members) ---

    private void promptAddMember() {
        try {
            System.out.print("Enter Member ID: ");
            String memberId = scanner.nextLine();
            System.out.print("Enter Member Name: ");
            String name = scanner.nextLine();

            if (memberId.isEmpty() || name.isEmpty()) {
                System.out.println("Error: All fields are required.");
                return;
            }

            library.addMember(new Member(memberId, name));
            System.out.println("Member added successfully!");
        } catch (DuplicateItemException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void promptViewMember() {
        try {
            System.out.print("Enter Member ID: ");
            String memberId = scanner.nextLine();
            Member member = library.findMemberById(memberId)
                    .orElseThrow(() -> new ItemNotFoundException("Member not found"));
            System.out.println(member);
        } catch (ItemNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void promptUpdateMember() {
        try {
            System.out.print("Enter Member ID to update: ");
            String memberId = scanner.nextLine();
            Member oldMember = library.findMemberById(memberId)
                    .orElseThrow(() -> new ItemNotFoundException("Member not found"));

            System.out.println("Current details: " + oldMember);
            System.out.print("Enter new Name (or press Enter to keep current): ");
            String newName = scanner.nextLine();

            library.updateMember(memberId, newName.isEmpty() ? oldMember.getName() : newName);
            System.out.println("Member updated successfully!");
        } catch (ItemNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void promptDeleteMember() {
        try {
            System.out.print("Enter Member ID to delete: ");
            String memberId = scanner.nextLine();
            library.deleteMember(memberId);
            System.out.println("Member deleted successfully!");
        } catch (ItemNotFoundException | LoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listAllMembers() {
        List<Member> members = library.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members in the system.");
            return;
        }
        System.out.println("\n--- All Members ---");
        members.forEach(System.out::println);
    }

    // --- Action Methods (Loans) ---

    private void promptCheckoutBook() {
        try {
            System.out.print("Enter Member ID: ");
            String memberId = scanner.nextLine();
            System.out.print("Enter Book ISBN: ");
            String isbn = scanner.nextLine();
            library.checkoutBook(memberId, isbn);
            System.out.println("Book checked out successfully!");
        } catch (ItemNotFoundException | LoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void promptReturnBook() {
        try {
            System.out.print("Enter Book ISBN to return: ");
            String isbn = scanner.nextLine();
            library.returnBook(isbn);
            System.out.println("Book returned successfully!");
        } catch (ItemNotFoundException | LoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listAllLoans() {
        List<Loan> loans = library.getAllLoans();
        if (loans.isEmpty()) {
            System.out.println("No active loans.");
            return;
        }
        System.out.println("\n--- Active Loans ---");

        // Join with book and member data for a richer display
        for (Loan loan : loans) {
            String bookTitle = library.findBookByIsbn(loan.getIsbn())
                    .map(Book::getTitle)
                    .orElse("Unknown Book");
            String memberName = library.findMemberById(loan.getMemberId())
                    .map(Member::getName)
                    .orElse("Unknown Member");

            System.out.println("Member: " + memberName + " (ID: " + loan.getMemberId() + ")");
            System.out.println("  Book: " + bookTitle + " (ISBN: " + loan.getIsbn() + ")");
            System.out.println("  Date: " + loan.getCheckoutDate());
            System.out.println("--------------------");
        }
    }

    // --- Utility Methods ---

    private int getIntInput() throws InputMismatchException {
        return scanner.nextInt();
    }

    // --- Persistence (File I/O) ---

    /**
     * Loads the library state from a file.
     * If no file is found, returns a new Library object.
     */
    private Library loadLibrary() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No existing data file found. Creating new library.");
            return new Library();
        }

        // Try-with-resources for automatic stream closing
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Library loadedLibrary = (Library) ois.readObject();
            System.out.println("Library data loaded successfully.");
            return loadedLibrary;
        } catch (FileNotFoundException e) {
            // This case is handled by file.exists() but good to have
            System.out.println("Data file not found. Creating new library.");
            return new Library();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading library data: " + e.getMessage());
            System.out.println("A new, empty library will be used.");
            return new Library(); // Return a fresh library on error
        }
    }

    /**
     * Saves the current library state to a file.
     */
    private void saveLibrary() {
        // Try-with-resources for automatic stream closing
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(library);
        } catch (IOException e) {
            System.out.println("Error saving library data: " + e.getMessage());
        }
    }
}