import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A simple, console-based chat client.
 *
 * How it works:
 * - The `ChatClient` class connects to the `ChatServer` at a fixed
 * address and port.
 * - It uses two threads for communication:
 * 1. The `main` thread: After sending the username, it enters a loop
 * that reads user input from the console and sends it to the server.
 * 2. The `ServerListener` thread (an inner class): This thread is
 * spawned to continuously listen for messages *from* the server and
 * print them to the user's console.
 * - This two-threaded approach (one for reading, one for writing)
 * prevents the client from being blocked and allows it to receive
 * messages at any time, even while the user is typing.
 */
public class ChatClient {

    private static final String SERVER_ADDRESS = "127.0.0.1"; // localhost
    private static final int SERVER_PORT = 9001;

    public static void main(String[] args) {

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {

            // Set up streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // true for auto-flush
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            // Send Username
            System.out.print("Enter your username: ");
            String username = consoleReader.readLine();
            out.println(username);

            // Start a new thread to listen for messages from the server
            ServerListener listener = new ServerListener(in);
            new Thread(listener).start();

            // Send Messages
            // Read from console and send to server on the main thread
            System.out.println("You are connected! Type 'exit' to quit.");
            String userInput;
            while (true) {
                userInput = consoleReader.readLine();

                // Send the message to the server
                out.println(userInput);

                // Check if the user wants to exit
                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
        System.out.println("Disconnected from chat server.");
    }

    /**
     * A listener thread that runs in the background.
     * It continuously reads messages from the server and prints them to the console.
     */
    private static class ServerListener implements Runnable {
        private BufferedReader in;

        public ServerListener(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String serverMessage;
                // Read messages from the server and print them
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                // This will happen when the server or client disconnects
                System.out.println("Connection to server lost.");
            }
        }
    }
}