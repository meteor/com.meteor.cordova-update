package com.meteor.cordova.updater;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

public class CordovaUpdatePlugin extends CordovaPlugin {
    private static final String TAG = "meteor.cordova.updater";

    private static final String DEFAULT_HOST = "meteor.local";
    private static final String DEFAULT_PAGE = "index.html";

    final Set<String> hosts = new HashSet<String>();
    final Set<String> schemes = new HashSet<String>();
    private List<UriRemapper> remappers = new ArrayList<UriRemapper>();

    private String wwwRoot;
    private String cordovajsRoot;

    public CordovaUpdatePlugin() {
        this.hosts.add(DEFAULT_HOST);
        this.schemes.add("http");
        this.schemes.add("https");
    }

    @Override
    public Uri remapUri(Uri uri) {
        Log.d(TAG, "remapUri " + uri);

        String scheme = uri.getScheme();
        if (scheme == null || !schemes.contains(scheme)) {
            Log.d(TAG, "Scheme is not intercepted: " + scheme);
            return uri;
        }
        String host = uri.getHost();
        if (host == null || !hosts.contains(host)) {
            Log.d(TAG, "Host is not intercepted: " + host);
            return uri;
        }

        List<UriRemapper> remappers;
        synchronized (this) {
            remappers = this.remappers;
        }

        for (UriRemapper remapper : remappers) {
            Uri remapped = remapper.remapUri(uri);
            if (remapped != null) {
                return remapped;
            }
        }

        // XXX: <dir> -> <dir>/ redirection

        // Serve defaultPage if directory
        if (uri.getPath().endsWith("/")) {
            Uri defaultPage = Uri.withAppendedPath(uri, DEFAULT_PAGE);

            for (UriRemapper remapper : remappers) {
                Uri remapped = remapper.remapUri(defaultPage);
                if (remapped != null) {
                    return remapped;
                }
            }
        }

        // No remapping; return unaltered
        return uri;
    }

    private static final String ACTION_START_SERVER = "startServer";
    private static final String ACTION_SET_LOCAL_PATH = "setLocalPath";
    private static final String ACTION_GET_CORDOVAJSROOT = "getCordovajsRoot";

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        try {
            if (ACTION_START_SERVER.equals(action)) {
                String wwwRoot = inputs.getString(0);
                String cordovaRoot = inputs.getString(1);

                String result = startServer(wwwRoot, cordovaRoot, callbackContext);

                callbackContext.success(result);

                return true;
            } else if (ACTION_SET_LOCAL_PATH.equals(action)) {
                String wwwRoot = inputs.getString(0);

                setLocalPath(wwwRoot, callbackContext);

                callbackContext.success();
                return true;
            } else if (ACTION_GET_CORDOVAJSROOT.equals(action)) {
                String result = getCordovajsRoot(callbackContext);

                callbackContext.success(result);

                return true;
            } else {
                Log.w(TAG, "Invalid action passed: " + action);
                PluginResult result = new PluginResult(Status.INVALID_ACTION);
                callbackContext.sendPluginResult(result);
            }
        } catch (Exception e) {
            Log.w(TAG, "Caught exception during execution: " + e);
            String message = e.toString();
            callbackContext.error(message);
        }

        return true;
    }

    private void setLocalPath(String wwwRoot, CallbackContext callbackContext) {
        Log.w(TAG, "setLocalPath(" + wwwRoot + ")");

        this.updateLocations(wwwRoot, this.cordovajsRoot);
    }

    private void updateLocations(String wwwRoot, String cordovajsRoot) {
        Context ctx = cordova.getActivity().getApplicationContext();
        AssetManager assetManager = ctx.getResources().getAssets();

        synchronized (this) {
            List<UriRemapper> remappers = new ArrayList<UriRemapper>();
            remappers.add(new FilesystemUriRemapper(new File(wwwRoot)));

            String androidAssetRoot = cordovajsRoot;
            androidAssetRoot = Utils.stripPrefix(androidAssetRoot, "/android_asset/");
            androidAssetRoot = Utils.stripPrefix(androidAssetRoot, "/");
            remappers.add(new ResourceUriRemapper(assetManager, androidAssetRoot));

            this.wwwRoot = wwwRoot;
            this.cordovajsRoot = cordovajsRoot;
            this.remappers = remappers;
        }
    }

    private String getCordovajsRoot(CallbackContext callbackContext) {
        Log.w(TAG, "getCordovajsRoot");

        return this.cordovajsRoot;
    }

    private String startServer(String wwwRoot, String cordovaRoot, CallbackContext callbackContext)
            throws JSONException {
        Log.w(TAG, "startServer(" + wwwRoot + "," + cordovaRoot + ")");

        this.updateLocations(wwwRoot, cordovaRoot);

        return "http://" + DEFAULT_HOST;
    }

}
