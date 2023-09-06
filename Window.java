import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Window extends Application {

    private Socket socket;
    private SimpleStringProperty id = new SimpleStringProperty("");
    private SimpleStringProperty textFieldInput = new SimpleStringProperty("");

    public void initializeConnection() {
        try {
            socket = new Socket("localhost", 4711);
            System.out.println("connection established with " + socket.getInetAddress());
            listenForMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket != null) {
                    String message = handleNextMessage();
                    if (message == null) {
                        System.out.println("connection closed");
                        break;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            String messageType = message.substring(0, 1);
                            String messageContent = message.substring(1);
                            switch (messageType) {
                                case "i":
                                    id.set(messageContent);
                                    break;
                                default:
                                    System.out.println("unknown message type: " + messageType);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    public String handleNextMessage() {
        try {
            InputStream in = socket.getInputStream();
            String message = "";
            do {
                int byt = in.read();
                if (byt == -1) {
                    return null;
                }
                message += (char) byt;

            } while (in.available() > 0);
            return message;

        } catch (IOException e) {
            return null;
        }
    }

    public void sendMessage(String message) {
        try {
            socket.getOutputStream().write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Hello World!");

        Label label = new Label();
        label.textProperty().bind(id);

        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(textFieldInput);

        Button btn = new Button("Start Connection");

        btn.setOnAction(event -> sendMessage(textFieldInput.get()));

        BorderPane root = new BorderPane();
        VBox vBox = new VBox(10); // Add some vertical spacing
        vBox.setAlignment(Pos.CENTER); // Center contents vertically
        vBox.getChildren().addAll(label, textField, btn);

        root.setCenter(vBox);

        Scene scene = new Scene(root, 300, 250);
        stage.setScene(scene);
        stage.show();

        initializeConnection();
    }

    @Override
    public void stop() throws Exception {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.stop();
    }
}
