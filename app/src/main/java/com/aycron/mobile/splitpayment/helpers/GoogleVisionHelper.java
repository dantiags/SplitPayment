package com.aycron.mobile.splitpayment.helpers;

import android.app.Activity;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by carlos.dantiags on 8/9/2016.
 */
public class GoogleVisionHelper {


    private static String BUCKET_NAME = "splitpayment.appspot.com";

    public static String UploadImage(Activity activity, String  path){
        String s ="";
        try {

            boolean useCustomMetadata = false;

            //Get the File
            File mediaFile = new File(path);
            InputStreamContent mediaContent =
                    new InputStreamContent("image/jpeg",
                            new BufferedInputStream(new FileInputStream(mediaFile)));

            // Knowing the stream length allows server-side optimization, and client-side progress
            // reporting with a MediaHttpUploaderProgressListener.
            mediaContent.setLength(mediaFile.length());

            StorageObject objectMetadata = null;

            if (useCustomMetadata) {
                // If you have custom settings for metadata on the object you want to set
                // then you can allocate a StorageObject and set the values here. You can
                // leave out setBucket(), since the bucket is in the insert command's
                // parameters.
                objectMetadata = new StorageObject()
                        .setName("myobject")
                        .setMetadata(ImmutableMap.of("key1", "value1", "key2", "value2"))
                        .setAcl(ImmutableList.of(
                                new ObjectAccessControl().setEntity("domain-example.com").setRole("READER"),
                                new ObjectAccessControl().setEntity("user-administrator@example.com").setRole("OWNER")
                        ))
                        .setContentDisposition("attachment");
            }

            Storage storage = GoogleStorageFactory.getService(activity.getApplicationContext());

            Storage.Objects.Insert insertObject = storage.objects().insert(BUCKET_NAME, objectMetadata,
                    mediaContent);

            if (!useCustomMetadata) {
                // If you don't provide metadata, you will have specify the object
                // name by parameter. You will probably also want to ensure that your
                // default object ACLs (a bucket property) are set appropriately:
                // https://developers.google.com/storage/docs/json_api/v1/buckets#defaultObjectAcl
                insertObject.setName(mediaFile.getName());
            }

            // For small files, you may wish to call setDirectUploadEnabled(true), to
            // reduce the number of HTTP requests made to the server.
            if (mediaContent.getLength() > 0 && mediaContent.getLength() <= 2 * 1000 * 1000 /* 2MB */) {
                insertObject.getMediaHttpUploader().setDirectUploadEnabled(true);
            }

            insertObject.execute();

        }catch (Exception ex){
            s = ex.getMessage();
        }

        return s;
    }

}
