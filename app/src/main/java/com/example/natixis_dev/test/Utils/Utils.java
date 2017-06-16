package com.example.natixis_dev.test.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Created by natixis-dev on 16/06/2017.
 */

public class Utils {

    private static final String APP_PREF = "TestApp_pref";

    public static String encodeFromUtf8ToBase64(String sDataToEncode){
        byte[] data = new byte[0];
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                data = sDataToEncode.getBytes(StandardCharsets.UTF_8);
            } else data = sDataToEncode.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String base64Str = Base64.encodeToString(data, Base64.DEFAULT);
        return base64Str;
    }

    public static String decodeFromBase64ToUtf8(String sDataToDecode){
        byte[] data = Base64.decode(sDataToDecode, Base64.DEFAULT);
        String utf8Str = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                utf8Str = new String(data, StandardCharsets.UTF_8);
            } else utf8Str = new String(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return utf8Str;
    }

    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(APP_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    public static Boolean getFromPreferences(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(APP_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }
}
