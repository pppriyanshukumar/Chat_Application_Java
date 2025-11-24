import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ChatClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton connectButton;
    private JTextField usernameField;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Thread receiveThread;

    public ChatClientGUI() {
        setTitle("Java Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        add(chatScroll, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // User panel
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setPreferredSize(new Dimension(150, 0));
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userPanel.add(new JLabel("Online Users"), BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        add(userPanel, BorderLayout.EAST);

        // Top panel for username and connect
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernameField = new JTextField(10);
        connectButton = new JButton("Connect");
        topPanel.add(new JLabel("Username:"));
        topPanel.add(usernameField);
        topPanel.add(connectButton);
        add(topPanel, BorderLayout.NORTH);

        sendButton.setEnabled(false);
        inputField.setEnabled(false);

        connectButton.addActionListener(e -> connectToServer());
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void connectToServer() {
        String user = usernameField.getText().trim();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.");
            return;
        }
        username = user;
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Handle username registration
            new Thread(() -> {
                try {
                    String line = in.readLine(); // "Enter your username:"
                    out.println(username);
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("Welcome to the chat")) {
                            appendChat(line);
                            break;
                        } else if (line.startsWith("Username")) {
                            appendChat(line);
                            out.println(username);
                        } else {
                            appendChat(line);
                        }
                    }
                    // Start receiving messages
                    receiveThread = new Thread(this::receiveMessages);
                    receiveThread.start();
                } catch (IOException ex) {
                    appendChat("Connection error: " + ex.getMessage());
                }
            }).start();

            connectButton.setEnabled(false);
            usernameField.setEnabled(false);
            sendButton.setEnabled(true);
            inputField.setEnabled(true);
            inputField.requestFocus();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not connect to server.");
        }
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (msg.isEmpty() || out == null) return;
        out.println(msg);
        inputField.setText("");
        if (msg.equals("/quit")) {
            disconnect();
        }
    }

    private void receiveMessages() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("=== ONLINE USERS")) {
                    // Parse user list
                    userListModel.clear();
                    while ((line = in.readLine()) != null && line.startsWith("• ")) {
                        userListModel.addElement(line.substring(2).replace(" (you)", ""));
                    }
                    if (line != null && !line.isEmpty()) appendChat(line);
                } else {
                    appendChat(line);
                }
            }
        } catch (IOException ex) {
            appendChat("Disconnected from server.");
        } finally {
            disconnect();
        }
    }

    private void appendChat(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        sendButton.setEnabled(false);
        inputField.setEnabled(false);
        connectButton.setEnabled(true);
        usernameField.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI().setVisible(true);
        });
    }
}
