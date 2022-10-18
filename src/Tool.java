import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;

public class Tool {
    public enum Type {
        ERASER
    }

    private final Rectangle border;
    public StackPane stackPane = new StackPane();
    public boolean highlighted;
    public final Type type;

    public Tool(FileInputStream filein, double boxSize, Type type) {
        highlighted = false;
        this.type = type;
        ImageView tool = new ImageView(new Image(filein));
        tool.setFitWidth(boxSize);
        tool.setFitHeight(boxSize);
        border = new Rectangle(boxSize, boxSize);
        border.setFill(Color.TRANSPARENT);
        stackPane.getChildren().add(0, tool);
        stackPane.getChildren().add(1, border);

        stackPane.setOnMousePressed(event -> {
            if (highlighted) {
                toggleHighlight(Main.Command.OFF);
                highlighted = false;

                if (this.type == Type.ERASER) {
                    Main.erasing = false;
                }
            } else {
                //Turn off color select highlighting
                for (ColorSelect colorSelect : Main.colorGrid) {
                    colorSelect.toggleHighlight(Main.Command.OFF);
                }

                if (this.type == Type.ERASER) {
                    Main.erasing = true;
                }

                toggleHighlight(Main.Command.ON);
                highlighted = true;
            }
        });
    }

    public void toggleHighlight(Main.Command command) {
        if (command == Main.Command.ON) {
            border.setStyle("-fx-stroke: black; -fx-stroke-width: 1");
        } else {
            border.setStyle("-fx-stroke: transparent");
        }
    }
}
