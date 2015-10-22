package tt.richTaxist.Bricks;

/**
 * Created by Tau on 12.07.2015.
 */

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import tt.richTaxist.R;
import tt.richTaxist.Storage;

public class DateTimeSpinners extends Fragment {
    private static final String LOG_TAG = "DateTimeSpinnersFrag";
    private Context context;
    private OnDateTimeSpinnersFragmentInteractionListener mListener;
    private Calendar dateTimeLocal = Calendar.getInstance();
    private CustomDatePicker datePicker;
    private CustomTimePicker timePicker;

    public DateTimeSpinners() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //проверим, реализован ли нужный интерфейс родительским фрагментом или активностью
            mListener = (OnDateTimeSpinnersFragmentInteractionListener) getParentFragment();
            if (mListener == null) mListener = (OnDateTimeSpinnersFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());
        }
        dateTimeLocal.setTimeInMillis(getArguments().getLong("arrivalDateTime"));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();

        datePicker = new CustomDatePicker(context);
        datePicker.setCalendarViewShown(false);
        datePicker.setSpinnersShown(true);
        timePicker = new CustomTimePicker(context);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));//timePicker: 1:00 --> 13:00
        timePicker.setOnTimeChangedListener(
                new CustomTimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        dateTimeLocal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        dateTimeLocal.set(Calendar.MINUTE, minute);
                        mListener.onDateOrTimeSet(dateTimeLocal);
                    }
                }
        );

        container.addView(datePicker);
        container.addView(timePicker);

        View rootView = inflater.inflate(R.layout.fragment_input_style_spinners, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        setDateTime(dateTimeLocal);
        return rootView;
    }

    public void setDateTime(Calendar cal){
        dateTimeLocal.setTimeInMillis(cal.getTimeInMillis());
        datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        //на этой строке стабильно вижу ArrayIndexOutOfBoundsException: length=11; index=200 при добавлении спиннера в лэйаут
        timePicker.setCurrentHour(Calendar.HOUR_OF_DAY);
        timePicker.setCurrentMinute(Calendar.MINUTE);
    }

    public interface OnDateTimeSpinnersFragmentInteractionListener {
        void onDateOrTimeSet(Calendar cal);
    }


    private class CustomDatePicker extends DatePicker {
        //no proxy unlike CustomTimePicker
        public CustomDatePicker(Context context) {
            super(context);
            try {
                Class<?> classForId = Class.forName("com.android.internal.R$id");
                Field field = classForId.getField("year");

                NumberPicker yearSpinner = (NumberPicker) this.findViewById(field.getInt(null));
                yearSpinner.setMinValue(115);//start year is 1900
                yearSpinner.setMaxValue(125);//start year is 1900
                List<String> displayedValues = new ArrayList<>();
                for (int i = 2015; i <= 2025; i ++)
                    displayedValues.add(String.format("%02d", i%1000));

                if (yearSpinner.getMaxValue() - yearSpinner.getMinValue() + 1 != displayedValues.size())
                    throw new Exception ("displayedValues.size differs from selectable range");
                yearSpinner.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private class CustomTimePicker extends TimePicker {
        private OnTimeChangedListener mListener;

        public CustomTimePicker(Context context) {
            super(context);
            try {
                Class<?> classForId = Class.forName("com.android.internal.R$id");
                Field field = classForId.getField("minute");

                NumberPicker minuteSpinner = (NumberPicker) this.findViewById(field.getInt(null));
                minuteSpinner.setMaxValue((60 / Storage.timePickerStep) - 1);
                ArrayList<String> displayedValues = new ArrayList<>();
                for (int i = 0; i < 60; i += Storage.timePickerStep)
                    displayedValues.add(String.format("%02d", i));
                minuteSpinner.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private int maxMinuteIndex() {
            return (60 / Storage.timePickerStep) - 1;
        }

        @Override
        public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
            super.setOnTimeChangedListener(internalTimeChangedListener);
            this.mListener = onTimeChangedListener;
        }

        @Override
        public Integer getCurrentMinute() {
            return super.getCurrentMinute() * Storage.timePickerStep;
        }

        @Override
        public void setCurrentMinute(Integer currentMinute) {
            int cleanMinute = currentMinute / Storage.timePickerStep;
            if (currentMinute % Storage.timePickerStep > 0) {
                if (cleanMinute == maxMinuteIndex()) {
                    cleanMinute = 0;
                    setCurrentHour(getCurrentHour() + 1);
                } else {
                    cleanMinute++;
                }
            }
            super.setCurrentMinute(cleanMinute);
        }

        // We want to proxy all the calls to our member variable OnTimeChangedListener with our own
        // internal listener in order to make sure our overridden getCurrentMinute is called. Without
        // this some versions of android return the underlying minute index.
        private OnTimeChangedListener internalTimeChangedListener = new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mListener.onTimeChanged(view, hourOfDay, getCurrentMinute());
            }
        };
    }
}