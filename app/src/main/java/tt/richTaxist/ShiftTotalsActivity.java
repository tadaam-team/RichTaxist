package tt.richTaxist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.Bricks.DF_NumberInput;
import tt.richTaxist.DB.Sources.BillingsSource;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.DB.Tables.ShiftsTable;

/**
 * Created by Tau on 27.06.2015.
 */
public class ShiftTotalsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, DF_NumberInput.NumberInputDialogListener {
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;
    public static final String EXTRA_AUTHOR = "author";
    private Shift currentShift;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private Calendar shiftStart, shiftEnd;
    private String clickedButtonID;
    private String author;

    private Button buttonShiftStartDate, buttonShiftStartTime, buttonShiftEndDate, buttonShiftEndTime,
            tv_petrol, tv_carRent, buttonContinueShift;
    private TextView tv_revenueOfficial, tv_revenueCash, tv_revenueCard, tv_revenueBonus,
            tv_toTheCashier, tv_salaryOfficial, tv_salaryUnofficial, tv_workHoursSpent, tv_salaryPerHour;
    private ToggleButton buttonShiftIsClosed;
    private CustomSpinner spnTaxopark;
    private Locale locale;
    private ShiftsSource shiftsSource;
    private OrdersSource ordersSource;
    private BillingsSource billingsSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_totals);
        Util.measureScreenWidth(this, (ViewGroup) findViewById(R.id.activity_shift_totals));
        dateSetListener = ShiftTotalsActivity.this;
        timeSetListener = ShiftTotalsActivity.this;

        shiftsSource = new ShiftsSource(getApplicationContext());
        ordersSource = new OrdersSource(getApplicationContext());
        billingsSource = new BillingsSource(getApplicationContext());

        locale = getResources().getConfiguration().locale;
        currentShift = MainActivity.currentShift;
        if (currentShift != null) {
            currentShift.calculateShiftTotals(0, 0, shiftsSource, ordersSource, billingsSource);
        }

        //найдем даты начала и конца смены
        shiftStart = new GregorianCalendar(2015, Calendar.JANUARY, 1);
        shiftStart.setTime(currentShift.beginShift);
        shiftEnd = Calendar.getInstance();
        initiateWidgets();

        if (currentShift.isClosed()) {
            shiftEnd.setTime(currentShift.endShift);
            buttonContinueShift.setEnabled(false);
        }
        else {
            buttonShiftEndDate.setEnabled(false);
            buttonShiftEndTime.setEnabled(false);
            buttonContinueShift.setEnabled(true);
        }
        createButtons(shiftStart, shiftEnd);
        refreshSTControls();
        if (getIntent() != null) author = getIntent().getStringExtra("author");
        else author = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        createTaxoparkSpinner();
    }

    public void onButtonExitToMainMenuClick(View button) {
        startActivity(new Intent(getApplicationContext(), FirstScreenActivity.class));
        finish();
    }

    public void onContinueShiftClick(View v) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    public void onPetrolClick(View v) {
        new DF_NumberInput().show(getSupportFragmentManager(), "fragment_petrol_input");
    }

    public void onCarRentClick(View v) {
        new DF_NumberInput().show(getSupportFragmentManager(), "fragment_car_rent_input");
    }

    private void createButtons(final Calendar rangeStart, final Calendar rangeEnd) {
        buttonShiftStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog startDatePD = DatePickerDialog.newInstance(dateSetListener,
                        rangeStart.get(Calendar.YEAR), rangeStart.get(Calendar.MONTH), rangeStart.get(Calendar.DAY_OF_MONTH), false);
                startDatePD.setVibrate(false);
                startDatePD.setYearRange(2015, 2025);
                startDatePD.setCloseOnSingleTapDay(true);
                startDatePD.show(getSupportFragmentManager(), "datepicker");
                clickedButtonID = "buttonShiftStartDate";
            }
        });
        buttonShiftStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog startTimePD = TimePickerDialog.newInstance(timeSetListener,
                        rangeStart.get(Calendar.HOUR_OF_DAY), rangeStart.get(Calendar.MINUTE), true, false);
                startTimePD.setVibrate(false);
                startTimePD.setCloseOnSingleTapMinute(Util.twoTapTimePick);
                startTimePD.show(getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonShiftStartTime";
            }
        });
        buttonShiftEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog endDatePD = DatePickerDialog.newInstance(dateSetListener,
                        rangeEnd.get(Calendar.YEAR), rangeEnd.get(Calendar.MONTH), rangeEnd.get(Calendar.DAY_OF_MONTH), false);
                endDatePD.setVibrate(false);
                endDatePD.setYearRange(2015, 2025);
                endDatePD.setCloseOnSingleTapDay(true);
                endDatePD.show(getSupportFragmentManager(), "datepicker");
                clickedButtonID = "buttonShiftEndDate";
            }
        });
        buttonShiftEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog endTimePD = TimePickerDialog.newInstance(timeSetListener,
                        rangeEnd.get(Calendar.HOUR_OF_DAY), rangeEnd.get(Calendar.MINUTE), true, false);
                endTimePD.setVibrate(false);
                endTimePD.setCloseOnSingleTapMinute(Util.twoTapTimePick);
                endTimePD.show(getSupportFragmentManager(), "timepicker");
                clickedButtonID = "buttonShiftEndTime";
            }
        });
    }

    //вызывается после завершения ввода в диалоге даты
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        switch (clickedButtonID){
            case "buttonShiftStartDate":
                shiftStart.set(Calendar.YEAR, year);
                shiftStart.set(Calendar.MONTH, month);
                shiftStart.set(Calendar.DAY_OF_MONTH, day);
                updateShiftDate(shiftStart, currentShift.beginShift);
                break;
            case "buttonShiftEndDate":
                shiftEnd.set(Calendar.YEAR, year);
                shiftEnd.set(Calendar.MONTH, month);
                shiftEnd.set(Calendar.DAY_OF_MONTH, day);
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
                updateShiftTime(shiftStart, currentShift.beginShift);
                break;
            case "buttonShiftEndTime":
                shiftEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                shiftEnd.set(Calendar.MINUTE, minute);
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
        currentShift.calculateShiftTotals(0, spnTaxopark.taxoparkID, shiftsSource, ordersSource, billingsSource);
        refreshSTControls();
        shiftsSource.update(currentShift);
    }

    public void updateShiftTime(Calendar source, Date destination) {
        Calendar buffer = Calendar.getInstance();
        buffer.setTime(destination);
        buffer.set(Calendar.HOUR_OF_DAY, source.get(Calendar.HOUR_OF_DAY));
        buffer.set(Calendar.MINUTE, source.get(Calendar.MINUTE));
        destination.setTime(buffer.getTime().getTime());
        currentShift.calculateShiftTotals(0, spnTaxopark.taxoparkID, shiftsSource, ordersSource, billingsSource);
        refreshSTControls();
        shiftsSource.update(currentShift);
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
                        currentShift.closeShift(shiftsSource);
                        buttonShiftEndDate.setEnabled(true);
                        buttonShiftEndTime.setEnabled(true);
                        buttonContinueShift.setEnabled(false);
                    }
                });
                quitDialog.setNegativeButton("Вернуться", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        buttonShiftIsClosed.setChecked(false);
                    }
                });
                quitDialog.show();
            } else{
                currentShift.closeShift(shiftsSource);
                buttonShiftEndDate.setEnabled(true);
                buttonShiftEndTime.setEnabled(true);
                buttonContinueShift.setEnabled(false);
            }
        }
        else {
            currentShift.openShift(shiftsSource);
            buttonShiftEndDate.setEnabled(false);
            buttonShiftEndTime.setEnabled(false);
            buttonContinueShift.setEnabled(true);
        }
    }

    @Override
    public void onFinishEditDialog(int inputNumber, String authorTag) {
        if ("fragment_petrol_input".equals(authorTag)) {
            currentShift.petrol = inputNumber;
            currentShift.petrolFilledByHands = true;
            currentShift.calculateShiftTotals(inputNumber, spnTaxopark.taxoparkID, shiftsSource, ordersSource, billingsSource);
        } else if ("fragment_car_rent_input".equals(authorTag)){
            currentShift.carRent = inputNumber;
            currentShift.calculateShiftTotals(0, spnTaxopark.taxoparkID, shiftsSource, ordersSource, billingsSource);
        }
        refreshSTControls();
    }

    private void initiateWidgets() {
        buttonShiftStartDate    = (Button)   findViewById(R.id.buttonShiftStartDate);
        buttonShiftStartTime    = (Button)   findViewById(R.id.buttonShiftStartTime);
        buttonShiftEndDate      = (Button)   findViewById(R.id.buttonShiftEndDate);
        buttonShiftEndTime      = (Button)   findViewById(R.id.buttonShiftEndTime);
        tv_revenueOfficial      = (TextView) findViewById(R.id.tv_revenueOfficial);
        tv_revenueCash          = (TextView) findViewById(R.id.tv_revenueCash);
        tv_revenueCard          = (TextView) findViewById(R.id.tv_revenueCard);
        tv_revenueBonus         = (TextView) findViewById(R.id.tv_revenueBonus);
        tv_petrol               = (Button)   findViewById(R.id.tv_petrol);
        tv_toTheCashier         = (TextView) findViewById(R.id.tv_toTheCashier);
        tv_salaryOfficial       = (TextView) findViewById(R.id.tv_salaryOfficial);
        tv_carRent              = (Button)   findViewById(R.id.tv_carRent);
        tv_salaryUnofficial     = (TextView) findViewById(R.id.tv_salaryPlusBonus);
        tv_workHoursSpent       = (TextView) findViewById(R.id.tv_workHoursSpent);
        tv_salaryPerHour        = (TextView) findViewById(R.id.tv_salaryPerHour);
        buttonShiftIsClosed     = (ToggleButton) findViewById(R.id.buttonShiftIsClosed);
        buttonContinueShift     = (Button)   findViewById(R.id.buttonContinueShift);
        spnTaxopark             = (CustomSpinner) findViewById(R.id.spnTaxopark);
        createTaxoparkSpinner();
    }

    private void createTaxoparkSpinner(){
        spnTaxopark.createSpinner(TypeOfSpinner.TAXOPARK, true);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                spnTaxopark.saveSpinner(TypeOfSpinner.TAXOPARK);
                currentShift.calculateShiftTotals(0, spnTaxopark.taxoparkID, shiftsSource, ordersSource, billingsSource);
                refreshSTControls();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }

    private void refreshSTControls(){
        buttonShiftStartDate    .setText(getStringDateFromCal(shiftStart));
        buttonShiftEndDate      .setText(getStringDateFromCal(shiftEnd));
        buttonShiftStartTime    .setText(getStringTimeFromCal(shiftStart));
        buttonShiftEndTime      .setText(getStringTimeFromCal(shiftEnd));

        tv_revenueOfficial      .setText(String.format(locale, "%,d", currentShift.revenueOfficial));
        tv_revenueCash          .setText(String.format(locale, "%,d", currentShift.revenueCash));
        tv_revenueCard          .setText(String.format(locale, "%,d", currentShift.revenueCard));
        tv_revenueBonus         .setText(String.format(locale, "%,d", currentShift.revenueBonus));

        if (spnTaxopark.taxoparkID == 0) {
            tv_petrol           .setText(String.format(locale, "%,d", currentShift.petrol));
            tv_toTheCashier     .setText(String.format(locale, "%,d", currentShift.toTheCashier));
            tv_salaryOfficial   .setText(String.format(locale, "%,d", currentShift.salaryOfficial));
            tv_carRent          .setText(String.format(locale, "%,d", currentShift.carRent));
            tv_salaryUnofficial .setText(String.format(locale, "%,d", currentShift.salaryUnofficial));
            tv_workHoursSpent   .setText(String.valueOf(currentShift.workHoursSpent));
            tv_salaryPerHour    .setText(String.format(locale, "%,d", currentShift.salaryPerHour));
        } else {
            tv_petrol           .setText("- - -");
            tv_toTheCashier     .setText("- - -");
            tv_salaryOfficial   .setText("- - -");
            tv_carRent          .setText("- - -");
            tv_salaryUnofficial .setText("- - -");
            tv_workHoursSpent   .setText("- - -");
            tv_salaryPerHour    .setText("- - -");
        }
        buttonShiftIsClosed.setChecked(currentShift.isClosed());
    }
    private String getStringDateFromCal(Calendar date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        return String.format(locale, "%02d.%02d.%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR) % 100);
    }
    private String getStringTimeFromCal(Calendar date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        return String.format(locale, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }


    @Override
    public void onBackPressed() {
        if (author.equals("FirstScreenActivity")) {
            startActivity(new Intent(getApplicationContext(), FirstScreenActivity.class));
        }
        else if (author.equals("MainActivity")) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        finish();
    }
}
