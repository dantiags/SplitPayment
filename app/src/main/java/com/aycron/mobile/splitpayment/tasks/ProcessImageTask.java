package com.aycron.mobile.splitpayment.tasks;

import android.os.AsyncTask;
import android.widget.TextView;

import com.aycron.mobile.splitpayment.MainActivity;
import com.aycron.mobile.splitpayment.helpers.GoogleVisionHelper;
import com.aycron.mobile.splitpayment.helpers.TicketHelper;


/**
 * Created by carlos.dantiags on 12/9/2016.
 */
public class ProcessImageTask extends AsyncTask<Object, Void, String> {

    private Exception exception;
    private MainActivity mainActivity;

    protected String doInBackground(Object... params) {
        String result;
        try {
            this.mainActivity = (MainActivity)params[0];
            String path = (String) params[1];
            result = GoogleVisionHelper.ProcessImage(this.mainActivity, path);
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
            this.mainActivity.getResultText().setText(candidateLines);
            this.mainActivity.launchFullSizeImageIntent();

        }else {
            this.mainActivity.getResultText().setText(this.exception.getMessage());
        }

    }
}
