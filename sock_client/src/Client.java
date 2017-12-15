import java.io.*;
import java.net.Socket;

public class Client {
    private final static int port_number = 6510;

    public static void main(String [] args){
        try{
            Socket clientSocket = new Socket("localhost",port_number);
            PrintWriter write_out = new PrintWriter(clientSocket.getOutputStream(),true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String ip_adress="121.23.4324";
            //sending ip adress to server
            write_out.println(ip_adress);
            System.out.println("We sent:"+ip_adress);


           // BufferedOutputStream outputString = new BufferedOutputStream(clientSocket.getOutputStream());

        }
        catch (IOException e){
            e.printStackTrace();
        }




    }


}
