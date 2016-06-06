package tt.richTaxist.DB;

import android.content.Context;
import android.content.res.Resources;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import tt.richTaxist.DB.Sources.BillingsSource;
import tt.richTaxist.DB.Sources.LocationsSource;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.DB.Sources.TaxoparksSource;
import tt.richTaxist.R;
import tt.richTaxist.Units.Billing;
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Units.Taxopark;
import tt.richTaxist.Util;
/**
 * Created by TAU on 05.06.2016.
 */
public class DataSource {
    private BillingsSource billingsSource;
    private LocationsSource locationsSource;
    private OrdersSource ordersSource;
    private ShiftsSource shiftsSource;
    private TaxoparksSource taxoparksSource;

    public DataSource(Context context) {
        billingsSource = new BillingsSource(context);
        locationsSource = new LocationsSource(context);
        ordersSource = new OrdersSource(context);
        shiftsSource = new ShiftsSource(context);
        taxoparksSource = new TaxoparksSource(context);

        if (shiftsSource.getLastShift() == null) {
            populateDB(context);
        }
    }

    public BillingsSource getBillingsSource() {
        return billingsSource;
    }
    public LocationsSource getLocationsSource() {
        return locationsSource;
    }
    public OrdersSource getOrdersSource() {
        return ordersSource;
    }
    public ShiftsSource getShiftsSource() {
        return shiftsSource;
    }
    public TaxoparksSource getTaxoparksSource() {
        return taxoparksSource;
    }

    private void populateDB(Context context) {
        Resources res = context.getResources();

        billingsSource.create(new Billing("85/15", 15f));
        billingsSource.create(new Billing("50/50", 50f));

        taxoparksSource.create(new Taxopark(res.getString(R.string.defaultTaxoparkName1), true, 0));
        taxoparksSource.create(new Taxopark(res.getString(R.string.defaultTaxoparkName2), false, 1));

        addMockShiftAndClose(new Date(new GregorianCalendar(2016, 4, 26, 14, 0).getTimeInMillis()), true);//месяц +1
        addMockShiftAndClose(new Date(new GregorianCalendar(2016, 4, 28, 8, 40).getTimeInMillis()), true);
        addMockShiftAndClose(new Date(new GregorianCalendar(2016, 5, 1, 10, 0).getTimeInMillis()), true);
        addMockShiftAndClose(new Date(new GregorianCalendar(2016, 5, 3, 12, 30).getTimeInMillis()), true);
        addMockShiftAndClose(new Date(new GregorianCalendar(2016, 5, 5, 9, 20).getTimeInMillis()), false);
    }

    private void addMockShiftAndClose(Date shiftStart, boolean close) {
        Shift mockShift = new Shift(shiftStart);//создадим смену
        mockShift.shiftID = shiftsSource.create(mockShift);//сохраним ее в бд, получив ID
        Date lastOrderArrivalPlus30 = populateShiftWithOrders(mockShift.shiftID, shiftStart);//наполним смену заказами

        //посчитаем итоги смены, обновив информацию в бд
        if (close) {
            mockShift.closeShift(lastOrderArrivalPlus30, this);//closeShift включает в себя calculateShiftTotals
        } else {
            mockShift.calculateShiftTotals(0, 0, this);
        }
    }

    private Date populateShiftWithOrders(long shiftID, Date shiftStart) {
        Date previousDate = shiftStart;
        for (int i = 0; i < 7; i++) {
            //определим паузу между заказами, вычислим дату следующего заказа и сохраним ее в локальную переменную для следующего заказа
            int minutesLag = Util.generateInt(15, 90);
            Date arrival = addMinutesToDate(previousDate, minutesLag);
            previousDate = arrival;

            //подготовим остальные поля заказа
            //NB: в SQL (например taxoparkID) первый id == 1, а в массиве (например typeofPaymentID) == 0
            int price = Util.generateInt(150, 2000);
            int typeofPaymentID = Util.generateInt(0, 2);
            String note = Util.generateInt(0, 5) == 5 ? "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
                    "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." : "";
            long taxoparkID = i < 4 ? 1 : 2;
            long billingID = i < 4 ? 1 : 2;

            //сформируем заказ и сохраним его
            Order newOrder = new Order(arrival, price, typeofPaymentID, shiftID, note, 0, 0, taxoparkID,  billingID);
            ordersSource.create(newOrder);
        }
        return addMinutesToDate(previousDate, 30);
    }

    private Date addMinutesToDate(Date date, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return new Date(cal.getTimeInMillis());
    }
}
