package networking;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    // ------------SERVER-CONSTANTS--------------
    private final int port;
    private final int maxNoClients = 10;
    // ------------------------------------------

    // --------------SERVER-FLAGS----------------
    private int noClients = 0;
    // ------------------------------------------

    // ------------NECESSARY-HANDLES-------------
    private ServerSocket server;                                             // for starting service
    private final ArrayList<Client> clients = new ArrayList<>() {{
        for (int i = 0; i < maxNoClients; ++i) {
            add(null);
        }
    }}; // for echoing messages
    // ------------------------------------------


    public static void main(String args[]) {
        // ---------SERVER-STARTING-POINT--------
        new Server(6666);
        // ------------------------------------------
    }

    private Server(int port) {
        this.port = port;

        this.openPort();
        this.start();
    }

    private void openPort() {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("SERVER: Couldn't open port " + port);
        }
    }

    private void start() {
        // DIRTY WHILE
        while (true) {
            try {
                Socket client = server.accept();

                int idx = clients.indexOf(null);
                if (idx != -1) {
                    clients.set(idx, new Client(client));
                    clients.get(idx).start();
                } else {
                    this.sendMessage(new PrintStream(client.getOutputStream()),
                            "SERVER", "", "Server is too busy, try later.");
                }
            } catch (IOException e) {
                System.err.println("SERVER: Couldn't established connection with client.");
                System.err.println(e);
            }
        }
    }

    private boolean isFull() {
        return noClients == maxNoClients;
    }

    private void sendMessage(PrintStream os, String author, String date, String message) {
        os.println(author);
        os.println(date);
        os.println(message);
    }

    private class Client extends Thread {
        // ------------NECESSARY-HANDLES-------------
        private Socket socket;         // for establishing I/O streams with client
        private DataInputStream is;  // for retrieving messages from client
        private PrintStream os;        // for sending messages to client
        // ------------------------------------------

        String name;

        public Client(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            this.openStreams();
            this.makeHandshake();
            this.notifyOthers("User " + name + " entered the room!");
            this.listenAndEcho();
            this.notifyOthers("User " + name + " left the room!");
            this.closeStreams();
        }

        private void openStreams() {
            try {
                is = new DataInputStream(socket.getInputStream());
                os = new PrintStream(socket.getOutputStream());
            } catch (IOException e) {
                System.err.println("SERVER: Connection couldn't be established.");
                System.err.println(e);
            }
        }

        private void makeHandshake() {
            try {
                HashMap<String, String> messageTable = readMessage();
                name = messageTable.get("author");

                sendMessage(os, "Server", "", "Hello "+name);
            } catch (IOException e) {
                System.err.println("SERVER: Handshakes couldn't be made.");
                System.err.println(e);
            }
        }

        private void notifyOthers(String message) {
            for (Client client : clients) {
                if (client != null && client != this) {
                    sendMessage(client.os,
                            "Server", "", message);
                }
            }
        }

        private void listenAndEcho() {
            try {
                while (true) {
                    HashMap<String, String> messageTable = readMessage();

                    if (messageTable.get("message").startsWith(":quit")) {
                        break;
                    }

                    for (Client client : clients) {
                        if (client != null) {
                            sendMessage(client.os,
                                    messageTable.get("author"), messageTable.get("date"), messageTable.get("message"));
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("SERVER: Something went wrong while listening one of the clients.");
                System.err.println(e);
            } finally {
                this.removeFromTable();
                this.notifyOthers("User " + name + " left the room!");
                this.closeStreams();
            }
        }

        private void removeFromTable() {
            for (int idx = 0; idx < clients.size(); ++idx) {
                if (clients.get(idx) == this) {
                    clients.set(idx, null);
                }
            }
        }

        private void closeStreams() {
            try {
                is.close();
                os.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("SERVER: Couldn't close streams.");
                System.err.println(e);
            }
        }

        private HashMap<String, String> readMessage() throws IOException {
            return new HashMap<>() {{
                put("author", is.readLine());
                put("date", is.readLine());
                put("message", is.readLine());
            }};
        }
    }
}
