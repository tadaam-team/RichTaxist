package tt.richTaxist.Bricks;

/**
 * Created by Tau on 12.07.2015.
 */

import java.util.Calendar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class DF_TimeInput extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private TimePickedListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // установим текущее время в диалоговом окне
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // создадим экземпляр класса TimePickerDialog и вернем его
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onAttach(Activity activity) {
        // when the fragment is initially shown (i.e. attached to the activity),
        // cast the activity to the callback interface type
        super.onAttach(activity);
        try {
            mListener = (TimePickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + TimePickedListener.class.getName());
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Пользователь выбрал время. Выводим его в текстовой метке
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        mListener.onTimePicked(c);
    }

    public interface TimePickedListener {
        void onTimePicked(Calendar time);
    }
}
