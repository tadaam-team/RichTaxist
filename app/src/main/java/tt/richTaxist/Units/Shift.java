package tt.richTaxist.Units;

import android.content.res.Resources;
import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import tt.richTaxist.DB.BillingsSQLHelper;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.DB.ShiftsSQLHelper;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.Util;

/**
 * Created by Tau on 27.06.2015.
 */
public class Shift {
    public int shiftID;
    public Date beginShift;
    public Date endShift; //по этому полю проверяется закрыта ли смена. == null пока не закрыта
    public int revenueCash, revenueCard, revenueOfficial, petrol;
    public boolean petrolFilledByHands;
    public int toTheCashier, salaryOfficial, revenueBonus, carRent, salaryUnofficial;
    public double workHoursSpent;
    public int salaryPerHour, distance;
    public long travelTime;
    private static final String LOG_TAG = "ShiftClass";

    public Shift() {
        Shift lastShift = ShiftsSQLHelper.dbOpenHelper.getLastShift();
        if (lastShift == null) shiftID = 0;
        else shiftID = lastShift.shiftID + 1;

        beginShift = new Date();
        petrolFilledByHands = false;
        Log.d(LOG_TAG, "new shift created. ID: " + String.valueOf(shiftID));
        ShiftsSQLHelper.dbOpenHelper.create(this);
    }

    public Shift(int shiftID) {
        //если передаем в конструктор ID значит в SQL добавлять запись не нужно
        this.shiftID = shiftID;
        beginShift = new Date();
        petrolFilledByHands = false;
    }

    public void closeShift() {
        this.endShift = new Date();
        //в этой точке бензин уже введен и итоги уже рассчитаны
        ShiftsSQLHelper.dbOpenHelper.update(this);
    }

    public void openShift() {
        this.endShift = null;
        ShiftsSQLHelper.dbOpenHelper.update(this);
    }
    public boolean isClosed(){
        return this.endShift != null;
    }
    public boolean hasOrders(){
        ArrayList<Order> ordersList = OrdersSQLHelper.dbOpenHelper.getOrdersByShift(this.shiftID);
        return !ordersList.isEmpty();
    }

    public void calculateShiftTotals(int petrol, int taxoparkID){
        ArrayList<Order> orders;
        if (taxoparkID == 0)
            orders = OrdersSQLHelper.dbOpenHelper.getOrdersByShift(this.shiftID);
        else
            orders = OrdersSQLHelper.dbOpenHelper.getOrdersByShiftAndTaxopark(this.shiftID, taxoparkID);

        revenueCash = revenueCard = revenueOfficial = revenueBonus = toTheCashier = salaryOfficial = salaryUnofficial = 0;

        for (Order order : orders){
            float commission = BillingsSQLHelper.dbOpenHelper.getBillingByID(order.billingID).commission;
            switch (order.typeOfPayment){
                case CASH: revenueCash += order.price;
                    salaryOfficial += order.price * (1 - commission/100);
                    break;
                case CARD: revenueCard += order.price;
                    salaryOfficial += order.price * (1 - commission/100);
                    break;
                case TIP: revenueBonus += order.price;
                    break;
            }
        }
        revenueOfficial = revenueCash + revenueCard;

        if (petrol == 0) {//значит факт бензин еще не известен или уже заполнен
            if (!petrolFilledByHands) this.petrol = (int) (revenueOfficial * 0.15);
            /*иначе мы не трогаем введенный руками бензин*/
        }
        else{//нажата кнопка _st_petrol после ввода факт. бензина
            this.petrol = petrol;
        }

        toTheCashier = revenueCash - this.petrol;
        salaryOfficial -= this.petrol;
        salaryUnofficial = salaryOfficial + revenueBonus - carRent;

        long rangeEnd  = (endShift == null) ? Calendar.getInstance().getTimeInMillis() : endShift.getTime();
        workHoursSpent = (double) (rangeEnd - beginShift.getTime()) / (1000 * 60 * 60);
        workHoursSpent = Util.RoundResult(workHoursSpent, 2);
        salaryPerHour  = (int) Math.round(salaryUnofficial / workHoursSpent);
        ShiftsSQLHelper.dbOpenHelper.update(this);
    }

    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginShift);
        Resources res = MainActivity.context.getResources();
        String text = String.format(res.getString(R.string.shiftStart) + ": %02d.%02d %02d:%02d,\n" +
                        res.getString(R.string.revenueOfficial) + ": %d,\n" +
                        res.getString(R.string.toTheCashier) + ": %d,\n" +
                        res.getString(R.string.petrol) + ": %d, " +
                        res.getString(R.string.shiftIsClosed) + "? %s",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                revenueOfficial, toTheCashier, petrol, this.isClosed() ? res.getString(R.string.yes) : res.getString(R.string.no));
        if (carRent != 0) text += String.format("\n" + res.getString(R.string.carRent) + ": %d", carRent);
        return text;
    }
}
