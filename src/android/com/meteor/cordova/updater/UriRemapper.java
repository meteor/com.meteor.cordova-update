package com.meteor.cordova.updater;

import android.net.Uri;

public interface UriRemapper {
    public static class Remapped {
        public final Uri uri;
        public final boolean isDirectory;

        public Remapped(Uri uri, boolean isDirectory) {
            this.uri = uri;
            this.isDirectory = isDirectory;
        }
    }

    Remapped remapUri(Uri uri);
}
