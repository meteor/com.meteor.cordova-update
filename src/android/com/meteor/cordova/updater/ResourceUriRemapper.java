package com.meteor.cordova.updater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

public class ResourceUriRemapper implements UriRemapper {
    private static final String TAG = "meteor.cordova.updater";

    final Asset assetBase;
    final AssetManager assetManager;

    public ResourceUriRemapper(AssetManager assetManager, String assetBase) {
        // Make sure assetBase does not end with slash
        if (assetBase != "" && assetBase.endsWith("/")) {
            assetBase = assetBase.substring(0, assetBase.length() - 1);
        }

        this.assetManager = assetManager;
        this.assetBase = new Asset(null, assetBase);

        // For debug...
        this.assetBase.dump();
    }

    // The asset API is really slow; cache lookups
    class Asset {
        final String name;
        final String path;

        private List<Asset> children;

        public Asset(String name, String path) {
            this.name = name;
            this.path = path;
            assert !path.endsWith("/");
        }

        public List<Asset> getChildren() {
            if (this.children == null) {
                List<Asset> children;
                String[] assets = null;
                try {
                    assets = assetManager.list(path);
                } catch (IOException e) {
                    Log.w(TAG, "Error listing assets at " + path, e);
                }
                if (assets == null || assets.length == 0) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList<Asset>(assets.length);
                    for (String asset : assets) {
                        children.add(new Asset(asset, path + "/" + asset));
                    }
                }
                this.children = children;
            }
            return this.children;
        }

        public void dump() {
            Log.i(TAG, "Found asset: " + this.path);
            for (Asset child : getChildren()) {
                child.dump();
            }
        }

        public boolean exists(String path) {
            String[] pathTokens = path.split("/");
            Asset current = this;
            for (String pathToken : pathTokens) {
                current = current.getChild(pathToken);
                if (current == null) {
                    return false;
                }
            }
            return current != null;
        }

        private Asset getChild(String name) {
            for (Asset child : getChildren()) {
                if (child.name.equals(name)) {
                    return child;
                }
            }
            return null;
        }
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
