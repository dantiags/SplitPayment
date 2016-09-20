package com.aycron.mobile.splitpayment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Vertex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlos.dantiags on 16/9/2016.
 */
public class FullImageView extends ImageView {

    private List<EntityAnnotation> textResponses = new ArrayList<>();

    private int origW = 0;
    private int origH = 0;

    // Calculate the actual dimensions
    private int actW = 0;
    private int actH = 0;

    private float scaleX = 0f;
    private float scaleY = 0f;


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

        // These holds the ratios for the ImageView and the bitmap
        Bitmap bitmap = ((BitmapDrawable)this.getDrawable()).getBitmap();
        double bitmapRatio  = ((double)bitmap.getWidth())/bitmap.getHeight();
        double imageViewRatio  = ((double)this.getWidth())/this.getHeight();

        double drawLeft = 0;
        double drawTop = 0;
        double drawHeight = 0;
        double drawWidth = 0;

        if(bitmapRatio > imageViewRatio)
        {
            drawLeft = 0;
            drawHeight = (imageViewRatio/bitmapRatio) * this.getHeight();
            drawTop = (this.getHeight() - drawHeight)/2;
        }
        else
        {
            drawTop = 0;
            drawWidth = (bitmapRatio/imageViewRatio) * this.getWidth();
            drawLeft = (this.getWidth() - drawWidth)/2;
        }

        Float positionX = Float.parseFloat(String.valueOf(drawLeft));
        Float positionY = Float.parseFloat(String.valueOf(drawTop));
        Float deltaX = scaleX;
        Float deltaY = scaleY;


        for (EntityAnnotation textBox : textResponses ) {
            String textString = textBox.getDescription();
            BoundingPoly poly = textBox.getBoundingPoly();
            List<Vertex> vertices = poly.getVertices();

            Vertex v1 = vertices.get(0);
            Vertex v2 = vertices.get(1);
            Vertex v3 = vertices.get(2);
            Vertex v4 = vertices.get(3);

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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get image matrix values and place them in an array
        float[] f = new float[9];
        getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        Log.e("DBG", "["+origW+","+origH+"] -> ["+actW+","+actH+"] & scales: x="+scaleX+" y="+scaleY);

        this.origW = origW;
        this.origH = origH;
        this.actW = actW;
        this.actH = actH;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

}
