import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private List<ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Ask client for their username
            out.println("ENTER_USERNAME");
            username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                username = "Anonymous_" + socket.getPort();
            }

            System.out.println(username + " joined the chat.");
            broadcastMessage("SERVER", username + " has joined the chat!");

            String message;
            // Continuously read messages from this client
            while ((message = in.readLine()) != null) {
                broadcastMessage(username, message);
            }
        } catch (IOException e) {
            System.out.println("Error in ClientHandler (" + username + "): " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.remove(this);
            if (username != null) {
                System.out.println(username + " left the chat.");
                broadcastMessage("SERVER", username + " has left the chat.");
            }
        }
    }

    // Send a message to all connected clients
    private void broadcastMessage(String sender, String message) {
        for (ClientHandler client : clients) {
            // Include sender name with message
            client.sendMessage(sender + ": " + message);
        }
    }

    // Helper method to send a message to this specific client's socket
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
