import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A JavaFX application for tracking expenses.
 * * Features:
 * - Add new expenses with description, amount, and category.
 * - View all expenses in a sortable table.
 * - See a pie chart visualization of spending by category.
 * - Data is persisted to a local CSV file (expenses.csv).
 * - Delete selected expenses.
 */
public class ExpenseTracker extends Application {

    // Data
    private ObservableList<Expense> allExpenses;
    private ObservableList<PieChart.Data> pieChartData;
    private static final String FILE_NAME = "expenses.csv";

    // UI Components
    private TableView<Expense> tableView;
    private PieChart pieChart;
    private TextField descriptionInput;
    private TextField amountInput;
    private ComboBox<String> categoryInput;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Expense Tracker");

        // Initialize data lists
        allExpenses = FXCollections.observableArrayList();
        pieChartData = FXCollections.observableArrayList();

        // Left Side: Input Form & Title
        VBox leftVBox = new VBox(10);
        leftVBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Expense Tracker");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane inputGrid = createInputForm();
        leftVBox.getChildren().addAll(titleLabel, inputGrid);

        // Center: Expense Table & Delete Button
        VBox centerVBox = new VBox(10);
        centerVBox.setPadding(new Insets(10));
        tableView = createExpenseTable();

        Button deleteButton = new Button("Delete Selected Expense");
        deleteButton.setOnAction(e -> deleteSelectedExpense());

        centerVBox.getChildren().addAll(new Label("All Expenses"), tableView, deleteButton);
        VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS); // Table takes up available space

        // Right Side: Pie Chart
        VBox rightVBox = new VBox(10);
        rightVBox.setPadding(new Insets(10));
        pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Expenses by Category");
        rightVBox.getChildren().addAll(pieChart);

        // Main Layout
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(leftVBox);
        borderPane.setCenter(centerVBox);
        borderPane.setRight(rightVBox);

        BorderPane.setMargin(leftVBox, new Insets(10));
        BorderPane.setMargin(centerVBox, new Insets(10, 0, 10, 0));
        BorderPane.setMargin(rightVBox, new Insets(10));


        // Load Data and Update UI
        loadExpenses();
        updateChartData();

        // Show Scene
        Scene scene = new Scene(borderPane, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the GridPane for adding new expenses.
     */
    private GridPane createInputForm() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        Label descLabel = new Label("Description:");
        descriptionInput = new TextField();
        descriptionInput.setPromptText("e.g., Coffee");

        Label amountLabel = new Label("Amount (R):");
        amountInput = new TextField();
        amountInput.setPromptText("e.g., 4.50");

        // Add a TextFormatter to allow only valid decimal numbers
        amountInput.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            } else {
                return null;
            }
        }));

        Label categoryLabel = new Label("Category:");
        categoryInput = new ComboBox<>();
        categoryInput.getItems().addAll("Food", "Transport", "Shopping", "Utilities", "Entertainment", "Other");
        categoryInput.setValue("Food"); // Default value

        Button addButton = new Button("Add Expense");
        addButton.setOnAction(e -> addExpense());

        grid.add(descLabel, 0, 0);
        grid.add(descriptionInput, 1, 0);
        grid.add(amountLabel, 0, 1);
        grid.add(amountInput, 1, 1);
        grid.add(categoryLabel, 0, 2);
        grid.add(categoryInput, 1, 2);
        grid.add(addButton, 1, 3);

        return grid;
    }

    /**
     * Creates the TableView for displaying expenses.
     */
    private TableView<Expense> createExpenseTable() {
        TableView<Expense> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Description Column
        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Amount Column
        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Format amount as currency
        amountCol.setCellFactory(tc -> new TableCell<Expense, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("R%.2f", item));
                }
            }
        });

        // Category Column
        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        table.getColumns().addAll(descCol, amountCol, catCol);
        table.setItems(allExpenses); // Link table to data list

        return table;
    }

    /**
     * Handles the "Add Expense" button click.
     * Validates input, creates an Expense object, adds it to the list,
     * saves to file, and updates the UI.
     */
    private void addExpense() {
        String description = descriptionInput.getText();
        String amountStr = amountInput.getText();
        String category = categoryInput.getValue();

        if (description.isEmpty() || amountStr.isEmpty() || category == null) {
            showAlert("Input Error", "All fields must be filled.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showAlert("Input Error", "Amount must be greater than zero.");
                return;
            }

            Expense newExpense = new Expense(description, amount, category);
            allExpenses.add(newExpense);

            saveExpenses();
            updateChartData();

            // Clear inputs
            descriptionInput.clear();
            amountInput.clear();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Invalid amount. Please enter a number.");
        }
    }

    /**
     * Handles the "Delete Selected Expense" button click.
     * Confirms deletion, removes from list, saves, and updates UI.
     */
    private void deleteSelectedExpense() {
        Expense selectedExpense = tableView.getSelectionModel().getSelectedItem();

        if (selectedExpense == null) {
            showAlert("No Selection", "Please select an expense from the table to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Expense");
        confirmAlert.setContentText("Are you sure you want to delete this expense?\n" +
                selectedExpense.getDescription() + " (R" + selectedExpense.getAmount() + ")");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            allExpenses.remove(selectedExpense);
            saveExpenses();
            updateChartData();
        }
    }

    /**
     * Aggregates data from `allExpenses` and updates the `pieChartData`.
     */
    private void updateChartData() {
        Map<String, Double> categoryTotals = new HashMap<>();

        // Sum expenses by category
        for (Expense expense : allExpenses) {
            categoryTotals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }

        pieChartData.clear(); // Clear old data

        // Create new PieChart.Data objects
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + String.format(" (R%.2f)", entry.getValue()), entry.getValue()));
        }
    }

    /**
     * Saves the current `allExpenses` list to the CSV file.
     */
    private void saveExpenses() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Expense expense : allExpenses) {
                // Use CSV format
                String line = String.format("\"%s\",%.2f,\"%s\"%n",
                        expense.getDescription().replace("\"", "\"\""), // Escape quotes
                        expense.getAmount(),
                        expense.getCategory());
                bw.write(line);
            }
        } catch (IOException e) {
            showAlert("Save Error", "Could not save expenses to file: " + e.getMessage());
        }
    }

    /**
     * Loads expenses from the CSV file into the `allExpenses` list.
     */
    private void loadExpenses() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return; // No file to load, just start empty
        }

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Basic CSV parsing (assumes "desc",amount,"cat" format)
                String[] parts = line.split("\",");
                if (parts.length == 3) {
                    try {
                        String description = parts[0].substring(1); // Remove leading quote
                        double amount = Double.parseDouble(parts[1]);
                        String category = parts[2].substring(0, parts[2].length() - 1); // Remove trailing quote

                        allExpenses.add(new Expense(description, amount, category));
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                        System.err.println("Skipping malformed line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            showAlert("Load Error", "Could not load expenses from file: " + e.getMessage());
        }
    }

    /**
     * Utility method to show a simple error/info dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Data model class for an Expense.
     * Uses JavaFX Properties to allow easy binding with TableView.
     */
    public static class Expense {
        private final StringProperty description;
        private final DoubleProperty amount;
        private final StringProperty category;

        public Expense(String description, double amount, String category) {
            this.description = new SimpleStringProperty(description);
            this.amount = new SimpleDoubleProperty(amount);
            this.category = new SimpleStringProperty(category);
        }

        public String getDescription() {
            return description.get();
        }
        public double getAmount() {
            return amount.get();
        }
        public String getCategory() {
            return category.get();
        }
        public StringProperty descriptionProperty() {
            return description;
        }
        public DoubleProperty amountProperty() {
            return amount;
        }
        public StringProperty categoryProperty() {
            return category;
        }
    }
}