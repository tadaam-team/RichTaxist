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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import tt.richTaxist.MainActivity;
import tt.richTaxist.Units.Order;
import tt.richTaxist.R;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Storage;

/**
 * Created by Tau on 13.10.2015.
 */
public class DateTimeRangeFrag extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private FragmentActivity mActivity;

    private ArrayList<? extends Object> list;
    private String workList;
    private Calendar rangeStart, rangeEnd;
    private Button buttonRangeStartDate, buttonRangeStartTime, buttonRangeEndDate, buttonRangeEndTime;
    private ViewGroup seekBarPlaceHolder;
    private RangeSeekBar<Long> seekBar;
    private String clickedButtonID;
    private OnDateTimeRangeFragmentInteractionListener mListener;

    public DateTimeRangeFrag() { }

//    public DateTimeRangeFrag(String workList) {
//        this.workList = workList;
//        if ("shifts".equals(workList))      list = MainActivity.shiftsStorage;
//        else if ("orders".equals(workList)) list = MainActivity.ordersStorage;
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //проверим, реализован ли нужный интерфейс родительским фрагментом или активностью
            mListener = (OnDateTimeRangeFragmentInteractionListener) getParentFragment();
            if (mListener == null) mListener = (OnDateTimeRangeFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());}
        dateSetListener = DateTimeRangeFrag.this;
        timeSetListener = DateTimeRangeFrag.this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_date_time_range, container, false);

        initiateControls(rootView);

        rangeStart = new GregorianCalendar(2015, Calendar.JANUARY, 1);
        rangeEnd = Calendar.getInstance();
        //благодаря обработке ниже, фрагмент сам определяет минимальную границу для себя
        if (savedInstanceState != null) {
            workList = savedInstanceState.getString("workList");
            if ("shifts".equals(workList))      list = MainActivity.shiftsStorage;
            else if ("orders".equals(workList)) list = MainActivity.ordersStorage;
            rangeStart.setTimeInMillis  (savedInstanceState.getLong("rangeStart"));
            rangeEnd.setTimeInMillis    (savedInstanceState.getLong("rangeEnd"));
        } else {
            workList = "shifts";
            list = MainActivity.shiftsStorage;
        }

        if (!list.isEmpty()) {
            //отсечем заведомо пустой кусок шкалы между 01.01.2015 и минимальной датой в list
            Object object;
            if (Storage.youngIsOnTop) object = list.get(list.size() - 1);
            else object = list.get(0);

            if (object instanceof Shift)        rangeStart.setTime(((Shift) object).beginShift);
            else if (object instanceof Order)   rangeStart.setTime(((Order) object).arrivalDateTime);
            refreshControls(true);
        } else {
            //если в списке нет смен, то отключаем кнопки. обратно сделать доступными их невозможно. только заново создать фрагмент и уже правильно пройти проверку
            refreshControls(false);
        }
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
                startDatePD.setYearRange(2015, 2025);
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
                startTimePD.setCloseOnSingleTapMinute(Storage.twoTapTimePick);
                startTimePD.show(mActivity.getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonRangeStartTime";
            }
        });
        buttonRangeEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog endDatePD = DatePickerDialog.newInstance(dateSetListener, rangeEnd.get(Calendar.YEAR), rangeEnd.get(Calendar.MONTH), rangeEnd.get(Calendar.DAY_OF_MONTH), false);
                endDatePD.setVibrate(false);
                endDatePD.setYearRange(2015, 2025);
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
                endTimePD.setCloseOnSingleTapMinute(Storage.twoTapTimePick);
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
                sendDataToParent();
                refreshControls(true);
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
        sendDataToParent();
        refreshControls(true);
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
        sendDataToParent();
        refreshControls(true);
    }

    public void refreshControls(boolean enabled) {
        buttonRangeStartDate.setEnabled(enabled);
        buttonRangeStartTime.setEnabled(enabled);
        buttonRangeEndDate  .setEnabled(enabled);
        buttonRangeEndTime  .setEnabled(enabled);

        buttonRangeStartDate.setText(getStringDateFromCal(rangeStart));
        buttonRangeStartTime.setText(getStringTimeFromCal(rangeStart));
        buttonRangeEndDate  .setText(getStringDateFromCal(rangeEnd));
        buttonRangeEndTime  .setText(getStringTimeFromCal(rangeEnd));
    }
    private String getStringDateFromCal(Calendar date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        return String.format("%02d.%02d.%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR) % 100);
    }
    private String getStringTimeFromCal(Calendar date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("workList", workList);
        outState.putLong("rangeStart", rangeStart.getTimeInMillis());
        outState.putLong("rangeEnd",   rangeEnd.getTimeInMillis());
    }

    public Calendar getRangeStart(){ return rangeStart; }
    public Calendar getRangeEnd(){   return rangeEnd; }

    private void sendDataToParent(){
        mListener.calculate(rangeStart, rangeEnd);
        mListener.refreshControls();
    }

    public interface OnDateTimeRangeFragmentInteractionListener {
        public void calculate(Calendar rangeStart, Calendar rangeEnd);
        public void refreshControls();
    }
}
