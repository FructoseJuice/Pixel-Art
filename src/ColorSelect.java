import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ColorSelect {
    public Rectangle rect;
    public final Color color;

    public ColorSelect(Color color, double boxSize) {
        rect = new Rectangle(boxSize, boxSize);
        rect.setFill(color);
        rect.setStyle("-fx-stroke: black; -fx-stroke-width: 1");
        this.color = color;

        rect.setOnMousePressed(event -> {
            Main.erasing = false;

            //Control "select" border
            toggleHighlight(Main.Command.ON);

            //Turn Color highlighting off
            for(ColorSelect colorSelect : Main.colorGrid) {
                if(colorSelect != this) {
                    colorSelect.toggleHighlight(Main.Command.OFF);
                }
            }

            //Turn tool highlighting off
            Main.eraser.highlighted = false;
            Main.eraser.toggleHighlight(Main.Command.OFF);

            Main.currentColor = color;
        });
    }

    public void toggleHighlight(Main.Command command) {
        if(command == Main.Command.ON) {
            rect.setStyle("-fx-stroke: black; -fx-stroke-width: 2");
        } else {
            rect.setStyle("-fx-stroke: black; -fx-stroke-width: 1");
        }
    }
}
