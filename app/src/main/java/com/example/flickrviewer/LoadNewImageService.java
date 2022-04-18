/*
 * @author: Rachel Stinnett
 * @file: LoadNewImageService.java
 * @assignment: Programming Assignment 6- Flickr Viewer
 * @course: CSC 317; Spring 2022
 * @description:The purpose of this program is to be create an application
 * for browsing and viewing photos from flickr. This program will allow the
 * user to view and browse through actual photos that users of clicker have
 * posted, and that are publicly available. If a user wants more details about
 * an image, the user is able to click on the image, which would take the user
 * to a web page view of the actual post of the photo on the flickr site.
 * The functionality for browsing and viewing photos relies  on Flickr’s
 * public API. This program uses a MainActivity which has 3 async tasks for
 * downloading the Json object and the 2 images. Then there is a WebViewActivity
 * that is used to display the website when the image is clicked. Then there
 * is a LoadNewImageService which is used to load new images in the background
 * every 10 seconds when the app is no longer active. This LoadNewImageService.java
 * is responsible for being a background service that loads images in the background
 * extending an IntentService. This program does this by using the onHandleIntent()
 * to create a Bundle object that stores the necessary information like the image
 * Bitmaps, date take, file format, and tags to be passed into the message object.
 * This program sets the thread.sleep to 10000 so that the background images can
 * be changed after 10 seconds have passed.
 */
package com.example.flickrviewer;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadNewImageService extends IntentService {
    public JSONObject imageOne = new JSONObject();
    public JSONObject imageTwo = new JSONObject();
    public JSONArray photos = new JSONArray();
    public Boolean keepServiceRunning = true;
    public Handler handler;

    /**
     * The purpose of this method is to be a constructor for
     * the LoadNewImage Service class. It is called when an instance
     * of the class is created. At the time of calling the constructor,
     * memory for the object is allocated in the memory. It is a special
     * type of method which is used to initialize the object. This constructor
     * does not have parameters that need to be initialized.
     */
    public LoadNewImageService () {
        super("LoadNewImageService ");
    }

    @Override
    /**
     * The purpose of this method is to be the place where the
     * activity starts for the java program. The onCreate()
     * method is supposed to initialize the activity. The
     * purpose of this onCreate() is to initialize a handler and
     * call the super since this java file extends IntentService.
     */
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
    }

    @Override
    /**
     * The purpose of this method is to invoke on the worker
     * thread with a request to process. Only one Intent is processed at a
     * time, but the processing happens on a worker thread that runs
     * independently from other application logic. If this code takes
     * a long time, it will hold up other requests to the same IntentService,
     * but it will not hold up anything else. Within this onHandleIntent()
     * method it tests a bundle and checks if it is empty. Then it creates a
     * messenger object. Then within the while true loop a helper method is
     * called that creates a new json object and gets all of the nesscary
     * info such as data taken, image file type, and tags. Then the images
     * are compressed into a byte array. All of this info is put into a Bundle
     * object. Then within this method the thread sleep is called for about
     * 10 seconds.
     */
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) { return; }
        Messenger messenger = (Messenger) bundle.get("MESSENGER");
        while (keepServiceRunning) {
            try {
                //Helper method
                createBundleHelper(messenger);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The purpose of this method is to be a helper method for the
     * onHandleIntent. This method creates a new json object and then
     * access thw two photos needed. From there this method gets
     * the date taken, file type, and tag from both images.
     * Then this method uses the getBitmapFromUrl() to get
     * the Bitmap and then compress it using a byte array.
     * A Bundle object is then initialzed to hold all of
     * these elements. After all of these elements are added
     * to the bundle it is then passed into the message using the
     * setData() method. The message is sent and then the bufferreader
     * is closed.
     * @param messenger = A Messenger object that allows for the implementation
     * of message-based communication across processes.
     * @throws Exception = A general exception that catches any exception
     * that may be thrown.
     */
    public void createBundleHelper(Messenger messenger) throws Exception {
            URL myUrl;
            myUrl = new URL(getString(R.string.serviceUrl));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(myUrl.openStream()));
            JSONObject jsonObject = new JSONObject(bufferedReader.readLine());
            photos = jsonObject.getJSONObject("photos").getJSONArray("photo");
            imageOne = photos.getJSONObject(0);
            imageTwo = photos.getJSONObject(1);
            String firstUrl = imageOne.getString("url_c");
            String secondUrl = imageTwo.getString("url_c");
            Bitmap resultOne = getBitmapFromURL(firstUrl);
            Bitmap resultTwo = getBitmapFromURL(secondUrl);
            ByteArrayOutputStream firstBytes = new ByteArrayOutputStream();
            resultOne.compress(Bitmap.CompressFormat.JPEG, 100, firstBytes);
            ByteArrayOutputStream secondBytes = new ByteArrayOutputStream();
            resultTwo.compress(Bitmap.CompressFormat.JPEG, 100, secondBytes);
            //Creates Bundle Object and adds the elements to it.
            Bundle mainBundle = new Bundle();
            mainBundle.putString("dateOne",imageOne.getString("datetaken") );
            mainBundle.putString("dateTwo",imageTwo.getString("datetaken") );
            mainBundle.putString("fileTwo",imageOne.getString("originalformat"));
            mainBundle.putString("fileTwo",imageTwo.getString("originalformat"));
            mainBundle.putString("tagOne",imageOne.getString("tags"));
            mainBundle.putString("tagTwo",imageTwo.getString("tags"));
            mainBundle.putString("firstId",imageOne.getString("id"));
            mainBundle.putString("secondId",imageTwo.getString("id"));
            mainBundle.putString("firstOwner",imageOne.getString("owner"));
            mainBundle.putString("secondOwner",imageTwo.getString("owner"));
            mainBundle.putByteArray("byteArrayOne",firstBytes.toByteArray());
            mainBundle.putByteArray("byteArrayTwo",secondBytes.toByteArray());
            Message msg= Message.obtain();
            msg.setData(mainBundle);
            messenger.send(msg);
            bufferedReader.close();
            Thread.sleep(10000);
    }

    /**
     * The purpose of this method is to create a bitmap from a url
     * which will be useful for downloading the images. This method does
     * this by creating a url. Then crates an HttpUrlConnection in order
     * to set the do input and then connect it using the connect() method.
     * An input stream is used to then decode the bitmap and then that is
     * returned.
     * @param src = A string that is used to represents the Url.
     * @return myBitmap = A Bitmap object that is used to download
     * the image.
     */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    /**
     * The purpose of this method is to destory
     * the service after it is no longer active and has
     * passed onStop(). This method does this by stopping
     * the while true loop.
     */
    public void onDestroy(){
        super.onDestroy();
        keepServiceRunning = false;
    }
}
