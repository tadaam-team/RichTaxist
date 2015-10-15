package tt.richTaxist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import tt.richTaxist.Bricks.DateTimeRangeFragment;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.DB.ShiftsSQLHelper;

public class GrandTotalsActivity extends AppCompatActivity implements DateTimeRangeFragment.OnDateTimeRangeFragmentInteractionListener {
    private String LOG_TAG = "GrandTotalsActivity";
    private DateTimeRangeFragment dateTimeRangeFragment;

    private int revenueOfficial, revenueCash, revenueCard, revenueBonus, petrol, toTheCashier, salaryOfficial, salaryPlusBonus;
    private EditText gt_revenueOfficial, gt_revenueCash, gt_revenueCard, gt_revenueBonus, gt_petrol,
            gt_toTheCashier, gt_salaryOfficial, gt_salaryPlusBonus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grand_totals);
        Storage.measureScreenWidth(getApplicationContext(), (ViewGroup) findViewById(R.id.activity_grand_totals));
        initiateWidgets();

        dateTimeRangeFragment = (DateTimeRangeFragment) getSupportFragmentManager().findFragmentById(R.id.dateTimeRangeFragment);
        if (!MainActivity.shiftsStorage.isEmpty()) {
            calculateGrandTotals();
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
    }

    @Override
    public void calculate(Calendar rangeStart, Calendar rangeEnd) {
        calculateGrandTotals();
    }

    @Override
    public void refreshControls() {
        refreshGTControls();
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
        ArrayList<Shift> shifts = ShiftsSQLHelper.dbOpenHelper.getShiftsInRange(
                dateTimeRangeFragment.getRangeStart(), dateTimeRangeFragment.getRangeEnd(), true);
        if (shifts.size() == 0) {
            //в интервал не попало начало ни одной смены
            //не факт, что заказы вообще есть в этом интервале, но processPartlyShift с этим разберется сам
            processPartlyShift(dateTimeRangeFragment.getRangeStart(), dateTimeRangeFragment.getRangeEnd());
            Log.d(LOG_TAG, "no whole shifts to process");
            return 1;
        }

        //надо отделить полные смены от не полных
        //если в выборке только 2 неполные смены, то firstShift == lastShift
        Shift firstShift = shifts.get(0);
        firstShiftStart.setTime(firstShift.beginShift);
        if (firstShift.isClosed()) firstShiftEnd.setTime(firstShift.endShift);
        Log.d(LOG_TAG, "---------------------------------------");
        logDate("rangeStart", dateTimeRangeFragment.getRangeStart());
        logDate("firstShiftStart", firstShiftStart);
        logDate("firstShiftEnd", firstShiftEnd);

        Shift lastShift = shifts.get(shifts.size() - 1);
        if (lastShift.isClosed()) lastShiftEnd.setTime(lastShift.endShift);
        lastShiftStart.setTime(lastShift.beginShift);//только для лога
        logDate("lastShiftStart", lastShiftStart);
        logDate("lastShiftEnd", lastShiftEnd);
        logDate("rangeEnd", dateTimeRangeFragment.getRangeEnd());

        //нам не нужно применять 13% если:
        // 1) первая смена закрыта и следовательно есть данные по факт. бензину
        // 2) выбранный rangeStart не отсекает часть первой смены, следовательно находится точно в firstShiftStart
        // в идеале бы проверку запилить на предмет, есть ли в отсеченном куске заказ и применять 13% только, если он там есть
        boolean firstShiftIsWhole = firstShift.isClosed() && dateTimeRangeFragment.getRangeStart().equals(firstShiftStart);

        //нам не нужно применять 13% если:
        // 1) последняя смена закрыта и следовательно есть данные по факт. бензину
        // 2) выбранный rangeEnd не отсекает часть последней смены, следовательно находится раньше lastShiftEnd
        // в идеале бы проверку запилить на предмет, есть ли в отсеченном куске заказ и применять 13% только, если он там есть
        //проверка lastShiftEnd.after(now) нужна на случай, если водитель уже добавил последний заказ будущим часом (числом),
        //закрыл смену и хочет посмотреть итоги, но дата закрытия еще не наступила
        Calendar now = Calendar.getInstance();
        boolean lastShiftIsWhole = lastShift.isClosed() && (dateTimeRangeFragment.getRangeEnd().after(lastShiftEnd) || lastShiftEnd.after(now));

        if (firstShiftIsWhole) {
            if (!lastShiftIsWhole) {
                lastShiftStart.setTime(lastShift.beginShift);
                preLastShiftDate.setTimeInMillis(lastShiftStart.getTimeInMillis() - 1);
                //получим список всегда целых смен с конца. здесь первая смена всегда целая
                ArrayList<Shift> wholeShifts = ShiftsSQLHelper.dbOpenHelper.getShiftsInRange(firstShiftStart, preLastShiftDate, true);

                processWholeShifts(wholeShifts);
                processPartlyShift(lastShiftStart, dateTimeRangeFragment.getRangeEnd());
            } else {
                processWholeShifts(shifts);
            }
        } else {
            processPartlyShift(dateTimeRangeFragment.getRangeStart(), firstShiftStart);
            if (!lastShiftIsWhole) {
                lastShiftStart.setTime(lastShift.beginShift);//он уже там
                preLastShiftDate.setTimeInMillis(lastShiftStart.getTimeInMillis() - 1);
                //получим список всегда целых смен с конца. здесь первая смена всегда не целая
                //таких может и не оказаться, если в интервал попало всего 2 нецелые смены
                if (preLastShiftDate.after(firstShiftStart)) {
                    ArrayList<Shift> wholeShifts = ShiftsSQLHelper.dbOpenHelper.getShiftsInRange(firstShiftStart, preLastShiftDate, true);
                    processWholeShifts(wholeShifts);
                }
                processPartlyShift(lastShiftStart, dateTimeRangeFragment.getRangeEnd());
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
    private void processPartlyShift(Calendar fromDate, Calendar toDate) {
        int revenueOfficialLocal, revenueCashLocal, revenueCardLocal, revenueBonusLocal, petrolLocal, toTheCashierLocal, salaryOfficialLocal, salaryPlusBonusLocal;
        revenueCashLocal = revenueCardLocal = revenueBonusLocal = 0;
        ArrayList<Order> orders = OrdersSQLHelper.dbOpenHelper.getOrdersInRange(fromDate.getTime(), toDate.getTime());
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
        //если в переданном куске нет заказов, то анализировать нечего, как нечего и прибавлять к (возможно) ранее посчитанным числам
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

    private void refreshGTControls(){
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
