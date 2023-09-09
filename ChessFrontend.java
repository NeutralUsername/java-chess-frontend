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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChessFrontend extends Application {
    private Stage stage;
    private Socket socket;
    private SimpleStringProperty id = new SimpleStringProperty("");
    private SimpleStringProperty textFieldInput = new SimpleStringProperty("");
    private SimpleStringProperty errorLabel = new SimpleStringProperty("");
    private String chessBoard = "";
    private Boolean isWhite;

    public void initializeConnection() {
        try {
            socket = new Socket("localhost", 4711);
            System.out.println("connection established with " + socket.getInetAddress());
            listenForMessages();
        } catch (IOException e) {
            System.out.println("connection failed");
            Platform.exit();
        }
    }

    public void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected()) {
                    String message = readNextMessage();
                    if (message == null) {
                        break;
                    }
                    handleMessage(message.substring(0, 1), message.substring(1));
                }
                System.out.println("connection closed");
                Platform.exit();
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
                if (byt == 0) {
                    return message;
                }
                message += (char) byt;

            } while (true);
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
                    case "b":
                        chessBoard = messageContent;
                        isWhite = false;
                        stage.setScene(getGameScene());
                        break;
                    case "w":
                        chessBoard = messageContent;
                        isWhite = true;
                        stage.setScene(getGameScene());
                        break;
                    default:
                        System.out.println("unknown message type: " + messageType);
                }
            }
        });
    }

    public void sendMessage(String messageType, String messageContent) {
        try {
            socket.getOutputStream().write((messageType + messageContent + "\0").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Scene getGameScene() {
        BorderPane root = new BorderPane();
        GridPane boardGridPane = new GridPane();
        Label currentPlayer = new Label("current turn: "+ (chessBoard.substring(0, 1).equals("w") ? "white" : "black"));
        Label playerColor  = new Label("your color: "+ (isWhite ? "white" : "black"));

        boardGridPane.setHgap(10);
        boardGridPane.setVgap(10);

        BorderPane.setAlignment(currentPlayer, Pos.CENTER);
        BorderPane.setAlignment(playerColor, Pos.CENTER);

        root.setTop(currentPlayer);
        root.setCenter(boardGridPane);
        root.setBottom(playerColor);
        return new Scene(root, 300, 250);
    }

    public Scene getHomeScene() {
        Label idLabel = new Label();
        idLabel.textProperty().bind(id);

        TextField textField = new TextField();
        textField.setMaxWidth(150);
        textField.textProperty().bindBidirectional(textFieldInput);
        textFieldInput.addListener((observable, oldValue, newValue) -> {
            this.errorLabel.set("");
        });

        Button btn = new Button("start game");

        Label errorLabel = new Label();
        errorLabel.textProperty().bind(this.errorLabel);

        btn.setOnAction(event -> {
            sendMessage("c", textFieldInput.get());
            this.errorLabel.set("");
        });

        BorderPane root = new BorderPane();
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(idLabel, textField, btn);

        BorderPane.setAlignment(errorLabel, Pos.CENTER);
        root.setCenter(vBox);
        root.setBottom(errorLabel);

        return new Scene(root, 300, 250);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("chess");
        stage.setScene(getHomeScene());
        stage.show();

        initializeConnection();
    }

    @Override
    public void stop() throws Exception {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.stop();
    }
}
