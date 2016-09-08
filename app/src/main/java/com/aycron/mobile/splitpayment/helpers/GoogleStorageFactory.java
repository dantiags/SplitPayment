package com.aycron.mobile.splitpayment.helpers;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

/**
 * Created by carlos.dantiags on 8/9/2016.
 */
public class GoogleStorageFactory {
    private static String STORAGE_NAME = "SplitPayment";
    private static Storage instance = null;

    public static synchronized Storage getService() throws IOException, GeneralSecurityException {
        if (instance == null) {
            instance = buildService();
        }
        return instance;
    }

    private static Storage buildService() throws IOException, GeneralSecurityException {
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