package tt.richTaxist;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Tau on 27.06.2015.
 */
public class Order {
    public static final String PARAM_DISTANCE = "tt.richTaxist.Order.DISTANCE";
    public static final String PARAM_TRAVEL_TIME = "tt.richTaxist.Order.TRAVEL_TIME";
    public Date arrivalDateTime;
    public int price;
    public TypeOfPayment typeOfPayment;
    public Shift shift;
    public int distance;
    public long travelTime;
    public String note;

    public Order(Date arrivalDateTime, TypeOfPayment typeOfPayment, int price, Shift shift, String note) {
        this.arrivalDateTime = arrivalDateTime;
        this.typeOfPayment = typeOfPayment;
        this.price = price;
        this.shift = shift;
        this.note = note;
    }

    public Order(Date arrivalDateTime, int price, TypeOfPayment typeOfPayment, Shift shift, int distance, long travelTime, String note) {
        this.arrivalDateTime = arrivalDateTime;
        this.price           = price;
        this.typeOfPayment   = typeOfPayment;
        this.shift           = shift;
        this.distance        = distance;
        this.travelTime      = travelTime;
        this.note            = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order that = (Order) o;
        return !(arrivalDateTime != null ? !arrivalDateTime.equals(that.arrivalDateTime) : that.arrivalDateTime != null);
    }

    @Override
    public int hashCode() {
        return arrivalDateTime != null ? 31 * Integer.parseInt(arrivalDateTime.toString()) : 0;
    }

    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(arrivalDateTime);
        return String.format("время подачи = %02d.%02d.%02d %02d:%02d, стоимость = %d, тип оплаты = %s\n",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), price, typeOfPayment.toString());
    }
}
