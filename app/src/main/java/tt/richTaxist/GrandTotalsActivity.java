package tt.richTaxist;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import tt.richTaxist.Bricks.DateTimeRangeFrag;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.DB.ShiftsSQLHelper;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.TypeOfSpinner;

public class GrandTotalsActivity extends AppCompatActivity implements DateTimeRangeFrag.OnDateTimeRangeFragmentInteractionListener {
    private static final String LOG_TAG = "GrandTotalsActivity";
    private static Context context;
    private static DateTimeRangeFrag dateTimeRangeFrag;

    private static int revenueOfficial, revenueCash, revenueCard, revenueBonus, petrol, toTheCashier, salaryOfficial, salaryPlusBonus;
    private static EditText gt_revenueOfficial, gt_revenueCash, gt_revenueCard, gt_revenueBonus, gt_petrol,
            gt_toTheCashier, gt_salaryOfficial, gt_salaryPlusBonus;
    private static Spinner spnTaxopark;
    public static ArrayAdapter spnTaxoparkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grand_totals);
        context = getApplicationContext();
        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_grand_totals));
        initiateWidgets();

        dateTimeRangeFrag = (DateTimeRangeFrag) getSupportFragmentManager().findFragmentById(R.id.dateTimeRangeFragment);
        if (!MainActivity.shiftsStorage.isEmpty()) {
            calculateGrandTotals(0);
            refreshGTControls();
        }
    }

    private void initiateWidgets() {
        gt_revenueOfficial  = (EditText) findViewById(R.id.gt_revenueOfficial);
        gt_revenueCash      = (EditText) findViewById(R.id.gt_revenueCash);
        gt_revenueCard      = (EditText) findViewById(R.id.gt_revenueCard);
        gt_revenueBonus     = (EditText) findViewById(R.id.gt_revenueBonus);
        gt_petrol           = (EditText) findViewById(R.id.gt_petrol);
        gt_toTheCashier     = (EditText) findViewById(R.id.gt_toTheCashier);
        gt_salaryOfficial   = (EditText) findViewById(R.id.gt_salaryOfficial);
        gt_salaryPlusBonus  = (EditText) findViewById(R.id.gt_salaryPlusBonus);
        spnTaxopark         = (Spinner)  findViewById(R.id.spnTaxopark);
        createTaxoparkSpinner();
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

    @Override
    public void calculate(Calendar rangeStart, Calendar rangeEnd) {
        Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
        calculateGrandTotals(Storage.taxoparkID);
    }

    @Override
    public void refreshControls() {
        refreshGTControls();
    }

    private static int calculateGrandTotals(int taxoparkID) {
        //необходимая инициализация. значения сразу перезаписываются
        Calendar firstShiftStart, firstShiftEnd, preLastShiftDate, lastShiftStart, lastShiftEnd;
        firstShiftStart = firstShiftEnd = preLastShiftDate = lastShiftStart = lastShiftEnd = Calendar.getInstance();

        revenueOfficial = revenueCash = revenueCard = revenueBonus = petrol = toTheCashier = salaryOfficial = salaryPlusBonus = 0;
        ArrayList<Shift> shifts = ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(
                dateTimeRangeFrag.getRangeStart(), dateTimeRangeFrag.getRangeEnd(), true, taxoparkID);
        if (shifts.size() == 0) {
            //в интервал не попало начало ни одной смены
            //не факт, что заказы вообще есть в этом интервале, но processPartlyShift с этим разберется сам
            processPartlyShift(dateTimeRangeFrag.getRangeStart(), dateTimeRangeFrag.getRangeEnd(), taxoparkID);
            Log.d(LOG_TAG, "no whole shifts to process");
            return 1;
        }

        //надо отделить полные смены от не полных
        //если в выборке только 2 неполные смены, то firstShift == lastShift
        Shift firstShift = shifts.get(0);
        firstShiftStart.setTime(firstShift.beginShift);
        if (firstShift.isClosed()) firstShiftEnd.setTime(firstShift.endShift);
        Log.d(LOG_TAG, "---------------------------------------");
        logDate("rangeStart", dateTimeRangeFrag.getRangeStart());
        logDate("firstShiftStart", firstShiftStart);
        logDate("firstShiftEnd", firstShiftEnd);

        Shift lastShift = shifts.get(shifts.size() - 1);
        if (lastShift.isClosed()) lastShiftEnd.setTime(lastShift.endShift);
        lastShiftStart.setTime(lastShift.beginShift);//только для лога
        logDate("lastShiftStart", lastShiftStart);
        logDate("lastShiftEnd", lastShiftEnd);
        logDate("rangeEnd", dateTimeRangeFrag.getRangeEnd());

        //нам не нужно применять 13% если:
        // 1) первая смена закрыта и следовательно есть данные по факт. бензину
        // 2) выбранный rangeStart не отсекает часть первой смены, следовательно находится точно в firstShiftStart
        // в идеале бы проверку запилить на предмет, есть ли в отсеченном куске заказ и применять 13% только, если он там есть
        boolean firstShiftIsWhole = firstShift.isClosed() && dateTimeRangeFrag.getRangeStart().equals(firstShiftStart);

        //нам не нужно применять 13% если:
        // 1) последняя смена закрыта и следовательно есть данные по факт. бензину
        // 2) выбранный rangeEnd не отсекает часть последней смены, следовательно находится раньше lastShiftEnd
        // в идеале бы проверку запилить на предмет, есть ли в отсеченном куске заказ и применять 13% только, если он там есть
        //проверка lastShiftEnd.after(now) нужна на случай, если водитель уже добавил последний заказ будущим часом (числом),
        //закрыл смену и хочет посмотреть итоги, но дата закрытия еще не наступила
        Calendar now = Calendar.getInstance();
        boolean lastShiftIsWhole = lastShift.isClosed() && (dateTimeRangeFrag.getRangeEnd().after(lastShiftEnd) || lastShiftEnd.after(now));

        if (firstShiftIsWhole) {
            if (!lastShiftIsWhole) {
                lastShiftStart.setTime(lastShift.beginShift);
                preLastShiftDate.setTimeInMillis(lastShiftStart.getTimeInMillis() - 1);
                //получим список всегда целых смен с конца. здесь первая смена всегда целая
                ArrayList<Shift> wholeShifts = ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(firstShiftStart, preLastShiftDate, true, taxoparkID);

                processWholeShifts(wholeShifts);
                processPartlyShift(lastShiftStart, dateTimeRangeFrag.getRangeEnd(), taxoparkID);
            } else {
                processWholeShifts(shifts);
            }
        } else {
            processPartlyShift(dateTimeRangeFrag.getRangeStart(), firstShiftStart, taxoparkID);
            if (!lastShiftIsWhole) {
                lastShiftStart.setTime(lastShift.beginShift);//он уже там
                preLastShiftDate.setTimeInMillis(lastShiftStart.getTimeInMillis() - 1);
                //получим список всегда целых смен с конца. здесь первая смена всегда не целая
                //таких может и не оказаться, если в интервал попало всего 2 нецелые смены
                if (preLastShiftDate.after(firstShiftStart)) {
                    ArrayList<Shift> wholeShifts = ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(firstShiftStart, preLastShiftDate, true, taxoparkID);
                    processWholeShifts(wholeShifts);
                }
                processPartlyShift(lastShiftStart, dateTimeRangeFrag.getRangeEnd(), taxoparkID);
            } else {
                //rangeEnd лежит в пределах: endShift смены перед > rangeEnd > beginShift следующей смены
                processWholeShifts(shifts);
            }
        }
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
        int revenueOfficialLocal, revenueCashLocal, revenueCardLocal, revenueBonusLocal, petrolLocal, toTheCashierLocal, salaryOfficialLocal, salaryPlusBonusLocal;
        revenueOfficialLocal = revenueCashLocal = revenueCardLocal = revenueBonusLocal = petrolLocal = toTheCashierLocal = salaryOfficialLocal = salaryPlusBonusLocal = 0;

        for (Shift shift : wholeShifts) {
            Log.d(LOG_TAG, "shift.workHoursSpent: " + String.valueOf(shift.workHoursSpent));
            revenueOfficialLocal += shift.revenueOfficial;
            revenueCashLocal     += shift.revenueCash;
            revenueCardLocal     += shift.revenueCard;
            revenueBonusLocal    += shift.revenueBonus;
            petrolLocal          += shift.petrol;
            toTheCashierLocal    += shift.toTheCashier;
            salaryOfficialLocal  += shift.salaryOfficial;
            salaryPlusBonusLocal += shift.salaryUnofficial;
        }

        revenueOfficial = revenueOfficialLocal;
        revenueCash     = revenueCashLocal;
        revenueCard     = revenueCardLocal;
        revenueBonus    = revenueBonusLocal;
        petrol          = petrolLocal;
        toTheCashier    = toTheCashierLocal;
        salaryOfficial  = salaryOfficialLocal;
        salaryPlusBonus = salaryPlusBonusLocal;
    }
    //обработать не целую смену (а точнее список заказов, по которым arrivalDateTime лежит между датами)
    //передаваемый интервал всегда меньше 1 смены, если юзер закрывает предыдущую смену перед тем как открывать новую
    private static void processPartlyShift(Calendar fromDate, Calendar toDate, int taxoparkID) {
        int revenueOfficialLocal, revenueCashLocal, revenueCardLocal, revenueBonusLocal, petrolLocal, toTheCashierLocal, salaryOfficialLocal, salaryPlusBonusLocal;
        revenueCashLocal = revenueCardLocal = revenueBonusLocal = 0;
        ArrayList<Order> orders = OrdersSQLHelper.dbOpenHelper.getOrdersInRangeByTaxopark(fromDate, toDate, taxoparkID);
        if (orders.size() != 0) {
            for (Order order : orders) {
                switch (order.typeOfPayment) {
                    case CASH: revenueCashLocal += order.price; break;
                    case CARD: revenueCardLocal += order.price; break;
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
        //если в переданном куске нет заказов, то анализировать нечего, как нечего и прибавлять к (возможно) ранее посчитанным числам
    }

    private static void refreshGTControls(){
        gt_revenueOfficial. setText(String.format(Locale.GERMANY, "%,d", revenueOfficial));
        gt_revenueCash.     setText(String.format(Locale.GERMANY, "%,d", revenueCash));
        gt_revenueCard.     setText(String.format(Locale.GERMANY, "%,d", revenueCard));
        gt_revenueBonus.    setText(String.format(Locale.GERMANY, "%,d", revenueBonus));
        gt_petrol.          setText(String.format(Locale.GERMANY, "%,d", petrol));
        gt_toTheCashier.    setText(String.format(Locale.GERMANY, "%,d", toTheCashier));
        gt_salaryOfficial.  setText(String.format(Locale.GERMANY, "%,d", salaryOfficial));
        gt_salaryPlusBonus. setText(String.format(Locale.GERMANY, "%,d", salaryPlusBonus));
    }
}
