package gov.nih.nlm.malaria_screener.camera;

import android.content.Context;
import android.hardware.Camera;

import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;


/**
 * Created by Wilson on 7/15/2015.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "MyDebug";
    private SurfaceHolder sHolder;
    private Camera cam;
    private Display display;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        //Log.d(TAG, "Creating camera preview object...");
        cam = camera;
        sHolder = getHolder();
        sHolder.addCallback(this);
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            cam.setPreviewDisplay(holder);
            cam.startPreview();
            cam.setDisplayOrientation(90);
            //Log.d(TAG, "Starting camera preview...");

        } catch (IOException e) {
            //Log.d(TAG, "Error displaying camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (cam != null) {
            cam.stopPreview();
            cam.setPreviewCallback(null);
            //Log.d(TAG, "Preview destroyed.");
            cam.release();
            cam = null;
            //Log.d(TAG, "Camera released.");

        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        Camera.Parameters parameters = cam.getParameters();

        /*List<Camera.Size> preview_sizes = parameters.getSupportedPreviewSizes();

        double surfaceRatio = (double)h/(double)w ;

        Log.d(TAG, "Ratio: " + surfaceRatio);

        double[] ratio_diff = new double[preview_sizes.size()];

        for (int i=0;i<preview_sizes.size();i++) {

            double height = preview_sizes.get(i).width;
            double width = preview_sizes.get(i).height;

            double providedRatio = height/width;

            ratio_diff[i] = Math.abs(surfaceRatio - providedRatio);

            //Log.d(TAG, "ratio_diff: " + ratio_diff[i]);

        }

        double min = ratio_diff[0];
        int minIndex = 0;
        for (int i=0;i<preview_sizes.size();i++){
            if (ratio_diff[i] <min && preview_sizes.get(i).height >1000 && preview_sizes.get(i).width>1000){
                minIndex = i;
            }
        }*/

        if (sHolder.getSurface() == null) {
            //Log.d(TAG, "No surface.");
            return;
        }

        try {
            cam.stopPreview();
            //Log.d(TAG, "Stopping camera preview...");
        } catch (Exception e) {
            //Log.d(TAG, "Error stopping camera preview: " + e.getMessage());
        }

        //set preview size and make any resize, rotate or
        //reformatting changes here


////        Camera.Size best_size = preview_sizes.get(minIndex);
////        Log.d(TAG, "Best size: " + best_size.height + " " + best_size.width);
////        parameters.setPreviewSize(best_size.width, best_size.height);
        cam.setParameters(parameters);

        // start preview with new settings

        try { // rotate camera preview according to phone orientation
            if (display.getRotation() == Surface.ROTATION_90){ // reverse landscape
                cam.setDisplayOrientation(0);
            } else if (display.getRotation() == Surface.ROTATION_180) { //reverse portrait
                cam.setDisplayOrientation(270);
            } else if (display.getRotation() == Surface.ROTATION_270) { //landscape
                cam.setDisplayOrientation(180);
            } else if (display.getRotation() == Surface.ROTATION_0) { //portrait
                cam.setDisplayOrientation(90);
            }
            cam.setPreviewDisplay(holder);
            cam.startPreview();

            //Log.d(TAG, "Restarting camera preview...");
        } catch (Exception e) {
            //Log.d(TAG, "Error starting camera preview from surfaceChanged: " + e.getMessage());
        }

    }


}