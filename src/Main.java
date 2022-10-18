import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main extends Application {
    //Used for controlling the highlighting of tools/colors
    public enum Command {
        ON,
        OFF
    }

    //Color that's currently selected
    static Color currentColor = Color.TRANSPARENT;
    static Tool eraser;
    static List<ColorSelect> colorGrid = new ArrayList<>();
    private final List<Pixel> pixels = new ArrayList<>();
    static boolean erasing = false;
    static boolean gridOn = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setResizable(false);
        BorderPane root = new BorderPane();
        HBox taskbar = new HBox();
        HBox prompt = new HBox();
        root.setCenter(prompt);
        root.setTop(taskbar);

        prompt.setPadding(new Insets(10, 10, 10, 10));
        prompt.setSpacing(3);
        Scene scene = new Scene(root);
        Label label = new Label("Enter desired amount of pixels: ");

        TextField textField = new TextField();
        textField.setPrefWidth(50);

        Button submit = new Button("Submit");

        prompt.getChildren().add(label);
        prompt.getChildren().add(textField);
        prompt.getChildren().add(submit);

        Button load = new Button("Load File");
        taskbar.getChildren().add(load);

        Button convert = new Button("Convert file");
        taskbar.getChildren().add(convert);

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        submit.setOnMousePressed(event -> {
            try {
                int pixels = Integer.parseInt(textField.getText());
                if (pixels > 256 || pixels < 8) {
                    throw new Exception("Number must be in range 8 - 256 (inclusive)");
                }

                primaryStage.close();
                drawingBoard(pixels, null);
            } catch (Exception e) {
                String message;

                if (e instanceof IllegalArgumentException) {
                    message = textField.getText() + " is not a valid number.";
                } else {
                    message = e.getMessage();
                }

                Alert alert = new Alert(Alert.AlertType.ERROR, message);
                alert.showAndWait();
            }
        });

        load.setOnMousePressed(event -> {
            File[] files = new File("C:/Users/bzhid/Documents/Pixel Art/Saved").listFiles();

            Stage secondaryStage = new Stage();
            VBox fileList = new VBox();
            Scene secondaryScene = new Scene(fileList);
            secondaryStage.setScene(secondaryScene);

            if (files != null && files.length != 0) {
                for (File file : files) {
                    Button fileLaunch = new Button(file.getName());
                    fileLaunch.setOnMousePressed(newEvent -> {
                        secondaryStage.close();
                        primaryStage.close();
                        drawingBoard(0, file.getAbsolutePath());
                    });
                    fileList.getChildren().add(fileLaunch);
                }
            } else {
                Label none = new Label("No files found.");
                fileList.getChildren().add(none);
            }

            secondaryStage.show();
        });

        convert.setOnMousePressed(event -> {
            Stage secondaryStage = new Stage();
            VBox vBox = new VBox();
            HBox hBox = new HBox();
            Label secondaryLabel = new Label("Enter name of picture to be converted: ");
            TextField secondaryTextField = new TextField();
            Button secondarySubmit = new Button("Submit");
            Button cancel = new Button("Cancel");
            hBox.getChildren().add(secondaryLabel);
            hBox.getChildren().add(secondaryTextField);
            hBox.getChildren().add(secondarySubmit);
            vBox.getChildren().add(hBox);
            vBox.getChildren().add(cancel);

            cancel.setOnMousePressed(newEvent -> secondaryStage.close());

            secondarySubmit.setOnMousePressed(newEvent -> {
                String fileName = secondaryTextField.getText();
                String[] nameExtension = fileName.split("\\.");
                if (nameExtension.length != 2) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Invalid file name.");
                    alert.showAndWait();
                } else {
                    convertImage(nameExtension[0], nameExtension[1]);
                    secondaryStage.close();
                }
            });

            Scene secondaryScene = new Scene(vBox);
            secondaryStage.setScene(secondaryScene);
            secondaryStage.show();
        });
    }

    public void drawingBoard(int numPixels, String fileName) {
        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        GridPane canvas = new GridPane();
        GridPane colors = new GridPane();
        HBox toolBar = new HBox();
        root.setTop(toolBar);
        root.setCenter(canvas);
        root.setRight(colors);

        //Full drag gesture for drawing in one stroke
        root.setOnDragDetected(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                event.consume();
                root.startFullDrag();
            }
        });

        double boxSize;
        //Size of window
        final double CANVAS_SIZE = 768.0;
        if (fileName == null) {
            //Adds pixels for drawing on canvas
            boxSize = CANVAS_SIZE / numPixels;
            for (int i = 0; i < numPixels; i++) {
                for (int j = 0; j < numPixels; j++) {
                    Pixel pixel = new Pixel(boxSize, Color.TRANSPARENT);
                    pixels.add(pixel);
                    canvas.add(pixel.rect, j, i);
                }
            }
        } else {
            //Constructing from save
            try (Scanner fin = new Scanner(new FileInputStream(fileName))) {
                numPixels = Integer.parseInt(fin.nextLine());

                boxSize = CANVAS_SIZE / numPixels;
                for (int i = 0; i < numPixels; i++) {
                    for (int j = 0; j < numPixels; j++) {
                        //Retrieve color from file
                        Color newColor = (Color) Paint.valueOf(fin.nextLine());
                        Pixel pixel = new Pixel(boxSize, newColor);
                        pixel.getExportFriendly();
                        pixels.add(pixel);
                        canvas.add(pixel.rect, j, i);
                    }
                }
            } catch (IOException e) {
                System.out.println("Couldn't load save.");
            }
        }

        boxSize = CANVAS_SIZE / 15;
        constructColorPicker(colors, boxSize);

        //Construct toolbar
        Button export = new Button("Export png");
        export.setOnMousePressed(event -> handleExport(canvas));

        Button saveAs = new Button("Save as");
        int finalNumPixels = numPixels;
        saveAs.setOnMousePressed(event -> handleSave(finalNumPixels));

        toolBar.getChildren().add(export);
        toolBar.getChildren().add(saveAs);

        //TODO: Set event for closing while drawing
        //stage.setOnCloseRequest(null);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Pixel Art");
        //stage.setResizable(false);
        stage.show();
    }

    public void constructColorPicker(GridPane gridPane, double boxSize) {
        List<Color> colors = new ArrayList<>();
        colors.add(Color.BLACK);
        colors.add(Color.WHITE);
        colors.add(Color.RED);
        colors.add(Color.ORANGE);
        colors.add(Color.YELLOW);
        colors.add(Color.DARKBLUE);
        colors.add(Color.GREEN);
        colors.add(Color.LIGHTBLUE);
        colors.add(Color.PURPLE);
        colors.add(Color.PINK);

        gridPane.setPadding(new Insets(5, 5, 5, 5));
        gridPane.setHgap(3);
        gridPane.setVgap(3);
        gridPane.add(new Label("Colors"), 0, 0);
        int col = 0;
        int row = 1;

        //Create color palette
        for (Color color : colors) {
            ColorSelect temp = new ColorSelect(color, boxSize);
            colorGrid.add(temp);
            gridPane.add(temp.rect, col, row);

            if (col == 1) {
                col = 0;
                ++row;
            } else {
                ++col;
            }
        }

        //Add tools at bottom
        gridPane.add(new Label("Tools"), 0, ++row); //Implicitly adds adjustment to row
        ++row;
        try {
            eraser = new Tool(new FileInputStream("Images/eraser.png"), boxSize, Tool.Type.ERASER);
            FileInputStream fin = new FileInputStream("Images/grid.png");
            ImageView grid = new ImageView(new Image(fin, boxSize, boxSize, false, false));

            grid.setOnMousePressed(event -> {
                if (gridOn) {
                    gridOn = false;
                    for (Pixel pixel : pixels) {
                        pixel.getExportFriendly();
                    }
                } else {
                    gridOn = true;
                    for (Pixel pixel : pixels) {
                        pixel.setBorder();
                    }
                }
            });

            gridPane.add(eraser.stackPane, 0, row);
            gridPane.add(grid, 1, row);
        } catch (IOException e) {
            System.out.println("Image resource not found.");
        }
    }

    public void handleExport(GridPane canvas) {
        Stage secondaryStage = new Stage();
        TextField textField = new TextField();
        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");
        GridPane prompt = constructPrompt(submit, cancel, textField);

        submit.setOnMousePressed(event -> {
            if (!textField.getText().isBlank()) {
                File file = new File("C:/Users/bzhid/Documents/Pixel Art/" + textField.getText() + ".png");

                try {
                    //Removes borders from pixels to get ready for screenshot
                    for (Pixel pixel : pixels) {
                        pixel.getExportFriendly();
                    }

                    WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

                    for (Pixel pixel : pixels) {
                        pixel.setBorder();
                    }
                    secondaryStage.close();
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage());
                    alert.showAndWait();
                }

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Must put in valid file name");
                alert.showAndWait();
            }
        });

        cancel.setOnMousePressed(event -> secondaryStage.close());

        Scene scene = new Scene(prompt);
        secondaryStage.setScene(scene);
        secondaryStage.initStyle(StageStyle.UNDECORATED);
        secondaryStage.show();
    }

    public void handleSave(int numPixels) {
        TextField textField = new TextField();
        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");
        Stage secondaryStage = new Stage();
        GridPane prompt = constructPrompt(submit, cancel, textField);

        submit.setOnMousePressed(event -> {
            if (!textField.getText().isBlank()) {
                try {
                    FileOutputStream fout = new FileOutputStream("C:/Users/bzhid/Documents/Pixel Art/Saved/" + textField.getText() + ".pix");
                    PrintWriter writer = new PrintWriter(fout);

                    writer.println(numPixels);
                    for (Pixel pixel : pixels) {
                        writer.println(pixel.toString());
                    }
                    writer.flush();
                    fout.close();
                    secondaryStage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Must put in valid file name");
                alert.showAndWait();
            }
        });

        cancel.setOnMousePressed(event -> secondaryStage.close());

        Scene scene = new Scene(prompt);
        secondaryStage.setScene(scene);
        secondaryStage.initStyle(StageStyle.UNDECORATED);
        secondaryStage.show();
    }

    public GridPane constructPrompt(Button submit, Button cancel, TextField textField) {
        GridPane prompt = new GridPane();
        prompt.setPadding(new Insets(5, 5, 5, 5));
        Label label = new Label("Name your artwork: ");
        prompt.add(label, 0, 0);
        prompt.add(textField, 1, 0);
        prompt.add(submit, 2, 0);
        prompt.add(cancel, 1, 1);

        return prompt;
    }

    public void convertImage(String fileName, String extension) {
        String filePath = "C:/Users/bzhid/Documents/Pixel Art/" + fileName + "." + extension;
        Image image = new Image(filePath, 1024, 1024, false, false);
        PixelReader pixelReader = image.getPixelReader();

        try (PrintWriter fin = new PrintWriter(
                new FileOutputStream("C:/Users/bzhid/Documents/Pixel Art/Saved/" + fileName + ".pix"))) {
            fin.println(256);

            for (int i = 0; i < 1024; i += 4) {
                for (int j = 0; j < 1024; j += 4) {
                    fin.println(pixelReader.getColor(j, i));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
