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

import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by carlos.dantiags on 16/9/2016.
 */
public class FullImageView extends ImageView {

    private List<EntityAnnotation> textResponses = new ArrayList<>();

    public FullImageView(Context context) {
        super(context);
    }

    public FullImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTextResponses(List<EntityAnnotation> textResponses) {
        this.textResponses = textResponses;
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
        int intrinsicHeight = this.getMeasuredHeight(); // drawable.getIntrinsicHeight();
        int intrinsicWidth = this.getMeasuredWidth(); // drawable.getIntrinsicWidth();

        //height and width of the visible (scaled) image
        int scaledHeight = imageBounds.height();
        int scaledWidth = imageBounds.width();

        //Find the ratio of the original image to the scaled image
        //Should normally be equal unless a disproportionate scaling
        //(e.g. fitXY) is used.
        float heightRatio = intrinsicHeight / scaledHeight;
        float widthRatio = intrinsicWidth / scaledWidth;

        for (EntityAnnotation textBox : textResponses ) {
            String textString = textBox.getDescription();
            BoundingPoly poly = textBox.getBoundingPoly();
            List<Vertex> vertices = poly.getVertices();

            Vertex v1 = vertices.get(0);
            Vertex v2 = vertices.get(1);
            Vertex v3 = vertices.get(2);
            Vertex v4 = vertices.get(3);

            // Draws the bounding box around the TextBlock.
/*          Float deltaX = 3.0f;
            Float deltaY = 3.0f;
            Float positionX = 50f;
            Float positionY = 450f;*/

            Float deltaX = widthRatio;
            Float deltaY = heightRatio;
            Float positionX = 50f;
            Float positionY = 50f;

            Path wallpath = new Path();
            wallpath.reset(); // only needed when reusing this path for a new build
            wallpath.moveTo((Float.parseFloat(v1.getX().toString())  * deltaX) + positionX, (Float.parseFloat(v1.getY().toString()) * deltaY)+ positionY);
            wallpath.lineTo((Float.parseFloat(v2.getX().toString())  * deltaX) + positionX, (Float.parseFloat(v2.getY().toString()) * deltaY)+ positionY);
            wallpath.lineTo((Float.parseFloat(v3.getX().toString())  * deltaX) + positionX, (Float.parseFloat(v3.getY().toString()) * deltaY)+ positionY);
            wallpath.lineTo((Float.parseFloat(v4.getX().toString())  * deltaX) + positionX, (Float.parseFloat(v4.getY().toString()) * deltaY)+ positionY);
            wallpath.lineTo((Float.parseFloat(v1.getX().toString())  * deltaX) + positionX, (Float.parseFloat(v1.getY().toString()) * deltaY)+ positionY);

            canvas.drawPath(wallpath, paint);

        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
