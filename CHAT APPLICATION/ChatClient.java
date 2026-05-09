import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ChatClient() {
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Java Chat Client");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Chat Area (Displaying messages)
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        // Simple dark theme
        chatArea.setBackground(new Color(40, 44, 52));
        chatArea.setForeground(new Color(171, 178, 191));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Input Panel (Typing new messages)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        
        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.addActionListener(e -> sendMessage()); // Hit Enter to send
        
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendButton.setBackground(new Color(97, 175, 239));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage()); // Click button to send

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        frame.add(inputPanel, BorderLayout.SOUTH);
    }

    public void startClient() {
        // Prompt user for their display name before connecting
        username = JOptionPane.showInputDialog(
            frame, 
            "Enter your username:", 
            "User Login", 
            JOptionPane.PLAIN_MESSAGE
        );
        
        // Exit if user cancels or enters empty name
        if (username == null || username.trim().isEmpty()) {
            System.exit(0);
        }

        frame.setTitle("Java Chat Client - " + username);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Start background thread to listen for incoming messages from server
            Thread readerThread = new Thread(new IncomingReader());
            readerThread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error connecting to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && out != null) {
            // Send message to server
            out.println(message);
            // Clear input field
            messageField.setText("");
        }
    }

    // Background thread class to listen for messages without freezing the GUI
    private class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (message.equals("ENTER_USERNAME")) {
                        // The server is asking for our username
                        out.println(username);
                    } else {
                        // Regular message from server
                        String finalMessage = message;
                        // Use invokeLater to safely update the Swing GUI from a background thread
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(finalMessage + "\n");
                            // Auto-scroll to the bottom of the chat
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        });
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("Connection to server lost.\n");
                });
            }
        }
    }

    public static void main(String[] args) {
        // Use system look and feel for a more native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Ensure GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ChatClient client = new ChatClient();
            client.startClient();
        });
    }
}
