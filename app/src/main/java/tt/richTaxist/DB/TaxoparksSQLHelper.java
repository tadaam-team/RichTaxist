package tt.richTaxist.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import tt.richTaxist.MainActivity;
import tt.richTaxist.Units.Taxopark;

/**
 * Created by Tau on 04.10.2015.
 */
public class TaxoparksSQLHelper extends SQLHelper {
    private static final String LOG_TAG = "TaxoparksSQLHelper";
    static final String TABLE_NAME = "taxoparks";
    static final String TAXOPARK_ID = "taxoparkID";
    static final String TAXOPARK_NAME = "taxoparkName";
    static final String IS_DEFAULT = "isDefault";
    static final String DEFAULT_BILLING = "defaultBilling";

    public static TaxoparksSQLHelper dbOpenHelper = new TaxoparksSQLHelper(MainActivity.context);

    static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
            + TAXOPARK_ID       + " INT, "
            + TAXOPARK_NAME     + " TEXT, "
            + IS_DEFAULT        + " INT, "
            + DEFAULT_BILLING   + " INT)";

    public TaxoparksSQLHelper(Context context)  {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public boolean create(Taxopark taxopark){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TAXOPARK_ID,     taxopark.taxoparkID);
        cv.put(TAXOPARK_NAME,   taxopark.taxoparkName);
        cv.put(IS_DEFAULT,      taxopark.isDefault ? 1 : 0);
        cv.put(DEFAULT_BILLING, taxopark.defaultBilling);

        long result = db.insert(TABLE_NAME, null, cv);
        db.close();
        return result != -1;
    }

    public boolean update(Taxopark taxopark){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TAXOPARK_ID,     taxopark.taxoparkID);
        cv.put(TAXOPARK_NAME,   taxopark.taxoparkName);
        cv.put(IS_DEFAULT,      taxopark.isDefault ? 1 : 0);
        cv.put(DEFAULT_BILLING, taxopark.defaultBilling);

        long result = db.update(TABLE_NAME, cv, TAXOPARK_ID + " = ?", new String[]{String.valueOf(taxopark.taxoparkID)});
        db.close();
        return result != -1;
    }

    public Taxopark getTaxoparkByID(int taxoparkID) {
        Taxopark taxopark = null;
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + TAXOPARK_ID + " = '" + String.valueOf(taxoparkID) + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) taxopark = loadTaxoparkFromCursor(cursor);
        return taxopark;
    }

    public Taxopark getLastTaxopark() {
        Taxopark taxopark = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY _id DESC LIMIT 1";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // в курсоре единственная запись и луп излишен
        if (cursor.moveToFirst()) {
            do taxopark = loadTaxoparkFromCursor(cursor);
            while (cursor.moveToNext());
        }
        return taxopark;
    }

    public ArrayList<Taxopark> getAllTaxoparks() {
        ArrayList<Taxopark> taxoparksList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do taxoparksList.add(loadTaxoparkFromCursor(cursor));
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taxoparksList;
    }

    private Taxopark loadTaxoparkFromCursor(Cursor cursor){
        int taxoparkID          = cursor.getInt(cursor.getColumnIndex(TAXOPARK_ID));
        Taxopark taxopark       = new Taxopark(taxoparkID, "", false, 0);
        taxopark.taxoparkName   = cursor.getString(cursor.getColumnIndex(TAXOPARK_NAME));
        taxopark.isDefault      = cursor.getInt(cursor.getColumnIndex(IS_DEFAULT)) == 1;
        taxopark.defaultBilling = cursor.getInt(cursor.getColumnIndex(DEFAULT_BILLING));
        return taxopark;
    }

    public int remove(Taxopark taxopark){
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_NAME, TAXOPARK_ID + " = ? ", new String[]{String.valueOf(taxopark.taxoparkID)});
        db.close();
        return result;
    }
}
