package tb.sockets.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Konsola {

	public static void main(String[] args) {
		try {
			ServerSocket sSock = new ServerSocket(6666);
			Socket sock = sSock.accept();
			DataInputStream in = new DataInputStream(sock.getInputStream());
			BufferedReader is = new BufferedReader(new InputStreamReader(in));
			System.out.println("Przeczytano z gniazdka: " + is.readLine());
			is.close();
			in.close();
			sock.close();
			sSock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
