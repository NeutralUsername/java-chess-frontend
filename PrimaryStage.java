import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PrimaryStage extends Application {

    private Socket client;

    public void initializeConnection() {
        try {
            client = new Socket("localhost", 4711);
            System.out.println("Client: connected to " + client.getInetAddress());
            InputStream in = client.getInputStream();
            byte b[] = new byte[100];
            int bytes = in.read(b);
            System.out.println("Client: recieved " + bytes + " Bytes from Server");
            String message = new String(b);
            System.out.println("Client: Message from Server: " + message);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        initializeConnection();

        stage.setTitle("Hello World!");
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
        stage.setScene(new Scene(root, 300, 250));
        stage.show();
    }
}
