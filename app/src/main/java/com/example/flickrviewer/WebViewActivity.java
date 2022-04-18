/*
 * @author: Rachel Stinnett
 * @file: WebViewActivity.java
 * @assignment: Programming Assignment 6- Flickr Viewer
 * @course: CSC 317; Spring 2022
 * @description: The purpose of this program is to be create an application
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
 * every 10 seconds when the app is no longer active. This WebViewActivity.java
 * updates the WebView programmatically.
 */
package com.example.flickrviewer;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends AppCompatActivity {
    public WebView webView;
    @Override
    /**
     * The purpose of this method is to be the place where the
     * activity starts for the java program. The onCreate()
     * method is supposed to initialize the activity. The
     * setContentView is set to R.layout.activity_web_view.
     * In this onCreate() this is where it access the owner
     * and id passed in the intent to build an Url. And then
     * load the Url into the webView within the xml.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Intent intent = getIntent();
        String id = intent.getStringExtra("imageId");
        String owner = intent.getStringExtra("imageOwner");
        webView = (WebView)findViewById(R.id.mainWebview);
        webView.getSettings().setJavaScriptEnabled(true);
        //Builds the url
        String buildUrl = getString(R.string.templateUrl)+ owner + "/" + id;
        webView.loadUrl(buildUrl);
    }
}