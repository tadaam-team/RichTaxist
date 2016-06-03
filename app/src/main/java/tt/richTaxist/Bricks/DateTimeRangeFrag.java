package tt.richTaxist.Bricks;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import java.util.Calendar;
import java.util.GregorianCalendar;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.R;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Util;

/**
 * Created by Tau on 13.10.2015.
 */
public class DateTimeRangeFrag extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final String FRAGMENT_TAG = "DateTimeRangeFrag";
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private FragmentActivity mActivity;
    private Calendar rangeStart, rangeEnd;
    private Button buttonRangeStartDate, buttonRangeStartTime, buttonRangeEndDate, buttonRangeEndTime;
    private ViewGroup seekBarPlaceHolder;
    private RangeSeekBar<Long> seekBar;
    private String clickedButtonID;
    private DateTimeRangeFragInterface mListener;

    public DateTimeRangeFrag() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //проверим, реализован ли нужный интерфейс родительским фрагментом или активностью
            mListener = (DateTimeRangeFragInterface) getParentFragment();
            if (mListener == null) mListener = (DateTimeRangeFragInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());}
        dateSetListener = this;
        timeSetListener = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_date_time_range, container, false);
        initiateControls(rootView);

        rangeStart = new GregorianCalendar(2016, Calendar.JANUARY, 1);
        rangeEnd = Calendar.getInstance();
        if (savedInstanceState != null) {
            rangeStart.setTimeInMillis (savedInstanceState.getLong("rangeStart"));
            rangeEnd.setTimeInMillis (savedInstanceState.getLong("rangeEnd"));
        } else {
            //отсечем заведомо пустой кусок шкалы между 01.01.2016 и минимальной датой
            ShiftsSource shiftsSource = new ShiftsSource(getContext().getApplicationContext());
            Shift firstShift = shiftsSource.getFirstShift();
            rangeStart.setTime(firstShift.beginShift);
        }
        refreshControls();
        createButtonsAndSeekBar(rangeStart, rangeEnd);
        return rootView;
    }

    private void initiateControls(View rootView) {
        buttonRangeStartDate = (Button)     rootView.findViewById(R.id.buttonRangeStartDate);
        buttonRangeStartTime = (Button)     rootView.findViewById(R.id.buttonRangeStartTime);
        buttonRangeEndDate   = (Button)     rootView.findViewById(R.id.buttonRangeEndDate);
        buttonRangeEndTime   = (Button)     rootView.findViewById(R.id.buttonRangeEndTime);
        seekBarPlaceHolder   = (ViewGroup)  rootView.findViewById(R.id.seekBarPlaceHolder);
    }

    private void createButtonsAndSeekBar(final Calendar rangeStart, final Calendar rangeEnd) {
        if (buttonRangeStartDate == null || buttonRangeStartTime == null || buttonRangeEndDate == null || buttonRangeEndTime == null)
            throw new NullPointerException("some controls of DateTimeRangeFrag are not ready");
        buttonRangeStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog startDatePD = DatePickerDialog.newInstance(dateSetListener, rangeStart.get(Calendar.YEAR), rangeStart.get(Calendar.MONTH), rangeStart.get(Calendar.DAY_OF_MONTH), false);
                startDatePD.setVibrate(false);
                startDatePD.setYearRange(2016, 2025);
                startDatePD.setCloseOnSingleTapDay(true);
                startDatePD.show(mActivity.getSupportFragmentManager(), "datepicker");
                clickedButtonID = "buttonRangeStartDate";
            }
        });
        buttonRangeStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog startTimePD = TimePickerDialog.newInstance(timeSetListener, rangeStart.get(Calendar.HOUR_OF_DAY), rangeStart.get(Calendar.MINUTE), true, false);
                startTimePD.setVibrate(false);
                startTimePD.setCloseOnSingleTapMinute(Util.singleTapTimePick);
                startTimePD.show(mActivity.getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonRangeStartTime";
            }
        });
        buttonRangeEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog endDatePD = DatePickerDialog.newInstance(dateSetListener, rangeEnd.get(Calendar.YEAR), rangeEnd.get(Calendar.MONTH), rangeEnd.get(Calendar.DAY_OF_MONTH), false);
                endDatePD.setVibrate(false);
                endDatePD.setYearRange(2016, 2025);
                endDatePD.setCloseOnSingleTapDay(true);
                endDatePD.show(mActivity.getSupportFragmentManager(), "datepicker");
                clickedButtonID = "buttonRangeEndDate";
            }
        });
        buttonRangeEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog endTimePD = TimePickerDialog.newInstance(timeSetListener, rangeEnd.get(Calendar.HOUR_OF_DAY), rangeEnd.get(Calendar.MINUTE), true, false);
                endTimePD.setVibrate(false);
                endTimePD.setCloseOnSingleTapMinute(Util.singleTapTimePick);
                endTimePD.show(mActivity.getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonRangeEndTime";
            }
        });

        //add RangeSeekBar to window
        seekBar = new RangeSeekBar<>(rangeStart.getTimeInMillis(), rangeEnd.getTimeInMillis(), mActivity);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                rangeStart.setTimeInMillis(minValue);
                rangeEnd.setTimeInMillis(maxValue);
                mListener.calculate(rangeStart, rangeEnd);
                refreshControls();
            }
        });
        seekBarPlaceHolder.removeAllViews();
        seekBarPlaceHolder.addView(seekBar);
    }

    //вызывается после завершения ввода в диалоге даты
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        switch (clickedButtonID){
            case "buttonRangeStartDate":
                rangeStart.set(Calendar.YEAR, year);
                rangeStart.set(Calendar.MONTH, month);
                rangeStart.set(Calendar.DAY_OF_MONTH, day);
                seekBar.setSelectedMinValue(rangeStart.getTimeInMillis());
                break;
            case "buttonRangeEndDate":
                rangeEnd.set(Calendar.YEAR, year);
                rangeEnd.set(Calendar.MONTH, month);
                rangeEnd.set(Calendar.DAY_OF_MONTH, day);
                seekBar.setSelectedMaxValue(rangeEnd.getTimeInMillis());
                break;
        }
        mListener.calculate(rangeStart, rangeEnd);
        refreshControls();
    }

    //вызывается после завершения ввода в диалоге времени
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        switch (clickedButtonID){
            case "buttonRangeStartTime":
                rangeStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
                rangeStart.set(Calendar.MINUTE, minute);
                seekBar.setSelectedMinValue(rangeStart.getTimeInMillis());
                break;
            case "buttonRangeEndTime":
                rangeEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                rangeEnd.set(Calendar.MINUTE, minute);
                seekBar.setSelectedMaxValue(rangeEnd.getTimeInMillis());
                break;
            default: Toast.makeText(mActivity, R.string.irregularErrMSG, Toast.LENGTH_SHORT).show();
                break;
        }
        mListener.calculate(rangeStart, rangeEnd);
        refreshControls();
    }

    public void refreshControls() {
        buttonRangeStartDate.setText(Util.getStringDateFromCal(rangeStart, getContext()));
        buttonRangeEndDate  .setText(Util.getStringDateFromCal(rangeEnd, getContext()));

        buttonRangeStartTime.setText(Util.getStringTimeFromCal(rangeStart, getContext()));
        buttonRangeEndTime  .setText(Util.getStringTimeFromCal(rangeEnd, getContext()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("rangeStart", rangeStart.getTimeInMillis());
        outState.putLong("rangeEnd",   rangeEnd.getTimeInMillis());
    }

    public Calendar getRangeStart(){ return rangeStart; }
    public Calendar getRangeEnd(){   return rangeEnd; }

    public interface DateTimeRangeFragInterface {
        void calculate(Calendar rangeStart, Calendar rangeEnd);
    }
}
