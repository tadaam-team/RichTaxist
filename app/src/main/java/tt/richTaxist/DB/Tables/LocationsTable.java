package tt.richTaxist.DB.Tables;

import android.content.ContentValues;
import java.util.Date;
import tt.richTaxist.DB.MySQLHelper;
import tt.richTaxist.Util;
import tt.richTaxist.gps.Coordinates;

public class LocationsTable {
    public static final String TABLE_NAME = "Locations";

    public static final String DATE_TIME = "dateTime";
    public static final String LON = "lon";
    public static final String LAT = "lat";

    public static final String FIELDS = MySQLHelper.PRIMARY_KEY
            + DATE_TIME + " TEXT, "
            + LON + " REAL, "
            + LAT + " REAL";

    public LocationsTable() { } //table cannot be instantiated

    public static ContentValues getContentValues(Coordinates coordinates) {
        ContentValues cv = new ContentValues();
        cv.put(LON, coordinates.getLon());
        cv.put(LAT, coordinates.getLat());
        cv.put(DATE_TIME, Util.dateFormat.format(new Date()));
        return cv;
    }
}
