package com.example.appsplitbill.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class StorageUtils {
    private static final String PREF_NAME = "AppPrefs";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getCurrentUserEmail(Context context) {
        if (context == null) return "guest";
        return getPrefs(context).getString("userEmail", "guest");
    }

    public static String getUserKey(Context context, String baseKey) {
        String email = getCurrentUserEmail(context).replace(".", "_").replace("@", "_");
        return baseKey + "_" + email;
    }

    public static String getBillHistoryKey(Context context) {
        return getUserKey(context, "billHistory");
    }

    public static String getFriendsListKey(Context context) {
        return getUserKey(context, "friendsList");
    }
}
