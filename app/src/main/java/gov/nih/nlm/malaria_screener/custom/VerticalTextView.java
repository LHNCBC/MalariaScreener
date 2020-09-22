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
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by yuh5 on 5/19/2016.
 */
public class VerticalTextView extends TextView {

    final boolean topDown;

    public VerticalTextView( Context context,
                             AttributeSet attrs )
    {
        super( context, attrs );
        final int gravity = getGravity();
        if ( Gravity.isVertical(gravity)
                && ( gravity & Gravity.VERTICAL_GRAVITY_MASK )
                == Gravity.BOTTOM )
        {
            setGravity(
                    ( gravity & Gravity.HORIZONTAL_GRAVITY_MASK )
                            | Gravity.TOP );
            topDown = false;
        }
        else
        {
            topDown = true;
        }
    }

    @Override
    protected void onMeasure( int widthMeasureSpec,
                              int heightMeasureSpec )
    {
        super.onMeasure( heightMeasureSpec,
                widthMeasureSpec );
        setMeasuredDimension( getMeasuredHeight(),
                getMeasuredWidth() );
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        TextPaint textPaint = getPaint();
        textPaint.setColor( getCurrentTextColor() );
        textPaint.drawableState = getDrawableState();

        canvas.save();

        if ( topDown )
        {
            canvas.translate( getWidth(), 0 );
            canvas.rotate( 90 );
        }
        else
        {
            canvas.translate( 0, getHeight() );
            canvas.rotate( -90 );
        }

        canvas.translate( getCompoundPaddingLeft(),
                getExtendedPaddingTop() );

        getLayout().draw( canvas );
        canvas.restore();
    }

}
