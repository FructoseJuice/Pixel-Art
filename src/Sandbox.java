import javafx.application.Application;
import javafx.stage.Stage;

public class Sandbox extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Converter converter = new Converter("C:/Users/bzhid/Desktop/komi.png", "komi");
        System.exit(0);
    }
}
