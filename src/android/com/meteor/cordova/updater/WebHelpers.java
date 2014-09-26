package com.meteor.cordova.updater;

import java.io.ByteArrayInputStream;

import android.util.Log;
import android.webkit.WebResourceResponse;

public class WebHelpers {
    private static final String TAG = "meteor.cordova.updater";

    public static WebResourceResponse toWebResourceResponse(String path, byte[] data) {
        String mimeType = "text/html";
        String encoding = "UTF-8";

        Log.d(TAG, "Guessing mimeType=" + mimeType + " and encoding=" + encoding + " for " + path);
        return new WebResourceResponse(mimeType, encoding, new ByteArrayInputStream(data));
    }

}
