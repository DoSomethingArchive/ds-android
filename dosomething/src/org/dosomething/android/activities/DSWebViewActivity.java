package org.dosomething.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.dosomething.android.R;

import roboguice.inject.InjectView;

public class DSWebViewActivity extends AbstractActionBarActivity {

    @InjectView(R.id.webview) private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_dswebview);

        // Enable ActionBar home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);

        // Override Url loading so any redirects stay within the app's WebView
        String url = getIntent().getDataString();
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                v.loadUrl(url);
                return false;
            }
        });

        // Set WebChromeClient to display progress bar as web page loads
        final Activity mActivity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView v, int progress) {
                mActivity.setProgress(progress * 100);
            }
        });

        // Modify the URL for any that are going to open a PDF
        url = modifyUrlForPdf(url);
        mWebView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If home button is selected on ActionBar, then end the activity
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        }
        else {
            finish();
        }
    }

    /**
     * Modifies a URL to use Google Docs to view a PDF since WebView itself cannot.
     * @param url String of the url we want to load
     * @return Modified URL if it's a PDF. Otherwise, the same URL from the parameter.
     */
    private String modifyUrlForPdf(String url) {
        String extension = url.substring(url.lastIndexOf("."));
        extension = extension.toLowerCase();
        if (extension.startsWith(".pdf"))
            return "http://docs.google.com/gview?embedded=true&url=" + url;
        else
            return url;
    }
    
}
