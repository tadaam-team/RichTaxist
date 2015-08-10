package tt.richTaxist.DB.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tt.richTaxist.DB.OrdersStorageList;
import tt.richTaxist.DB.ShiftsStorage;
import tt.richTaxist.Order;
import tt.richTaxist.Shift;
import tt.richTaxist.TypeOfPayment;

/**
 * Created by AlexShredder on 29.06.2015.
 */
public class OrdersSQLHelper extends SQLHelper {
    static final String TABLE_NAME = "orders";
    static final String TYPE_OF_PAYMENT = "typeOfPayment";
    static final String PRICE = "price";
    static final String SHIFT = "shift";
    static final String ARRIVAL_DATE_TIME = "arrivalDateTime";
    static final String DISTANCE = "distance";
    static final String TRAVEL_TIME = "travelTime";
    static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
            + TYPE_OF_PAYMENT + " TINYINT, "
            + ARRIVAL_DATE_TIME + " DATETIME, "
            + PRICE + " INT, "
            + SHIFT + " DATETIME, "
            + DISTANCE + " INT, "
            + TRAVEL_TIME + " LONGINT)";

    public OrdersSQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        super.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { super.onUpgrade(sqLiteDatabase, i, i1); }

    public boolean commit(Order order){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(PRICE, order.price);
        cv.put(ARRIVAL_DATE_TIME, dateFormat.format(order.arrivalDateTime));
        cv.put(TYPE_OF_PAYMENT, order.typeOfPayment.id);
        cv.put(SHIFT, dateFormat.format(order.shift.shiftID));

        long result = db.insert(OrdersSQLHelper.TABLE_NAME, null, cv);
        db.close();
        return result != -1;
    }

    public int remove(Order order){
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_NAME, PRICE        + " = ?"
                        + " AND " + ARRIVAL_DATE_TIME   + " = ?"
                        + " AND " + TYPE_OF_PAYMENT     + " = ?"
                        + " AND " + SHIFT               + " = ?",
                new String[]{String.valueOf(order.price), dateFormat.format(order.arrivalDateTime),
                        String.valueOf(order.typeOfPayment.id), dateFormat.format(order.shift.shiftID)});
        db.close();
        return result;
    }

    public void deleteOrdersByShift(Shift shift) {
        SQLiteDatabase db = getWritableDatabase();
        // Select All Query
        int result = db.delete(TABLE_NAME, SHIFT + " = ?", new String[]{dateFormat.format(shift.shiftID)});
        db.close();
    }

    public int remove(List<Order> orders){
        SQLiteDatabase db = getWritableDatabase();
        int result = 0;
        for (int i = 0; i < orders.size(); i++) {
            Order order =  orders.get(i);
            result += db.delete(TABLE_NAME, PRICE           + " = ?"
                            + " AND " + ARRIVAL_DATE_TIME   + " = ?"
                            + " AND " + TYPE_OF_PAYMENT     + " = ?"
                            + " AND " + SHIFT               + " = ?",
                    new String[] { String.valueOf(order.price), dateFormat.format(order.arrivalDateTime),
                            String.valueOf(order.typeOfPayment.id), dateFormat.format(order.shift.shiftID)});
        }
        db.close();
        return result;
    }

    public OrdersStorageList getOrders(Date fromDate, Date toDate) {
        OrdersStorageList ordersStorage = new OrdersStorageList(false);

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + ARRIVAL_DATE_TIME + "<='" + dateFormat.format(toDate) + "' AND "
                + ARRIVAL_DATE_TIME + ">='" + dateFormat.format(fromDate) + "'";
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
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SHIFT + "='" + dateFormat.format(shift.shiftID) + "'";

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
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SHIFT + "='" + dateFormat.format(shift.shiftID) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount()>0;
    }

    public Map<TypeOfPayment,Integer> getSumOrdersByShift(Shift shift) {
        Map<TypeOfPayment,Integer> result = new HashMap<>();
//        OrdersStorageList ordersStorage = new OrdersStorageList(false);
        // Select All Query
        String selectQuery = "SELECT typeOfPayment, SUM(price) FROM " + TABLE_NAME +
                " WHERE " + SHIFT + "='" + dateFormat.format(shift.shiftID) + "' GROUP BY typeOfPayment";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TypeOfPayment typeOfPayment = TypeOfPayment.getById(cursor.getInt(0));
                int sum = cursor.getInt(1);
                result.put(typeOfPayment,sum);
            } while (cursor.moveToNext());
        }
        return result;
    }

    public Map<String,Object> getDistanceAndTimeByShift(Shift shift) {
        Map<String,Object> result = new HashMap<>();
//        OrdersStorageList ordersStorage = new OrdersStorageList(false);

        // Select All Query
        String selectQuery = "SELECT SUM(distance), SUM(travelTime) FROM " + TABLE_NAME +
                " WHERE " + SHIFT + "='" + dateFormat.format(shift.shiftID) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                result.put(Order.PARAM_DISTANCE, cursor.getInt(0));
                result.put(Order.PARAM_TRAVEL_TIME, cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        return result;
    }

//    public OrdersStorageList getOrders(Date date) {
//        return getOrders(date, date);
//    }

    private Order loadOrderFromCursor(Cursor cursor) {
        Date arrivalDateTime = null;
        try {
            arrivalDateTime = dateFormat.parse(cursor.getString(2));
        } catch (ParseException e) { e.printStackTrace(); }
        TypeOfPayment typeOfPayment = TypeOfPayment.getById(cursor.getInt(1));
        int price = cursor.getInt(3);
        String shift = cursor.getString(4);
        int distance = cursor.getInt(5);
        long travelTime = cursor.getLong(6);

        return new Order(arrivalDateTime, price, typeOfPayment, ShiftsStorage.getShiftByID(shift), distance, travelTime);
    }
}
