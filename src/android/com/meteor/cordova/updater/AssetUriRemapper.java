package com.meteor.cordova.updater;

import java.util.HashSet;
import java.util.Set;

import android.net.Uri;
import android.util.Log;

public class AssetUriRemapper implements UriRemapper {
    private static final String TAG = "meteor.cordova.updater";

    final Asset assetBase;

    static final Set<String> KNOWN_FILE_EXTENSIONS;

    static {
        KNOWN_FILE_EXTENSIONS = new HashSet<String>();

        KNOWN_FILE_EXTENSIONS.add("htm");
        KNOWN_FILE_EXTENSIONS.add("html");

        KNOWN_FILE_EXTENSIONS.add("js");

        KNOWN_FILE_EXTENSIONS.add("css");

        KNOWN_FILE_EXTENSIONS.add("map");

        KNOWN_FILE_EXTENSIONS.add("ico");
        KNOWN_FILE_EXTENSIONS.add("png");
        KNOWN_FILE_EXTENSIONS.add("jpg");
        KNOWN_FILE_EXTENSIONS.add("jpeg");
        KNOWN_FILE_EXTENSIONS.add("gif");

        KNOWN_FILE_EXTENSIONS.add("json");
    }

    public AssetUriRemapper(Asset assetBase) {
        this.assetBase = assetBase;

        if (assetBase == null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Remapped remapUri(Uri uri) {
        String path = uri.getPath();

        assert path.startsWith("/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // String assetPath = assetBase.path + path;
        // Log.d(TAG, "Asset path is " + assetBase.path + path);

        Asset asset = assetBase.find(path);
        if (asset == null) {
            // No such asset
            return null;
        }

        // Don't serve directories.
        // hasChildren is slow... so we use some heuristics first
        int lastDot = path.lastIndexOf('.');
        String extension = null;
        if (lastDot != -1) {
            extension = path.substring(lastDot + 1);
        }

        boolean isDirectory = false;
        if (extension != null && KNOWN_FILE_EXTENSIONS.contains(extension)) {
            Log.d(TAG, "Assuming not a directory: " + path);
        } else {
            if (asset.hasChildren()) {
                isDirectory = true;
            }
        }

        Uri assetUri = Uri.parse("file:///android_asset/" + assetBase.path + "/" + path);
        return new Remapped(assetUri, isDirectory);
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
