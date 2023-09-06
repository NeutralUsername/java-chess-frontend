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
    private SimpleStringProperty errorLabel = new SimpleStringProperty("");

    public void initializeConnection() {
        try {
            socket = new Socket("localhost", 4711);
            System.out.println("connection established with " + socket.getInetAddress());
            listenForMessages();
        } catch (IOException e) {
            Platform.exit();
        }
    }

    public void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket != null) {
                    String message = readNextMessage();
                    if (message == null) {
                        System.out.println("connection closed");
                        Platform.exit();
                        break;
                    }
                    handleMessage(message.substring(0, 1), message.substring(1));
                }
            }
        }).start();
    }

    public String readNextMessage() {
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

    public void handleMessage(String messageType, String messageContent) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (messageType) {
                    case "i":
                        id.set(messageContent);
                        break;
                    case "e":
                        errorLabel.set(messageContent);
                        break;
                    default:
                        System.out.println("unknown message type: " + messageType);
                }
            }
        });
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
        stage.setTitle("game");

        Label idLabel = new Label();
        idLabel.textProperty().bind(id);

        TextField textField = new TextField();
        textField.setMaxWidth(150);
        textField.textProperty().bindBidirectional(textFieldInput);
        textFieldInput.addListener((observable, oldValue, newValue) -> {
            this.errorLabel.set("");
        });

        Button btn = new Button("Start Connection");

        Label errorLabel = new Label();
        errorLabel.textProperty().bind(this.errorLabel);

        btn.setOnAction(event -> {
            sendMessage(textFieldInput.get());
            this.errorLabel.set("");
        });

        BorderPane root = new BorderPane();
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(idLabel, textField, btn);

        root.setCenter(vBox);
        root.setBottom(errorLabel);

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
