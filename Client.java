import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
 
public class Client extends Application {
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
            } catch (IOException e) {
           // Generelles Exception-Handling zu Demo-Zwecken
            e.printStackTrace();
            }
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
}
