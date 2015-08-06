package tt.richTaxist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import tt.richTaxist.Bricks.DF_NumberInput;
import tt.richTaxist.DB.ShiftsStorage;

/**
 * Created by Tau on 27.06.2015.
 */
public class ShiftTotalsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, DF_NumberInput.EditNameDialogListener {
    private Shift currentShift;
    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;
    Calendar shiftStart, shiftEnd;
    String clickedButtonID;

    Button buttonShiftStartDate;
    Button buttonShiftStartTime;
    Button buttonShiftEndDate;
    Button buttonShiftEndTime;
    EditText st_revenueOfficial;
    EditText st_revenueCash;
    EditText st_revenueCard;
    EditText st_revenueBonus;
    Button   st_petrol;
    EditText st_toTheCashier;
    EditText st_salaryOfficial;
    EditText st_salaryPlusBonus;
    ToggleButton st_shiftIsClosed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_totals);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dateSetListener = ShiftTotalsActivity.this;
        timeSetListener = ShiftTotalsActivity.this;

        currentShift = MainActivity.currentShift;
        currentShift.calculateShiftTotals(0);

        buttonShiftStartDate = (Button)       findViewById(R.id.buttonShiftStartDate);
        buttonShiftStartTime = (Button)       findViewById(R.id.buttonShiftStartTime);
        buttonShiftEndDate   = (Button)       findViewById(R.id.buttonShiftEndDate);
        buttonShiftEndTime   = (Button)       findViewById(R.id.buttonShiftEndTime);
        st_revenueOfficial   = (EditText)     findViewById(R.id.st_revenueOfficial);
        st_revenueCash       = (EditText)     findViewById(R.id.st_revenueCash);
        st_revenueCard       = (EditText)     findViewById(R.id.st_revenueCard);
        st_revenueBonus      = (EditText)     findViewById(R.id.st_revenueBonus);
        st_petrol            = (Button)       findViewById(R.id.st_petrol);
        st_toTheCashier      = (EditText)     findViewById(R.id.st_toTheCashier);
        st_salaryOfficial    = (EditText)     findViewById(R.id.st_salaryOfficial);
        st_salaryPlusBonus   = (EditText)     findViewById(R.id.st_salaryPlusBonus);
        st_shiftIsClosed     = (ToggleButton) findViewById(R.id.buttonShiftIsClosed);

        //найдем даты начала и конца смены
        shiftStart = new GregorianCalendar(2015, Calendar.JANUARY, 1);
        shiftStart.setTime(currentShift.beginShift);
        shiftEnd = Calendar.getInstance();
        if (currentShift.isClosed()) shiftEnd.setTime(currentShift.endShift);
        else {
            buttonShiftEndDate.setEnabled(false);
            buttonShiftEndTime.setEnabled(false);
        }
        createButtons(shiftStart, shiftEnd);

        //достанем из хранилища данные по этой смене и заполним поля ShiftTotalsActivity
        st_revenueOfficial.setText(Integer.toString(currentShift.revenueOfficial));
        st_revenueCash.    setText(Integer.toString(currentShift.revenueCash));
        st_revenueCard.    setText(Integer.toString(currentShift.revenueCard));
        st_revenueBonus.   setText(Integer.toString(currentShift.revenueBonus));

        st_petrol.setText(Integer.toString(currentShift.petrol));
        st_petrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DF_NumberInput().show(getSupportFragmentManager(), "fragment_petrol_input");
            }
        });

        st_toTheCashier.   setText(Integer.toString(currentShift.handOverToTheCashier));
        st_salaryOfficial. setText(Integer.toString(currentShift.salaryOfficial));
        st_salaryPlusBonus.setText(Integer.toString(currentShift.salaryPlusBonus));
        st_shiftIsClosed.  setChecked(currentShift.isClosed());
    }

    private void createButtons(final Calendar rangeStart, final Calendar rangeEnd) {
        buttonShiftStartDate.setText(getStringDateFromCal(rangeStart));
        buttonShiftStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog startDatePD = DatePickerDialog.newInstance(dateSetListener, rangeStart.get(Calendar.YEAR), rangeStart.get(Calendar.MONTH), rangeStart.get(Calendar.DAY_OF_MONTH), false);
                startDatePD.setVibrate(false);
                startDatePD.setYearRange(2015, 2017);
                startDatePD.setCloseOnSingleTapDay(true);
                startDatePD.show(getSupportFragmentManager(), "datepicker");
                clickedButtonID = "buttonShiftStartDate";
            }
        });
        buttonShiftStartTime.setText(getStringTimeFromCal(rangeStart));
        buttonShiftStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog startTimePD = TimePickerDialog.newInstance(timeSetListener, rangeStart.get(Calendar.HOUR_OF_DAY), rangeStart.get(Calendar.MINUTE), false, false);
                startTimePD.setVibrate(false);
                startTimePD.setCloseOnSingleTapMinute(Storage.singleTapTimePick);
                startTimePD.show(getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonShiftStartTime";
            }
        });
        buttonShiftEndDate.setText(getStringDateFromCal(rangeEnd));
        buttonShiftEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog endDatePD = DatePickerDialog.newInstance(dateSetListener, rangeEnd.get(Calendar.YEAR), rangeEnd.get(Calendar.MONTH), rangeEnd.get(Calendar.DAY_OF_MONTH), false);
                endDatePD.setVibrate(false);
                endDatePD.setYearRange(2015, 2017);
                endDatePD.setCloseOnSingleTapDay(true);
                endDatePD.show(getSupportFragmentManager(), "datepicker");
                clickedButtonID = "buttonShiftEndDate";
            }
        });
        buttonShiftEndTime.setText(getStringTimeFromCal(rangeEnd));
        buttonShiftEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog endTimePD = TimePickerDialog.newInstance(timeSetListener, rangeEnd.get(Calendar.HOUR_OF_DAY), rangeEnd.get(Calendar.MINUTE), false, false);
                endTimePD.setVibrate(false);
                endTimePD.setCloseOnSingleTapMinute(Storage.singleTapTimePick);
                endTimePD.show(getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonShiftEndTime";
            }
        });
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
            case "buttonShiftStartDate":
                shiftStart.set(Calendar.YEAR, year);
                shiftStart.set(Calendar.MONTH, month);
                shiftStart.set(Calendar.DAY_OF_MONTH, day);
                buttonShiftStartDate.setText(getStringDateFromCal(shiftStart));
                updateShiftDate(shiftStart, currentShift.beginShift);
                break;
            case "buttonShiftEndDate":
                shiftEnd.set(Calendar.YEAR, year);
                shiftEnd.set(Calendar.MONTH, month);
                shiftEnd.set(Calendar.DAY_OF_MONTH, day);
                buttonShiftEndDate.setText(getStringDateFromCal(shiftEnd));
                updateShiftDate(shiftEnd, currentShift.endShift);
                break;
        }
    }

    //вызывается после завершения ввода в диалоге времени
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        switch (clickedButtonID){
            case "buttonShiftStartTime":
                shiftStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
                shiftStart.set(Calendar.MINUTE, minute);
                buttonShiftStartTime.setText(getStringTimeFromCal(shiftStart));
                updateShiftTime(shiftStart, currentShift.beginShift);
                break;
            case "buttonShiftEndTime":
                shiftEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                shiftEnd.set(Calendar.MINUTE, minute);
                buttonShiftEndTime.setText(getStringTimeFromCal(shiftEnd));
                updateShiftTime(shiftEnd, currentShift.endShift);
                break;
            default: Toast.makeText(getApplicationContext(), "ошибка ввода", Toast.LENGTH_SHORT).show(); break;
        }
    }

    public void updateShiftDate(Calendar source, Date destination) {
        Calendar buffer = Calendar.getInstance();
        buffer.setTime(destination);
        buffer.set(Calendar.YEAR, source.get(Calendar.YEAR));
        buffer.set(Calendar.MONTH, source.get(Calendar.MONTH));
        buffer.set(Calendar.DAY_OF_MONTH, source.get(Calendar.DAY_OF_MONTH));
        destination.setTime(buffer.getTime().getTime());
        ShiftsStorage.update(currentShift);
    }

    public void updateShiftTime(Calendar source, Date destination) {
        Calendar buffer = Calendar.getInstance();
        buffer.setTime(destination);
        buffer.set(Calendar.HOUR_OF_DAY, source.get(Calendar.HOUR_OF_DAY));
        buffer.set(Calendar.MINUTE, source.get(Calendar.MINUTE));
        destination.setTime(buffer.getTime().getTime());
        ShiftsStorage.update(currentShift);
    }

    public void onButtonContinueShiftClick(View button) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        Toast.makeText(getApplicationContext(), "продолжим смену", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onButtonExitToMainMenuClick(View button) {
        startActivity(new Intent(getApplicationContext(), FirstScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    public void onButtonShiftIsClosedClick(View button) {
        Boolean goingToCloseShift = ((ToggleButton) button).isChecked();
        if (goingToCloseShift) {
            if (!currentShift.petrolFilledByHands){
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
                quitDialog.setTitle("Вы не записали фактически потраченный бензин");
                quitDialog.setPositiveButton("Игнорировать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentShift.closeShift();
                        buttonShiftEndDate.setEnabled(true);
                        buttonShiftEndTime.setEnabled(true);
                    }
                });
                quitDialog.setNegativeButton("Вернуться", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        st_shiftIsClosed.setChecked(false);
                    }
                });
                quitDialog.show();
            } else{
                currentShift.closeShift();
                buttonShiftEndDate.setEnabled(true);
                buttonShiftEndTime.setEnabled(true);
            }
        }
        else {
            currentShift.openShift();
            buttonShiftEndDate.setEnabled(false);
            buttonShiftEndTime.setEnabled(false);
        }
    }

    @Override
    public void onFinishEditDialog(int inputNumber) {
        currentShift.petrol = inputNumber;
        currentShift.petrolFilledByHands = true;
        st_petrol.setText(String.valueOf(inputNumber));
        currentShift.calculateShiftTotals(inputNumber);

        st_toTheCashier.   setText(String.valueOf(currentShift.handOverToTheCashier));
        st_salaryOfficial. setText(String.valueOf(currentShift.salaryOfficial));
        st_salaryPlusBonus.setText(String.valueOf(currentShift.salaryPlusBonus));
    }

    //TODO в это окно надо передавать автора и возвращаться в него. авторов м.б. три: FirstScreenActivity, ShiftsListActivity, MainActivity
    @Override
    public void onBackPressed() {
        Storage.openQuitDialog(this);
    }
}
