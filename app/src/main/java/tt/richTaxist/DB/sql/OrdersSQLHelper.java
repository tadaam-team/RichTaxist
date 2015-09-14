package tt.richTaxist.DB.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tt.richTaxist.DB.OrdersStorageList;
import tt.richTaxist.DB.ShiftsStorage;
import tt.richTaxist.Order;
import tt.richTaxist.Shift;
import tt.richTaxist.Enums.TypeOfPayment;

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
    static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
            + ARRIVAL_DATE_TIME + " DATETIME, "
            + PRICE + " INT, "
            + TYPE_OF_PAYMENT + " TINYINT, "
            + SHIFT_ID + " INT, "
            + DISTANCE + " INT, "
            + TRAVEL_TIME + " LONGINT,"
            + NOTE + " TEXT)";

    public OrdersSQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        super.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) { super.onUpgrade(sqLiteDatabase, oldVer, newVer); }

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

        long result = db.insert(OrdersSQLHelper.TABLE_NAME, null, cv);
        db.close();
        return result != -1;
    }

    public OrdersStorageList getOrders(Date fromDate, Date toDate) {
        OrdersStorageList ordersStorage = new OrdersStorageList(false);

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + ARRIVAL_DATE_TIME + ">='" + dateFormat.format(fromDate) + "' AND "
                + ARRIVAL_DATE_TIME + "<='" + dateFormat.format(toDate) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do ordersStorage.add(loadOrderFromCursor(cursor));
            while (cursor.moveToNext());
        }
        ordersStorage.setWriteToDB(true);
        return ordersStorage;
    }

    public OrdersStorageList getOrdersByShift(Shift shift) {
        OrdersStorageList ordersStorage = new OrdersStorageList(false);
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SHIFT_ID + "='" + String.valueOf(shift.shiftID) + "'";

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do ordersStorage.add(loadOrderFromCursor(cursor));
            while (cursor.moveToNext());
        }
        ordersStorage.setWriteToDB(true);
        return ordersStorage;
    }

    public boolean hasShiftOrders(Shift shift) {
//        OrdersStorageList ordersStorage = new OrdersStorageList(false);
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SHIFT_ID + "='" + String.valueOf(shift.shiftID) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public Map<TypeOfPayment,Integer> getSumOrdersByShift(Shift shift) {
        Map<TypeOfPayment,Integer> result = new HashMap<>();
//        OrdersStorageList ordersStorage = new OrdersStorageList(false);
        // Select All Query
        String selectQuery = "SELECT typeOfPayment, SUM(price) FROM " + TABLE_NAME +
                " WHERE " + SHIFT_ID + "='" + String.valueOf(shift.shiftID) + "' GROUP BY typeOfPayment";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d(LOG_TAG, "cursor.getColumnCount(): " + String.valueOf(cursor.getColumnCount()));

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TypeOfPayment typeOfPayment = TypeOfPayment.getById(cursor.getInt(cursor.getColumnIndex(TYPE_OF_PAYMENT)));
                int revenueOfSuchType = cursor.getInt(1);
                result.put(typeOfPayment, revenueOfSuchType);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public Map<String,Object> getDistanceAndTimeByShift(Shift shift) {
        Map<String,Object> result = new HashMap<>();
//        OrdersStorageList ordersStorage = new OrdersStorageList(false);

        // Select All Query
        String selectQuery = "SELECT SUM(distance), SUM(travelTime) FROM " + TABLE_NAME +
                " WHERE " + SHIFT_ID + "='" + String.valueOf(shift.shiftID) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                result.put(Order.PARAM_DISTANCE, cursor.getInt(cursor.getColumnIndex(DISTANCE)));
                result.put(Order.PARAM_TRAVEL_TIME, cursor.getInt(cursor.getColumnIndex(TRAVEL_TIME)));
            } while (cursor.moveToNext());
        }
        return result;
    }

//    public OrdersStorageList getOrders(Date date) {
//        return getOrders(date, date);
//    }

    private Order loadOrderFromCursor(Cursor cursor) {
        Date arrivalDateTime = null;
        try { arrivalDateTime = dateFormat.parse(cursor.getString(cursor.getColumnIndex(ARRIVAL_DATE_TIME)));
        } catch (ParseException e) { e.printStackTrace(); }
        int price = cursor.getInt(cursor.getColumnIndex(PRICE));
        TypeOfPayment typeOfPayment = TypeOfPayment.getById(cursor.getInt(cursor.getColumnIndex(TYPE_OF_PAYMENT)));
        int shiftID = cursor.getInt(cursor.getColumnIndex(SHIFT_ID));
        int distance = cursor.getInt(cursor.getColumnIndex(DISTANCE));
        long travelTime = cursor.getLong(cursor.getColumnIndex(TRAVEL_TIME));
        String note = cursor.getString(cursor.getColumnIndex(NOTE));

        return new Order(arrivalDateTime, price, typeOfPayment, ShiftsStorage.getShiftByID(shiftID), distance, travelTime, note);
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

    public int remove(List<Order> orders){
        SQLiteDatabase db = getWritableDatabase();
        int result = 0;
        for (int i = 0; i < orders.size(); i++) {
            Order order =  orders.get(i);
            String[] tag = new String[]{dateFormat.format(order.arrivalDateTime),
                    String.valueOf(order.price),
                    String.valueOf(order.typeOfPayment.id),
                    String.valueOf(order.shift.shiftID)};
            result += db.delete(TABLE_NAME, ARRIVAL_DATE_TIME + " = ?"
                    + " AND " + PRICE + " = ?"
                    + " AND " + TYPE_OF_PAYMENT + " = ?"
                    + " AND " + SHIFT_ID + " = ?", tag);
        }
        db.close();
        return result;
    }

    public void deleteOrdersByShift(Shift shift) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, SHIFT_ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        db.close();
    }
}
