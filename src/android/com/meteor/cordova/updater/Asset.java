package com.meteor.cordova.updater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.res.AssetManager;
import android.util.Log;

// The asset API is really slow; cache lookups
public class Asset {
    private static final String TAG = "meteor.cordova.updater";

    final Asset parent;
    final AssetManager assetManager;
    final String name;
    final String path;

    private List<Asset> children;

    private Asset(Asset parent, String name, String path) {
        this.parent = parent;
        this.name = name;
        this.path = path;
        this.assetManager = parent.assetManager;
        assert !path.endsWith("/");
    }

    // Constructor for root
    public Asset(AssetManager assetManager, String path) {
        this.assetManager = assetManager;
        this.parent = null;
        this.name = null;
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
                    String childPath = (path.length() != 0) ? (path + "/" + asset) : asset;
                    children.add(new Asset(this, asset, childPath));
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
        return find(path) != null;
    }

    private Asset getChild(String name) {
        for (Asset child : getChildren()) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }

    public Asset find(String path) {
        String[] pathTokens = path.split("/");
        Asset current = this;
        for (String pathToken : pathTokens) {
            if (pathToken.length() == 0) {
                // Ignore empty bits (either a leading slash, or a double slash)
                continue;
            }
            current = current.getChild(pathToken);
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}
