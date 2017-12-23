package networking;

import authorization.LoginAuthorization;
import networking.messages.Credentials;
import networking.messages.Consignment;
import networking.messages.Validation;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;

public class Server {
    // ------------NECESSARY-HANDLES-------------
    private ServerSocket server;
    private final ArrayList<Client> clients = new ArrayList<>();
    // ------------------------------------------

    public static void main(String args[]) {
        // ---------SERVER-STARTING-POINT--------
        new Server(6666);
        // ------------------------------------------
    }

    private Server(int port) {
        // ----------SERVER-STARTING-POINT----------
        this.openPort(port);
        this.start();
        // ------------------------------------------
    }

    private void start() {
        while (true) {
            try {
                this.addClient(server.accept());
            } catch (IOException e) {
                System.err.println("Server: Couldn't establish connection with client.");
                System.err.println(e.getMessage());
            }
        }
    }

    private void openPort(int port) {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Server: Couldn't open port " + port);
        }
    }

    private void addClient(Socket client) {
        Client clientWrapper = new Client(client);
        clientWrapper.setDaemon(true);
        clientWrapper.start();
        clients.add(clientWrapper);
    }

    private class Client extends Thread {
        // ------------NECESSARY-HANDLES-------------
        private Socket socket;         // for establishing I/O streams with client
        private ObjectInputStream is;  // for retrieving messages from client
        private ObjectOutputStream os;        // for sending messages to client
        // ------------------------------------------

        private String name;

        private Client(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            this.openStreams();
            if (this.validate()) {
                this.notifyMe("Hello " + name + "!");
                this.notifyOthers("User " + name + " entered the room!");
                this.listenAndEcho();
                this.notifyOthers("User " + name + " left the room!");
            }
            this.closeStreams();
            this.removeFromTable();
        }

        private void openStreams() {
            try {
                is = new ObjectInputStream(socket.getInputStream());
                os = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.err.println("Server: Connection couldn't be established.");
                System.err.println(e.getMessage());
            }
        }

        private boolean validate() {
            boolean valid = false;
            try {
                Object object = is.readObject();

                if (object instanceof Credentials) {
                    Credentials credentials = (Credentials) object;
                    valid = new LoginAuthorization().autorize(credentials.getUsername(), credentials.getPassword());
                    if (valid) {
                        this.name = credentials.getUsername();
                    }
                    os.writeObject(new Validation(valid));
                }
            } catch (Exception e){
                System.err.println("Server: Couldn't validate the client.");
                System.err.println(e.getMessage());
            }
            return valid;
        }

        private void notifyMe(String message) {
            send(os, new Consignment("Server", getCurrentDate(), message));
        }

        private void notifyOthers(String message) {
            for (Client client : clients) {
                if (client != this) {
                    send(client.os, new Consignment("Server", getCurrentDate(), message));
                }
            }
        }

        private void listenAndEcho() {
            try {
                while (true) {
                    Consignment message = (Consignment) is.readObject();
                    for (Client client : clients) {
                        send(client.os, new Consignment(message.getAuthor(),message.getDate(),message.getMessage()));
                    }
                }
            } catch (Exception e) {
                System.err.println("Server: Something went wrong while listening one of the clients.");
                System.err.println("Server: User " + name + " probably left the room.");
            }
        }

        private void removeFromTable() {
            clients.remove(this);
        }

        private void closeStreams() {
            try {
                is.close();
                os.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Server: Couldn't close streams.");
                System.err.println(e.getMessage());
            }
        }

        private void send(ObjectOutputStream os, Consignment consignment) {
            try {
                os.writeObject(consignment);
            } catch (IOException e) {
                System.err.println("Server: Couldn't send a message.");
                System.err.println(e.getMessage());
            }
        }

        private String getCurrentDate() {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
        }
    }
}
