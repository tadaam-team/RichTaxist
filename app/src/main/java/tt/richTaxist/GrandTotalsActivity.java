package tt.richTaxist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.DB.Tables.ShiftsTable;
import tt.richTaxist.Units.Shift;

public class GrandTotalsActivity extends AppCompatActivity {
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;
    public static final String AUTHOR = "author";
    private Shift firstShift, lastShift;

    private int revenueOfficial, revenueCash, revenueCard, revenueBonus, petrol,
            toTheCashier, salaryOfficial, carRent, salaryPlusBonus, salaryPerHour;
    private double workHoursSpent;
    private TextView tv_revenueOfficial, tv_revenueCash, tv_revenueCard, tv_revenueBonus, tv_petrol,
            tv_toTheCashier, tv_salaryOfficial, tv_carRent, tv_salaryPlusBonus, tv_workHoursSpent, tv_salaryPerHour;
    private Spinner spnFirstShift, spnLastShift;
    private CustomSpinner spnTaxopark;
    private Locale locale;
    private String author = "";
    private Cursor shiftCursor;
    private ShiftsSource shiftsSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grand_totals);
        Util.measureScreenWidth(this, (ViewGroup) findViewById(R.id.activity_grand_totals));

        locale = getResources().getConfiguration().locale;
        ArrayList<Shift> list = new ArrayList<>();
        shiftsSource = new ShiftsSource(getApplicationContext());
        list.addAll(shiftsSource.getAllShifts(Util.youngIsOnTop));
        firstShift = list.get(0);
        lastShift = list.get(list.size() - 1);
        initiateWidgets();

        if (getIntent() != null){
            author = getIntent().getStringExtra(AUTHOR);
        }
        EventBus.getDefault().register(this);
    }

    private void initiateWidgets() {
        spnFirstShift       = (Spinner)  findViewById(R.id.spnFirstShift);
        spnLastShift        = (Spinner)  findViewById(R.id.spnLastShift);
        tv_revenueOfficial  = (TextView) findViewById(R.id.tv_revenueOfficial);
        tv_revenueCash      = (TextView) findViewById(R.id.tv_revenueCash);
        tv_revenueCard      = (TextView) findViewById(R.id.tv_revenueCard);
        tv_revenueBonus     = (TextView) findViewById(R.id.tv_revenueBonus);
        tv_petrol           = (TextView) findViewById(R.id.tv_petrol);
        tv_toTheCashier     = (TextView) findViewById(R.id.tv_toTheCashier);
        tv_salaryOfficial   = (TextView) findViewById(R.id.tv_salaryOfficial);
        tv_carRent          = (TextView) findViewById(R.id.tv_carRent);
        tv_salaryPlusBonus  = (TextView) findViewById(R.id.tv_salaryPlusBonus);
        tv_workHoursSpent   = (TextView) findViewById(R.id.tv_workHoursSpent);
        tv_salaryPerHour    = (TextView) findViewById(R.id.tv_salaryPerHour);
        spnTaxopark         = (CustomSpinner)  findViewById(R.id.spnTaxopark);
    }

    private void calculateGrandTotals() {
        Calendar firstShiftStart = Calendar.getInstance();
        firstShiftStart.setTime(firstShift.beginShift);
        Calendar lastShiftStart = Calendar.getInstance();
        lastShiftStart.setTime(lastShift.beginShift);

        ArrayList<Shift> wholeShifts = shiftsSource.getShiftsInRangeByTaxopark(
                firstShiftStart, lastShiftStart, true, spnTaxopark.taxoparkID);
        processWholeShifts(wholeShifts);
//        Util.logDate("firstShiftStart", firstShiftStart);
//        Util.logDate("lastShiftStart", lastShiftStart);
//        Log.d(LOG_TAG, "taxoparkID: " + String.valueOf(taxoparkID));
//        Log.d(LOG_TAG, "wholeShifts.size(): " + String.valueOf(wholeShifts.size()));
    }

    private void processWholeShifts(ArrayList<Shift> wholeShifts) {
        revenueOfficial = revenueCash = revenueCard = revenueBonus = petrol = toTheCashier = salaryOfficial
                = carRent = salaryPlusBonus = salaryPerHour = 0;
        workHoursSpent = 0d;
        for (Shift shift : wholeShifts) {
            revenueOfficial += shift.revenueOfficial;
            revenueCash     += shift.revenueCash;
            revenueCard     += shift.revenueCard;
            revenueBonus    += shift.revenueBonus;
            petrol          += shift.petrol;
            toTheCashier    += shift.toTheCashier;
            salaryOfficial  += shift.salaryOfficial;
            carRent         += shift.carRent;
            salaryPlusBonus += shift.salaryUnofficial;
            workHoursSpent  += shift.workHoursSpent;
        }
        salaryPerHour = (int) Math.round(salaryPlusBonus / workHoursSpent);
    }

    private void refreshGTControls(){
        tv_revenueOfficial  .setText(String.format(locale, "%,d", revenueOfficial));
        tv_revenueCash      .setText(String.format(locale, "%,d", revenueCash));
        tv_revenueCard      .setText(String.format(locale, "%,d", revenueCard));
        tv_revenueBonus     .setText(String.format(locale, "%,d", revenueBonus));
        tv_petrol           .setText(String.format(locale, "%,d", petrol));
        tv_toTheCashier     .setText(String.format(locale, "%,d", toTheCashier));
        tv_salaryOfficial   .setText(String.format(locale, "%,d", salaryOfficial));
        tv_carRent          .setText(String.format(locale, "%,d", carRent));
        tv_salaryPlusBonus  .setText(String.format(locale, "%,d", salaryPlusBonus));
        tv_workHoursSpent   .setText(String.valueOf(workHoursSpent));
        tv_salaryPerHour    .setText(String.format(locale, "%,d", salaryPerHour));
    }

    @Override
    protected void onResume() {
        super.onResume();
        shiftCursor = shiftsSource.getShiftCursor();

        createShiftSpinner(true, spnFirstShift);
        createShiftSpinner(false, spnLastShift);
        createTaxoparkSpinner();
        calculateGrandTotals();
        refreshGTControls();
    }

    private void createShiftSpinner(final boolean spinnerRefersToFirstShift, Spinner spinner){
        CursorAdapter shiftAdapter = new ShiftCursorAdapter(this, shiftCursor, 0);
        spinner.setAdapter(shiftAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Shift selectedShift = shiftsSource.getShiftByItsStart(((TextView) itemSelected).getText().toString());
                EventBus.getDefault().postSticky(new SpinnerEvent(spinnerRefersToFirstShift, selectedShift));
                calculateGrandTotals();
                refreshGTControls();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
        if (!spinnerRefersToFirstShift) {
            spinner.setSelection(spinner.getCount() - 1);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpinnerEvent(SpinnerEvent event) {
        if (event.spinnerRefersToFirstShift){
            firstShift = event.shift;
        } else {
            lastShift = event.shift;
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    private class SpinnerEvent{
        public final boolean spinnerRefersToFirstShift;
        public final Shift shift;
        public SpinnerEvent(boolean spinnerRefersToFirstShift, Shift shift) {
            this.spinnerRefersToFirstShift = spinnerRefersToFirstShift;
            this.shift = shift;
        }
    }

    private void createTaxoparkSpinner(){
        spnTaxopark.createSpinner(TypeOfSpinner.TAXOPARK, true);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                spnTaxopark.saveSpinner(TypeOfSpinner.TAXOPARK);
                calculateGrandTotals();
                refreshGTControls();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        shiftCursor.close();
    }

    @Override
    public void onBackPressed() {
        if (author.equals("FirstScreenActivity")) {
            startActivity(new Intent(this, FirstScreenActivity.class));
        }
        else if (author.equals("MainActivity")) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    class ShiftCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        public ShiftCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        //is called to create a View object representing on item in the list
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.list_entry_spinner, parent, false);
        }

        //View returned from newView is passed as first parameter to bindView
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView content = (TextView) view.findViewById(R.id.row_content);
            String date = cursor.getString(cursor.getColumnIndex(ShiftsTable.BEGIN_SHIFT));
//            content.setText(SQLHelper.dateFormat.format(date));
            content.setText(date);
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
    }
}
