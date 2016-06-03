package tt.richTaxist.Bricks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import java.util.Calendar;
import tt.richTaxist.R;
import tt.richTaxist.Util;

public class DateTimeButtons extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    public static final String FRAGMENT_TAG = "DateTimeButtons";
    public static final String DATE_TIME_EXTRA = "DateTimeExtra";
    private DateTimeButtonsInterface mListener;
    private Calendar dateTimeLocal = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private Button btnDate, btnTime;

    public DateTimeButtons() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //проверим, реализован ли нужный интерфейс родительским фрагментом или активностью
            mListener = (DateTimeButtonsInterface) getParentFragment();
            if (mListener == null) mListener = (DateTimeButtonsInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());
        }
        dateSetListener = this;
        timeSetListener = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateTimeLocal = Calendar.getInstance();
        if (savedInstanceState != null) {
            dateTimeLocal.setTimeInMillis(savedInstanceState.getLong(DATE_TIME_EXTRA));
        } else {
            dateTimeLocal.setTimeInMillis(getArguments().getLong(DATE_TIME_EXTRA));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_input_style_buttons, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        btnDate = (Button) rootView.findViewById(R.id.tbInputStyle);
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePD = DatePickerDialog.newInstance(dateSetListener,
                        dateTimeLocal.get(Calendar.YEAR), dateTimeLocal.get(Calendar.MONTH), dateTimeLocal.get(Calendar.DAY_OF_MONTH), false);
                datePD.setVibrate(false);
                datePD.setYearRange(2015, 2025);
                datePD.setCloseOnSingleTapDay(true);
                datePD.show(getChildFragmentManager(), "datepicker");
            }
        });

        btnTime = (Button) rootView.findViewById(R.id.btnTime);
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePD = TimePickerDialog.newInstance(timeSetListener,
                        dateTimeLocal.get(Calendar.HOUR_OF_DAY), dateTimeLocal.get(Calendar.MINUTE), true, false);
                timePD.setVibrate(false);
                timePD.setCloseOnSingleTapMinute(Util.singleTapTimePick);
                timePD.show(getChildFragmentManager(), "timepicker");
            }
        });
        setDateTime(dateTimeLocal);
        return rootView;
    }

    //вызывается после завершения ввода в диалоге даты
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        dateTimeLocal.set(Calendar.YEAR, year);
        dateTimeLocal.set(Calendar.MONTH, month);
        dateTimeLocal.set(Calendar.DAY_OF_MONTH, day);
        btnDate.setText(Util.getStringDateFromCal(dateTimeLocal, getContext()));
        mListener.onDateOrTimeSet(dateTimeLocal);
    }

    //вызывается после завершения ввода в диалоге времени
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        dateTimeLocal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateTimeLocal.set(Calendar.MINUTE, minute);
        btnTime.setText(Util.getStringTimeFromCal(dateTimeLocal, getContext()));
        mListener.onDateOrTimeSet(dateTimeLocal);
    }

    public void setDateTime(Calendar cal){
        dateTimeLocal.setTimeInMillis(cal.getTimeInMillis());
        btnDate.setText(Util.getStringDateFromCal(dateTimeLocal, getContext()));
        btnTime.setText(Util.getStringTimeFromCal(dateTimeLocal, getContext()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DATE_TIME_EXTRA, dateTimeLocal.getTimeInMillis());
    }

    public interface DateTimeButtonsInterface {
        void onDateOrTimeSet(Calendar cal);
    }
}
