package tt.richTaxist.Units;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import java.util.Calendar;
import java.util.Date;
import tt.richTaxist.DB.Sources.BillingsSource;
import tt.richTaxist.DB.Sources.TaxoparksSource;
import tt.richTaxist.TypeOfPayment;
import tt.richTaxist.R;
import tt.richTaxist.Util;
import java.text.ParseException;
import static tt.richTaxist.DB.Tables.OrdersTable.*;
/**
 * Created by Tau on 27.06.2015.
 */
public class Order implements Parcelable {
    public static final String ORDER_KEY = "tt.richTaxist.Units.Order.KEY";
    public static final String PARAM_DISTANCE = "tt.richTaxist.Units.Order.DISTANCE";
    public static final String PARAM_TRAVEL_TIME = "tt.richTaxist.Units.Order.TRAVEL_TIME";

    public long orderID;
    public Date arrivalDateTime;
    public int price;
    //TODO: typeOfPaymentID
    public TypeOfPayment typeOfPayment;
    public long shiftID;
    public String note;
    public int distance;
    public long travelTime;
    public long taxoparkID;
    public long billingID;

    public Order(Date arrivalDateTime, int price, TypeOfPayment typeOfPayment, long shiftID, String note,
                 int distance, long travelTime, long taxoparkID, long billingID) {
        this.orderID = -1;//необходимо для использования автоинкремента id новой записи в sql
        this.arrivalDateTime = arrivalDateTime;
        this.price           = price;
        this.typeOfPayment   = typeOfPayment;
        this.shiftID         = shiftID;
        this.note            = note;
        this.distance        = distance;
        this.travelTime      = travelTime;
        this.taxoparkID      = taxoparkID;
        this.billingID       = billingID;
    }

    public Order(Cursor cursor) {
        orderID = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        arrivalDateTime = null;
        try {
            arrivalDateTime = Util.dateFormat.parse(cursor.getString(cursor.getColumnIndex(ARRIVAL_DATE_TIME)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        price = cursor.getInt(cursor.getColumnIndex(PRICE));
        typeOfPayment = TypeOfPayment.getById(cursor.getInt(cursor.getColumnIndex(TYPE_OF_PAYMENT)));
        shiftID = cursor.getLong(cursor.getColumnIndex(SHIFT_ID));
        note = cursor.getString(cursor.getColumnIndex(NOTE));
        distance = cursor.getInt(cursor.getColumnIndex(DISTANCE));
        travelTime = cursor.getLong(cursor.getColumnIndex(TRAVEL_TIME));
        taxoparkID = cursor.getLong(cursor.getColumnIndex(TAXOPARK_ID));
        billingID = cursor.getLong(cursor.getColumnIndex(BILLING_ID));
    }

    private Order(Parcel parcel) {
        orderID = parcel.readLong();
        arrivalDateTime = new Date(parcel.readLong());
        price = parcel.readInt();
        typeOfPayment = TypeOfPayment.getById(parcel.readInt());
        shiftID = parcel.readLong();
        note = parcel.readString();
        distance = parcel.readInt();
        travelTime = parcel.readLong();
        taxoparkID = parcel.readLong();
        billingID = parcel.readLong();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel parcel) {
            return new Order(parcel);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass() || arrivalDateTime == null) return false;
        Order that = (Order) obj;
        if (that.arrivalDateTime == null) return false;
        return arrivalDateTime.equals(that.arrivalDateTime);
    }

    @Override
    public int hashCode() {
        return arrivalDateTime != null ? 31 * Integer.parseInt(arrivalDateTime.toString()) : 0;
    }

    public void update(Date arrivalDateTime, int price, TypeOfPayment typeOfPayment, long shiftID, String note,
        int distance, long travelTime, long taxoparkID, long billingID) {
            this.arrivalDateTime = arrivalDateTime;
            this.price           = price;
            this.typeOfPayment   = typeOfPayment;
            this.shiftID         = shiftID;
            this.note            = note;
            this.distance        = distance;
            this.travelTime      = travelTime;
            this.taxoparkID      = taxoparkID;
            this.billingID       = billingID;
    }

    public String getDescription(Context context, TaxoparksSource taxoparksSource, BillingsSource billingsSource) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(arrivalDateTime);
        Resources res = context.getResources();
        String text = String.format(res.getString(R.string.arrivalDateTime) + ": %02d.%02d.%02d %02d:%02d,\n" +
                        res.getString(R.string.price) + ": %d, " +
                        res.getString(R.string.payType) + ": %s,\n" +
                        res.getString(R.string.taxopark) + ": %s, " +
                        res.getString(R.string.billing) + ": %s",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), price, typeOfPayment.getDescription(context),
                taxoparksSource.getTaxoparkByID(taxoparkID),
                billingsSource.getBillingByID(billingID));
        if (!"".equals(note)) text += String.format(",\n" + res.getString(R.string.note) + ": %s", note);
        return text;
    }

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(orderID);
        parcel.writeLong(arrivalDateTime.getTime());
        parcel.writeInt(price);
        parcel.writeInt(typeOfPayment.id);
        parcel.writeLong(shiftID);
        parcel.writeString(note);
        parcel.writeInt(distance);
        parcel.writeLong(travelTime);
        parcel.writeLong(taxoparkID);
        parcel.writeLong(billingID);
    }
}
