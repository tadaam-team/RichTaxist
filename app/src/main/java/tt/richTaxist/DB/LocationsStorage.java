package tt.richTaxist.DB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tt.richTaxist.DB.sql.LocationsSqlHelper;
import tt.richTaxist.MainActivity;
import tt.richTaxist.Shift;
import tt.richTaxist.gps.Coordinates;

/**
 * Created by AlexShredder on 16.07.2015.
 */
public class LocationsStorage {

    private static LocationsSqlHelper dbOpenHelper = new LocationsSqlHelper(MainActivity.context);
    private LocationsStorage() {
    }

    private static LocationsSqlHelper getDatabase() {
        return dbOpenHelper;
    }

    public static ArrayList<Coordinates> getLocationsByShift(Shift shift) {
        return getDatabase().getLocationsByShift(shift);
    }

    public static boolean storeLocation(Coordinates coordinates){
        return getDatabase().storeLocation(coordinates);
    }

    public static List<Coordinates> getLocationsByPeriod(Date dateFrom, Date dateTo) {
        return getDatabase().getLocationsByPeriod(dateFrom, dateTo);
    }
}
