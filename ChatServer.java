import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static Map<String, ClientHandler> clientMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat Server starting on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running. Waiting for clients...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                
                System.out.println("New client connected. Total clients: " + clients.size());
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        if (client.getUsername() != null) {
            clientMap.remove(client.getUsername());
            broadcastMessage("*** " + client.getUsername() + " left the chat ***", null);
        }
        System.out.println("Client disconnected. Total clients: " + clients.size());
    }
    
    public static void addClientToMap(String username, ClientHandler client) {
        clientMap.put(username, client);
    }
    
    public static boolean isUsernameTaken(String username) {
        return clientMap.containsKey(username);
    }
    
    public static Set<String> getOnlineUsers() {
        return clientMap.keySet();
    }
}