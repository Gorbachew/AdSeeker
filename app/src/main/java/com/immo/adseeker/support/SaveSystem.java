package com.immo.adseeker.support;

import android.app.Activity;
import android.content.SharedPreferences;


import static android.content.Context.MODE_PRIVATE;

public class SaveSystem {

    public static void saveData(Activity activity, String[] data) {
        SharedPreferences sPref;
        sPref = activity.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("phoneNum", data[0]);
        ed.putString("operator", data[1]);
        ed.apply();
    }

    public static String[] loadData(Activity activity) {
        SharedPreferences sPref;
        sPref = activity.getPreferences(MODE_PRIVATE);
        String[] savedText = new String[2];
        savedText[0] = sPref.getString("phoneNum", "");
        savedText[1] = sPref.getString("operator", "");
        return savedText;
    }

}
