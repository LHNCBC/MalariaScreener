/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

Licensed under GNU General Public License v3.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package gov.nih.nlm.malaria_screener.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * Created by yuh5 on 1/8/2016.
 */
public class MyImageView extends ImageView {

    private static float MIN_ZOOM = 1.0f;
    private static float MAX_ZOOM = 10.0f;

    private float scaleFactor = 1.0f;
    private ScaleGestureDetector detector;

    Paint paint;
    private Bitmap resBitmap;

    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;
    private int mode;

    private float startX = 0.0f;
    private float startY = 0.0f;

    private float translateX = 0.0f;
    private float translateY = 0.0f;

    //these two variables keep track of the Amount we translated the X and Y coordinates the last time we panned
    private float previousTranslateX = 0.0f;
    private float previousTranslateY = 0.0f;

    private boolean dragged = false;

    public MyImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        detector = new ScaleGestureDetector(getContext(), new ScaleGestureListener());
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void passBitmap(Bitmap bitmap) {
        resBitmap = bitmap;

    }

    public void setImageScaleFactor(float SF) {

        float imageHeight = resBitmap.getHeight();
        float imageWidth = resBitmap.getWidth();

        resBitmap = Bitmap.createScaledBitmap(resBitmap, (int)(imageWidth*SF), (int)(imageHeight*SF), false);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // this is when first finger is pressed onto the screen
                mode = DRAG;
                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;

                break;
            case MotionEvent.ACTION_MOVE:
                // this is when the finger moves across the screen
                translateX = event.getX() - startX;
                translateY = event.getY() - startY;

                double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) +
                        Math.pow(event.getY() - (startY + previousTranslateY), 2));

                if (distance > 0) {
                    dragged = true;
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // this is when a second finger is pressed onto the screen

                mode = ZOOM;
                break;
            case MotionEvent.ACTION_UP:

                // this is when all fingers are off the screen
                mode = NONE;
                dragged = false;
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // this is when the second finger is off the screen , but the first finger is still on the screen

                mode = DRAG;
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
        }

        detector.onTouchEvent(event);

        if ((mode == DRAG && scaleFactor != MIN_ZOOM && dragged) || mode == ZOOM) {
            invalidate();
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // size of the original resized image(with no scalefactor from zooming)
        float imageHeight = resBitmap.getHeight();
        float imageWidth = resBitmap.getWidth();

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());

        // ************************** Boundary limitation/condition ***********************************
        // This takes care of the Left bound. We don't want the left bound of the image leave the left edge of the screen. Hence, if translateX is larger
        // than 0 let's set it to 0.
        // ** Note that when we say "this takes care of Left/Right/Top/Bottom bound, it means that it takes care of, for example, Right bound
        //    of the image so that when user drag the image to the left, the right bound won't leave the right edge of the screen.
        if (translateX > 0) {
            translateX = 0;
        }

        // This is where we take care of the Right bound. We compare the scaled image and the original image.
        // For example, if the zoomed-in image has a scale factor of 1.5, then we don't want the image to translate more than (1.5 - 1)*ImageWidth to the left
        // as its right bound will leave the right edge of the screen.
        // ** Note that here the minimum Zoom factor is MIN_ZOOM instead of 1.0 since the image was enlarged to fit the screen size
        else if ((-translateX) > (scaleFactor - MIN_ZOOM) * imageWidth) {
            translateX = -((scaleFactor - MIN_ZOOM) * imageWidth);
        }

        // Top bound
        if (translateY > 0) {
            translateY = 0;
        }

        //We do the exact same thing for the Bottom bound, except in this case we use the height of the image
        else if ((-translateY) > (scaleFactor - MIN_ZOOM) * imageHeight) {
            translateY = -((scaleFactor - MIN_ZOOM) * imageHeight);
        }

        // ************************** Boundary limitation/condition ***********************************
        canvas.translate(translateX/scaleFactor, translateY/scaleFactor);

        canvas.drawBitmap(resBitmap, 0, 0, paint);

        canvas.restore();
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float tempScaleFactor = detector.getScaleFactor();
            scaleFactor = scaleFactor * tempScaleFactor;
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            invalidate();

            return true;
        }
    }

}
