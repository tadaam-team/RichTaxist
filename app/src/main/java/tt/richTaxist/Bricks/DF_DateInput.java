package tt.richTaxist.Bricks;

/**
 * Created by Tau on 23.07.2015.
 */
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;

public class DF_DateInput extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private DatePickedListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // установим текущую дату в диалоговом окне
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // создадим экземпляр класса DatePickerDialog с временным интервалом от 01.01.2015 до текущей даты + 2 месяца
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        long timestamp = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime().getTime();
        dialog.getDatePicker().setMinDate(timestamp);

        calendar.add(Calendar.MONTH, 2);
        timestamp = calendar.getTime().getTime();
        dialog.getDatePicker().setMaxDate(timestamp);

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        // when the fragment is initially shown (i.e. attached to the activity), cast the activity to the callback interface type
        super.onAttach(activity);
        try { mListener = (DatePickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + DatePickedListener.class.getName());
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        mListener.onDatePicked(calendar);
    }

    public interface DatePickedListener {
        void onDatePicked(Calendar calendar);
    }
}
