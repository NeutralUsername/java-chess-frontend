import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChessFrontend extends Application {
    private Stage stage;
    private Socket socket;

    private SimpleStringProperty id = new SimpleStringProperty("");
    private SimpleStringProperty textFieldInput = new SimpleStringProperty("");
    private SimpleStringProperty errorLabel = new SimpleStringProperty("");
    private int draggingPieceIndex;
    private Label draggingLabel;

    private String chessBoard = "";
    private boolean isWhite;
    private boolean isWhiteTurn;
    private SimpleLongProperty timerWhite = new SimpleLongProperty(0L);
    private SimpleLongProperty timerBlack = new SimpleLongProperty(0L);
    private long lastTimerUpdate;

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

    public void updateTimer() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (isWhiteTurn) {
                    long newTime = timerWhite.get() - (System.currentTimeMillis() - lastTimerUpdate);
                    if (newTime >= 0) {
                        timerWhite.set(timerWhite.get() - (System.currentTimeMillis() - lastTimerUpdate));
                    }
                } else {
                    long newTime = timerBlack.get() - (System.currentTimeMillis() - lastTimerUpdate);
                    if (newTime >= 0) {
                        timerBlack.set(timerBlack.get() - (System.currentTimeMillis() - lastTimerUpdate));
                    }
                }
                lastTimerUpdate = System.currentTimeMillis();
            }
        });
    }

    public void timerLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!socket.isClosed()) {
                    updateTimer();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
                        String[] splitB = messageContent.split(",");
                        isWhiteTurn = splitB[0].equals("w");
                        chessBoard = splitB[3];
                        isWhite = false;
                        timerWhite.set(Long.parseLong(splitB[1]));
                        timerBlack.set(Long.parseLong(splitB[2]));
                        lastTimerUpdate = System.currentTimeMillis();

                        stage.setScene(getGameScene());
                        break;
                    case "w":
                        String[] splitW = messageContent.split(",");
                        isWhiteTurn = splitW[0].equals("w");
                        chessBoard = splitW[3];
                        isWhite = true;
                        timerWhite.set(Long.parseLong(splitW[1]));
                        timerBlack.set(Long.parseLong(splitW[2]));
                        lastTimerUpdate = System.currentTimeMillis();
                        stage.setScene(getGameScene());
                        break;
                    case "x":
                        stage.setScene(getHomeScene());
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
        boardGridPane.setHgap(10);
        boardGridPane.setVgap(10);
        boardGridPane.onMouseDragReleasedProperty().set(event -> {
            draggingLabel.setStyle("-fx-border-color: black;");
        });
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int fieldIndex = isWhite ? ((7 - i) * 8 + j) : (i * 8 + j);
                String piece = chessBoard.substring(fieldIndex, fieldIndex + 1);
                Label label = new Label(piece);
                label.onDragDetectedProperty().set(event -> {
                    if (isWhite != isWhiteTurn || piece.equals(" ")
                            || (isWhite && piece.equals(piece.toLowerCase()))
                            || (!isWhite && piece.equals(piece.toUpperCase()))) {
                        return;
                    }
                    draggingPieceIndex = fieldIndex;
                    draggingLabel = label;
                    label.startFullDrag();
                    label.setStyle("-fx-border-color: blue;");
                });
                label.onMouseDragReleasedProperty().set(event -> {
                    sendMessage("m", draggingPieceIndex + "," + fieldIndex);
                });
                label.onMouseDragEnteredProperty().set(event -> {
                    if (draggingPieceIndex == fieldIndex) {
                        return;
                    }
                    label.setStyle("-fx-border-color: red;");
                });
                label.onMouseDragExitedProperty().set(event -> {
                    if (draggingPieceIndex == fieldIndex) {
                        return;
                    }
                    label.setStyle("-fx-border-color: black;");
                });
                label.setMinSize(50, 50);
                label.setMaxSize(50, 50);
                label.setAlignment(Pos.CENTER);
                label.setStyle("-fx-border-color: black;");
                boardGridPane.add(label, j, i);
            }
        }

        Label currentPlayer = new Label(
                "current turn: " + (isWhiteTurn ? "white" : "black"));
        BorderPane.setAlignment(currentPlayer, Pos.CENTER);

        Label timerWhiteLabel = new Label();
        timerWhiteLabel.textProperty().bind(timerWhite.divide(1000).asString());
        HBox timerWhiteBox = new HBox(10);
        timerWhiteBox.getChildren().addAll(new Label("white:"), timerWhiteLabel);

        Label timerBlackLabel = new Label();
        timerBlackLabel.textProperty().bind(timerBlack.divide(1000).asString());
        HBox timerBlackBox = new HBox(10);
        timerBlackBox.getChildren().addAll(new Label("black:"), timerBlackLabel);

        HBox timerBox = new HBox(10);
        timerBox.getChildren().addAll(timerWhiteBox, timerBlackBox);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setSpacing(50);

        Button exitButton = new Button("exit");
        exitButton.setOnAction(event -> {
            sendMessage("x", "");
        });

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(timerBox, new Label("your color: " + (isWhite ? "white" : "black")), exitButton);
        vBox.setAlignment(Pos.CENTER);

        root.setBottom(vBox);
        root.setPadding(new Insets(10));

        boardGridPane.setAlignment(Pos.CENTER);
        root.setTop(currentPlayer);
        root.setCenter(boardGridPane);
        return new Scene(root, 600, 600);
    }

    public Scene getHomeScene() {
        Label idLabel = new Label();
        idLabel.textProperty().bind(id);
        idLabel.onMouseClickedProperty().set(event -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(id.get());
            clipboard.setContents(stringSelection, null);
        });

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
        timerLoop();
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
