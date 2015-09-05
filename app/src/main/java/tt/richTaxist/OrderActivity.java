package tt.richTaxist;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import tt.richTaxist.gps.Coordinates;
import tt.richTaxist.gps.GPSHelper;
import tt.richTaxist.gps.GPSService;

/**
 * Created by AlexShredder on 07.07.2015.
 */
public class OrderActivity extends AppCompatActivity{
    private static final String LOG_TAG = "Order activity";
    static Context context;
    private int distance;
    private long travelTime;
    private Date startTime;
    private List<Coordinates> coordinatesList;

    UpdateTimeTask updateTimeTask;
    TextView travelTimeTextView;
    TextView distanceTextView;

    ServiceConnection serviceConnection;
    GPSService gpsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();
        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_order));

        distance = 0;
        travelTime = 0;
        coordinatesList = new ArrayList<>();
        Log.d(LOG_TAG, "onCreate");

        try {
            //stopService(new Intent(this,GPSService.class));
        } catch (Exception e) {
            Log.d(LOG_TAG, "Ошибка остановки сервиса");
        }

       Intent intent = new Intent(OrderActivity.this, GPSService.class);//.putExtra(GPSHelper.PARAM_PINTENT, pi);
        // стартуем сервис
        //startService(intent);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                gpsService = ((GPSService.MyBinder) service).getService();
                // Создаем PendingIntent
                PendingIntent pi = createPendingResult(GPSHelper.GPS_REQUEST_FROM_ORDER, getIntent(), 0);
                gpsService.setPendingIntent(pi);
                gpsService.restart();
                Log.d(LOG_TAG,"Service connected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG,"Service disconnected");
            }
        };
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);


        startTime = new Date();
        travelTimeTextView = (TextView) findViewById(R.id.travelTimeTextView);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);

        Button buttonCloseOrder = (Button) findViewById(R.id.buttonCloseOrder);
        buttonCloseOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                Intent intent = new Intent();
                intent.putExtra(Order.PARAM_DISTANCE, distance);
                intent.putExtra(Order.PARAM_TRAVEL_TIME, travelTime);
                setResult(RESULT_OK, intent);//RESULT_CANCELED если закрываем аппаратным возвратом
                finish();
            }
        });

        Button buttonOpenMap = (Button) findViewById(R.id.buttonOpenMap);
        buttonOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                startActivity(new Intent(OrderActivity.this, GPSHelper.getLocActivityClass()));
            }
        });

        updateTimeTask = new UpdateTimeTask();
        updateTimeTask.execute();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "On destroy");

        try {
            unbindService(serviceConnection);
            serviceConnection = null;
            //stopService(new Intent(this,GPSService.class));
        } catch (Exception e) {
            Log.d(LOG_TAG, "Ошибка остановки сервиса");
        }
        if (updateTimeTask != null) updateTimeTask.cancel(true);

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "on activity result: " + requestCode + " " + resultCode);
        if (requestCode == GPSHelper.GPS_REQUEST_FROM_ORDER && resultCode == GPSHelper.PARAM_RETURN_DATA){
            distance   = data.getIntExtra(GPSHelper.PARAM_DISTANCE, 0);
            double lat = data.getDoubleExtra(GPSHelper.PARAM_LAT, 0);
            double lon = data.getDoubleExtra(GPSHelper.PARAM_LON, 0);
            coordinatesList.add(new Coordinates(lon,lat));
            distanceTextView.setText(String.format("%d km %d m", distance / 1000, distance % 1000));
        }
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "OnPause");
        super.onPause();
    }


    class UpdateTimeTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            travelTimeTextView.setText("---");
            Log.d(LOG_TAG, "UTT: Preexecute");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "UTT: doInBackground");
            try {
                while (true) {
                    TimeUnit.SECONDS.sleep(1);
                    publishProgress();
                    if (isCancelled()) break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Date endTime = new Date();
            Calendar diff = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            diff.setTimeInMillis(endTime.getTime() - startTime.getTime());
            travelTimeTextView.setText(getCoolTime(diff));
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(LOG_TAG, "UTT: onPostExecute");
            super.onPostExecute(result);
            travelTimeTextView.setText("stop travel");
        }

        @Override
        protected void onCancelled() {
            Log.d(LOG_TAG, "UTT: cancelled");
            super.onCancelled();
        }

        private String getCoolTime(Calendar time) {
            travelTime = time.getTimeInMillis();

            int day  = (time.get(Calendar.DAY_OF_YEAR) - 1) * 24;
            int hour   = time.get(Calendar.HOUR_OF_DAY);
            int minute = time.get(Calendar.MINUTE);
            int second = time.get(Calendar.SECOND);
            return String.format("%dд  %02d:%02d:%02d", day, hour, minute, second);
        }
    }
}
