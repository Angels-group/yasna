package by.angel.yasna;

import android.content.Context;
import android.content.SharedPreferences;

import com.securepreferences.SecurePreferences;

public class Credentials {

    private static Credentials credentials;
    private final SharedPreferences prefs;

    public static Credentials getInstance(Context context) {
        if (credentials == null) {
            credentials = new Credentials(context);
        }
        return credentials;
    }

    private Credentials(Context context) {
        prefs = new SecurePreferences(context, "", "credentials");
    }

    public String getLogin() {
        return prefs.getString("login", null);
    }

    public void setLogin(String key) {
        prefs.edit().putString("login", key).apply();
    }

    public String getPassword() {
        return prefs.getString("password", null);
    }

    public void setPassword(String key) {
        prefs.edit().putString("password", key).apply();
    }



}
