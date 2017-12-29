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
    private boolean isClosed = false;
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
                // Wait on first message.
                // If it will come, it means that user passed validation.
                // It it won't come, NullPointerException will be thrown.
                Message message = (Message) is.readObject();
                this.notifyMainThread();
                // If it passed, user is valid and can start listing for
                // further messages. If not, finally block will close
                // all opened streams and socket connection.
                while (true) {
                    if (message instanceof  Consignment) {
                        this.addMessage((Consignment) message);
                    }
                    message = (Message) is.readObject();
                }
            } catch (Exception e) {
                System.err.println("Client: Input stream has been broken.");
                System.err.println("Client: It's likely you didn't pass validation.");
            } finally {
                isClosed = true;
                this.disconnect();
                // Though it's redundant, it's required
                // in case first message was null
                // because of closed connection by a server.
                // It's done when user didn't pass a validation.
                this.notifyMainThread();
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

    private void notifyMainThread() {
        synchronized (ui.getMainThread()) {
            ui.getMainThread().notify();
        }
    }

    public boolean isClosed() {
        return isClosed;
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
