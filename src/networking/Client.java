package networking;

import java.io.*;
import java.net.Socket;

import ui.Message;
import ui.Scarlet;

public class Client {
    // ------------NECESSARY-HANDLES-------------
    private Socket client;      // for establishing I/O streams with server
    private PrintStream os;     // for sending messages to server
    private DataInputStream is; // for retrieving messages from server

    private Scarlet ui;         // for adding messages to Scarlet UI
    // ------------------------------------------

    // ------------------FLAGS-------------------
    private boolean closed;
    // ------------------------------------------

    public Client(String host, Integer port, Scarlet ui) {
        this.connect(host, port);
        this.listen();

        this.ui = ui;
    }

    private void connect(String host, Integer port) {
        try {
            client = new Socket(host, port);
            os = new PrintStream(client.getOutputStream());
            is = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            System.err.println("CLIENT: Connection couldn't be established.");
            System.err.println(e);
        }
    }

    private void listen() {
        if (client != null && os != null && is != null) {
            new Thread(() -> {
                try {
                    while (!closed) {
                        String author = is.readLine();
                        String date = is.readLine();
                        String message = is.readLine();

                        if (author == null || date == null || message == null) {
                            closed = true;
                            continue;
                        }

                        ui.addMessage(new Message(author, date, message));
                    }
                } catch (Exception e) {
                    System.err.println("CLIENT: Input stream has been broken.");
                    System.err.println(e);
                }
            }).start();
        }
    }

    private void disconnect() {
        if (client != null && os != null && is != null) {
            try {
                os.close();
                is.close();
                client.close();
            } catch (IOException e) {
                System.err.println("CLIENT: Couldn't clean streams and socket smoothly.");
                System.err.println(e);
            }
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        this.disconnect();
    }

    public void send(Message message) {
        try {
            os.println(message.getAuthor());
            os.println(message.getDate());
            os.println(message.getMessage());
        } catch (Exception e) {
            System.err.println("CLIENT: Couldn't send message.");
            System.err.println(e);
        }
    }
}
