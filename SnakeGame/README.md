# Snake Game

This is a classic implementation of the Snake game, built from scratch using Java and the Swing library for the GUI.

Features:

- Start Screen: The game waits for the user to "Press Enter to Play."
- Game Over Screen: Displays the final score and offers options to "Press Enter to Restart" or "Press Esc to Quit."
- Score Tracking: The score increases as the snake eats food.
- Smooth Controls: Uses a Swing Timer for the game loop and KeyAdapter for responsive arrow key controls (no "Enter" key required!).
- Safe Quit: The 'Escape' key can be used at any time (on the start screen, in-game, or on the game over screen) to safely close the application.

## How to Build and Run:

This is a Maven-based project. You must have Java and Maven installed to run it.

Clone the repository (if you haven't already).

Navigate to the project directory:

```cd SnakeGame```

Compile the project:

```mvn compile```

Run the application:

```mvn exec:java -Dexec.mainClass="SnakeGame"```

A new window will open, and the game will start.

## Technologies Used:

- Java
- Java Swing: (for the all GUI components, drawing, and key listeners)
- Maven: (for build management and project structure)
