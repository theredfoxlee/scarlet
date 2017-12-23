package networking;

import ui.Message;

import javax.xml.crypto.Data;
import java.io.*;
import java.lang.management.LockInfo;
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

    //variables used in client validation
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private boolean is_valid;

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

    private void initialize_streams(Socket client) {
        try {
            is = new ObjectInputStream(client.getInputStream());
            os = new ObjectOutputStream(client.getOutputStream());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        // DIRTY WHILE
        while (true) {
            try {
                Socket client = server.accept();

//                if(validateClient(client)) {

                    int idx = clients.indexOf(null);
                    if (idx != -1) {
                        clients.set(idx, new Client(client));
                        clients.get(idx).start();
                    } else {
                        this.sendMessage(new ObjectOutputStream(client.getOutputStream()),
                                new MessageCard("SERVER","","Server is too busy, try later."));
                    }
//                }
//                else{
//                    client.close();
//                    System.err.println("Client's socket closed");
//                }
            } catch (IOException e) {
                System.err.println("SERVER: Couldn't established connection with client.");
                System.err.println(e);
            }
        }
    }

//    private boolean validateClient(Socket client) {
//        this.initialize_streams(client);
//
//        try {
//            MessageCard message = (MessageCard) is.readObject();
//            String login=message.author;
//            String date=message.date;
//            String password=message.message;
//
//            System.out.println(login);
//            System.out.println(password);
//            LoginAuthorization authorization = new LoginAuthorization();
//            if (authorization.autorize(login, password)) {
//                is_valid = true;
//            } else {
//                is_valid = false;
//            }
//
//
//        } catch (Exception e) {
//            System.err.println("Couldnt read first input stream (login and password)");
//        }
//
//        System.out.println(is_valid);
//        return is_valid;
//    }

    private boolean isFull() {
        return noClients == maxNoClients;
    }

    private void sendMessage(ObjectOutputStream os, MessageCard message) {
        try {
            os.writeObject(message);
        } catch(Exception E) {

        }
    }
    //---------------------------------------------///


    private class Client extends Thread {
        // ------------NECESSARY-HANDLES-------------
        private Socket socket;         // for establishing I/O streams with client
        private ObjectInputStream is;  // for retrieving messages from client
        private ObjectOutputStream os;        // for sending messages to client
        // ------------------------------------------

        String name;
        String login;
        String password;
        boolean is_valid;

        public Client(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            this.openStreams();
            if(this.validateClient()) {
                this.makeHandshake();
                this.notifyOthers("User " + name + " entered the room!");
                this.listenAndEcho();
                //if user left while loop breaks and:
                this.notifyOthers("User " + name + " left the room!");


                this.closeStreams();
            }
            else {
                this.closeStreams();
            }
        }

        private void openStreams() {
            try {
                is = new ObjectInputStream(socket.getInputStream());
                os = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.err.println("SERVER: Connection couldn't be established.");
                System.err.println(e);
            }
        }
        private boolean validateClient() {

            try {
                MessageCard message = (MessageCard) is.readObject();
                String login=message.author;
                String date=message.date;
                String password=message.message;

                System.out.println(login);
                System.out.println(password);
                LoginAuthorization authorization = new LoginAuthorization();
                if (authorization.autorize(login, password)) {
                    is_valid = true;
                } else {
                    is_valid = false;
                }


            } catch (Exception e) {
                System.err.println("Couldnt read first input stream (login and password)");
            }

            System.out.println(is_valid);
            return is_valid;
        }



        private void makeHandshake() {
            try {
                MessageCard message = (MessageCard) is.readObject();
                sendMessage(os, new MessageCard("Server","","Hello"+message.author));
            } catch (Exception e) {
                System.err.println("SERVER: Handshakes couldn't be made.");
                System.err.println(e);
            }
        }

        private void notifyOthers(String message) {
            for (Client client : clients) {
                if (client != null && client != this) {
                    sendMessage(client.os, new MessageCard("SERVER", "", message));
                }
            }
        }

        private void listenAndEcho() {
            try {
                while (true) {
                    /*HashMap<String, String> messageTable = readMessage();*/

                    MessageCard message = (MessageCard) is.readObject();

                    /*if (messageTable.get("message").startsWith(":quit")) {
                        break;
                    }*/

                    for (Client client : clients) {
                        if (client != null) {
                            /*sendMessage(client.os,
                                    messageTable.get("author"), messageTable.get("date"), messageTable.get("message"));*/
                            sendMessage(client.os, new MessageCard(message.author,message.date,message.message));
                        }
                    }
                }
            } catch (Exception e) {
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

    }
}
