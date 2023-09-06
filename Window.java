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

public class Window extends Application {

    private Socket client;

    public void initializeConnection() {
        try {
            client = new Socket("localhost", 4711);

            System.out.println("connection established with " + client.getInetAddress());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String message = receiveMessage();
                    if (message != null) {
                        System.out.println("received message: " + message);
                    }
                }
            }
        }).start();
    }
    
    public String receiveMessage() {
        try {
            while (client.getInputStream().available() == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            InputStream in = client.getInputStream();
            byte b[] = new byte[100];
            in.read(b);
            return new String(b);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void closeConnection() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        listenForMessages();
    }
}
