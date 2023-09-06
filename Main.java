import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try {
            Socket client = new Socket("localhost", 4711);
            System.out.println("Client: connected to " +
                    client.getInetAddress());
            InputStream in = client.getInputStream();
            byte b[] = new byte[100];
            int bytes = in.read(b);
            System.out.println("Client: recieved " + bytes + " Bytes from Server");
            String message = new String(b);
            System.out.println("Client: Message from Server: " +
                    message);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
