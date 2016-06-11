package tt.richCabman.database.sources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;
import tt.richCabman.database.MySQLHelper;
import tt.richCabman.model.Shift;
import tt.richCabman.util.Util;
import tt.richCabman.model.Coordinates;
import static tt.richCabman.database.tables.LocationsTable.*;
/**
 * Created by TAU on 04.05.2016.
 */
public class LocationsSource {
    //represents top level of abstraction from dataBase
    //all work with db layer must be done in this class
    //getReadableDatabase() and getWritableDatabase() must not be called outside this class
    private static final String ERROR_TOAST = "db access error";
    private MySQLHelper dbHelper;
    private Context context;

    public LocationsSource(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = MySQLHelper.getInstance(this.context);
    }

    public long create(Coordinates coordinates) {
        long id = -1;
        try {
            if (coordinates.getLat() == 0d || coordinates.getLon() == 0) {
                return 0;
            }
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = getContentValues(coordinates);
            id = db.insert(TABLE_NAME, null, cv);
        } catch (SQLiteException e) {
            Toast.makeText(context, ERROR_TOAST, Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public ArrayList<Coordinates> getLocationsByShift(Shift shift) {
        Date fromTime = shift.beginShift;
        Date toTime = shift.endShift;
        if (toTime == null) {
            toTime = new Date();
        }

        ArrayList<Coordinates> coordinates = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "
                + DATE_TIME + ">='" + Util.dateFormat.format(fromTime) + "' AND "
                + DATE_TIME + "<='" + Util.dateFormat.format(toTime) + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do coordinates.add(loadLocationFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return coordinates;
    }

    public ArrayList<Coordinates> getLocationsByPeriod(Date fromTime, Date toTime) {
        ArrayList<Coordinates> coordinates = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "
                + DATE_TIME + ">='" + Util.dateFormat.format(fromTime) + "' AND "
                + DATE_TIME + "<='" + Util.dateFormat.format(toTime) + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
        return new Coordinates(lon, lat);
    }
}
