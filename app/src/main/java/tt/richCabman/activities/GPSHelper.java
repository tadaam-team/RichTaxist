package tt.richCabman.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import tt.richCabman.interfaces.MapProvider;
import tt.richCabman.services.GPSService;
import tt.richCabman.fragments.MapPathFragment;
import tt.richCabman.model.Coordinates;

/**
 * Created by AlexShredder on 06.07.2015.
 */
public class GPSHelper extends AppCompatActivity {

    public static final int GPS_REQUEST_FROM_ORDER = 16549196;
    public static final String PARAM_PINTENT = "pendingIntenet";
    public static final String PARAM_DISTANCE = "distance";
    public static final String PARAM_LON = "lon";
    public static final String PARAM_LAT = "lat";
    public static final int PARAM_DATA = 11;
    public static final int PARAM_RETURN_DATA = 100;
    public static final int LOCATION_UPDATE = 10;
    public static boolean serviceStarted = false;
    private static MapProvider mapProvider = new GoogleMaps();

    private GPSHelper() { }

    public static void setMapProvider(MapProvider mapProvider) {
        GPSHelper.mapProvider = mapProvider;
    }

    public static MapProvider getMapProvider() {
        return mapProvider;
    }

    public static Coordinates getCoordinates(){
        return new Coordinates(30,50);
    }

    public static Class getLocActivityClass(){
        return mapProvider.getLocActivityClass();
    }

    public static Fragment getMapFragment(){
        return new MapPathFragment();
    }

    public static void startService(Context context) {
       if (!serviceStarted) {
           Intent intent = new Intent(context, GPSService.class);
           context.startService(intent);
           serviceStarted = true;
       }
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, GPSService.class);
        context.stopService(intent);
    }
}
