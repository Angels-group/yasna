package by.angel.yasna;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    private static Settings settings;

    private final SharedPreferences prefs;

    public static Settings getInstance(Context context) {
        if (settings == null) {
            settings = new Settings(context);
        }
        return settings;
    }

    private Settings(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public boolean isLogined(){
        return prefs.getBoolean("logined", false);
    }

    public void setLogined(boolean logined) {
        prefs.edit().putBoolean("logined", logined).apply();
    }

}
