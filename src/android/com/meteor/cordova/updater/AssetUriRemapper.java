package com.meteor.cordova.updater;

import android.net.Uri;
import android.util.Log;

public class AssetUriRemapper implements UriRemapper {
    private static final String TAG = "meteor.cordova.updater";

    final Asset assetBase;

    public AssetUriRemapper(Asset assetBase) {
        this.assetBase = assetBase;
    }

    @Override
    public Uri remapUri(Uri uri) {
        String path = uri.getPath();

        assert path.startsWith("/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // String assetPath = assetBase.path + path;
        // Log.d(TAG, "Asset path is " + assetBase.path + path);

        if (assetBase.exists(path)) {
            Log.d(TAG, "Remapping to " + "file:///android_asset/" + assetBase.path + "/" + path);
            return Uri.parse("file:///android_asset/" + assetBase.path + "/" + path);
        }

        return null;
    }

    // private boolean assetExists(String assetPath) {
    // InputStream is = null;
    // try {
    // is = assetManager.open(assetPath);
    // } catch (FileNotFoundException e) {
    // return false;
    // } catch (IOException e) {
    // Log.d(TAG, "Error while opening " + assetPath + "(" + e + ")");
    // return false;
    // } finally {
    // Utils.closeQuietly(is);
    // }
    // return true;
    // }

}
