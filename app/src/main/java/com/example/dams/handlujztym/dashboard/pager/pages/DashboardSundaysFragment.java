package com.example.dams.handlujztym.dashboard.pager.pages;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.dams.handlujztym.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DashboardSundaysFragment extends Fragment {

    public static Fragment newInstance() { return new DashboardSundaysFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_sundays, container, false);
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize views
        WebView webView = view.findViewById(R.id.web_view);
        ProgressBar webViewProgress = view.findViewById(R.id.web_view_progress);

        // enable java script
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // show and hide progress bar while web page is being loaded
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webViewProgress.setVisibility(VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webViewProgress.setVisibility(GONE);
            }
        });

        webView.loadUrl("https://www.kalbi.pl/index.php/niedziele-handlowe");
    }
}
