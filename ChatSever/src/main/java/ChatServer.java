import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * A multi-threaded, console-based chat server.
 *
 * How it works:
 * - The `ChatServer` class opens a `ServerSocket` on a fixed port and waits
 * for clients.
 * - When a client connects, the server spawns a new `Thread` running a
 * `ClientHandler` instance to manage that client.
 * - The `ClientHandler` (an inner class) handles reading the username and
 * all subsequent messages from its specific client.
 * - A shared, static `Set<PrintWriter>` (`clientWriters`) holds the output
 * streams for all currently connected clients.
 * - All access to the `clientWriters` set is controlled by `synchronized`
 * methods (`addClient`, `removeClient`, `broadcastMessage`) to ensure
 * thread-safety and prevent concurrency issues.
 * - Messages received from one client are broadcast to all clients by
 * iterating over the shared set.
 */
public class ChatServer {

    // The port the server listens on.
    private static final int PORT = 9001;

    /**
     * A set of all PrintWriters for all connected clients.
     * This set is shared among all ClientHandler threads.
     * We must synchronize access to this set to avoid concurrency issues.
     */
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("The chat server is running on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Wait for a client to connect.
                // The accept() method blocks until a connection is made.
                Socket clientSocket = serverSocket.accept();

                // Create a new thread to handle the client
                ClientHandler clientThread = new ClientHandler(clientSocket);
                new Thread(clientThread).start();
            }
        } catch (IOException e) {
            System.err.println("Error in server socket: " + e.getMessage());
        }
    }

    /**
     * Adds a client's writer to the shared set.
     * Synchronized to ensure thread-safety.
     */
    private static void addClient(PrintWriter writer) {
        synchronized (clientWriters) {
            clientWriters.add(writer);
        }
    }

    /**
     * Removes a client's writer from the shared set.
     * Synchronized to ensure thread-safety.
     */
    private static void removeClient(PrintWriter writer) {
        synchronized (clientWriters) {
            clientWriters.remove(writer);
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     * Synchronized to ensure thread-safety during iteration.
     */
    private static void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }

    /**
     * A handler thread for a single client.
     * Listens for messages from the client and broadcasts them to all clients.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Set up streams for communication
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true); // true for auto-flush

                // Get Username
                // The first message from the client must be their username
                username = in.readLine();
                if (username == null || username.trim().isEmpty()) {
                    username = "Anonymous";
                }
                System.out.println(username + " has connected.");

                // Add this client's writer to the shared set
                addClient(out);

                // Announce New User
                // Broadcast to all clients that a new user has joined
                broadcastMessage(username + " has joined the chat.");

                // Handle Client Messages
                String message;
                while ((message = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(message)) {
                        break;
                    }
                    // Broadcast the received message to all clients
                    broadcastMessage(username + ": " + message);
                }

            } catch (IOException e) {
                System.err.println("Client handler error: " + e.getMessage());
            } finally {
                // Cleanup
                // When the client disconnects (or loop breaks)
                if (username != null) {
                    System.out.println(username + " has disconnected.");
                    broadcastMessage(username + " has left the chat.");
                }
                if (out != null) {
                    removeClient(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}