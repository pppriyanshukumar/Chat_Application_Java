import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;
    
    public ChatClient() {
        scanner = new Scanner(System.in);
        
    }
    
    public void start() {
        try {
            // Connect to server
            System.out.println("Connecting to chat server...");
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("Connected to server!");
            
            // Start thread to receive messages from server
            Thread receiveThread = new Thread(this::receiveMessages);
            receiveThread.setDaemon(true);
            receiveThread.start();
            
            // Main thread handles user input
            String userInput;
            while ((userInput = scanner.nextLine()) != null) {
                out.println(userInput);
                if (userInput.equals("/quit")) {
                    break;
                }
            }
            
        } catch (ConnectException e) {
            System.out.println("Could not connect to server. Make sure the server is running.");
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.out.println("Connection lost.");
            }
        }
    }
    
    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            scanner.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Java Chat Client ===");
        System.out.println("Starting chat client...");
        
        ChatClient client = new ChatClient();
        client.start();
    }

}
