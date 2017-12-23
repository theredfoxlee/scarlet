package networking;

import java.io.*;
import java.net.Socket;

import networking.messages.Consignment;
import networking.messages.Message;
import ui.Scarlet;

public class Client {
    // ------------NECESSARY-HANDLES-------------
    private Socket client;      // for establishing I/O streams with server
    private ObjectOutputStream os;     // for sending messages to server
    private ObjectInputStream is; // for retrieving messages from server

    private Scarlet ui;         // for adding messages to Scarlet UI
    // ------------------------------------------

    // ------------------FLAGS-------------------
    private boolean closed;
    // ------------------------------------------

    public Client(String host, Integer port, Scarlet ui) {
        // ----------CLIENT-STARTING-POINT----------
        this.establishConnection(host, port);
        this.runListener();
        this.ui = ui;
        // ------------------------------------------
    }

    private void establishConnection(String host, Integer port) {
        try {
            client = new Socket(host, port);
            os = new ObjectOutputStream(client.getOutputStream());
            is = new ObjectInputStream(client.getInputStream());
            os.flush();
        } catch (IOException e) {
            System.err.println("Client: Connection couldn't be established.");
            System.err.println(e.getMessage());
        }
    }

    private void runListener() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    Message message = (Message) is.readObject();
                    if (message instanceof  Consignment) {
                        this.addMessage((Consignment) message);
                    }
                }
            } catch (Exception e) {
                System.err.println("Client: Input stream has been broken.");
                System.err.println("Client: It's likely you didn't pass validation.");
            } finally {
                this.disconnect();
                closed = true;
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void disconnect() {
        try {
            os.close();
            is.close();
            client.close();
        } catch (IOException e) {
            System.err.println("Client: Couldn't clean streams and socket smoothly.");
            System.err.println(e.getMessage());
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public void send(networking.messages.Message message) {
        try {
            os.writeObject(message);
        } catch (IOException e) {
            System.err.println("Client: Couldn't send message.");
            System.err.println(e.getMessage());
        }
    }
    private void addMessage(Consignment message) {
        ui.addMessage(new ui.Message(message.getAuthor(), message.getDate(), message.getMessage()));
    }
}
