/*
 * @author: Rachel Stinnett
 * @file: MainActivity.java
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
 * every 10 seconds when the app is no longer active. This MainActivity.java
 * is responsible for creating a json task and downloading the two images
 * with the image information. This program does this by using three async
 * tasks to download the json and to download the two images. Then the images
 * are updated in their associated ImageView. This program also utilizes the
 * onClick method needed to start the WebViewActivity to display the webpage
 * if photo is clicked by using an intent. This program also handles the
 * case when the page needs to change when the previous, refresh, or next
 * button is clicked. Then this program creates a handler that is used
 * for the load new image service that checks for new images every
 * 10 seconds.
 */
package com.example.flickrviewer;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    public JSONObject imageOne = new JSONObject();
    public JSONObject imageTwo = new JSONObject();
    public JSONArray photos = new JSONArray();
    public int pageNumber = 1;
    public Handler handler;
    public Intent serviceIntent;
    public String idOne;
    public String ownerOne;
    public String idTwo;
    public String ownerTwo;

    /**
     * The purpose of this private class is to be an async task that is
     * responsible for making requests to flickr’s API to get information
     * about images. This class uses the API to fetch two recent images,
     * and updates the image information TextViews.
     */
    private class DownloadJsonTask extends AsyncTask<String, Integer, Long> {

        @Override
        /**
         * The purpose of this method is to perform a computation on
         * a background thread. This method uses the API to fetch two recent
         * images from flickr by creating a url and then using a buffered reader.
         * Then this method creates a Json Object and access the photos by using
         * the .getJSONArray() method. Then this method saves the two image
         * information into class variables. The buffered reader is then
         * closed. The actual updating of the TextViews is in the onPostExecute()
         * to actually update the UI correctly.
         * @param strs = A String that holds multiple strings for this method
         * to work with.
         * @return long = A Long object used for a place holder to follow
         * the syntax of the doInBackground method.
         */
        protected Long doInBackground(String... strs) {
            try {
                URL myUrl;
                myUrl = new URL(getString(R.string.mainUrlPart1)+pageNumber+
                        getString(R.string.mainUrlPart2));
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(myUrl.openStream()));
                JSONObject jsonObject = new JSONObject(bufferedReader.readLine());
                photos = jsonObject.getJSONObject("photos").getJSONArray("photo");
                imageOne = photos.getJSONObject(0);
                imageTwo = photos.getJSONObject(1);
                idOne = imageOne.getString("id");
                ownerOne = imageOne.getString("owner");
                idTwo = imageTwo.getString("id");
                ownerTwo = imageTwo.getString("owner");
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Long(0) ;
        }
        @Override
        /**
         * The purpose of this method is to run the Ui thread after
         * doInBackground() method is finished. This method access the
         * image information that needs to be updates like the date taken,
         * the file format, and the tags. This method does this by
         * using findViewById() method to access the TextViews. Then
         * this method uses the .setText() method as accesses the
         * information using the .getString() method within the image
         * Json Objects that were saved in the doInBackground() method.
         * @param result = A Long object used for a place holder to follow
         * the syntax of the onPostExecute() method.
         */
        protected void onPostExecute(Long result){
            //Get the TextViews
            TextView calandarInfoOne = findViewById(R.id.calandarTextOne);
            TextView fileInfoOne = findViewById(R.id.fileTextOne);
            TextView tagInfoOne = findViewById(R.id.tagTextOne);
            TextView calandarInfoTwo = findViewById(R.id.calandarTextTwo);
            TextView fileInfoTwo = findViewById(R.id.fileTextTwo);
            TextView tagInfoTwo = findViewById(R.id.tagTextTwo);

            try {
                //Update the TextViews
                calandarInfoOne.setText(imageOne.getString("datetaken"));
                fileInfoOne.setText(imageOne.getString("originalformat"));
                tagInfoOne.setText(imageOne.getString("tags"));
                calandarInfoTwo.setText(imageTwo.getString("datetaken"));
                fileInfoTwo.setText(imageTwo.getString("originalformat"));
                tagInfoTwo.setText(imageTwo.getString("tags"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The purpose of this private class is to be an async task that is
     * responsible for downloading the bitmaps of the images based on
     * their URLs, and then display those images in the ImageViews.
     */
    private class DownloadImageTaskOne extends AsyncTask
            <String, Integer, Bitmap> {
        /**
         * The purpose of this method is to perform a computation on
         * a background thread. This method first gets the specific
         * image url by using the getString() method with the
         * "url_c" as the key. Then this returns the getBitmapFromURL()
         * method which then returns the bitmap to the onPostExecute.
         * The getBitmapFromURL() method basically functions as a helper
         * to get the right image bitmap for it to be downloaded and
         * displayed properly.
         * @param strs = A String that holds multiple strings for this method
         * to work with.
         * @return bitmap = A Bitmap that will be used to update the ImageViews
         * in the Ui.
         */
        protected Bitmap doInBackground(String... strs) {
            String url = null;
            try {
                url = imageOne.getString("url_c");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return getBitmapFromURL(url);
        }

        /**
         * The purpose of this method is to run the Ui thread after
         * doInBackground() method is finished. This method takes
         * the specefic ImageViw that is accessed using the findViewById()
         * method and then gets Bitmap that was returned from getBitmapFromURL()
         * tp then set the image Bitmap to that so that it is updated correctly
         * in the Ui.
         * @param result = = A Bitmap that will be used to update the ImageViews
         * in the Ui.
         */
        protected void onPostExecute(Bitmap result) {
            ImageView imageView = findViewById(R.id.imageOne);
            imageView.setImageBitmap(result);
        }
    }

    /**
     * The purpose of this private class is to be an async task that is
     * responsible for downloading the bitmaps of the images based on
     * their URLs, and then display those images in the ImageViews.
     */
    private class DownloadImageTaskTwo extends AsyncTask
            <String, Integer, Bitmap> {
        /**
         * The purpose of this method is to perform a computation on
         * a background thread. This method first gets the specific
         * image url by using the getString() method with the
         * "url_c" as the key. Then this returns the getBitmapFromURL()
         * method which then returns the bitmap to the onPostExecute.
         * The getBitmapFromURL() method basically functions as a helper
         * to get the right image bitmap for it to be downloaded and
         * displayed properly.
         * @param strs = A String that holds multiple strings for this method
         * to work with.
         * @return bitmap = A Bitmap that will be used to update the ImageViews
         * in the Ui.
         */
        protected Bitmap doInBackground(String... strs) {
            String url = null;
            try {
                url = imageTwo.getString("url_c");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return getBitmapFromURL(url);
        }

        /**
         * The purpose of this method is to run the Ui thread after
         * doInBackground() method is finished. This method takes
         * the specefic ImageViw that is accessed using the findViewById()
         * method and then gets Bitmap that was returned from getBitmapFromURL()
         * tp then set the image Bitmap to that so that it is updated correctly
         * in the Ui.
         * @param result = = A Bitmap that will be used to update the ImageViews
         * in the Ui.
         */
        protected void onPostExecute(Bitmap result) {
            ImageView imageView = findViewById(R.id.imageTwo);
            imageView.setImageBitmap(result);
        }
    }
    @Override
    /**
     * The purpose of this method is to be the place where the
     * activity starts for the java program. The onCreate()
     * method is supposed to initialize the activity. The
     * setContentView is set to R.layout.activity_main.In
     * this onCreate() method the DownloadJsonTask() object is
     * created and executed. As well as the DownloadImageTaskOne()
     * and the DowloadImageTaskTwo() to execute those async tasks.
     * This method also calls a serviceHandlerHelper() which
     * handles the service to change the images in the background.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadJsonTask jsonTask = new DownloadJsonTask();
        jsonTask.execute();
        DownloadImageTaskOne imageTaskOne= new DownloadImageTaskOne();
        imageTaskOne.execute();
        DownloadImageTaskTwo imageTaskTwo= new DownloadImageTaskTwo();
        imageTaskTwo.execute();
        serviceHandlerHelper();
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
    public static Bitmap getBitmapFromURL(String src){
        try{
            URL url = new URL (src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The purpose of this method is to load the image flickr web
     * page when the image is clicked on. This method is called by
     * using onClick within the xml file. Then this method creates
     * the intent. The id and owner of the image is accessed by using
     * the save image json object and the getString() method with the
     * id and owner key. Then putExtra() is used to put this extra
     * information into the intent. Then the intent is started by
     * using the starActivity() method.
     * @param view = A view that is passed in to be used
     * for the context and intent.
     */
    public void loadWebPageOne(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("imageOwner", ownerOne);
        intent.putExtra("imageId", idOne);
        startActivity(intent);
    }

    /**
     * The purpose of this method is to load the image flickr web
     * page when the image is clicked on. This method is called by
     * using onClick within the xml file. Then this method creates
     * the intent. The id and owner of the image is accessed by using
     * the save image json object and the getString() method with the
     * id and owner key. Then putExtra() is used to put this extra
     * information into the intent. Then the intent is started by
     * using the starActivity() method.
     * @param view = A view that is passed in to be used
     * for the context and intent.
     */
    public void loadWebPageTwo(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("imageOwner", ownerTwo);
        intent.putExtra("imageId", idTwo);
        startActivity(intent);
    }

    /**
     * The purpose of this method is to control what the page value
     * should be set to when making an API call. This method is
     * for if the previous button is clicked. This method
     * deincraments the pageNumber by one and creates a
     * new json object, imagetaskone objact, and a new imagetasktwo
     * object to be downloaded and displayed in the Ui. This allows
     * for new images to appear when the previous button is clicked
     * since it is going back to the previous page.
     * @param view = A view that is passed in to be used
     * for the context and intent.
     */
    public void changeToPreviousImage(View view){
        //math.max of one with page-1 if negatives don't work
        pageNumber-= 1;
        DownloadJsonTask newJsonTask = new DownloadJsonTask();
        newJsonTask.execute();
        DownloadImageTaskOne newImageTaskOne= new DownloadImageTaskOne();
        newImageTaskOne.execute();
        DownloadImageTaskTwo newImageTaskTwo= new DownloadImageTaskTwo();
        newImageTaskTwo.execute();
    }

    /**
     * The purpose of this method is to control what the page value
     * should be set to when making an API call. This method is
     * for if the rest button is clicked. This method
     * resets the pageNumber to be one and creates a new json object,
     * imagetaskone objact, and a new imagetasktwo object to be
     * downloaded and displayed in the Ui. This allows for new images
     * to appear when the reset button is clicked since it is going back
     * to the original default page.
     * @param view = A view that is passed in to be used
     * for the context and intent.
     */
    public void changeToReset(View view){
        pageNumber = 1;
        DownloadJsonTask newJsonTask = new DownloadJsonTask();
        newJsonTask.execute();
        DownloadImageTaskOne newImageTaskOne= new DownloadImageTaskOne();
        newImageTaskOne.execute();
        DownloadImageTaskTwo newImageTaskTwo= new DownloadImageTaskTwo();
        newImageTaskTwo.execute();
    }

    /**
     * The purpose of this method is to control what the page value
     * should be set to when making an API call. This method is
     * for if the next button is clicked. This method increments
     * the pageNumber by one and creates a new json object, imagetaskone
     * object, and a new imagetasktwo object to be executed and
     * downloaded and displayed in the Ui. This allows for new images
     * to appear when the previous button is clicked since it is going
     * back to the previous page.
     * @param view = A view that is passed in to be used
     * for the context and intent.
     */
    public void changeToNextImage(View view){
        pageNumber+= 1;
        DownloadJsonTask newJsonTask = new DownloadJsonTask();
        newJsonTask.execute();
        DownloadImageTaskOne newImageTaskOne= new DownloadImageTaskOne();
        newImageTaskOne.execute();
        DownloadImageTaskTwo newImageTaskTwo= new DownloadImageTaskTwo();
        newImageTaskTwo.execute();
    }

    /**
     * The purpose of this method is to be a helper method for the handler
     * to be used for the LoadNewImageService. This method first creates a
     * handler method that then handles a message that holds the bundle that
     * was created within the service and held in the data of the message
     * object. This method first unpacks the bundle by accessing the
     * date taken, file type, and tags for each image. Then the image
     * bitmaps for rach image is decoded from the byte array. Then
     * this method updates all of the correct ImageViews and TextViews
     * within the Ui using the findViewById() method and the
     * setText() and setImageBitmap() methods. This method has a lot
     * of lines of code, however all of these elements are neccessary
     * to update the Ui correctly.
     */
    public void serviceHandlerHelper(){
        handler = new Handler() {
            public void handleMessage(Message msg) {
                String dateInfo1 = msg.getData().getString("dateOne");
                String dateInfo2 = msg.getData().getString("dateTwo");
                String fileInfo1 = msg.getData().getString("fileOne");
                String fileInfo2 = msg.getData().getString("fileTwo");
                String tagInfo1 = msg.getData().getString("fileOne");
                String tagInfo2 = msg.getData().getString("fileTwo");
                idOne = msg.getData().getString("firstId");
                idTwo = msg.getData().getString("secondId");
                ownerOne = msg.getData().getString("firstOwner");
                ownerTwo = msg.getData().getString("secondOwner");
                byte[] byteArray1 =  msg.getData().getByteArray("byteArrayOne");
                byte[] byteArray2 =  msg.getData().getByteArray("byteArrayTwo");
                //Decode the byte arrays
                Bitmap firstImageBitmap = BitmapFactory.decodeByteArray(byteArray1,0, byteArray1.length);
                Bitmap secondImageBitmap = BitmapFactory.decodeByteArray(byteArray2,0, byteArray2.length);
                ImageView firstImageView = findViewById(R.id.imageOne);
                firstImageView.setImageBitmap(firstImageBitmap);
                ImageView secondImageView = findViewById(R.id.imageTwo);
                secondImageView.setImageBitmap(secondImageBitmap);
                TextView dateText1 = findViewById(R.id.calandarTextOne);
                dateText1.setText(dateInfo1);
                TextView fileText1 = findViewById(R.id.fileTextOne);
                fileText1.setText(fileInfo1);
                TextView tagText1 = findViewById(R.id.tagTextOne);
                tagText1.setText(tagInfo1);
                TextView dateText2 = findViewById(R.id.calandarTextTwo);
                dateText2.setText(dateInfo2);
                TextView fileText2 = findViewById(R.id.fileTextTwo);
                fileText2.setText(fileInfo2);
                TextView tagText2 = findViewById(R.id.tagTextTwo);
                tagText2.setText(tagInfo2);

            }
        };

    }
    @Override
    /**
     * The purpose of this method is to start the LoadNewImageService
     * when the the application is no longer active in the foreground.
     * That is why onStop() is used for this to guarantee this activity
     * is not in the foreground.This method does this by creating an
     * intent and passing in a Messenger handler. Then this method
     * uses the startService() method to start the service with the intent.
     */
    public void onStop() {
        super.onStop();
        serviceIntent = new Intent(this, LoadNewImageService.class);
        serviceIntent.putExtra("MESSENGER", new Messenger(handler));
        startService(serviceIntent);
    }
    @Override
    /**
     * The purpose of this method is to end the LoadNewImageService when
     * the activity becomes active in the foreground. That is why onRestart()
     * is used to understand when the activity is restarted back in the
     * foreground. This method uses the stopService() method with the intent
     * to stop the service.
     */
    public void onRestart() {
        super.onRestart();
        stopService(serviceIntent);
    }

}