package tt.richTaxist;

import android.util.Log;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import tt.richTaxist.DB.OrdersStorage;
import tt.richTaxist.DB.ShiftsStorage;
import tt.richTaxist.Enums.TypeOfPayment;

/**
 * Created by Tau on 27.06.2015.
 */
public class Shift {
//    final public ArrayList<ArrayList<Order>> listOfAllOrdersPerOneShift = new ArrayList<>();
    public int shiftID;
    public Date beginShift;
    public Date endShift; //по этому полю проверяется закрыта ли смена. == null пока не закрыта
    public int revenueCash, revenueCard, revenueOfficial, petrol;
    public boolean petrolFilledByHands;
    public int toTheCashier, salaryOfficial, revenueBonus, salaryPlusBonus;
    public double workHoursSpent;
    public int salaryPerHour, distance;
    public long travelTime;
    private static final String LOG_TAG = "ShiftClass";

    public Shift() {
        Shift lastShift = ShiftsStorage.getLastShift();
        if (lastShift == null) shiftID = 1;
        else shiftID = lastShift.shiftID + 1;

        beginShift = new Date();
        petrolFilledByHands = false;
        Log.d(LOG_TAG, "new shift created. ID: " + String.valueOf(shiftID));
        ShiftsStorage.commit(this);
    }

    public Shift(int shiftID) {
        this.shiftID = shiftID;
    }

    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginShift);
        return String.format("дата начала: %02d.%02d %02d:%02d,\nвыручка офиц.: %d,\nсдать в кассу: %d,\nбензин: %d, закрыта? %s",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                revenueOfficial, toTheCashier, petrol, this.isClosed()?"да":"нет");
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
        Map<TypeOfPayment,Integer> pricesOfOrders = OrdersStorage.getSumOrdersByShift(this);
        revenueCash = revenueCard = revenueOfficial = revenueBonus = toTheCashier = salaryOfficial = salaryPlusBonus = 0;

        for (Map.Entry<TypeOfPayment,Integer> priceOfOrder : pricesOfOrders.entrySet()){
            switch (priceOfOrder.getKey()){
                case CASH: revenueCash = priceOfOrder.getValue(); break;
                case CARD: revenueCard = priceOfOrder.getValue(); break;
                case TIP: revenueBonus = priceOfOrder.getValue(); break;
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

        toTheCashier = revenueCash - this.petrol;
        salaryOfficial = (revenueOfficial / 2) - this.petrol;
        salaryPlusBonus = salaryOfficial + revenueBonus;

        long rangeEnd  = (endShift == null) ? Calendar.getInstance().getTimeInMillis() : endShift.getTime();
        workHoursSpent = (double) (rangeEnd - beginShift.getTime()) / (1000 * 60 * 60);
        workHoursSpent = Storage.RoundResult(workHoursSpent, 2);
        salaryPerHour  = (int) Math.round(salaryPlusBonus / workHoursSpent);
        ShiftsStorage.update(this);
    }
}
