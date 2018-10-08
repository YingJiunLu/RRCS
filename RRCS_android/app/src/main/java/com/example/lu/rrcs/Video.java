package com.example.lu.rrcs;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Video extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        WebView webview = (WebView) findViewById(R.id.webView);

        //縮放
     //   webview.getSettings().setBuiltInZoomControls(true);
        //JavaScript
        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("https://stu-web.tkucs.cc/404411695/index.html");

    }

}
