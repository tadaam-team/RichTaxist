package tt.richCabman.database.sources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import tt.richCabman.database.MySQLHelper;
import tt.richCabman.database.tables.BillingsTable;
import tt.richCabman.model.Billing;
import static tt.richCabman.database.tables.BillingsTable.*;
/**
 * Created by TAU on 29.04.2016.
 */
public class BillingsSource {
    //represents top level of abstraction from dataBase
    //all work with db layer must be done in this class
    //getReadableDatabase() and dbHelper.getWritableDatabase() must not be called outside this class
    private static final String ERROR_TOAST = "db access error";
    private MySQLHelper dbHelper;
    private Context context;

    public BillingsSource(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = MySQLHelper.getInstance(this.context);
    }

    public long create(Billing billing){
        long id = -1;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = BillingsTable.getContentValues(billing);
            id = db.insert(TABLE_NAME, null, cv);
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public boolean update(Billing billing){
        int result = -1;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = getContentValues(billing);
            result = db.update(BillingsTable.TABLE_NAME, cv, BaseColumns._ID + " = ?",
                    new String[]{String.valueOf(billing.billingID)});
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return result != -1;
    }

    public int remove(Billing billing){
        int result = 0;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            result = db.delete(BillingsTable.TABLE_NAME, BaseColumns._ID + " = ? ",
                    new String[]{String.valueOf(billing.billingID)});
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public Billing getBillingByID(long billingID) {
        Billing billing = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + BillingsTable.TABLE_NAME + " WHERE "
                    + BaseColumns._ID + " = '" + String.valueOf(billingID) + "'";
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                billing = new Billing(cursor);
            }
            cursor.close();
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return billing;
    }

    public ArrayList<Billing> getAllBillings() {
        ArrayList<Billing> billingsList = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + BillingsTable.TABLE_NAME;
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    billingsList.add(new Billing(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return billingsList;
    }


    public class GetBillingsListEvent {
        public final List<Billing> list;
        public GetBillingsListEvent(List<Billing> list) {
            this.list = list;
        }
    }
}
