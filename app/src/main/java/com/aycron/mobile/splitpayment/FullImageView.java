package com.aycron.mobile.splitpayment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by carlos.dantiags on 16/9/2016.
 */
public class FullImageView extends ImageView {

    private Object[] textObjects;

    public FullImageView(Context context) {
        super(context);
    }

    public FullImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTextObjects(Object[] textObjects) {
        this.textObjects = textObjects;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        Drawable drawable = this.getDrawable();
        Rect imageBounds = drawable.getBounds();

        //original height and width of the bitmap
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int intrinsicWidth = drawable.getIntrinsicWidth();

        //height and width of the visible (scaled) image
        int scaledHeight = imageBounds.height();
        int scaledWidth = imageBounds.width();

        //Find the ratio of the original image to the scaled image
        //Should normally be equal unless a disproportionate scaling
        //(e.g. fitXY) is used.
        float heightRatio = intrinsicHeight / scaledHeight;
        float widthRatio = intrinsicWidth / scaledWidth;

        for (Object textBox : textObjects ) {
            HashMap<String, Object> textBoxMap = (HashMap<String, Object>) textBox;
            String textString = (String) textBoxMap.get("description");
            HashMap<String, Object> verticesMap = (HashMap<String, Object> ) textBoxMap.get("boundingPoly");
            ArrayList<HashMap<String, Object>> verticesArray = (ArrayList<HashMap<String, Object>>) verticesMap.get("vertices");

            HashMap<String, Object> v1 = verticesArray.get(0);
            HashMap<String, Object> v2 = verticesArray.get(1);
            HashMap<String, Object> v3 = verticesArray.get(2);
            HashMap<String, Object> v4 = verticesArray.get(3);

            // Draws the bounding box around the TextBlock.
            Float deltaX = 3.0f;
            Float deltaY = 3.0f;
            Float positionX = 50f;
            Float positionY = 450f;

            Path wallpath = new Path();
            wallpath.reset(); // only needed when reusing this path for a new build
            wallpath.moveTo((Float.parseFloat(v1.get("x").toString())  * deltaX) + positionX, (Float.parseFloat(v1.get("y").toString()) * deltaY)+ positionY);
            wallpath.lineTo((Float.parseFloat(v2.get("x").toString())  * deltaX) + positionX, (Float.parseFloat(v2.get("y").toString()) * deltaY)+ positionY);
            wallpath.lineTo((Float.parseFloat(v3.get("x").toString())  * deltaX) + positionX, (Float.parseFloat(v3.get("y").toString()) * deltaY)+ positionY);
            wallpath.lineTo((Float.parseFloat(v4.get("x").toString())  * deltaX) + positionX, (Float.parseFloat(v4.get("y").toString()) * deltaY)+ positionY);
            wallpath.lineTo((Float.parseFloat(v1.get("x").toString())  * deltaX) + positionX, (Float.parseFloat(v1.get("y").toString()) * deltaY)+ positionY);

            canvas.drawPath(wallpath, paint);

        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
