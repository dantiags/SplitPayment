package com.aycron.mobile.splitpayment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ErrorActivity extends AppCompatActivity {

    TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        Bundle extras = getIntent().getExtras();
        String errorSt = extras.getString("error");

        errorText = (TextView) findViewById(R.id.errorText);

        errorText.setText(errorSt);

    }
}
