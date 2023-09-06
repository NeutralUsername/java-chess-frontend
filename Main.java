import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
            Application.launch(PrimaryStage.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
