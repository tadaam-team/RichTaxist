package tt.richTaxist;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
    private static Context context;
    private static Shift firstShift, lastShift;

    private static int revenueOfficial, revenueCash, revenueCard, revenueBonus, petrol, toTheCashier, salaryOfficial, carRent, salaryPlusBonus;
    private static EditText gt_revenueOfficial, gt_revenueCash, gt_revenueCard, gt_revenueBonus, gt_petrol,
            gt_toTheCashier, gt_salaryOfficial, gt_carRent, gt_salaryPlusBonus;
    private static Spinner spnFirstShift, spnLastShift, spnTaxopark;
    private static ShiftAdapter4Spinners shiftAdapter;
    public static ArrayAdapter spnShiftAdapter, spnTaxoparkAdapter;
    private String author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grand_totals);
        context = getApplicationContext();
        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_grand_totals));

        ArrayList<Shift> list = new ArrayList<>();
        list.addAll(ShiftsSQLHelper.dbOpenHelper.getAllShifts());
        firstShift = list.get(0);
        lastShift = list.get(list.size() - 1);
//        shiftAdapter = new ShiftAdapter4Spinners(context);
        initiateWidgets();

        if (!MainActivity.shiftsStorage.isEmpty()) {
            calculateGrandTotals(0);
            refreshGTControls();
        }
        if (getIntent() != null) author = getIntent().getStringExtra("author");
        else author = "";
    }

    private void initiateWidgets() {
        spnFirstShift       = (Spinner)  findViewById(R.id.spnFirstShift);
        spnLastShift        = (Spinner)  findViewById(R.id.spnLastShift);
        createShiftsSpinners();
        gt_revenueOfficial  = (EditText) findViewById(R.id.gt_revenueOfficial);
        gt_revenueCash      = (EditText) findViewById(R.id.gt_revenueCash);
        gt_revenueCard      = (EditText) findViewById(R.id.gt_revenueCard);
        gt_revenueBonus     = (EditText) findViewById(R.id.gt_revenueBonus);
        gt_petrol           = (EditText) findViewById(R.id.gt_petrol);
        gt_toTheCashier     = (EditText) findViewById(R.id.gt_toTheCashier);
        gt_salaryOfficial   = (EditText) findViewById(R.id.gt_salaryOfficial);
        gt_carRent          = (EditText) findViewById(R.id.gt_carRent);
        gt_salaryPlusBonus  = (EditText) findViewById(R.id.gt_salaryPlusBonus);
        spnTaxopark         = (Spinner)  findViewById(R.id.spnTaxopark);
        createTaxoparkSpinner();
    }

    public static void createShiftsSpinners(){
        spnFirstShift.setAdapter(shiftAdapter);
        spnFirstShift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                firstShift = (Shift) parent.getItemAtPosition(selectedItemPosition);
                calculateGrandTotals(Storage.taxoparkID);
                refreshGTControls();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spnLastShift.setAdapter(shiftAdapter);
        spnLastShift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                lastShift = (Shift) parent.getItemAtPosition(selectedItemPosition);
                calculateGrandTotals(Storage.taxoparkID);
                refreshGTControls();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public static void createTaxoparkSpinner(){
        ArrayList<Taxopark> list = new ArrayList<>();
        list.add(0, new Taxopark(0, "- - -", false, 0));
        list.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        //создать list_entry_spinner.xml пришлось, т.к. текст этого спиннера отображался белым и не был виден
        spnTaxoparkAdapter = new ArrayAdapter<>(context, R.layout.list_entry_spinner, list);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
                calculateGrandTotals(Storage.taxoparkID);
                refreshGTControls();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
    }

    private static int calculateGrandTotals(int taxoparkID) {
        //необходимая инициализация. значения сразу перезаписываются
        Calendar firstShiftStart, firstShiftEnd, lastShiftStart, lastShiftEnd;
        firstShiftStart = firstShiftEnd = lastShiftStart = lastShiftEnd = Calendar.getInstance();
        revenueOfficial = revenueCash = revenueCard = revenueBonus = petrol = toTheCashier = salaryOfficial = carRent = salaryPlusBonus = 0;

        firstShiftStart.setTime(firstShift.beginShift);
        if (firstShift.isClosed()) firstShiftEnd.setTime(firstShift.endShift);
        Log.d(LOG_TAG, "---------------------------------------");
        logDate("firstShiftStart", firstShiftStart);
        logDate("firstShiftEnd", firstShiftEnd);

        if (lastShift.isClosed()) lastShiftEnd.setTime(lastShift.endShift);
        lastShiftStart.setTime(lastShift.beginShift);
        logDate("lastShiftStart", lastShiftStart);
        logDate("lastShiftEnd", lastShiftEnd);

        ArrayList<Shift> wholeShifts = ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(firstShiftStart, lastShiftStart, true, taxoparkID);
        processWholeShifts(wholeShifts);

        return 0;
    }
    private static void logDate (String dateName, Calendar dateToLog){
        String log = String.format("%02d.%02d.%04d %02d:%02d:%02d", dateToLog.get(Calendar.DAY_OF_MONTH), dateToLog.get(Calendar.MONTH) + 1,
                dateToLog.get(Calendar.YEAR), dateToLog.get(Calendar.HOUR_OF_DAY), dateToLog.get(Calendar.MINUTE), dateToLog.get(Calendar.SECOND));
        if (dateName.length() >= 20) Log.d(LOG_TAG, dateName + String.valueOf(log));
        else {
            while (dateName.length() < 20) dateName += '.';
            Log.d(LOG_TAG, dateName + String.valueOf(log));
        }
    }
    private static void processWholeShifts(ArrayList<Shift> wholeShifts) {
        for (Shift shift : wholeShifts) {
            Log.d(LOG_TAG, "shift.workHoursSpent: " + String.valueOf(shift.workHoursSpent));
            revenueOfficial += shift.revenueOfficial;
            revenueCash     += shift.revenueCash;
            revenueCard     += shift.revenueCard;
            revenueBonus    += shift.revenueBonus;
            petrol          += shift.petrol;
            toTheCashier    += shift.toTheCashier;
            salaryOfficial  += shift.salaryOfficial;
            carRent         += shift.carRent;
            salaryPlusBonus += shift.salaryUnofficial;
        }
    }

    private static void refreshGTControls(){
        gt_revenueOfficial. setText(String.format(Locale.GERMANY, "%,d", revenueOfficial));
        gt_revenueCash.     setText(String.format(Locale.GERMANY, "%,d", revenueCash));
        gt_revenueCard.     setText(String.format(Locale.GERMANY, "%,d", revenueCard));
        gt_revenueBonus.    setText(String.format(Locale.GERMANY, "%,d", revenueBonus));
        gt_petrol.          setText(String.format(Locale.GERMANY, "%,d", petrol));
        gt_toTheCashier.    setText(String.format(Locale.GERMANY, "%,d", toTheCashier));
        gt_salaryOfficial.  setText(String.format(Locale.GERMANY, "%,d", salaryOfficial));
        gt_carRent.         setText(String.format(Locale.GERMANY, "%,d", carRent));
        gt_salaryPlusBonus. setText(String.format(Locale.GERMANY, "%,d", salaryPlusBonus));
    }

    @Override
    public void onBackPressed() {
        if (author.equals("FirstScreenActivity")) {
            startActivity(new Intent(context, FirstScreenActivity.class));
        }
        else if (author.equals("MainActivity")) {
            startActivity(new Intent(context, MainActivity.class));
        }
        finish();
    }


    class ShiftAdapter4Spinners extends ArrayAdapter<Shift> {
        private final Context context;

        public ShiftAdapter4Spinners(Context context) {
            super(context, android.R.layout.simple_list_item_1, MainActivity.shiftsStorage);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Shift shift = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry_spinner, parent, false);
            }

            //установим, какие данные из Shift отобразятся в полях списка
            Resources res = MainActivity.context.getResources();
            TextView textViewMain = (TextView) convertView.findViewById(R.id.entryTextViewMain);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(shift.beginShift);
            textViewMain.setText(String.format(res.getString(R.string.shift)+" %02d.%02d",
                    calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1));

            return convertView;
        }
    }
}
