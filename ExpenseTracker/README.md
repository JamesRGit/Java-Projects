# JavaFX Expense Tracker

This is a simple desktop application for tracking personal expenses, built with JavaFX. It allows users to log expenses, categorize them, and visualize their spending in a pie chart. All expense data is saved locally to a file named `expenses.csv`.

## Features

- **Add Expenses**: Log new expenses with a description, amount, and category.
- **View Expenses**: See all entries in a sortable table.
- **Delete Expenses**: Remove unwanted entries from the table.
- **Data Persistence**: Expenses are automatically saved to `expenses.csv` and reloaded when the app starts.
- **Visualization**: A dynamic pie chart shows the percentage of spending for each category.

## Prerequisites

- Java JDK (Version 17 or newer)
- Apache Maven

## How to Run (Maven)

This is the recommended, modern way to run any Java project. It uses a build tool (Maven) to automatically download and configure all dependencies, including JavaFX.

1. **Use a Maven Project Structure**: Your `ExpenseTracker`.java file should be placed in `src/main/java/` (you may need to create these folders).

2. **Use the `pom.xml`**: This file, located in the root of the project, tells Maven what dependencies (like JavaFX) your project needs.

```bash
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="[http://maven.apache.org/POM/4.0.0](http://maven.apache.org/POM/4.0.0)"
xmlns:xsi="[http://www.w3.org/2001/XMLSchema-instance](http://www.w3.org/2001/XMLSchema-instance)"
xsi:schemaLocation="[http://maven.apache.org/POM/4.0.0](http://maven.apache.org/POM/4.0.0) [http://maven.apache.org/xsd/maven.4.0.0.xsd](http://maven.apache.org/xsd/maven.4.0.0.xsd)">
<modelVersion>4.0.0</modelVersion>

    <groupId>com.github.JamesRGit</groupId>
    <artifactId>ExpenseTracker</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>24</maven.compiler.source>
        <maven.compiler.target>24</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>21</javafx.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-graphics</artifactId>
            <version>${javafx.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>ExpenseTracker</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

3. **Run the App**:

- **In IntelliJ (Recommended)**:
  1. Open the `pom.xml` file.
  2. If prompted, allow IntelliJ to "Load Maven Project".
  3. Open the Maven tool window (View > Tool Windows > Maven).
  4. Expand `Plugins` > `javafx`.
  5. Double-click `javafx:run` to start the application.


- From Command Line: Open a terminal in your project's root folder and run:
```bash
mvn clean javafx:run
```

## How the App Works

- Input Form (Left): Enter the expense details and click "Add Expense".
- Expense Table (Center): Shows all current expenses. You can click a row and use the "Delete" button.
- Pie Chart (Right): Automatically updates as you add or delete expenses, showing the total spending by category.
- `expenses.csv`: A file will be created in the root directory of the project to store your data.