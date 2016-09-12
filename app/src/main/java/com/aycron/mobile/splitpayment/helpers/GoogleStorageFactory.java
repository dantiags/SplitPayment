package com.aycron.mobile.splitpayment.helpers;

import android.content.Context;

import com.aycron.mobile.splitpayment.MainActivity;
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
                try
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

                    java.io.File licenseFile = getSecretFile(context);
                    GoogleCredential credential = new GoogleCredential.Builder()
                            .setTransport(httpTransport)
                            .setJsonFactory(jsonFactory)
                            .setServiceAccountId("adminappsplitpayment@splitpayment.iam.gserviceaccount.com")
                            .setServiceAccountScopes(scopes)
                            .setServiceAccountPrivateKeyFromP12File(licenseFile)
                            .build();

                    com.google.api.services.storage.Storage.Builder builder = new com.google.api.services.storage.Storage.Builder(httpTransport, jsonFactory, credential);
                    builder.setApplicationName(STORAGE_NAME);
                    com.google.api.services.storage.Storage client = builder.build();

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


    /*private static Storage buildService(Context context) throws IOException, GeneralSecurityException {

        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/auth") *//* auth endpoint *//*,
                Uri.parse("https://www.googleapis.com/oauth2/v4/token") *//* token endpoint *//*
        );

        String clientId = "adminappsplitpayment@splitpayment.iam.gserviceaccount.com";
        Uri redirectUri = Uri.parse("127.0.0.1");
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                clientId,
                AuthorizationRequest.RESPONSE_TYPE_CODE,
                redirectUri
        );
        builder.setScopes("https://www.googleapis.com/auth/devstorage.full_control");
        AuthorizationRequest request = builder.build();

        AuthorizationService authorizationService = new AuthorizationService(context);

        String action = "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE";
        //authorizationService.performAuthorizationRequest()











        HttpTransport transport = new com.google.api.client.http.javanet.NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = GoogleCredential.getApplicationDefault(transport, jsonFactory);

        // Depending on the environment that provides the default credentials (for
        // example: Compute Engine, App Engine), the credentials may require us to
        // specify the scopes we need explicitly.  Check for this case, and inject
        // the Cloud Storage scope if required.
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }

        return new Storage.Builder(transport, jsonFactory, credential)
                .setApplicationName(STORAGE_NAME)
                .build();
    }


*/
/*
    function callAuthorizedGoogleApi(callbackFunction, args) {

        var d = new Date();
        var iat = d.getTime() /1000;
        d.setHours(d.getHours() + 1)
        var exp = d.getTime() /1000;

        var url = 'https://www.googleapis.com/oauth2/v4/token';
        var header = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9';
        var data = {
                "iss": "adminappsplitpayment@splitpayment.iam.gserviceaccount.com",
                "scope": "https://www.googleapis.com/auth/devstorage.full_control",
                "aud": "https://www.googleapis.com/oauth2/v4/token",
                "exp": Math.round(exp),
                "iat": Math.round(iat)
        }

        var unsignedToken = header + "." + base64url(CryptoJS.enc.Utf8.parse(JSON.stringify(data)));

        // initialize
        var sig = new KJUR.crypto.Signature({ "alg": "SHA256withRSA" });
        var rsaPrivateKey = "-----BEGIN PRIVATE KEY-----\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCdgZSvNioE+fZL\nm9OiL3x9vvXIKB4Uwdx1X2c4dvv7kfaQhzqpi4KwQBXqYpMIbe5ASyZ1vfQPuhXh\nlk5CerdWt1UMKhLScKsZoJWXGLWxmHnyVLcsxMeKxyDYbiFrqDThyAyfo9YcIsLY\nayfhXEWDBzbPGBKu/bCuhZ7zw2PBis1Ezl+Wf0aySuSf/F/05Fs9+1uW8gH8f5Fn\nZ3h4IhIUhXE00gA3IEK8vzPnOZN5xCH6CHMdrKRFmEqCBo1aQkyTtWsPOz+7YUTn\nATf23lOMjx8lCGolkkmPgsBjGoDFyDRvSWICBhcREPs8qwJahVjso9UUgTeRd49g\nICVnGQIBAgMBAAECggEANXdyaJPjabMMl5f7HHgwM2NqfZqYs4UneDR9jp5dZYWk\nBGXTVRCFuZzXk6wIq3jdwreAA8IKongRy3VBdbHAoNA1L57sqsESY+2uOg9qRjIz\nWz7Eu47FCegUz0N7VtdPtEE7f8mW+hAWwm6FqCuxcQw239AVL4/wtR2o2qi+KUmT\nioldqFog1Lu8oB7k+5d0pHZobFhg+8OBFePH36QJVnHEwBAoKYueVWu2hYbniaM+\nNqiQ4p/CmRw2TbmnNyEDWu4mU/Az6Pb0NLZ7486wuNni0jW6dZT/ijqwbyMzwe5k\nlTMjr3dWXTE6OG12Nf2r0DU4mOt8+FKkViUyJN+e0QKBgQDOGb7+oBJ7wzMLQ1/3\nUGv/nSXIIsxC8FAtwF/o3CwG4ypVXFnuabWrGw1/PSUvYjrboZ9fXoLkqpB6iaF0\nNT2OuHdy6MWmJHllx1igKPSen/cQqSFhM/VTwgSq+MR7b0icf0GNjC/ZPfXFhq0N\nM0dcnrzKBJ4nPFUi43tuVagEPQKBgQDDo+vDB6pI8GYAazIyRulst6QSzm9alp9M\nWI1biNBwRChKdotIrvuA5JuZ4mnyMA9/Af6Aw3Kk0YH0y0C2lpSEpLcdJQ+XNJ/k\na7l6g+dUhbxjc6sk2FmA9M9UAT7HDz6JnapbxxPFtOqKjqcVKTkER7kHPXRR7+Yb\na9weacjdFQKBgQC7iwoVwauQG0wzj5egAPqFm4Zp2+BreSo8t7WOu+sESWqYSnU7\nUc4SaYeapRVVTNmqSiQwMFyDoAHgv6S/jkL5wDpRwic4zC/7wa6P7zTJs16rNnw9\na35uPTrqKX0BpX45ikMofsx7rroaiDaosGTbj0bTvef8ZBZErSQAfslpMQKBgQCc\nCGenc7yXUcw4d0ZgnPfcspZUPXrISCsiq9mJ8JM1htaPlP4/aPfD9BA56j9PXo9t\nX3zI25ja5RF92IFzXqNzKx+0SfOmR91iQ5x5nyvn2IXI197ekwLHe/jDaf7Uqk7Z\nWPsfGaamX0VUXrPZh7gmtsFWief01Z2zrxGu2/XMhQKBgQCIJYhHZeYUkT70y4el\nZkS08J4fdwunwyz1xyRvID4TaHYjScLZXDjwCoqfwQyElQXHEkBTin0bNAthep4V\n0i+HDywKzVthU0e+eJ+atVOzCTOz4A/fWq1rigYbjzTBtokHEOzOAFMWLkvbHw3r\nBn3FG0azELrEhpnFfvrZg/VbCQ==\n-----END PRIVATE KEY-----\n";

        // initialize for signature generation
        sig.init(rsaPrivateKey);   // rsaPrivateKey of RSAKey object
        // update data
        sig.updateString(unsignedToken)
        // calculate signature
        var sigValueHex = sig.sign()

        signature = base64urlHEX(sigValueHex);

        var signedToken = unsignedToken + "." + signature;

        $.ajax(
                {
                        type: "POST",
                url: url,
                data: "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=" + signedToken,
                success: function (ret) {

            oAuthToken = ret.access_token;

            callbackFunction(oAuthToken, args);
        },
        error: function (xhr, textStatus, errorThrown) {
            console.log(xhr, textStatus, errorThrown + 'error');
            return false;
        }
        });


    }*/

}