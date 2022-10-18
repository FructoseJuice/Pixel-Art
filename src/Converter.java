import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Takes in a picture and converts it to .pix format
 */
public class Converter {
    public Converter(String filePath, String fileName) {
        Image image = new Image(filePath, 1024, 1024, false, false);
        PixelReader pixelReader = image.getPixelReader();


        try(PrintWriter fin = new PrintWriter(
                new FileOutputStream("C:/Users/bzhid/Documents/Pixel Art/Saved/" + fileName + ".pix"))) {
            fin.println(256);

            for(int i = 0;i < 1024;i += 4) {
                for(int j = 0;j < 1024;j += 4) {
                    fin.println(pixelReader.getColor(j, i));
                }
            }

        } catch(IOException e) {
            e.printStackTrace();
        }


    }
}
