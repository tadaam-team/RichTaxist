package tt.richTaxist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.DB.ShiftsSQLHelper;
import tt.richTaxist.Units.Taxopark;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.TypeOfSpinner;

public class GrandTotalsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "GrandTotalsActivity";
    public static final String AUTHOR = "author";
    private Shift firstShift, lastShift;

    private int revenueOfficial, revenueCash, revenueCard, revenueBonus, petrol,
            toTheCashier, salaryOfficial, carRent, salaryPlusBonus, salaryPerHour;
    private double workHoursSpent;
    private TextView gt_revenueOfficial, gt_revenueCash, gt_revenueCard, gt_revenueBonus, gt_petrol,
            gt_toTheCashier, gt_salaryOfficial, gt_carRent, gt_salaryPlusBonus, gt_workHoursSpent, gt_salaryPerHour;
    private Spinner spnFirstShift, spnLastShift, spnTaxopark;
    private Locale locale;
    private String author = "";

    private SQLiteDatabase db;
    private Cursor shiftCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grand_totals);
        Storage.measureScreenWidth(this, (ViewGroup) findViewById(R.id.activity_grand_totals));

        locale = getResources().getConfiguration().locale;
        ArrayList<Shift> list = new ArrayList<>();
        list.addAll(ShiftsSQLHelper.dbOpenHelper.getAllShifts());
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
        gt_revenueOfficial  = (TextView) findViewById(R.id.gt_revenueOfficial);
        gt_revenueCash      = (TextView) findViewById(R.id.gt_revenueCash);
        gt_revenueCard      = (TextView) findViewById(R.id.gt_revenueCard);
        gt_revenueBonus     = (TextView) findViewById(R.id.gt_revenueBonus);
        gt_petrol           = (TextView) findViewById(R.id.gt_petrol);
        gt_toTheCashier     = (TextView) findViewById(R.id.gt_toTheCashier);
        gt_salaryOfficial   = (TextView) findViewById(R.id.gt_salaryOfficial);
        gt_carRent          = (TextView) findViewById(R.id.gt_carRent);
        gt_salaryPlusBonus  = (TextView) findViewById(R.id.gt_salaryPlusBonus);
        gt_workHoursSpent   = (TextView) findViewById(R.id.gt_workHoursSpent);
        gt_salaryPerHour    = (TextView) findViewById(R.id.gt_salaryPerHour);
        spnTaxopark         = (Spinner)  findViewById(R.id.spnTaxopark);
    }

    private void calculateGrandTotals(int taxoparkID) {
        Calendar firstShiftStart = Calendar.getInstance();
        firstShiftStart.setTime(firstShift.beginShift);
        logDate("firstShiftStart", firstShiftStart);

        Calendar lastShiftStart = Calendar.getInstance();
        lastShiftStart.setTime(lastShift.beginShift);
        logDate("lastShiftStart", lastShiftStart);

        ArrayList<Shift> wholeShifts = ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(
                firstShiftStart, lastShiftStart, true, taxoparkID);
        processWholeShifts(wholeShifts);
    }
    private void logDate(String dateName, Calendar dateToLog){
        String log = String.format("%02d.%02d.%04d %02d:%02d:%02d", dateToLog.get(Calendar.DAY_OF_MONTH),
                dateToLog.get(Calendar.MONTH) + 1, dateToLog.get(Calendar.YEAR), dateToLog.get(Calendar.HOUR_OF_DAY),
                dateToLog.get(Calendar.MINUTE), dateToLog.get(Calendar.SECOND));
        if (dateName.length() >= 20) {
            Log.d(LOG_TAG, dateName + log);
        } else {
            while (dateName.length() < 20) dateName += '.';
            Log.d(LOG_TAG, dateName + log);
        }
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
        gt_revenueOfficial  .setText(String.format(locale, "%,d", revenueOfficial));
        gt_revenueCash      .setText(String.format(locale, "%,d", revenueCash));
        gt_revenueCard      .setText(String.format(locale, "%,d", revenueCard));
        gt_revenueBonus     .setText(String.format(locale, "%,d", revenueBonus));
        gt_petrol           .setText(String.format(locale, "%,d", petrol));
        gt_toTheCashier     .setText(String.format(locale, "%,d", toTheCashier));
        gt_salaryOfficial   .setText(String.format(locale, "%,d", salaryOfficial));
        gt_carRent          .setText(String.format(locale, "%,d", carRent));
        gt_salaryPlusBonus  .setText(String.format(locale, "%,d", salaryPlusBonus));
        gt_workHoursSpent   .setText(String.valueOf(workHoursSpent));
        gt_salaryPerHour    .setText(String.format(locale, "%,d", salaryPerHour));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SQLiteOpenHelper helper = new ShiftsSQLHelper(this);
        db = helper.getReadableDatabase();
        shiftCursor = db.query(ShiftsSQLHelper.TABLE_NAME, new String[]{"_id", ShiftsSQLHelper.BEGIN_SHIFT},
                null, null, null, null, null);

        createShiftSpinner(true, spnFirstShift);
        createShiftSpinner(false, spnLastShift);
        createTaxoparkSpinner();
        calculateGrandTotals(Storage.taxoparkID);
        refreshGTControls();
    }

    private void createShiftSpinner(final boolean spinnerRefersToFirstShift, Spinner spinner){
        CursorAdapter shiftAdapter = new ShiftCursorAdapter(this, shiftCursor, 0);
        spinner.setAdapter(shiftAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Cursor cursor = db.query(ShiftsSQLHelper.TABLE_NAME, null, ShiftsSQLHelper.BEGIN_SHIFT + "=?",
                        new String[]{((TextView) itemSelected).getText().toString()}, null, null, null);
                if (cursor.moveToFirst()) {
                    Shift selectedShift = ShiftsSQLHelper.dbOpenHelper.loadShiftFromCursor(cursor);
                    EventBus.getDefault().postSticky(new SpinnerEvent(spinnerRefersToFirstShift, selectedShift));
                }
                cursor.close();

                calculateGrandTotals(Storage.taxoparkID);
                refreshGTControls();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
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
        ArrayList<Taxopark> list = new ArrayList<>();
        list.add(0, new Taxopark(0, "- - -", false, 0));
        list.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        //создать list_entry_spinner.xml пришлось, т.к. текст этого спиннера отображался белым и не был виден
        ArrayAdapter spnTaxoparkAdapter = new ArrayAdapter<>(this, R.layout.list_entry_spinner, list);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
                calculateGrandTotals(Storage.taxoparkID);
                refreshGTControls();
            }
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        shiftCursor.close();
        db.close();
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
            String date = cursor.getString(cursor.getColumnIndex(ShiftsSQLHelper.BEGIN_SHIFT));
//            content.setText(SQLHelper.dateFormat.format(date));
            content.setText(date);
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
    }
}
