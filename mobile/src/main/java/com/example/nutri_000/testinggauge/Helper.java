package com.example.nutri_000.testinggauge;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.plus.model.moments.Moment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Amon on 2/21/2018.
 */

//This class helps the app interact with sharedpreferences, saving and retrieving data

public class Helper {
    public static int getThreshold(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
        return prefs.getInt("seekBarValue", 10); // 0 is default
    }

    public static void updateThreshold(Context context,int value) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
        prefs.edit().putInt("seekBarValue", value).commit();
    }

    public static ArrayList<Movement> getStoredMovementsList(Context context) {

        ArrayList<Movement> movements = new ArrayList();
        SharedPreferences share = context.getSharedPreferences("share preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = share.getString("movements", null);
        Type type = new TypeToken<ArrayList<Movement>>() {
        }.getType();
        movements = gson.fromJson(json, type);
        //movements.clear();

        return movements;
    }

    public static void writeMovements(Context context,ArrayList<Movement> movements) {
        SharedPreferences share = context.getSharedPreferences("share preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        Gson gson = new Gson();
        String json = gson.toJson(movements);
        editor.putString("movements", json);
        editor.commit();     // This line is IMPORTANT !!!
    }
}
