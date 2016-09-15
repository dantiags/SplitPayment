package com.aycron.mobile.splitpayment.helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.aycron.mobile.splitpayment.factories.GoogleStorageFactory;
import com.aycron.mobile.splitpayment.factories.GoogleVisionFactory;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageSource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by carlos.dantiags on 8/9/2016.
 */
public class GoogleVisionHelper {


    private static String BUCKET_NAME = "splitpayment.appspot.com";
    private static String BUCKET_URL = "gs://"+BUCKET_NAME+"/";


    public static String ProcessImage(Activity activity, String  path){
        String resultString = "";
        try {

            //resultString = ProcessImageWithStorage(activity, path);
            resultString = ProcessImageWithoutStorage(activity, path);

        }catch (Exception ex){
            resultString = ex.getMessage();
        }

        return resultString;
    }

    private static String ProcessImageWithStorage(Activity activity, String  path){

        String resultString = "";
        try {
            String fileName = UploadImage(activity, path);

            resultString = DetectText(activity, fileName);

        }catch (Exception ex){
            resultString = ex.getMessage();
        }

        return resultString;

    }

    private static String ProcessImageWithoutStorage(Activity activity, String  path){

        String resultString = "";
        try {
            Bitmap file = GetImage(path);

            resultString = DetectText(activity, file);

        }catch (Exception ex){
            resultString = ex.getMessage();
        }

        return resultString;
    }

    private static String UploadImage(Activity activity, String  path){
        String resultString ="";
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
                // Create a media file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String name = "UP_" + timeStamp + "_" + mediaFile.getName();
                insertObject.setName(name);
                resultString = name;
            }

            // For small files, you may wish to call setDirectUploadEnabled(true), to
            // reduce the number of HTTP requests made to the server.
            if (mediaContent.getLength() > 0 && mediaContent.getLength() <= 2 * 1000 * 1000 /* 2MB */) {
                insertObject.getMediaHttpUploader().setDirectUploadEnabled(true);
            }

            insertObject.execute();

        }catch (Exception ex){
            resultString = ex.getMessage();
        }

        return resultString;
    }

    private static String DetectText(Activity activity, String filename) {

        String resultString = filename;

        try {

            Vision vision = GoogleVisionFactory.getService(activity.getApplicationContext());

            ImageSource imageSource = new ImageSource();
            imageSource.setGcsImageUri( BUCKET_URL + filename);

            AnnotateImageRequest request =
                    new AnnotateImageRequest()
                            .setImage(new Image().setSource(imageSource))
                            .setFeatures(ImmutableList.of(
                                    new Feature()
                                            .setType("TEXT_DETECTION")));
            Vision.Images.Annotate annotate =
                    vision.images()
                            .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));

            BatchAnnotateImagesResponse batchResponse = annotate.execute();
            assert batchResponse.getResponses().size() == 1;
            AnnotateImageResponse response = batchResponse.getResponses().get(0);

            if (response.getTextAnnotations() == null) {
                throw new IOException(
                        response.getError() != null
                                ? response.getError().getMessage()
                                : "Unknown error getting image annotations");
            }

            List<EntityAnnotation> responses =  response.getTextAnnotations();

            String s = "";
            for (EntityAnnotation entityAnnotation : responses) {

                s = s + entityAnnotation.getDescription() + "\n\n";
            }

            resultString = s;


        }catch (Exception ex){
            resultString = ex.getMessage();
        }

        return resultString;
    }

    private static Bitmap GetImage(String  path) {
        Bitmap bm = BitmapFactory.decodeFile(path);
        return bm;
    }

    private static String DetectText(Activity activity, Bitmap bitmap ) {

        String resultString = "";

        try {

            Vision vision = GoogleVisionFactory.getService(activity.getApplicationContext());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bitmap is the bitmap object
            byte[] byteArrayImage = baos.toByteArray();
            Base64 base64 = new Base64();
            String encodedImage = base64.encodeToString(byteArrayImage);

            AnnotateImageRequest request =
                    new AnnotateImageRequest()
                            .setImage(new Image().setContent(encodedImage))
                            .setFeatures(ImmutableList.of(
                                    new Feature()
                                            .setType("TEXT_DETECTION")));
            Vision.Images.Annotate annotate =
                    vision.images()
                            .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));

            BatchAnnotateImagesResponse batchResponse = annotate.execute();
            assert batchResponse.getResponses().size() == 1;
            AnnotateImageResponse response = batchResponse.getResponses().get(0);

            if (response.getTextAnnotations() == null) {
                throw new IOException(
                        response.getError() != null
                                ? response.getError().getMessage()
                                : "Unknown error getting image annotations");
            }

            List<EntityAnnotation> responses =  response.getTextAnnotations();

            String s = "";
            for (EntityAnnotation entityAnnotation : responses) {

                s = s + entityAnnotation.getDescription() + "\n\n";
            }

            resultString = s;


        }catch (Exception ex){
            resultString = ex.getMessage();
        }

        return resultString;
    }

}
