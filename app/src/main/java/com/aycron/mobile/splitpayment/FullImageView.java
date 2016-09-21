package com.aycron.mobile.splitpayment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.aycron.mobile.splitpayment.graphics.SelectionTag;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlos.dantiags on 16/9/2016.
 */
public class FullImageView extends ImageView {

    private List<EntityAnnotation> textResponses = new ArrayList<>();
    private List<SelectionTag> tags = new ArrayList<>();

    private int origW = 0;
    private int origH = 0;

    // Calculate the actual dimensions
    private int actW = 0;
    private int actH = 0;

    private float scaleX = 0f;
    private float scaleY = 0f;


    private double drawLeft = 0;
    private double drawTop = 0;


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
            drawTextResponsesBlocks(canvas, paint, positionX, positionY, deltaX, deltaY, textBox);
        }

        for (SelectionTag tag : this.tags ) {

            Paint circlePaint = new Paint();
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setStrokeWidth(5);
            canvas.drawCircle(tag.getX(), tag.getY(), 30, circlePaint);
        }

    }

    private void drawTextResponsesBlocks(Canvas canvas, Paint paint, Float positionX, Float positionY, Float deltaX, Float deltaY, EntityAnnotation textBox) {
        String textString = textBox.getDescription();
        BoundingPoly poly = textBox.getBoundingPoly();
        List<Vertex> vertices = poly.getVertices();

        Vertex v1 = vertices.get(0);
        Vertex v2 = vertices.get(1);
        Vertex v3 = vertices.get(2);
        Vertex v4 = vertices.get(3);

        //Only Draw the right ones...
        float middle = this.actW/2;
        float position1x = (Float.parseFloat(v1.getX().toString())  * deltaX) + positionX;
        float  position1y = (Float.parseFloat(v1.getY().toString()) * deltaY)+ positionY;
        float  position2x = (Float.parseFloat(v2.getX().toString()) * deltaX) + positionX;
        float  position2y = (Float.parseFloat(v2.getY().toString()) * deltaY)+ positionY;
        float  position3x = (Float.parseFloat(v3.getX().toString()) * deltaX) + positionX;
        float  position3y = (Float.parseFloat(v3.getY().toString()) * deltaY)+ positionY;
        float  position4x = (Float.parseFloat(v4.getX().toString()) * deltaX) + positionX;
        float  position4y = (Float.parseFloat(v4.getY().toString()) * deltaY)+ positionY;

        if(position1x > middle) {

            Path wallpath = new Path();
            wallpath.reset(); // only needed when reusing this path for a new build
            wallpath.moveTo(position1x, position1y);
            wallpath.lineTo(position2x, position2y);
            wallpath.lineTo(position3x, position3y);
            wallpath.lineTo(position4x, position4y);
            wallpath.lineTo(position1x, position1y);

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

    public EntityAnnotation isInside(float x, float y){
        EntityAnnotation result = null;

        for (EntityAnnotation textBox : textResponses) {
            BoundingPoly poly = textBox.getBoundingPoly();
            List<Vertex> vertices = poly.getVertices();

            Vertex v1 = vertices.get(0);

            float middle = this.actW/2;
            double  position1x = (Float.parseFloat(v1.getX().toString())  * this.scaleX) + drawLeft;

            if(position1x > middle) {
                Vertex v3 = vertices.get(2);
                double  position1y = (Float.parseFloat(v1.getY().toString()) * this.scaleY)+ drawTop;
                double  position3x = (Float.parseFloat(v3.getX().toString()) * this.scaleX) + drawLeft;
                double  position3y = (Float.parseFloat(v3.getY().toString()) * this.scaleY)+ drawTop;

                //first...
                if(x > position1x && x < position3x){
                    if(y > position1y && y < position3y ){
                        result = textBox;
                    }
                }

            }

        }

        return result;
    }

    public void drawTag(EntityAnnotation textBox) {

        BoundingPoly poly = textBox.getBoundingPoly();
        List<Vertex> vertices = poly.getVertices();

        Vertex v2 = vertices.get(1);
        Vertex v3 = vertices.get(2);

        double position2x = (Float.parseFloat(v2.getX().toString()) * this.scaleX) + drawLeft;
        double position2y = (Float.parseFloat(v2.getY().toString()) * this.scaleY) + drawTop;
        double position3x = (Float.parseFloat(v3.getX().toString()) * this.scaleX) + drawLeft;
        double position3y = (Float.parseFloat(v3.getY().toString()) * this.scaleY) + drawTop;

        SelectionTag tag = new SelectionTag();
        int circleX = (int) position2x + 50;
        tag.setX( circleX );

        int circleY = (int) (position2y + ( (position3y - position2y) / 2));
        tag.setY( circleY );

        tag.setOwner("CD");

        this.tags.add(tag);


    }

}
