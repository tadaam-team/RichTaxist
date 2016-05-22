package tt.richTaxist.DB.Sources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;
import android.widget.Toast;
import java.util.ArrayList;
import tt.richTaxist.DB.MySQLHelper;
import tt.richTaxist.Units.Taxopark;
import static tt.richTaxist.DB.Tables.TaxoparksTable.*;
/**
 * Created by TAU on 04.05.2016.
 */
public class TaxoparksSource {
    //represents top level of abstraction from dataBase
    //all work with db layer must be done in this class
    //getReadableDatabase() and getWritableDatabase() must not be called outside this class
    private static final String ERROR_TOAST = "db access error";
    private MySQLHelper dbHelper;
    private Context context;

    public TaxoparksSource(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = MySQLHelper.getInstance(this.context);
    }

    public long create(Taxopark taxopark){
        long id = -1;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = getContentValues(taxopark);
            id = db.insert(TABLE_NAME, null, cv);
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public boolean update(Taxopark taxopark){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = getContentValues(taxopark);
        long result = db.update(TABLE_NAME, cv, BaseColumns._ID + " = ?", new String[]{String.valueOf(taxopark.taxoparkID)});
        return result != -1;
    }

    public int remove(Taxopark taxopark){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(TABLE_NAME, BaseColumns._ID + " = ? ", new String[]{String.valueOf(taxopark.taxoparkID)});
        return result;
    }

    public Taxopark getTaxoparkByID(long taxoparkID) {
        Taxopark taxopark = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BaseColumns._ID + " = '" + String.valueOf(taxoparkID) + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            taxopark = new Taxopark(cursor);
        }
        cursor.close();
        return taxopark;
    }

    public Taxopark getLastTaxopark() {
        Taxopark taxopark = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + BaseColumns._ID + " DESC LIMIT 1";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            taxopark = new Taxopark(cursor);
        }
        cursor.close();
        return taxopark;
    }

    public Taxopark getDefaultTaxopark() {
        Taxopark taxopark = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + IS_DEFAULT + " = '" + String.valueOf(1) + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            taxopark = new Taxopark(cursor);
        }
        cursor.close();
        return taxopark;
    }

    public ArrayList<Taxopark> getAllTaxoparks() {
        ArrayList<Taxopark> taxoparksList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                taxoparksList.add(new Taxopark(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taxoparksList;
    }
}
