package tt.richTaxist.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Date;
import tt.richTaxist.MainActivity;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.gps.Coordinates;

/**
 * Created by AlexShredder on 16.07.2015.
 */
public class LocationsSQLHelper extends SQLHelper {
    static final String TABLE_NAME = "path";
    static final String DATE_TIME = "dateTime";
    static final String LON = "lon";
    static final String LAT = "lat";

    public static LocationsSQLHelper dbOpenHelper = new LocationsSQLHelper(MainActivity.context);

    static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
            + DATE_TIME + " DATETIME, "
            + LON + " FLOAT, "
            + LAT + " FLOAT)";

    public LocationsSQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public ArrayList<Coordinates> getLocationsByShift(Shift shift) {
        Date fromTime = shift.beginShift;
        Date toTime = shift.endShift;
        if (toTime == null) toTime = new Date();

        ArrayList<Coordinates> coordinates = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + DATE_TIME + ">='" + dateFormat.format(fromTime) + "' AND "
                + DATE_TIME + "<='" + dateFormat.format(toTime) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do coordinates.add(loadLocationFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return coordinates;
    }

    private Coordinates loadLocationFromCursor(Cursor cursor) {
        double lon = cursor.getDouble(cursor.getColumnIndex(LON));
        double lat = cursor.getDouble(cursor.getColumnIndex(LAT));
        return new Coordinates(lon,lat);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        super.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { super.onUpgrade(sqLiteDatabase, i, i1); }

    public boolean storeLocation(Coordinates coordinates) {
        if (coordinates.getLat() == 0d || coordinates.getLon() == 0) return true;
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(LON, coordinates.getLon());
        cv.put(LAT, coordinates.getLat());
        cv.put(DATE_TIME, dateFormat.format(new Date()));

        long result = db.insert(TABLE_NAME, null, cv);
        db.close();
        return result != -1;
    }

    public ArrayList<Coordinates> getLocationsByPeriod(Date fromTime, Date toTime) {
        ArrayList<Coordinates> coordinates = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + DATE_TIME + ">='" + dateFormat.format(fromTime) + "' AND "
                + DATE_TIME + "<='" + dateFormat.format(toTime) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do coordinates.add(loadLocationFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return coordinates;
    }
}
