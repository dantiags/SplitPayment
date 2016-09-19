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
import android.widget.ImageView;

import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Vertex;

import java.math.BigDecimal;
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

        //original height and width of the bitmap
        int intrinsicHeight =  this.getMeasuredHeight(); //drawable.getIntrinsicHeight();
        int intrinsicWidth =   this.getMeasuredWidth(); //drawable.getIntrinsicWidth();

        //height and width of the visible (scaled) image
        int scaledHeight = imageBounds.height();
        int scaledWidth = imageBounds.width();

        //Find the ratio of the original image to the scaled image
        //Should normally be equal unless a disproportionate scaling
        //(e.g. fitXY) is used.
        float heightRatio = (float) (intrinsicHeight - drawTop) / scaledHeight;
        float widthRatio = (float) (intrinsicWidth  - drawLeft) / scaledWidth;

        Float deltaX = round(widthRatio,2);
        Float deltaY = round(heightRatio,2);
        Float positionX = Float.parseFloat(String.valueOf(drawLeft));
        Float positionY = Float.parseFloat(String.valueOf(drawTop));

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

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
