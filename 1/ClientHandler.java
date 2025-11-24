import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ClientHandler implements Runnable {
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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Handle username registration
            out.println("Enter your username:");
            while (true) {
                String inputUsername = in.readLine();
                if (inputUsername == null) return;

                inputUsername = inputUsername.trim();
                if (inputUsername.isEmpty()) {
                    out.println("Username cannot be empty. Try again:");
                    continue;
                }

                if (ChatServer.isUsernameTaken(inputUsername)) {
                    out.println("Username already taken. Try another:");
                    continue;
                }

                this.username = inputUsername;
                ChatServer.addClientToMap(username, this);
                break;
            }

            out.println("Welcome to the chat, " + username + "!");
            out.println("Type '/help' for commands or start chatting!");

            // Notify other users
            ChatServer.broadcastMessage("*** " + username + " joined the chat ***", this);

            // Handle messages
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/")) {
                    if (handleCommand(message)) {
                        // If handleCommand returns true, exit loop (e.g. /quit)
                        break;
                    }
                } else if (!message.trim().isEmpty()) {
                    ChatServer.broadcastMessage(username + ": " + message, this);
                }
            }
        } catch (IOException e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    /**
     * Handles a command. Returns true if the connection should be closed (e.g. /quit), false otherwise.
     */
    private boolean handleCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/help":
                out.println("=== CHAT COMMANDS ===");
                out.println("/help - Show this help message");
                out.println("/users - List online users");
                out.println("/quit - Leave the chat");
                out.println("Just type a message to chat with everyone!");
                return false;

            case "/users":
                Set<String> users = ChatServer.getOnlineUsers();
                out.println("=== ONLINE USERS (" + users.size() + ") ===");
                for (String user : users) {
                    out.println("• " + user + (user.equals(username) ? " (you)" : ""));
                }
                return false;

            case "/quit":
                out.println("Goodbye!");
                return true; // Signal to exit loop and cleanup

            default:
                out.println("Unknown command. Type '/help' for available commands.");
                return false;
        }
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    public String getUsername() {
        return username;
    }
    
    private void cleanup() {
        try {
            ChatServer.removeClient(this);
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}