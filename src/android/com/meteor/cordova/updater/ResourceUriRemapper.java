package com.meteor.cordova.updater;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

public class ResourceUriRemapper implements UriRemapper {
    private static final String TAG = "meteor.cordova.updater";

    final String assetBase;
    final AssetManager assetManager;

    public ResourceUriRemapper(AssetManager assetManager, String assetBase) {
        if (assetBase != "" && !assetBase.endsWith("/")) {
            assetBase += "/";
        }
        this.assetBase = assetBase;

        this.assetManager = assetManager;
    }

    @Override
    public Uri remapUri(Uri uri) {
        String path = uri.getPath();

        String assetPath = assetBase + path;
        Log.d(TAG, "Asset path is " + assetPath);

        if (exists(assetPath)) {
            Log.d(TAG, "Remapping to " + "file:///android_asset/" + path);
            return Uri.parse("file:///android_asset/" + path);
        }

        return null;
    }

    private boolean exists(String assetPath) {
        InputStream is = null;
        try {
            is = assetManager.open(assetPath);
        } catch (IOException e) {
            Log.d(TAG, "Error while opening " + assetPath, e);
            return false;
        } finally {
            closeQuietly(is);
        }
        return true;
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            Log.w(TAG, "Error closing: " + closeable, e);
        }
    }

}
