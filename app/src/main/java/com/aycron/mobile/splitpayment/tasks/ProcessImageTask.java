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
    private TextView text;

    protected String doInBackground(Object... params) {
        String result;
        try {
            MainActivity activity = (MainActivity)params[0];
            text = activity.getResultText();
            String path = (String) params[1];
            result = GoogleVisionHelper.ProcessImage(activity, path);
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
            this.text.setText(candidateLines);

        }else {

            this.text.setText(this.exception.getMessage());
        }

    }
}
