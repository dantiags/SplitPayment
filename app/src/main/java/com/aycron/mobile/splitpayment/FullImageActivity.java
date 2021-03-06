package com.aycron.mobile.splitpayment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.aycron.mobile.splitpayment.helpers.GoogleVisionHelper;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullImageActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private FullImageView mContentView;
    private String selectedImagePath;
    private List<EntityAnnotation> textResponses;
    private Button btnProcessImage;
    private static final int ACTION_PROCESS_IMAGE = 1000;
    private static final int ACTION_SET_TAG = 2000;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_image);

        Bundle extras = getIntent().getExtras();
        selectedImagePath = (String) extras.get("IMAGE");
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = (FullImageView) findViewById(R.id.fullScreenImage);
        Bitmap bitmapPhoto = BitmapFactory.decodeFile(selectedImagePath);
        bitmapPhoto = fixOrientation(90, bitmapPhoto);
        mContentView.setImageBitmap(bitmapPhoto);
        mContentView.setOnClickListener(this);
        mContentView.setOnTouchListener(this);

        btnProcessImage =  (Button)  findViewById(R.id.btnImageProcess);
        btnProcessImage.setOnTouchListener(mDelayHideTouchListener);
        btnProcessImage.setOnClickListener(this);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public Bitmap fixOrientation(int degrees, Bitmap bitmapPhoto) {

        if (bitmapPhoto.getWidth() > bitmapPhoto.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            bitmapPhoto = Bitmap.createBitmap(bitmapPhoto , 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
        }
        return bitmapPhoto;
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btnImageProcess:
                Object[] params = {this, ACTION_PROCESS_IMAGE, selectedImagePath };
                new ProcessImageTask().execute(params);
                break;

            case R.id.fullScreenImage:
                toggle();
                break;
        }

    }


    public void setTextResponses(List<EntityAnnotation> textResponses) {
        this.textResponses = textResponses;
        mContentView.setTextResponses(textResponses);
        mContentView.invalidate();
    }


    public void setTag(MotionEvent motionEvent) {
        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();

        EntityAnnotation e = this.mContentView.isInside(x,y);

        if(e != null){
            this.mContentView.drawTag(e);
        }
        mContentView.invalidate();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()){

            case R.id.fullScreenImage:
                Object[] params = {this, ACTION_SET_TAG, motionEvent };
                new ProcessImageTask().execute(params);
                break;
        }
        return false;

    }


    //Async Task

    class ProcessImageTask extends AsyncTask<Object, Void, String> {

        private Exception exception;
        private FullImageActivity activity;
        private ProgressDialog pdia;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(FullImageActivity.this);
            pdia.setMessage("Loading...");
            pdia.show();
        }


        protected String doInBackground(Object... params) {
            String result ="";

            try {

                this.activity = (FullImageActivity)params[0];
                int action =  (int)params[1];

                switch (action){

                    case ACTION_PROCESS_IMAGE:

                        String path = (String) params[2];
                        result = GoogleVisionHelper.ProcessImage(this.activity, path);
                        break;

                    case ACTION_SET_TAG:

                        MotionEvent motionEvent = (MotionEvent) params[2];
                        this.activity.setTag(motionEvent);
                        break;
                }

            } catch (Exception e) {
                this.exception = e;

                return null;
            }
            return result;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdia.dismiss();
            if(this.exception == null){
                //this.text.setText(result);
                //String candidateLines = TicketHelper.extractLines(result);
            }
        }
    }

}
