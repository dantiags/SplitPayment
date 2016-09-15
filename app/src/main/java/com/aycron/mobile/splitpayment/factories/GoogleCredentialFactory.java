package com.aycron.mobile.splitpayment.factories;

import android.content.Context;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Created by carlos.dantiags on 15/9/2016.
 */
public class GoogleCredentialFactory {

    private static String SERVICE_ACCOUNT_ID = "adminappsplitpayment@splitpayment.iam.gserviceaccount.com";

    public static GoogleCredential getCredential(Context context, HttpTransport httpTransport,JsonFactory jsonFactory, ArrayList<String> scopes){

        GoogleCredential credential = null;
        try
            {
                java.io.File licenseFile = getSecretFile(context);
                credential = new GoogleCredential.Builder()
                        .setTransport(httpTransport)
                        .setJsonFactory(jsonFactory)
                        .setServiceAccountId(SERVICE_ACCOUNT_ID)
                        .setServiceAccountScopes(scopes)
                        .setServiceAccountPrivateKeyFromP12File(licenseFile)
                        .build();

            }
        catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return credential;
    }


    private static java.io.File getSecretFile(Context context)
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
