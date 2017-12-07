package tb.sockets.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Konsola {

	public static void main(String[] args) {
		try {
			Socket sock = new Socket("10.104.35.168", 6666);
			DataOutputStream so = new DataOutputStream(sock.getOutputStream());
			so.writeChars("wysyłam tekst");
			so.close();
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
