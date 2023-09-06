import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Window extends Application {

    private Socket client;
    private StringProperty mostRecentMessage = new SimpleStringProperty("test");
    private boolean closed = false;

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
                while (!closed) {
                    String message = receiveMessage();
                    if (message != null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                mostRecentMessage.set(message);
                            }
                        });
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
                if (closed) {
                    return null;
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

    public void sendMessage(String message) {
        try {
            client.getOutputStream().write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        initializeConnection();

        stage.setTitle("Hello World!");

        Button btn = new Button();
        btn.textProperty().bind(mostRecentMessage);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendMessage("test message");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        stage.setScene(new Scene(root, 300, 250));
        stage.show();

        listenForMessages();
    }

    @Override
    public void stop() throws Exception {
        if (client != null) {
            try {
                client.close();
                closed = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.stop();
    }
}
