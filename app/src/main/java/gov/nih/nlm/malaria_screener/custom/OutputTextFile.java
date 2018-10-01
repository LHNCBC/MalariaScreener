package gov.nih.nlm.malaria_screener.custom;

import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by yuh5 on 8/3/2016.
 */
public class OutputTextFile {

    //private static final String TAG = "MyDebug";

    Mat dx = new Mat();

    public OutputTextFile(Mat mat) {

        dx = mat;

        File textFile = null;

        try {

            textFile = createTextFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textFile != null) {
            FileOutputStream outText = null;

            try {

                outText = new FileOutputStream(textFile, true);

                for (int i = 0; i < dx.rows(); i++) {
                    for (int j = 0; j < dx.cols(); j++) {

                        outText.write((dx.get(i, j)[0] + " ").getBytes());
                    }
                    outText.write(("\n").getBytes());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outText != null) {
                        outText.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private File createTextFile() throws IOException {

        File Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imgFile = new File(Dir, "temp" + ".txt");

        return imgFile;
    }
}
