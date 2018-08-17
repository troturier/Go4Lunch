package com.openclassrooms.go4lunch.controllers.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.openclassrooms.go4lunch.R;

/**
 * Class required to display restaurants websites
 */
public class WebViewActivity extends Activity {

    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Intent intent = getIntent();
        // Retrieves the URL of the restaurant
        String url = intent.getStringExtra("url");
        // Finds the WebView object
        WebView webView = findViewById(R.id.webView1);
        // Assigns a client to the WebView
        webView.setWebViewClient(new AuthClient());
        webView.getSettings().setJavaScriptEnabled(true);
        // Load the URL of the article
        webView.loadUrl(url);
    }

    /**
     * Allows the user to open the URL in the WebView and not in the default web browser
     * The second utility of this function is to allow the user to return to the previous activity with a single press of the back button
     * This is possible because the history of the WebView is cleaned with each new call
     */
    private class AuthClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView v, String url) {
            v.clearHistory();
        }
    }
}
