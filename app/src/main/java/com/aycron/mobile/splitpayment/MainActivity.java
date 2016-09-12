package com.aycron.mobile.splitpayment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aycron.mobile.splitpayment.exceptions.ExceptionHandler;
import com.aycron.mobile.splitpayment.helpers.GoogleVisionHelper;
import com.aycron.mobile.splitpayment.helpers.LocalGoogleOCRHelper;
import com.aycron.mobile.splitpayment.helpers.MarshMallowPermission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity  implements OnClickListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int RESULT_LOAD_IMAGE = 200;

    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String TAG = "SplitPaymentActivity";
    private Uri fileUri;
    private String selectedImagePath;
    Bitmap bitmapPhoto;
    Button takePhoto;
    Button selectPhoto;
    ImageView imageTaken;
    Button processPhoto;
    TextView resultText;
    MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);
    private static final String splitPaymentImageFolder = "SplitPaymentImages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        takePhoto = (Button) findViewById(R.id.takePhoto);
        takePhoto.setOnClickListener(this);
        imageTaken = (ImageView) findViewById(R.id.imgTaken);
        processPhoto = (Button) findViewById(R.id.processPhoto);
        processPhoto.setOnClickListener(this);
        resultText = (TextView) findViewById(R.id.txtResult);
        selectPhoto = (Button) findViewById(R.id.selectPhoto);
        selectPhoto.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.takePhoto:
                getPhotoFromCamera();
                break;

            case R.id.processPhoto:
                //String s = LocalGoogleOCRHelper.ProcessImage(this, bitmapPhoto);
                String s = GoogleVisionHelper.UploadImage(this,selectedImagePath);
                resultText.setText(s);
                break;

            case R.id.selectPhoto:
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
        }

    }




    public void getPhotoFromCamera() {

        if (!marshMallowPermission.checkPermissionForCamera()) {
            marshMallowPermission.requestPermissionForCamera();
        } else {
            if (!marshMallowPermission.checkPermissionForExternalStorage()) {
                marshMallowPermission.requestPermissionForExternalStorage();
            } else {
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                fileUri = getOutputMediaFileUri(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE); // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                // start the image capture Intent
                try {
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

                } catch (Exception ex){
                    String s = ex.getMessage();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // Image captured and saved to fileUri specified in the Intent
                MediaScannerConnection.scanFile(this, new String[] { fileUri.getPath() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i(TAG, "Scanned " + path);
                            }
                        });
                Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();


                this.getContentResolver().notifyChange(fileUri, null);
                ContentResolver cr = this.getContentResolver();

                try
                {
                    bitmapPhoto = android.provider.MediaStore.Images.Media.getBitmap(cr, fileUri);
                    ExifInterface exif = new ExifInterface(fileUri.getPath());
                    String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                    Toast.makeText(this, exifOrientation, Toast.LENGTH_SHORT).show();
                    //if(exifOrientation.equals("0")) {
                        fixOrientation(90);
                    //}
                    imageTaken.setImageBitmap(bitmapPhoto);
                    processPhoto.setEnabled(true);
                }
                catch (Exception e)
                {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed to load", e);
                }


            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Image capture failed", Toast.LENGTH_LONG).show();
                processPhoto.setEnabled(false);
            }
        }else if (requestCode == RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                // String picturePath contains the path of selected Image
                bitmapPhoto = BitmapFactory.decodeFile(picturePath);
                fixOrientation(90);
                imageTaken.setImageBitmap(bitmapPhoto);
                processPhoto.setEnabled(true);
                this.selectedImagePath = picturePath;
            }
        }
    }

    public void fixOrientation(int degrees) {
        if (bitmapPhoto.getWidth() > bitmapPhoto.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            bitmapPhoto = Bitmap.createBitmap(bitmapPhoto , 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
        }
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

       String storageState =  Environment.getExternalStorageState();
        File mediaFile = null;

        if (Environment.MEDIA_MOUNTED.equals(storageState)) {

            String root = Environment.getExternalStorageDirectory().toString();
            //String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File mediaStorageDir = new File(root + "/" + splitPaymentImageFolder);
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            if (type == MEDIA_TYPE_IMAGE) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_" + timeStamp + ".jpg");
            } else {
                return null;
            }
        }

        return mediaFile;
    }

}
