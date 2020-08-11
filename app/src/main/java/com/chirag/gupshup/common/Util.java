package com.chirag.gupshup.common;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Chirag Desai on 25-07-2020.
 */
public class Util {
    public static boolean connectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null) {
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        } else {
            return false;
        }
    }
}
