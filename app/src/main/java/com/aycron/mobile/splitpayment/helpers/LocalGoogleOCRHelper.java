package com.aycron.mobile.splitpayment.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by carlos.dantiags on 8/9/2016.
 */
public class LocalGoogleOCRHelper {
    private static final String TAG = "LocalGoogleOCRHelper";


    public static String ProcessImage(Activity activity, Bitmap bitmapPhoto){
        String s = "";
        List<String> textResults = GetRecognizedText(activity, bitmapPhoto);

        for (String text : textResults) {
            s = s + text + "\n\n";
        }
        return s;
    }

    public static List<String> GetRecognizedText(Activity activity, Bitmap myBitmap){

        List<String> results = new ArrayList<>();
        Context context = activity.getApplicationContext();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = activity.registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(activity, "Ocr dependencies cannot be downloaded due to low device storage", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Ocr dependencies cannot be downloaded due to low device storage");
            }
        }

        Frame frame = (new Frame.Builder()).setBitmap(myBitmap).build();
        SparseArray<TextBlock> detectedTextBlocks = textRecognizer.detect(frame);

        TreeMap<Float, Integer> sortedIndexes = new TreeMap<>();
        TextBlock block;
        for(int i = 0; i < detectedTextBlocks.size(); i++) {
            block = detectedTextBlocks.get(detectedTextBlocks.keyAt(i));
            RectF rect = new RectF(block.getBoundingBox());
            sortedIndexes.put(rect.top,detectedTextBlocks.keyAt(i));
        }

        for (Float index : sortedIndexes.keySet()) {
            results.add(detectedTextBlocks.get(sortedIndexes.get(index)).getValue());
        }


        return results;
    }
}
