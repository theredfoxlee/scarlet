import java.io.*;
import java.net.Socket;

public class Chat_Listener implements Runnable {
    Socket clientSocket;
    Chat_Listener(Socket client){
        this.clientSocket = client;
    }
    @Override
    public void run() {
        try {
            DataInputStream out = new DataInputStream(clientSocket.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(out));

            //printing input stream
            System.out.println(in);
        }
        catch (IOException e){
            System.out.println("Exception: "+e);
        }



    }
}
