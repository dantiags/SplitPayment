package com.aycron.mobile.splitpayment.tasks;

import android.os.AsyncTask;
import com.aycron.mobile.splitpayment.ImageActivity;
import com.aycron.mobile.splitpayment.helpers.GoogleVisionHelper;
import com.aycron.mobile.splitpayment.helpers.TicketHelper;


/**
 * Created by carlos.dantiags on 12/9/2016.
 */
public class ProcessImageTask extends AsyncTask<Object, Void, String> {

    private Exception exception;
    //private FullImageActivity activity;
    private ImageActivity activity;

    protected String doInBackground(Object... params) {
        String result;
        try {
            this.activity = (ImageActivity) params[0];
            //this.activity = (FullImageActivity)params[0];
            String path = (String) params[1];
            result = GoogleVisionHelper.ProcessImage(this.activity, path);
        } catch (Exception e) {
            this.exception = e;

            return null;
        }
        return result;
    }

    protected void onPostExecute(String result) {
        if(this.exception == null){
            //this.text.setText(result);
            String candidateLines = TicketHelper.extractLines(result);
        }
    }
}
