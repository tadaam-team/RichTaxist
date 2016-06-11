package tt.richCabman.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import tt.richCabman.activities.GPSHelper;
import tt.richCabman.database.DataSource;
import tt.richCabman.model.Coordinates;
import tt.richCabman.util.Constants;

public class GPSService extends Service {
    static PendingIntent pi;
    MyBinder binder = new MyBinder();
    Thread gpsTracking;
    private int distance;
    private Location lastLocation;
    private DataSource dataSource;

    public GPSService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataSource = new DataSource(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gpsTracking = new Thread(new ServiceThread());
        gpsTracking.run();
        return super.onStartCommand(intent, flags, startId);
    }

    public void restart() {
        lastLocation = null;
        distance = 0;
    }

    private class ServiceThread implements Runnable {
        private LocationManager locationManager;


        //private PendingIntent pi;

        public ServiceThread() {
            //this.pi = pi;
        }

        @Override
        protected void finalize() throws Throwable {
            Log.d(Constants.LOG_TAG, "on finalize Service thread");
            locationManager.removeUpdates(locationListener);
            super.finalize();
        }

        private void sendLocation(){

            if (lastLocation == null){
                Log.d(Constants.LOG_TAG,"curLoc = null");
                return;
            }

            if (pi != null) {
                Log.d(Constants.LOG_TAG, "create intent 4 sending");
                Intent intent = new Intent();
                intent.putExtra(GPSHelper.PARAM_DISTANCE, distance);
                intent.putExtra(GPSHelper.PARAM_LAT, lastLocation.getLatitude());
                intent.putExtra(GPSHelper.PARAM_LON, lastLocation.getLongitude());

                Log.d(Constants.LOG_TAG, "sending intent");
                try {
                    pi.send(GPSService.this, GPSHelper.PARAM_RETURN_DATA, intent);
                } catch (PendingIntent.CanceledException e) {
                    Log.d(Constants.LOG_TAG, e.getMessage());
                }
            }else{
                Log.d(Constants.LOG_TAG, "pi == null");
            }
            try {
                dataSource.getLocationsSource().create(new Coordinates(lastLocation.getLongitude(), lastLocation.getLatitude()));
            } catch (Exception e) {
                e.printStackTrace();

                Log.d(Constants.LOG_TAG, "Error by store locations, database not ready");
                stopSelf();
            }
        }
        @Override
        public void run() {
            Toast.makeText(GPSService.this, "gps servive: run", Toast.LENGTH_SHORT).show();
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 1, 10, locationListener);
            distance = 0;
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            sendLocation();
//            while (true){
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                    Log.d(Constants.LOG_TAG, "tiktak");
//                } catch (InterruptedException e) {
//                    break;
//                }
//            }
        }
        private LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d(Constants.LOG_TAG, "on loc change");
                if(lastLocation == null) lastLocation = location;
                float[] result = new float[1];
                Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                        location.getLatitude(), location.getLongitude(), result);
                lastLocation = location;
                distance += result[0];

                sendLocation();
                //Toast.makeText(GPSService.this, "gps servive: change location", Toast.LENGTH_SHORT).show();

                //distanceTextView.setText(String.format("%d km %d m",distance/1000,distance % 1000));
                //Toast.makeText(TaximeterActivity.this, "gps status: changed location", Toast.LENGTH_SHORT);
            }

            @Override
            public void onProviderDisabled(String provider) {
                //checkEnabled();
                Toast.makeText(GPSService.this,"gps status: down",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                //checkEnabled();
                //showLocation(locationManager.getLastKnownLocation(provider));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

                String caption = "???";
                switch (status){
                    case 0: caption = "out of service"; break;
                    case 1: caption = "temporarily_unavailable"; break;
                    case 2: caption = "ok"; break;
                }
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    Log.d(Constants.LOG_TAG, "gps status: "+ caption);
                    //Toast.makeText(GPSService.this, "gps status: "+ caption, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {

        //pi = null;
        //try {
        //    pi = intent.getParcelableExtra(GPSHelper.PARAM_PINTENT);
        //} catch (Exception e) {
        //    Log.d(Constants.LOG_TAG, "Ошибка при bind сервиса");
        //    stopSelf();

       // }

        Log.d(Constants.LOG_TAG, "on bind");
        return binder;

    }

    public void setPendingIntent(PendingIntent pendingIntent){
        pi = pendingIntent;
        Log.d(Constants.LOG_TAG, "set pi to "+pi);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(Constants.LOG_TAG, "on REbind");
        super.onRebind(intent);
    }

    public class MyBinder extends Binder{
        public GPSService getService(){
            return GPSService.this;
        }
    }



    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Constants.LOG_TAG, "on UNbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){
        Log.d(Constants.LOG_TAG, "on destroy");
        stopSelf();
        super.onDestroy();
    }
}
