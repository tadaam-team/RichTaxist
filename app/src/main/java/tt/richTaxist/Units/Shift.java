package tt.richTaxist.Units;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import tt.richTaxist.DB.Sources.BillingsSource;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.R;
import tt.richTaxist.TypeOfPayment;
import tt.richTaxist.Util;
import java.text.ParseException;
import java.util.Locale;
import static tt.richTaxist.DB.Tables.ShiftsTable.*;
/**
 * Created by Tau on 27.06.2015.
 */
public class Shift {
    public long shiftID;
    public Date beginShift;
    public Date endShift; //по этому полю проверяется закрыта ли смена. == null пока не закрыта
    public int revenueCash, revenueCard, revenueOfficial, petrol;
    public boolean petrolFilledByHands;
    public int toTheCashier, salaryOfficial, revenueBonus, carRent, salaryUnofficial;
    public double workHoursSpent;
    public int salaryPerHour, distance;
    public long travelTime;

    public Shift() {
        shiftID = -1;//необходимо для использования автоинкремента id новой записи в sql
        beginShift = new Date();
        petrolFilledByHands = false;
    }

    public Shift(Cursor cursor) {
        shiftID = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

        beginShift = null;
        endShift = null;
        try {
            beginShift = Util.dateFormat.parse(cursor.getString(cursor.getColumnIndex(BEGIN_SHIFT)));
            if (cursor.getString(cursor.getColumnIndex(END_SHIFT)) != null &&
                    !"".equals(cursor.getString(cursor.getColumnIndex(END_SHIFT)))) {
                endShift = Util.dateFormat.parse(cursor.getString(cursor.getColumnIndex(END_SHIFT)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        revenueOfficial       = cursor.getInt(cursor.getColumnIndex(REVENUE_OFFICIAL));
        revenueCash           = cursor.getInt(cursor.getColumnIndex(REVENUE_CASH));
        revenueCard           = cursor.getInt(cursor.getColumnIndex(REVENUE_CARD));
        petrol                = cursor.getInt(cursor.getColumnIndex(PETROL));
        petrolFilledByHands   = cursor.getInt(cursor.getColumnIndex(PETROL_FILLED_BY_HANDS)) != 0;
        toTheCashier          = cursor.getInt(cursor.getColumnIndex(TO_THE_CASHIER));
        salaryOfficial        = cursor.getInt(cursor.getColumnIndex(SALARY_OFFICIAL));
        revenueBonus          = cursor.getInt(cursor.getColumnIndex(REVENUE_BONUS));
        carRent               = cursor.getInt(cursor.getColumnIndex(CAR_RENT));
        salaryUnofficial      = cursor.getInt(cursor.getColumnIndex(SALARY_UNOFFICIAL));
        workHoursSpent        = cursor.getInt(cursor.getColumnIndex(WORK_HOURS_SPENT));
        salaryPerHour         = cursor.getInt(cursor.getColumnIndex(SALARY_PER_HOUR));
        distance              = cursor.getInt(cursor.getColumnIndex(DISTANCE));
        travelTime            = cursor.getInt(cursor.getColumnIndex(TRAVEL_TIME));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass() || shiftID == -1) return false;
        Shift that = (Shift) obj;
        if (that.shiftID == -1) return false;
        return shiftID == that.shiftID;
    }

    @Override
    public int hashCode() {
        return (int) (shiftID != -1 ? 31 * shiftID : 0);
    }

    public void closeShift(ShiftsSource shiftsSource) {
        this.endShift = new Date();
        //в этой точке бензин уже введен и итоги уже рассчитаны
        shiftsSource.update(this);
    }

    public void openShift(ShiftsSource shiftsSource) {
        this.endShift = null;
        shiftsSource.update(this);
    }
    public boolean isClosed(){
        return this.endShift != null;
    }

    public void calculateShiftTotals(int petrol, long taxoparkID, ShiftsSource shiftsSource,
                                     OrdersSource ordersSource, BillingsSource billingsSource) {
        ArrayList<Order> orders = ordersSource.getOrdersList(this.shiftID, taxoparkID);
        revenueCash = revenueCard = revenueOfficial = revenueBonus = toTheCashier = salaryOfficial = salaryUnofficial = 0;

        for (Order order : orders){
            float commission = billingsSource.getBillingByID(order.billingID).commission;
            switch (TypeOfPayment.getById(order.typeOfPaymentID)){
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
        shiftsSource.update(this);
    }

    public String getDescription(Context context){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginShift);
        Resources res = context.getResources();
        Locale locale = res.getConfiguration().locale;
        String text = String.format(locale, res.getString(R.string.shiftDescriptionFormatter),
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                revenueOfficial, toTheCashier, petrol, !this.isClosed() ? res.getString(R.string.not) : "");
        if (carRent != 0) {
            text += String.format(locale, res.getString(R.string.carRentFormatter), carRent);
        }
        return text;
    }
}
