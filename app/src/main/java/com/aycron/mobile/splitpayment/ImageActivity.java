package com.aycron.mobile.splitpayment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.aycron.mobile.splitpayment.tasks.ProcessImageTask;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.List;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener{

    private FullImageView mContentView;
    private String selectedImagePath;
    private List<EntityAnnotation> textResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Bundle extras = getIntent().getExtras();
        selectedImagePath = (String) extras.get("IMAGE");
        mContentView = (FullImageView) findViewById(R.id.imgTicketImage);
        Bitmap bitmapPhoto = BitmapFactory.decodeFile(selectedImagePath);
        mContentView.setImageBitmap(bitmapPhoto);
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(this);

    }


    public void setTextResponses(List<EntityAnnotation> textResponses) {
        this.textResponses = textResponses;
        mContentView.setTextResponses(textResponses);
        //mContentView.invalidate();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.imgTicketImage:
                Object[] params = {this,selectedImagePath};
                new ProcessImageTask().execute(params);
                break;
        }
    }
}
