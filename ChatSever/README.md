# Java Socket & Thread-Based Chat Application

This project is a classic, simple implementation of a multi-client chat room using Java sockets for networking and threads for concurrency.

## Files in This Project

1. `ChatServer.java`: The main server program. 
   - Opens a `ServerSocket` on a specified port (`9001`).
   - Listens for and accepts new client connections.
   - Spawns a new `ClientHandler` thread for each client.
   - Maintains a `synchronized` set of all client writers to broadcast messages.
   

2. `ChatClient.java`: The client program.
   - Connects to the server's address and port.
   - Uses two threads:
     - The **main thread** reads input from the user's console and sends it to the server.
     - A new `ServerListener` **thread** is spawned to continuously read messages from the server and print them to the console. This ensures the client is non-blocking.

## How to Compile and Run

You will need the Java Development Kit (JDK) installed.

**1. Compile the Code**

Open a terminal and compile both `.java` files:

```bash
javac ChatServer.java
javac ChatClient.java
```

**2. Start the Server** 

In the same terminal, run the server:
```bash
java ChatServer
```

You should see the output: `The chat server is running on port 9001...`

**3. Start One or More Clients**

Open a new terminal window for each client you want to run.

In the new terminal(s), run the client:
```bash
java ChatClient
```

**4. Chat!**

1. When you run the client, it will first prompt you: `Enter your username:`
2. Type a name (e.g., "Alice") and press Enter.
3. Open a second client in another terminal, and enter a different name (e.g., "Bob").
4. As soon as Bob connects, both Alice and Bob will see: `Bob has joined the chat.`
5. Now, anything typed in one client window will be broadcast to all other client windows, prefixed with the username.
6. To disconnect, simply type `exit` in any client window. All other clients will see: `Alice has left the chat.`