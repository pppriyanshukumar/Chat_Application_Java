import java.util.Scanner;

public class ChatApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Java Chat Application ===");
        System.out.println("1. Start Server");
        System.out.println("2. Start Client");
        System.out.println("3. Exit");
        System.out.print("Choose option (1-3): ");
        
        int choice = scanner.nextInt();
        
        switch (choice) {
            case 1:
                System.out.println("Starting Chat Server...");
                ChatServer.main(args);
                break;
            case 2:
                System.out.println("Starting Chat Client...");
                ChatClient.main(args);
                break;
            case 3:
                System.out.println("Goodbye!");
                break;
            default:
                System.out.println("Invalid option!");
        }
        
        scanner.close();
    }
}