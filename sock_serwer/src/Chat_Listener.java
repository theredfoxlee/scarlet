import java.io.*;
import java.net.Socket;

public class Chat_Listener implements Runnable {
    private Socket clientSocket;
    Chat_Listener(Socket client){
        this.clientSocket = client;
    }
    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(in));

            //printing input stream
            System.out.println(is.readLine());
        }
        catch (IOException e){
            System.out.println("Exception: "+e);
        }



    }
}
