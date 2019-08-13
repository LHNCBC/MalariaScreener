package gov.nih.nlm.malaria_screener.frontEnd.baseClass;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.TouchImageView;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;

public abstract class ResultDisplayerBaseActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

    }

    public void displayOriginalImage(Bundle bundle, final TouchImageView imageView, final Bitmap resultBitmap){

        String picFile = bundle.getString("picFile");

        float RV = bundle.getFloat("RV");
        boolean takenFromCam = bundle.getBoolean("cam");

        Mat oriSizeMat = UtilsCustom.oriSizeMat;
        //Mat oriSizeMat = Imgcodecs.imread(picFile, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        //Imgproc.cvtColor(oriSizeMat, oriSizeMat, Imgproc.COLOR_BGR2RGB);
        int width = (int) ((float) oriSizeMat.cols() / RV);
        int height = (int) ((float) oriSizeMat.rows() / RV);
        Mat resizedMat = new Mat();
        Imgproc.resize(oriSizeMat, resizedMat, new Size(width, height), 0, 0, Imgproc.INTER_CUBIC);
        Bitmap smallOriBitmap = Bitmap.createBitmap(resizedMat.width(), resizedMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(resizedMat, smallOriBitmap);

        if (takenFromCam) {
            int orientation = bundle.getInt("Orientation");

            Log.d(TAG, "ori result: " + orientation);

            Matrix m = new Matrix(); // rotate image according to phone orientation when image was taken
            if (orientation == Surface.ROTATION_0) {
                m.postRotate(90);
            } else if (orientation == Surface.ROTATION_270) {
                m.postRotate(180);
            } else if (orientation == Surface.ROTATION_180) {
                m.postRotate(270);
            } else if (orientation == Surface.ROTATION_90) {
                m.postRotate(0);
            }
            smallOriBitmap = Bitmap.createBitmap(smallOriBitmap, 0, 0, resizedMat.width(), resizedMat.height(), m, false);
        }

        final Switch imageSwitch = findViewById(R.id.switch_image);
        final Bitmap finalSmallOriBitmap = smallOriBitmap;
        imageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    imageSwitch.setText(R.string.result_image_switch);
                    imageView.setImageBitmap(finalSmallOriBitmap);

                } else {
                    imageSwitch.setText(R.string.original_image_switch);
                    imageView.setImageBitmap(resultBitmap);
                }
            }
        });
    }

    public Bitmap getResBitmap(Bundle bundle, TouchImageView imageView) {

        //set up Original bitmap
        byte[] byteArray = bundle.getByteArray("resImage");

        Bitmap bmp_res = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        Bitmap canvasBitmap = bmp_res.copy(Bitmap.Config.ARGB_8888, true); // set the second parameter put it to mutable
        bmp_res.recycle();
        imageView.changeEnlargedFlag(true);
        imageView.setImageBitmap(canvasBitmap);
        imageView.setMaxZoom(10.0f);

        return canvasBitmap;
    }

    public void createDirectoryAndSaveResultImage(Bitmap imageToSave, Bundle bundle) {

        String picFile = bundle.getString("picFile");

        File direct = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        // get image name
        String imgStr = picFile.toString().substring(picFile.toString().lastIndexOf("/") + 1);
        int endIndex = imgStr.lastIndexOf(".");
        String imageName = imgStr.substring(0, endIndex);

        File file = new File(new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New"), imageName + "_result.png");
        //File file = new File(new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New"), "_result.png");

        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_imagegallery, menu);
        return true;
    }


}
