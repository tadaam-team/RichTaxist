package tt.richCabman.database.sources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import tt.richCabman.util.Constants;
import tt.richCabman.database.MySQLHelper;
import tt.richCabman.model.Order;
import tt.richCabman.model.Shift;
import tt.richCabman.util.Util;
import static tt.richCabman.database.tables.ShiftsTable.*;
/**
 * Created by TAU on 04.05.2016.
 */
public class ShiftsSource {
    //represents top level of abstraction from dataBase
    //all work with db layer must be done in this class
    //getReadableDatabase() and getWritableDatabase() must not be called outside this class
    private static final String ERROR_TOAST = "db access error";
    private MySQLHelper dbHelper;
    private Context context;
    private OrdersSource ordersSource;

    public ShiftsSource(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = MySQLHelper.getInstance(this.context);
        ordersSource = new OrdersSource(this.context);
    }

    public long create(Shift shift){
        long id = -1;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = getContentValues(shift);
            id = db.insert(TABLE_NAME, null, cv);
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public boolean update(Shift shift){
        int result = -1;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = getContentValues(shift);
            result = db.update(TABLE_NAME, cv, BaseColumns._ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return result != -1;
    }

    public boolean remove(Shift shift){
        int result = -1;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            result = db.delete(TABLE_NAME, BaseColumns._ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return result != -1;
    }

    public void dropAllTablesInDB() {
        dbHelper.dropAllTablesInDB();
    }

    public ArrayList<Shift> getAllShifts(boolean youngIsOnTop) {
        ArrayList<Shift> shiftsList = new ArrayList<>();
        String sortMethod = "ASC";
        if (youngIsOnTop) sortMethod = "DESC";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + BEGIN_SHIFT + " " + sortMethod;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                shiftsList.add(new Shift(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return shiftsList;
    }

    public ArrayList<Shift> getShiftsByTaxopark(boolean youngIsOnTop, long taxoparkID) {
        ArrayList<Shift> shiftsList = new ArrayList<>();
        for (Shift shift : getAllShifts(youngIsOnTop)){
            if (taxoparkID == 0 || checkTaxoparkInShift(shift, taxoparkID)) {
                shiftsList.add(shift);
            }
        }
        Log.d(Constants.LOG_TAG, "shiftsList.size(): " + String.valueOf(shiftsList.size()));
        return shiftsList;
    }

    public ArrayList<Shift> getShiftsInRangeByTaxopark(Calendar fromDate, Calendar toDate, boolean youngIsOnTop, long taxoparkID) {
        ArrayList<Shift> shiftsList = new ArrayList<>();
        String sortMethod = "ASC";
        if (youngIsOnTop) sortMethod = "DESC";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "
                + BEGIN_SHIFT + ">='" + Util.dateFormat.format(fromDate.getTime()) + "' AND "
                + BEGIN_SHIFT + "<='" + Util.dateFormat.format(toDate.getTime())
                + "' ORDER BY " + BEGIN_SHIFT + " " + sortMethod;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Shift shift = new Shift(cursor);
                if (taxoparkID == 0 || checkTaxoparkInShift(shift, taxoparkID)) {
                    shiftsList.add(shift);
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(Constants.LOG_TAG, "shiftsList.size(): " + String.valueOf(shiftsList.size()));
        return shiftsList;
    }
    private boolean checkTaxoparkInShift(Shift shift, long taxoparkID){
        ArrayList<Order> orders = ordersSource.getOrdersList(shift.shiftID, taxoparkID);
        Log.d(Constants.LOG_TAG, "orders: " + String.valueOf(orders.size()));
        boolean hasOrdersWithTargetTaxopark = false;
        for (Order order: orders) {
            if (order.taxoparkID == taxoparkID) hasOrdersWithTargetTaxopark = true;
        }
        return hasOrdersWithTargetTaxopark;
    }

    public Cursor getShiftCursor(){
        Cursor cursor = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.query(TABLE_NAME, new String[]{BaseColumns._ID, BEGIN_SHIFT}, null, null, null, null, null);
        return cursor;
    }

    public Shift getShiftByItsStart(String shiftStart){
        Shift shift = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, BEGIN_SHIFT + "=?", new String[]{shiftStart}, null, null, null);
        if (cursor.moveToFirst()) {
            shift = new Shift(cursor);
        }
        cursor.close();
        return shift;
    }

    public Shift getFirstShift() {
        Shift shift = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + BEGIN_SHIFT + " ASC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            shift = new Shift(cursor);
        }
        cursor.close();
        return shift;
    }

    public Shift getLastShift() {
        Shift shift = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + BEGIN_SHIFT + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            shift = new Shift(cursor);
        }
        cursor.close();
        return shift;
    }

    public Shift getShiftByID(long shiftID) {
        Shift shift = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BaseColumns._ID + "='" + String.valueOf(shiftID) + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            shift = new Shift(cursor);
        }
        cursor.close();
        return shift;
    }
}
