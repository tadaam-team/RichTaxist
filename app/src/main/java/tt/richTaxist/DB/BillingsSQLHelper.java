package tt.richTaxist.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import tt.richTaxist.MainActivity;
import tt.richTaxist.Units.Billing;

/**
 * Created by Tau on 04.10.2015.
 */
public class BillingsSQLHelper extends SQLHelper {
    private static final String LOG_TAG = "BillingsSQLHelper";
    static final String TABLE_NAME = "billings";
    static final String BILLING_ID = "billingID";
    static final String BILLING_NAME = "billingName";
    static final String COMMISSION = "commission";

    public static BillingsSQLHelper dbOpenHelper = new BillingsSQLHelper(MainActivity.context);

    static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
            + BILLING_ID       + " INT, "
            + BILLING_NAME     + " TEXT, "
            + COMMISSION        + " FLOAT)";

    public BillingsSQLHelper(Context context)  {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public boolean create(Billing billing){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(BILLING_ID,      billing.billingID);
        cv.put(BILLING_NAME,    billing.billingName);
        cv.put(COMMISSION,      billing.commission);

        long result = db.insert(TABLE_NAME, null, cv);
        db.close();
        return result != -1;
    }

    public boolean update(Billing billing){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(BILLING_ID,      billing.billingID);
        cv.put(BILLING_NAME,    billing.billingName);
        cv.put(COMMISSION,      billing.commission);

        long result = db.update(TABLE_NAME, cv, BILLING_ID + " = ?", new String[]{String.valueOf(billing.billingID)});
        db.close();
        return result != -1;
    }

    public Billing getBillingByID(int billingID) {
        Billing billing = null;
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BILLING_ID + " = '" + String.valueOf(billingID) + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) billing = loadBillingFromCursor(cursor);
        return billing;
    }

    public Billing getLastBilling() {
        Billing billing = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY _id DESC LIMIT 1";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // в курсоре единственная запись и луп излишен
        if (cursor.moveToFirst()) {
            do billing = loadBillingFromCursor(cursor);
            while (cursor.moveToNext());
        }
        return billing;
    }

    public ArrayList<Billing> getAllBillings() {
        ArrayList<Billing> billingsList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do billingsList.add(loadBillingFromCursor(cursor));
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return billingsList;
    }

    private Billing loadBillingFromCursor(Cursor cursor){
        int billingID           = cursor.getInt(cursor.getColumnIndex(BILLING_ID));
        Billing billing         = new Billing(billingID);
        billing.billingName     = cursor.getString(cursor.getColumnIndex(BILLING_NAME));
        billing.commission      = cursor.getInt(cursor.getColumnIndex(COMMISSION));
        return billing;
    }

    public int remove(Billing billing){
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_NAME, BILLING_ID + " = ? ", new String[]{String.valueOf(billing.billingID)});
        db.close();
        return result;
    }
}
