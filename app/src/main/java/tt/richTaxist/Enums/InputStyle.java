package tt.richTaxist.Enums;

import android.util.Log;
import tt.richTaxist.FirstScreenActivity;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;

/**
 * Created by Tau on 31.07.2015.
 */
public enum InputStyle {
    BUTTON  (0, R.string.button),
    SPINNER (1, R.string.spinner);

    public final int id;
    private final int captionId;
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;

    InputStyle(int id, int captionId) {
        this.id = id;
        this.captionId = captionId;
    }

    public static InputStyle getById(int id){
        for (InputStyle x: InputStyle.values()){
            if (x.id == id) return x;
        }
        throw new IllegalArgumentException();
    }

    //два метода ниже нужны для отправки в Parse и получения из него, т.к. enum он не понимает
    public static InputStyle stringToInputStyle(String string) {
        if      (MainActivity.context.getString(R.string.button).equals(string))    return InputStyle.BUTTON;
        else if (MainActivity.context.getString(R.string.spinner).equals(string))   return InputStyle.SPINNER;
        else Log.d(LOG_TAG, "ошибка перевода String в enum"); return InputStyle.BUTTON;
    }

    @Override
    public String toString() {
        return MainActivity.context.getString(captionId);
    }
}
