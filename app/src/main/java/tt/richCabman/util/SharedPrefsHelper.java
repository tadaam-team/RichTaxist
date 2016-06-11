package tt.richCabman.util;

import android.content.SharedPreferences;
/**
 * Created by TAU on 10.05.2016.
 */
public class SharedPrefsHelper {
    static final String KEY_USERNAME = "key_username";
    static final String KEY_PASSWORD = "key_password";

    // The injected SharedPreferences implementation to use for persistence.
    private final SharedPreferences sharedPrefs;

    public SharedPrefsHelper(SharedPreferences sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
    }

    public boolean savePersonalInfo(SharedPrefEntry sharedPrefEntry){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_USERNAME, sharedPrefEntry.getName());
        editor.putString(KEY_PASSWORD, sharedPrefEntry.getPassword());
        return editor.commit();
    }

    public SharedPrefEntry getPersonalInfo() {
        String name = sharedPrefs.getString(KEY_USERNAME, "");
        String password = sharedPrefs.getString(KEY_PASSWORD, "");
        return new SharedPrefEntry(name, password);
    }
}
