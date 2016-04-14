package tt.richTaxist.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import tt.richTaxist.MainActivity;
import tt.richTaxist.Enums.TypeOfPayment;
import tt.richTaxist.Units.Billing;
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Shift;

/**
 * Created by AlexShredder on 29.06.2015.
 */
public class OrdersSQLHelper extends SQLHelper {
    private static final String LOG_TAG = "OrdersSQLHelper";
    static final String TABLE_NAME = "orders";
    static final String ARRIVAL_DATE_TIME = "arrivalDateTime";
    static final String PRICE = "price";
    static final String TYPE_OF_PAYMENT = "typeOfPayment";
    static final String SHIFT_ID = "shiftID";
    static final String DISTANCE = "distance";
    static final String TRAVEL_TIME = "travelTime";
    static final String NOTE = "note";
    static final String TAXOPARK_ID = "taxoparkID";
    static final String BILLING_ID = "billingID";

    public static OrdersSQLHelper dbOpenHelper = new OrdersSQLHelper(MainActivity.context);

    static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
            + ARRIVAL_DATE_TIME + " DATETIME, "
            + PRICE             + " INT, "
            + TYPE_OF_PAYMENT   + " TINYINT, "
            + SHIFT_ID          + " INT, "
            + DISTANCE          + " INT, "
            + TRAVEL_TIME       + " LONGINT,"
            + NOTE              + " TEXT,"
            + TAXOPARK_ID       + " INT, "
            + BILLING_ID        + " INT)";

    public OrdersSQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public boolean commit(Order order){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ARRIVAL_DATE_TIME, dateFormat.format(order.arrivalDateTime));
        cv.put(PRICE, order.price);
        cv.put(TYPE_OF_PAYMENT, order.typeOfPayment.id);
        cv.put(SHIFT_ID, order.shift.shiftID);
        cv.put(DISTANCE, order.shift.shiftID);
        cv.put(TRAVEL_TIME, order.shift.shiftID);
        cv.put(NOTE, order.note);
        cv.put(TAXOPARK_ID, order.taxoparkID);
        cv.put(BILLING_ID, order.billingID);

        long result = db.insert(TABLE_NAME, null, cv);
        db.close();
        return result != -1;
    }

    public ArrayList<Order> getOrdersInRangeByTaxopark(Calendar fromDate, Calendar toDate, int taxoparkID) {
        ArrayList<Order> ordersList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + ARRIVAL_DATE_TIME + ">='" + dateFormat.format(fromDate.getTime()) + "' AND "
                + ARRIVAL_DATE_TIME + "<='" + dateFormat.format(toDate.getTime()) + "'";
        if (taxoparkID != 0) selectQuery += " AND " + TAXOPARK_ID + "='" + String.valueOf(taxoparkID) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do ordersList.add(loadOrderFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return ordersList;
    }

    public ArrayList<Order> getOrdersByShift(int shiftID) {
        ArrayList<Order> ordersList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SHIFT_ID + "='" + String.valueOf(shiftID) + "'";

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do ordersList.add(loadOrderFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return ordersList;
    }

    public ArrayList<Order> getOrdersByShiftAndTaxopark(int shiftID, int taxoparkID) {
        ArrayList<Order> ordersList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + SHIFT_ID + "='" + String.valueOf(shiftID)  + "' AND "
                + TAXOPARK_ID + "='" + String.valueOf(taxoparkID) + "'";

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do ordersList.add(loadOrderFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return ordersList;
    }

    public boolean canWeDeleteBilling (Billing billing){
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "
                + BILLING_ID + "='" + String.valueOf(billing.billingID) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount() == 0;
    }

//    public Map<String,Object> getDistanceAndTimeByShift(Shift shift) {
//        Map<String,Object> result = new HashMap<>();
//
//        String selectQuery = "SELECT SUM(distance), SUM(travelTime) FROM " + TABLE_NAME +
//                " WHERE " + SHIFT_ID + "='" + String.valueOf(shift.shiftID) + "'";
//        SQLiteDatabase db = getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                result.put(Order.PARAM_DISTANCE, cursor.getInt(cursor.getColumnIndex(DISTANCE)));
//                result.put(Order.PARAM_TRAVEL_TIME, cursor.getInt(cursor.getColumnIndex(TRAVEL_TIME)));
//            } while (cursor.moveToNext());
//        }
//        return result;
//    }

    private Order loadOrderFromCursor(Cursor cursor) {
        Date arrivalDateTime = null;
        try { arrivalDateTime = dateFormat.parse(cursor.getString(cursor.getColumnIndex(ARRIVAL_DATE_TIME)));
        } catch (ParseException e) { e.printStackTrace(); }
        int price = cursor.getInt(cursor.getColumnIndex(PRICE));
        TypeOfPayment typeOfPayment = TypeOfPayment.getById(cursor.getInt(cursor.getColumnIndex(TYPE_OF_PAYMENT)));
        int shiftID = cursor.getInt(cursor.getColumnIndex(SHIFT_ID));
        String note = cursor.getString(cursor.getColumnIndex(NOTE));
        int distance = cursor.getInt(cursor.getColumnIndex(DISTANCE));
        long travelTime = cursor.getLong(cursor.getColumnIndex(TRAVEL_TIME));
        int taxoparkID = cursor.getInt(cursor.getColumnIndex(TAXOPARK_ID));
        int billingID = cursor.getInt(cursor.getColumnIndex(BILLING_ID));

        return new Order(arrivalDateTime, price, typeOfPayment, ShiftsSQLHelper.dbOpenHelper.getShiftByID(shiftID), note,
                distance, travelTime, taxoparkID, billingID);
    }

    public int remove(Order order){
        SQLiteDatabase db = getWritableDatabase();
        String[] tag = new String[]{dateFormat.format(order.arrivalDateTime),
                String.valueOf(order.price),
                String.valueOf(order.typeOfPayment.id),
                String.valueOf(order.shift.shiftID)};
        int result = db.delete(TABLE_NAME, ARRIVAL_DATE_TIME + " = ?"
                + " AND " + PRICE + " = ?"
                + " AND " + TYPE_OF_PAYMENT + " = ?"
                + " AND " + SHIFT_ID + " = ?", tag);
        db.close();
        return result;
    }

    public void deleteOrdersByShift(Shift shift) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, SHIFT_ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        db.close();
    }
}
