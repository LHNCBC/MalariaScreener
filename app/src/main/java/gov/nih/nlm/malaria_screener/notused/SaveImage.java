package gov.nih.nlm.malaria_screener.notused;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by linw2 on 7/29/2015.
 */
//public class SaveImage extends AsyncTask<Void, Void, Void> {
public class SaveImage {
    private File pictureFile;
    private byte[] data;
    //public static final String TAG = "MyDebug";
    private boolean fromBitmap = false;
    private Bitmap bitmap;

    private int RV = 4;

    public SaveImage(File pictureFile, byte[] data) {
        super();
        this.pictureFile = pictureFile;
        this.data = data;
        save();

    }

    public SaveImage(File pictureFile, Bitmap bitmap) {
        super();
        this.pictureFile = pictureFile;
        this.bitmap = bitmap;
        fromBitmap = true;
        save();
    }

    private Void save() {

        if (!fromBitmap) {
            try {

                FileOutputStream fos = new FileOutputStream(pictureFile);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inSampleSize = RV;

                long startTime1 = System.currentTimeMillis();

                Bitmap tmp = BitmapFactory.decodeByteArray(data, 0, data.length); // add options for scaling
                Matrix m = new Matrix();
                m.postRotate(90);
                tmp = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), m, false);

                long endTime1 = System.currentTimeMillis();
                long totalTime1 = endTime1 - startTime1;

                long startTime = System.currentTimeMillis();
                tmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;

                fos.close();
                tmp.recycle();



                //Log.d(TAG, "Image saved."); // Success
            } catch (FileNotFoundException e) {
                //Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                //Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

        } else {
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Matrix m = new Matrix();
                m.postRotate(90);
                Bitmap rot = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                rot.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                //Log.d(TAG, "Bitmap saved."); // Success
            } catch (FileNotFoundException e) {
                //Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                //Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
        return null;
    }


}
