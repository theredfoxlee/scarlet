import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Konsola {

	public static void main(String[] args) {
		try {
			Socket sock = new Socket("192.168.43.226", 6666);
			DataOutputStream so = new DataOutputStream(sock.getOutputStream());
			so.writeChars("10 20");
			so.close();
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
