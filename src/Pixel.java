import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Pixel {
    Rectangle rect;
    private Color color;

    public Pixel(double boxSize, Color color) {
        this.color = color;
        rect = new Rectangle(boxSize, boxSize);
        //Initial pixels
        rect.setStyle("-fx-stroke: gray; -fx-stroke-width: 1;");
        rect.setFill(color);

        //Event handlers
        rect.setOnMousePressed(event -> {
            if (Main.erasing) {
                rect.setFill(Color.TRANSPARENT);
                this.color = Color.TRANSPARENT;
            } else {
                rect.setFill(Main.currentColor);
                this.color = Main.currentColor;
            }
        });

        rect.setOnMouseDragEntered(event -> {
            if (Main.erasing) {
                rect.setFill(Color.TRANSPARENT);
                this.color = Color.TRANSPARENT;
            } else {
                rect.setFill(Main.currentColor);
                this.color = Main.currentColor;
            }
        });
    }

    public void setBorder() {
        rect.setStyle("-fx-stroke: gray; -fx-stroke-width: 1");
    }

    public void getExportFriendly() {
        rect.setStyle("-fx-stroke: transparent; -fx-stroke-width: 0");
    }

    @Override
    public String toString() {
        return color.toString();
    }
}
