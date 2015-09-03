package tt.richTaxist;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import tt.richTaxist.DB.OrdersStorage;
import tt.richTaxist.DB.ShiftsStorage;
import tt.richTaxist.gps.RangeSeekBar;


public class GrandTotalsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;
    String LOG_TAG = "GrandTotalsActivity";
    Calendar rangeStart, rangeEnd;
    ArrayList<Shift> shifts;

    Button buttonRangeStartDate;
    Button buttonRangeStartTime;
    Button buttonRangeEndDate;
    Button buttonRangeEndTime;
    ViewGroup seekBarPlaceHolder;
    RangeSeekBar<Long> seekBar;

    String clickedButtonID;
    int revenueOfficial, revenueCash, revenueCard, revenueBonus, petrol, toTheCashier, salaryOfficial, salaryPlusBonus, salaryPerHour;
    float workHoursSpent;
    EditText gt_revenueOfficial, gt_revenueCash, gt_revenueCard, gt_revenueBonus, gt_petrol,
            gt_toTheCashier, gt_salaryOfficial, gt_salaryPlusBonus, gt_workHoursSpent, gt_salaryPerHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grand_totals);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dateSetListener = GrandTotalsActivity.this;
        timeSetListener = GrandTotalsActivity.this;

        initiateWidgets();
        rangeStart = new GregorianCalendar(2015, Calendar.JANUARY, 1);
        rangeEnd = Calendar.getInstance();

        //получим список всех смен и найдем дату начала первой смены
        shifts = ShiftsStorage.getShifts(rangeStart.getTime(), rangeEnd.getTime(), false);
        if (shifts.size() != 0) {
            //отсечем заведомо пустой кусок шкалы между 01.01.2015 и датой начала первой смены
            rangeStart.setTime(shifts.get(0).beginShift);
            createButtonsAndSeekBar(rangeStart, rangeEnd);
            calculateGrandTotals();
            refreshWidgets();
        } else {
            //если в списке нет смен, то отключаем кнопки. seekBar не создаем, итоги не считаем
            buttonRangeStartDate.setEnabled(false);
            buttonRangeStartTime.setEnabled(false);
            buttonRangeEndDate.setEnabled(false);
            buttonRangeEndTime.setEnabled(false);
        }
    }

    private void initiateWidgets() {
        buttonRangeStartDate = (Button) findViewById(R.id.buttonRangeStartDate);
        buttonRangeStartTime = (Button) findViewById(R.id.buttonRangeStartTime);
        buttonRangeEndDate   = (Button) findViewById(R.id.buttonRangeEndDate);
        buttonRangeEndTime   = (Button) findViewById(R.id.buttonRangeEndTime);
        seekBarPlaceHolder   = (ViewGroup) findViewById(R.id.seekBarPlaceHolder);

        gt_revenueOfficial  = (EditText) findViewById(R.id.gt_revenueOfficial);
        gt_revenueCash      = (EditText) findViewById(R.id.gt_revenueCash);
        gt_revenueCard      = (EditText) findViewById(R.id.gt_revenueCard);
        gt_revenueBonus     = (EditText) findViewById(R.id.gt_revenueBonus);
        gt_petrol           = (EditText) findViewById(R.id.gt_petrol);
        gt_toTheCashier     = (EditText) findViewById(R.id.gt_toTheCashier);
        gt_salaryOfficial   = (EditText) findViewById(R.id.gt_salaryOfficial);
        gt_salaryPlusBonus  = (EditText) findViewById(R.id.gt_salaryPlusBonus);
        gt_workHoursSpent   = (EditText) findViewById(R.id.gt_workHoursSpent);
        gt_salaryPerHour    = (EditText) findViewById(R.id.gt_salaryPerHour);
    }

    private void createButtonsAndSeekBar(final Calendar rangeStart, final Calendar rangeEnd) {
        buttonRangeStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog startDatePD = DatePickerDialog.newInstance(dateSetListener, rangeStart.get(Calendar.YEAR), rangeStart.get(Calendar.MONTH), rangeStart.get(Calendar.DAY_OF_MONTH), false);
                startDatePD.setVibrate(false);
                startDatePD.setYearRange(2015, 2017);
                startDatePD.setCloseOnSingleTapDay(true);
                startDatePD.show(getSupportFragmentManager(), "datepicker");
                clickedButtonID = "buttonRangeStartDate";
            }
        });
        buttonRangeStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog startTimePD = TimePickerDialog.newInstance(timeSetListener, rangeStart.get(Calendar.HOUR_OF_DAY), rangeStart.get(Calendar.MINUTE), false, false);
                startTimePD.setVibrate(false);
                startTimePD.setCloseOnSingleTapMinute(Storage.singleTapTimePick);
                startTimePD.show(getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonRangeStartTime";
            }
        });
        buttonRangeEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog endDatePD = DatePickerDialog.newInstance(dateSetListener, rangeEnd.get(Calendar.YEAR), rangeEnd.get(Calendar.MONTH), rangeEnd.get(Calendar.DAY_OF_MONTH), false);
                endDatePD.setVibrate(false);
                endDatePD.setYearRange(2015, 2017);
                endDatePD.setCloseOnSingleTapDay(true);
                endDatePD.show(getSupportFragmentManager(), "datepicker");
                clickedButtonID = "buttonRangeEndDate";
            }
        });
        buttonRangeEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog endTimePD = TimePickerDialog.newInstance(timeSetListener, rangeEnd.get(Calendar.HOUR_OF_DAY), rangeEnd.get(Calendar.MINUTE), false, false);
                endTimePD.setVibrate(false);
                endTimePD.setCloseOnSingleTapMinute(Storage.singleTapTimePick);
                endTimePD.show(getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonRangeEndTime";
            }
        });

        //add RangeSeekBar to window
        seekBar = new RangeSeekBar<>(rangeStart.getTimeInMillis(), rangeEnd.getTimeInMillis(), GrandTotalsActivity.this);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                rangeStart.setTimeInMillis(minValue);
                rangeEnd.setTimeInMillis(maxValue);
                calculateGrandTotals();
                refreshWidgets();
            }
        });
        seekBarPlaceHolder.removeAllViews();
        seekBarPlaceHolder.addView(seekBar);
    }

    private int calculateGrandTotals() {
        //необходимая инициализация. значения сразу перезаписываются
        Calendar firstShiftStart = Calendar.getInstance();
        Calendar firstShiftEnd = Calendar.getInstance();
        Calendar lastShiftStart = Calendar.getInstance();
        Calendar lastShiftEnd = Calendar.getInstance();
        //точка для получения списка всегда целых смен с конца. она на 1 мс раньше начала последней не целой смены
        Calendar preLastShiftDate = Calendar.getInstance();

        revenueOfficial = revenueCash = revenueCard = revenueBonus = petrol = toTheCashier = salaryOfficial = salaryPlusBonus = 0;
        shifts = ShiftsStorage.getShifts(rangeStart.getTime(), rangeEnd.getTime(), true); //true заменить на переменную из сторожа
        if (shifts.size() == 0) {
            //в интервал не попало начало ни одной смены
            //не факт, что заказы вообще есть в этом интервале, но processPartlyShift с этим разберется сам
            processPartlyShift(rangeStart, rangeEnd);
            Log.d(LOG_TAG, "no whole shifts to process");
            return 1;
        }

        //надо отделить полные смены от не полных
        //если в выборке только 2 неполные смены, то firstShift == lastShift
        Shift firstShift = shifts.get(0);
        firstShiftStart.setTime(firstShift.beginShift);
        if (firstShift.isClosed()) firstShiftEnd.setTime(firstShift.endShift);
        Log.d(LOG_TAG, "---------------------------------------");
        logDate("rangeStart", rangeStart);
        logDate("firstShiftStart", firstShiftStart);
        logDate("firstShiftEnd", firstShiftEnd);

        Shift lastShift = shifts.get(shifts.size() - 1);
        if (lastShift.isClosed()) lastShiftEnd.setTime(lastShift.endShift);
        lastShiftStart.setTime(lastShift.beginShift);//только для лога
        logDate("lastShiftStart", lastShiftStart);
        logDate("lastShiftEnd", lastShiftEnd);
        logDate("rangeEnd", rangeEnd);

        //нам не нужно применять 13% если:
        // 1) первая смена закрыта и следовательно есть данные по факт. бензину
        // 2) выбранный rangeStart не отсекает часть первой смены, следовательно находится точно в firstShiftStart
        // в идеале бы проверку запилить на предмет, есть ли в отсеченном куске заказ и применять 13% только, если он там есть
        boolean firstShiftIsWhole = firstShift.isClosed() && rangeStart.equals(firstShiftStart);

        //нам не нужно применять 13% если:
        // 1) последняя смена закрыта и следовательно есть данные по факт. бензину
        // 2) выбранный rangeEnd не отсекает часть последней смены, следовательно находится раньше lastShiftEnd
        // в идеале бы проверку запилить на предмет, есть ли в отсеченном куске заказ и применять 13% только, если он там есть
        //проверка lastShiftEnd.after(now) нужна на случай, если водитель уже добавил последний заказ будущим часом (числом),
        //закрыл смену и хочет посмотреть итоги, но дата закрытия еще не наступила
        Calendar now = Calendar.getInstance();
        boolean lastShiftIsWhole = lastShift.isClosed() && (rangeEnd.after(lastShiftEnd) || lastShiftEnd.after(now));

        if (firstShiftIsWhole) {
            if (!lastShiftIsWhole) {
                lastShiftStart.setTime(lastShift.beginShift);
                preLastShiftDate.setTimeInMillis(lastShiftStart.getTimeInMillis() - 1);
                //получим список всегда целых смен с конца. здесь первая смена всегда целая
                ArrayList<Shift> wholeShifts = ShiftsStorage.getShifts(firstShiftStart.getTime(), preLastShiftDate.getTime(), true);

                processWholeShifts(wholeShifts);
                processPartlyShift(lastShiftStart, rangeEnd);
            } else {
                processWholeShifts(shifts);
            }
        } else {
            processPartlyShift(rangeStart, firstShiftStart);
            if (!lastShiftIsWhole) {
                lastShiftStart.setTime(lastShift.beginShift);//он уже там
                preLastShiftDate.setTimeInMillis(lastShiftStart.getTimeInMillis() - 1);
                //получим список всегда целых смен с конца. здесь первая смена всегда не целая
                //таких может и не оказаться, если в интервал попало всего 2 нецелые смены
                if (preLastShiftDate.after(firstShiftStart)) {
                    ArrayList<Shift> wholeShifts = ShiftsStorage.getShifts(firstShiftStart.getTime(), preLastShiftDate.getTime(), true);
                    processWholeShifts(wholeShifts);
                }
                processPartlyShift(lastShiftStart, rangeEnd);
            } else {
                //rangeEnd лежит в пределах: endShift смены перед > rangeEnd > beginShift следующей смены
                processWholeShifts(shifts);
            }
        }
        return 0;
    }

    private void processWholeShifts(ArrayList<Shift> wholeShifts) {
        int revenueOfficialLocal, revenueCashLocal, revenueCardLocal, revenueBonusLocal, petrolLocal, toTheCashierLocal, salaryOfficialLocal, salaryPlusBonusLocal;
        revenueOfficialLocal = revenueCashLocal = revenueCardLocal = revenueBonusLocal = petrolLocal = toTheCashierLocal = salaryOfficialLocal = salaryPlusBonusLocal = 0;
        float workHoursSpentLocal = 0.0f;

        for (Shift shift : wholeShifts) {
            Log.d(LOG_TAG, "shift.workHoursSpent: " + String.valueOf(shift.workHoursSpent));
            revenueOfficialLocal += shift.revenueOfficial;
            revenueCashLocal     += shift.revenueCash;
            revenueCardLocal     += shift.revenueCard;
            revenueBonusLocal    += shift.revenueBonus;
            petrolLocal          += shift.petrol;
            toTheCashierLocal    += shift.toTheCashier;
            salaryOfficialLocal  += shift.salaryOfficial;
            salaryPlusBonusLocal += shift.salaryPlusBonus;
            workHoursSpentLocal  += shift.workHoursSpent;
        }

        revenueOfficial = revenueOfficialLocal;
        revenueCash     = revenueCashLocal;
        revenueCard     = revenueCardLocal;
        revenueBonus    = revenueBonusLocal;
        petrol          = petrolLocal;
        toTheCashier    = toTheCashierLocal;
        salaryOfficial  = salaryOfficialLocal;
        salaryPlusBonus = salaryPlusBonusLocal;
        Log.d(LOG_TAG, "workHoursSpent: " + String.valueOf(workHoursSpent));
        workHoursSpent  = workHoursSpentLocal;
        Log.d(LOG_TAG, "workHoursSpent: " + String.valueOf(workHoursSpent));
        salaryPerHour   = Math.round(salaryPlusBonus / workHoursSpent);
    }

    //обработать не целую смену (а точнее список заказов, по которым arrivalDateTime лежит между датами)
    //передаваемый интервал всегда меньше 1 смены, если юзер закрывает предыдущую смену перед тем как открывать новую
    private void processPartlyShift(Calendar fromDate, Calendar toDate) {
        int revenueOfficialLocal, revenueCashLocal, revenueCardLocal, revenueBonusLocal, petrolLocal, toTheCashierLocal, salaryOfficialLocal, salaryPlusBonusLocal;
        revenueCashLocal = revenueCardLocal = revenueBonusLocal = 0;
        ArrayList<Order> orders = OrdersStorage.getOrders(fromDate.getTime(), toDate.getTime());
        if (orders.size() != 0) {
            for (Order order : orders) {
                switch (order.typeOfPayment) {
                    case CASH: revenueCashLocal   += order.price; break;
                    case CARD: revenueCardLocal   += order.price; break;
                    case TIP: revenueBonusLocal += order.price; break;
                }
            }

            revenueOfficialLocal    = revenueCashLocal + revenueCardLocal;
            petrolLocal             = (int) (revenueOfficialLocal * 0.13);
            toTheCashierLocal       = revenueCashLocal - petrolLocal;
            salaryOfficialLocal     = (revenueOfficialLocal / 2) - petrolLocal;
            salaryPlusBonusLocal    = salaryOfficialLocal + revenueBonusLocal;

            Log.d(LOG_TAG, "revenueOfficial: " + String.valueOf(revenueOfficial));
            revenueOfficial += revenueOfficialLocal;
            Log.d(LOG_TAG, "revenueOfficial: " + String.valueOf(revenueOfficial));
            revenueCash     += revenueCashLocal;
            revenueCard     += revenueCardLocal;
            revenueBonus    += revenueBonusLocal;
            petrol          += petrolLocal;
            toTheCashier    += toTheCashierLocal;
            salaryOfficial  += salaryOfficialLocal;
            salaryPlusBonus += salaryPlusBonusLocal;
            //processPartlyShift не обрабатывает отработку не целой смены, т.к. алгоритм слишком сложен
        }
        else {/*если в переданном куске нет заказов, то анализировать нечего, как нечего и прибавлять к (возможно) ранее посчитанным числам*/}
    }

    private void logDate (String dateName, Calendar dateToLog){
        String log = String.format("%02d.%02d.%04d %02d:%02d:%02d", dateToLog.get(Calendar.DAY_OF_MONTH), dateToLog.get(Calendar.MONTH) + 1,
                dateToLog.get(Calendar.YEAR), dateToLog.get(Calendar.HOUR_OF_DAY), dateToLog.get(Calendar.MINUTE), dateToLog.get(Calendar.SECOND));
        if (dateName.length() >= 20) Log.d(LOG_TAG, dateName + String.valueOf(log));
        else {
            while (dateName.length() < 20) dateName += '.';
            Log.d(LOG_TAG, dateName + String.valueOf(log));
        }
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
        calculateGrandTotals();
        refreshWidgets();
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
            default: Toast.makeText(getApplicationContext(), "ошибка ввода", Toast.LENGTH_SHORT).show(); break;
        }
        calculateGrandTotals();
        refreshWidgets();
    }

    private void refreshWidgets(){
        buttonRangeStartDate.setText(getStringDateFromCal(rangeStart));
        buttonRangeEndDate.  setText(getStringDateFromCal(rangeEnd));
        buttonRangeStartTime.setText(getStringTimeFromCal(rangeStart));
        buttonRangeEndTime.  setText(getStringTimeFromCal(rangeEnd));

        gt_revenueOfficial. setText(String.format(Locale.GERMANY, "%,d", revenueOfficial));
        gt_revenueCash.     setText(String.format(Locale.GERMANY, "%,d", revenueCash));
        gt_revenueCard.     setText(String.format(Locale.GERMANY, "%,d", revenueCard));
        gt_revenueBonus.    setText(String.format(Locale.GERMANY, "%,d", revenueBonus));
        gt_petrol.          setText(String.format(Locale.GERMANY, "%,d", petrol));
        gt_toTheCashier.    setText(String.format(Locale.GERMANY, "%,d", toTheCashier));
        gt_salaryOfficial.  setText(String.format(Locale.GERMANY, "%,d", salaryOfficial));
        gt_salaryPlusBonus. setText(String.format(Locale.GERMANY, "%,d", salaryPlusBonus));
        gt_workHoursSpent.  setText(String.valueOf(workHoursSpent));
        gt_salaryPerHour.   setText(String.format(Locale.GERMANY, "%,d", salaryPerHour));
    }
}
