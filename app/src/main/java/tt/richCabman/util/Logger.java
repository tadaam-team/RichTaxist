package tt.richCabman.util;

import android.util.Log;

public class Logger {
    public static final String TAG = "MY_LOG";
    private static boolean isDebug = true;

    public static void d(String message) {
        if (isDebug) {
            Log.d(TAG, message);
        }
    }

}
