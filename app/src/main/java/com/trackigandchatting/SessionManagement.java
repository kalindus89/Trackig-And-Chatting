package com.trackigandchatting;

import android.content.Context;
import android.content.SharedPreferences;
import static android.content.Context.MODE_PRIVATE;

public class SessionManagement {

    public static void setUserDetails(Context con,  String name, String imageUrl) {
        SharedPreferences.Editor editor = con.getSharedPreferences("userDetails", MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("imageUrl", imageUrl);
        editor.apply();
    }

    public static String getImageUrl(Context con) {
        SharedPreferences prefs = con.getSharedPreferences("userDetails", MODE_PRIVATE);
        return prefs.getString("imageUrl", "image defined");
    }

    public static String getName(Context con) {
        SharedPreferences prefs = con.getSharedPreferences("userDetails", MODE_PRIVATE);
        return prefs.getString("name", "image defined");
    }

}
