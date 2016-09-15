package com.aycron.mobile.splitpayment.factories;

import android.content.Context;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Created by carlos.dantiags on 8/9/2016.
 */
public class GoogleStorageFactory {
    private static String STORAGE_NAME = "SplitPayment";
    private static Storage instance = null;

    public static synchronized Storage getService(Context context) throws IOException, GeneralSecurityException {
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
                // Directory to store user credentials for this application;
                //dataStoreDir = new File(appConfig.appDirectoryOnDevice);

                // Instance of the JSON factory
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

                // Instance of the scopes required
                ArrayList<String> scopes = new ArrayList<>();
                scopes.add(StorageScopes.CLOUD_PLATFORM);

                // Http transport creation
                //httpTransport = AndroidHttp.newCompatibleTransport();
                HttpTransport httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();

                GoogleCredential credential = GoogleCredentialFactory.getCredential(context, httpTransport, jsonFactory, scopes);

                com.google.api.services.storage.Storage.Builder builder = new com.google.api.services.storage.Storage.Builder(httpTransport, jsonFactory, credential);
                builder.setApplicationName(STORAGE_NAME);
                com.google.api.services.storage.Storage client = builder.build();

                instance =  client;
            }
        });
        thread.start();
        thread.join();
    }

}