package com.meteor.cordova.updater;

import java.io.Closeable;
import java.io.FileNotFoundException;
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
        // Make sure assetBase does not end with slash
        if (assetBase != "" && assetBase.endsWith("/")) {
            assetBase = assetBase.substring(0, assetBase.length() - 1);
        }

        this.assetManager = assetManager;
        this.assetBase = assetBase;
    }

    @Override
    public Uri remapUri(Uri uri) {
        String path = uri.getPath();
        assert path.startsWith("/");
        assert !assetBase.endsWith("/");

        String assetPath = assetBase + path;
        Log.d(TAG, "Asset path is " + assetPath);

        if (assetExists(assetPath)) {
            Log.d(TAG, "Remapping to " + "file:///android_asset/" + assetPath);
            return Uri.parse("file:///android_asset/" + assetPath);
        }

        return null;
    }

    private boolean assetExists(String assetPath) {
        InputStream is = null;
        try {
            is = assetManager.open(assetPath);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            Log.d(TAG, "Error while opening " + assetPath + "(" + e + ")");
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
