package tt.richTaxist.DB.Sources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tt.richTaxist.DB.MySQLHelper;
import tt.richTaxist.FirstScreenActivity;
import tt.richTaxist.Units.Billing;
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Util;
import static tt.richTaxist.DB.Tables.OrdersTable.*;
/**
 * Created by TAU on 04.05.2016.
 */
public class OrdersSource {
    //represents top level of abstraction from dataBase
    //all work with db layer must be done in this class
    //getReadableDatabase() and getWritableDatabase() must not be called outside this class
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;
    private static final String ERROR_TOAST = "db access error";
    private MySQLHelper dbHelper;
    private Context context;

    public OrdersSource(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = MySQLHelper.getInstance(this.context);
    }

    public long create(Order order){
        long id = -1;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = getContentValues(order);
            id = db.insert(TABLE_NAME, null, cv);
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    //TODO: how do we update order?

    public int remove(Order order){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] tag = new String[]{Util.dateFormat.format(order.arrivalDateTime),
                String.valueOf(order.price),
                String.valueOf(order.typeOfPayment.id),
                String.valueOf(order.shiftID)};
        int result = db.delete(TABLE_NAME, ARRIVAL_DATE_TIME + " = ?"
                + " AND " + PRICE + " = ?"
                + " AND " + TYPE_OF_PAYMENT + " = ?"
                + " AND " + SHIFT_ID + " = ?", tag);
        return result;
    }

    public ArrayList<Order> getOrdersList(long shiftID, long taxoparkID) {
        ArrayList<Order> ordersList = new ArrayList<>();
        String sortMethod = "ASC";
        if (Util.youngIsOnTop) sortMethod = "DESC";
        String selectQuery;
        if (taxoparkID == 0) {
            selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "
                    + SHIFT_ID + "='" + String.valueOf(shiftID) + "'"
                    + " ORDER BY " + ARRIVAL_DATE_TIME + " " + sortMethod;
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "
                    + SHIFT_ID + "='" + String.valueOf(shiftID) + "' AND "
                    + TAXOPARK_ID + "='" + String.valueOf(taxoparkID) + "'"
                    + " ORDER BY " + ARRIVAL_DATE_TIME + " " + sortMethod;
        }
//        Log.d(LOG_TAG, "getOrdersList. selectQuery: " + String.valueOf(selectQuery));
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ordersList.add(new Order(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(LOG_TAG, "OrdersSource. ordersList.size(): " + String.valueOf(ordersList.size()));
        return ordersList;
    }

    public Order getLastOrder() {
        Order order = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY _id ASC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            order = new Order(cursor);
        }
        cursor.close();
        return order;
    }

    public boolean canWeDeleteBilling (Billing billing){
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "
                + BILLING_ID + "='" + String.valueOf(billing.billingID) + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean weCanDeleteBilling = cursor.getCount() == 0;
        cursor.close();
        return weCanDeleteBilling;
    }

    public Map<String,Object> getDistanceAndTimeByShift(Shift shift) {
        Map<String,Object> result = new HashMap<>();

        String selectQuery = "SELECT SUM(distance), SUM(travelTime) FROM " + TABLE_NAME +
                " WHERE " + SHIFT_ID + "='" + String.valueOf(shift.shiftID) + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                result.put(Order.PARAM_DISTANCE, cursor.getInt(cursor.getColumnIndex(DISTANCE)));
                result.put(Order.PARAM_TRAVEL_TIME, cursor.getInt(cursor.getColumnIndex(TRAVEL_TIME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public void deleteOrdersByShift(Shift shift) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, SHIFT_ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
    }
}
