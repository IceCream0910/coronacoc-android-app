package com.icecream.coronacoc;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import android.os.Handler;

public class CustomBridge {
    private Context mContext;
    private WebView mWebView;
    private WebView onboardingView;
    private Handler mHandler;

    /**
     * 생성자 * @param context 호출한 context * @param webview Bridge가 이용될 Webview
     */
    public CustomBridge(Context context, WebView webview) {
        this.mContext = context; // 대상 Context (Activity또는 Service 등등)
        this.mWebView = webview; // Bridge생성시 대상 Webview를 저장
        this.onboardingView = webview; //SetupActivity -> 온보딩
        mHandler = new Handler();
    }

    @JavascriptInterface
    public void openSettings() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext,SettingsActivity.class);
                mContext.startActivity(intent);
            }
        });
    }



}
