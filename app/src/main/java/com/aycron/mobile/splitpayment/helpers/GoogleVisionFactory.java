package com.aycron.mobile.splitpayment.helpers;

import android.content.Context;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Created by carlos.dantiags on 14/9/2016.
 */
public class GoogleVisionFactory {

    private static String STORAGE_NAME = "SplitPayment";
    private static Vision instance = null;

    public static synchronized Vision getService(Context context) throws IOException, GeneralSecurityException {
        if (instance == null) {
            try {
                buildService(context);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        return instance;
    }


    private static void buildService(final Context context) throws IOException, GeneralSecurityException, InterruptedException {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {

                    // Directory to store user credentials for this application;
                    //dataStoreDir = new File(appConfig.appDirectoryOnDevice);

                    // Instance of the JSON factory
                    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

                    // Instance of the scopes required
                    ArrayList<String> scopes = new ArrayList<>();
                    scopes.add(VisionScopes.CLOUD_PLATFORM);

                    // Http transport creation
                    //httpTransport = AndroidHttp.newCompatibleTransport();
                    HttpTransport httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();

                    java.io.File licenseFile = getSecretFile(context);
                    GoogleCredential credential = new GoogleCredential.Builder()
                            .setTransport(httpTransport)
                            .setJsonFactory(jsonFactory)
                            .setServiceAccountId("adminappsplitpayment@splitpayment.iam.gserviceaccount.com")
                            .setServiceAccountScopes(scopes)
                            .setServiceAccountPrivateKeyFromP12File(licenseFile)
                            .build();

                    com.google.api.services.vision.v1.Vision.Builder builder = new com.google.api.services.vision.v1.Vision.Builder(httpTransport, jsonFactory, credential);
                    builder.setApplicationName(STORAGE_NAME);
                    com.google.api.services.vision.v1.Vision  client = builder.build();

                    instance =  client;
                }
                catch (GeneralSecurityException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    public static java.io.File getSecretFile(Context context)
    {
        File f = new File(context.getCacheDir()+ "/" + "SplitPayment_secret.p12");
        if (f.exists())
        {
            f.delete();
        }
        try
        {
            InputStream is = context.getAssets().open("SplitPayment_secret.p12");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return f;
    }
}
