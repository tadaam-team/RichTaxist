package tt.richTaxist;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import tt.richTaxist.DB.OrdersStorage;
import tt.richTaxist.DB.ShiftsStorage;

/**
 * Created by Tau on 27.06.2015.
 */
public class Shift {
    final public ArrayList<ArrayList<Order>> listOfAllOrdersPerOneShift = new ArrayList<>();
    public Date shiftID;
    public int revenueCash;
    public int revenueCard;
    public int revenueOfficial;
    public int revenueBonus;
    public int petrol;
    public boolean petrolFilledByHands;
    public int handOverToTheCashier;
    public int salaryOfficial;
    public int salaryPlusBonus;
    public Date beginShift;
    public Date endShift; //по этому полю проверяется закрыта ли смена. == null пока не закрыта
    public float workHoursSpent;
    public int salaryPerHour;
    public int distance;
    public long travelTime;
    private static final String LOG_TAG = "ShiftClass";

    public Shift() {
        beginShift = shiftID = new Date();//инициализируем сегодняшним днем
        petrolFilledByHands = false;
        ShiftsStorage.commit(this);
    }

    public Shift(Date shiftID) {
        this.shiftID = shiftID;
    }

    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginShift);
        return String.format("дата начала = %02d.%02d.%02d %02d:%02d,\nвыручка офиц. = %d,\nсдать в кассу = %d,\nбензин = %d, закрыта? = %s",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR) % 100,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), revenueOfficial, handOverToTheCashier, petrol, this.isClosed()?"да":"нет");
    }

    public void closeShift() {
        this.endShift = new Date();
        //в этой точке бензин уже введен и итоги уже рассчитаны
        ShiftsStorage.update(this);
    }

    public void openShift() {
        this.endShift = null;
        ShiftsStorage.update(this);
    }
    public boolean isClosed(){
        return this.endShift != null;
    }
    public boolean hasOrders(){
        return OrdersStorage.hasShiftOrders(this);
    }

    public void calculateShiftTotals(int petrol){
        Map<TypeOfPayment,Integer> results = OrdersStorage.getSumOrdersByShift(this);
        revenueCash = revenueCard = revenueOfficial = revenueBonus = handOverToTheCashier = salaryOfficial = salaryPlusBonus = 0;

        for (Map.Entry<TypeOfPayment,Integer> x: results.entrySet()){
            switch (x.getKey()){
                case CASH: revenueCash = x.getValue(); break;
                case CARD: revenueCard = x.getValue(); break;
                case BONUS: revenueBonus = x.getValue(); break;
            }
        }
        revenueOfficial = revenueCash + revenueCard;

        if (petrol == 0) {//значит факт бензин еще не известен или уже заполнен
            if (!petrolFilledByHands) this.petrol = (int) (revenueOfficial * 0.13);
            else {/*иначе мы не трогаем введенный руками бензин*/}
        }
        else{//нажата кнопка _st_petrol после ввода факт. бензина
            this.petrol = petrol;
        }

        handOverToTheCashier = revenueCash - this.petrol;
        salaryOfficial = (revenueOfficial / 2) - this.petrol;
        salaryPlusBonus = salaryOfficial + revenueBonus;

        long rangeEnd  = (endShift == null) ? Calendar.getInstance().getTimeInMillis() : endShift.getTime();
        workHoursSpent = (float) (rangeEnd - beginShift.getTime()) / (1000 * 60 * 60);
        workHoursSpent = RoundResult(workHoursSpent, 1);
        salaryPerHour  = Math.round(salaryPlusBonus / workHoursSpent);
        ShiftsStorage.update(this);
    }


    float RoundResult (float value, int decimalSigns) {
        int multiplier = (int) Math.pow(10.0, (double) decimalSigns);
        int numerator = Math.round(value * multiplier);
        return (float) numerator / multiplier;
    }
}
