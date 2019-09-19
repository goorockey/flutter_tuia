package com.example.flutter_tuia;

import android.util.Log;

public class LogUtils {
    public static void i(String tag, String content) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        Log.i(tag, content);
    }

    public static void e(String tag, String content) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        Log.e(tag, content);
    }
}
