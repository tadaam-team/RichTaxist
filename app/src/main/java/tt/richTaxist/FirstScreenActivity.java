package tt.richTaxist;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.net.Uri;
import android.app.NotificationManager;
import android.content.res.Configuration;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.Fragments.ShiftsListFragment;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Enums.InputStyle;
import tt.richTaxist.Fragments.FirstScreenFragment;
import tt.richTaxist.gps.RouteActivity;

public class FirstScreenActivity extends AppCompatActivity implements
        FirstScreenFragment.FirstScreenInterface {
    static AppCompatActivity activity;
    static Context context;
    public static final String LOG_TAG = "MY_LOG";
    public static ArrayAdapter shiftAdapterMA;
    private boolean deviceIsInLandscape;
    private ShiftsSource shiftsSource;
    private OrdersSource ordersSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
        activity = FirstScreenActivity.this;
        context = getApplicationContext();
//        Util.measureScreenWidth(context, (ViewGroup) findViewById(R.id.container_first_screen));
        MainActivity.context = context;

//        GPSHelper.startService(MainActivity.context);

        shiftsSource = new ShiftsSource(getApplicationContext());
        ordersSource = new OrdersSource(getApplicationContext());
        MainActivity.currentShift = shiftsSource.getLastShift();
        if (MainActivity.currentShift != null) {
            MainActivity.shiftsStorage.clear();
            MainActivity.shiftsStorage.addAll(shiftsSource.getAllShifts(Util.youngIsOnTop));

            MainActivity.ordersStorage.clear();
            MainActivity.ordersStorage.addAll(ordersSource.getOrdersList(MainActivity.currentShift.shiftID, 0));
        }
        Util.init(this);
        Util.deviceIsInLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        try {
            Parse.initialize(this, "PF47pDUAtRLyPmuFEh607NmCOA4NxMHKAODTsAqy", "kax79lUpsVC0S3BN0rBPqvvkqPce4rVtBvNy8d0D");
            ParseAnalytics.trackAppOpened(getIntent());
            ParseUser.enableAutomaticUser();
        }catch (RuntimeException e){Log.d(LOG_TAG, "Parse already launched");}

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Util.deviceIMEI = tm.getDeviceId();
//        Log.d(LOG_TAG, "IMEI: " + Util.deviceIMEI);

        if (Util.currentUser == null) {
            // Отправляем данные на Parse.com для проверки только если юзер еще не авторизован
            Log.d(LOG_TAG, "username: " + Util.username + "password: " + Util.password);
            authorize(this);
        }

        //фрагментная логика
        deviceIsInLandscape = (findViewById(R.id.container_shifts_list) != null);
        if (deviceIsInLandscape){
            //if deviceIsInLandscape then FirstScreenFragment is statically added
            addShiftsListFragment(false);
        } else {
            addFirstScreenFragment();
        }
    }

    private void addShiftsListFragment(boolean isListSingleVisible){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ShiftsListFragment fragment = new ShiftsListFragment();
        fragment.setSoloView(isListSingleVisible);
        if (isListSingleVisible){
            ft.replace(R.id.container_first_screen, fragment);
            ft.addToBackStack("OrdersListFragmentTransaction");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        } else {
            ft.replace(R.id.container_shifts_list, fragment);
        }
        ft.commit();
    }

    private void addFirstScreenFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FirstScreenFragment fragment = new FirstScreenFragment();
        ft.replace(R.id.container_first_screen, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: this should be done in callback handler from settings action
        if (deviceIsInLandscape) {
            addShiftsListFragment(false);
        }
    }

    @Override
    public void onButtonSelected(int buttonIndex){
        switch (buttonIndex){
            case R.id.btnOpenLastShift:
                if (MainActivity.currentShift != null){
                    MainActivity.ordersStorage.clear();
                    MainActivity.ordersStorage.addAll(ordersSource.getOrdersList(MainActivity.currentShift.shiftID, 0));
                    Intent intent = new Intent(activity, ShiftTotalsActivity.class);
                    intent.putExtra("author", "FirstScreenActivity");
                    startActivity(intent);
                    Log.d(LOG_TAG, "открываю последнюю сохранённую смену");
                    finish();
                }
                else
                    Toast.makeText(activity, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnNewShift:
                Shift shift = new Shift();
                shift.shiftID = shiftsSource.create(shift);
                MainActivity.currentShift = shift;
                MainActivity.ordersStorage.clear();
                startActivity(new Intent(activity, MainActivity.class));
                Log.d(LOG_TAG, "открываю новую смену");
                finish();
                break;

            case R.id.btnOpenShift:
                //Обработчик нажатия кнопки "Список смен"
                if (!deviceIsInLandscape) {
                    if (MainActivity.currentShift != null){
                        addShiftsListFragment(true);
                    } else {
                        Toast.makeText(activity, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, R.string.allowedOnlyInPortraitMSG, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnSettings:
                startActivity(new Intent(activity, SettingsActivity.class));
                Log.d(LOG_TAG, "открываю настройки");
                break;

            case R.id.btnSignIn:
                startActivity(new Intent(activity, SignInActivity.class));
                Log.d(LOG_TAG, "открываю экран учетных записей");
                break;

            case R.id.btnRoute:
                if (MainActivity.currentShift != null) {
                    MainActivity.ordersStorage.clear();
                    MainActivity.ordersStorage.addAll(ordersSource.getOrdersList(MainActivity.currentShift.shiftID, 0));
                    startActivity(new Intent(activity, RouteActivity.class));
                    Log.d(LOG_TAG, "открываю карту маршрута смены");
                }
                else
                    Toast.makeText(activity, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnGrandTotals:
                if (MainActivity.currentShift != null){
                    Intent intent = new Intent(activity, GrandTotalsActivity.class);
                    intent.putExtra(GrandTotalsActivity.AUTHOR, "FirstScreenActivity");
                    startActivity(intent);
                    Log.d(LOG_TAG, "открываю итоги по зарплате");
                }
                else
                    Toast.makeText(activity, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnExit:
                onBackPressed();
                break;

            default:
                Toast.makeText(getApplicationContext(), "кнопка не определена", Toast.LENGTH_LONG).show();
                break;
        }
    }

    //пока не могу найти способ не задваивать метод авторизации
    //если выносить его в сторож, происходит рассинхронизация обработки результатов метода done,
    //что приводит к не штатному вызову SignInActivity.showLogInORLogOut()
    public void authorize(final Context context){
        // Отправляем данные на Parse.com для проверки
        LogInCallback logInCallback = new LogInCallback() {
            public void done(ParseUser user, ParseException error) {
                if (user != null) {
                    Util.userHasAccess = verifyUser(user);
                } else {
                    Log.d(LOG_TAG, "error code " + error.getCode());
                    String msg;
                    switch (error.getCode()){
                        case 100: msg = getResources().getString(R.string.noInternetMSG); break;
                        case 101: msg = getResources().getString(R.string.loginOrPasswordErrMSG); break;
                        default:  msg = getResources().getString(R.string.irregularErrMSG) + " " + error.getCode(); break;
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }
        };
        if (Util.username != null && !Util.username.equals(""))
            ParseUser.logInInBackground(Util.username, Util.password, logInCallback);
    }


    private boolean verifyUser(ParseUser user) {
        //пользователь авторизован. загрузим его сохраненные настройки из облака
        Log.d(LOG_TAG, "user logged in");

        Util.currentUser     = user;
        Util.premiumUser     = user.getBoolean("premiumUser");
        Util.emailVerified   = user.getBoolean("emailVerified");
        Util.showListHint    = user.getBoolean("showListHint");
        Util.youngIsOnTop    = user.getBoolean("youngIsOnTop");
        Util.twoTapTimePick  = user.getBoolean("twoTapTimePick");
        Util.hideTaxometer   = user.getBoolean("hideTaxometer");
        Util.inputStyle      = InputStyle.stringToInputStyle(user.getString("inputStyle"));

        //TODO: если письмо с подтверждением не пришло, то оно не может быть запрошено повторно, т.к. юзер уже в базе
        if (!Util.emailVerified) {
            //для бесплатного доступа вообще то необязательно подтверждать почту или сверять IMEI,
            // но для дисциплины напомним юзеру, что надо подтвердить
            Toast.makeText(context, "Здравствуйте, " + user.getUsername() +
                    "\nВаш email еще не подтвержден", Toast.LENGTH_LONG).show();
            return false;
        }
        Log.d(LOG_TAG, "email verified");
        //пользователь авторизован и почта подтверждена--------------------------------------------


        if (!Util.premiumUser){
            //если премиум доступа нет, то и нет смысла проверять IMEI
            Toast.makeText(context, "Здравствуйте, " + user.getUsername() +
                    "\nПриятной работы", Toast.LENGTH_LONG).show();
            return false;
        }
        Log.d(LOG_TAG, "user is premium");
        //пользователь авторизован, почта подтверждена и есть подписка-----------------------------


        if (user.getString("IMEI") == null || user.getString("IMEI").equals("detached")) {
            //IMEI еще не привязан или отвязан по запросу
            Util.currentUser.put("IMEI", Util.deviceIMEI);
            Util.currentUser.saveInBackground();
            Toast.makeText(context, "Вы привязали это устройство" +
                    "\nк своей учетной записи." +
                    "\nПриятной работы", Toast.LENGTH_LONG).show();

            Log.d(LOG_TAG, "IMEI saved");
            return true;
        }
        Log.d(LOG_TAG, "IMEI not null");
        //пользователь авторизован, почта подтверждена, есть подписка и IMEI не пустой-------------


        if (!Util.deviceIMEI.equals(user.getString("IMEI"))) {
            String header = "Здравствуйте, " + user.getUsername();
            String msg = "Вы вошли в систему с другого устройства." +
                    "\nСейчас платные опции недоступны." +
                    "\nЧтобы привязать логин к новому устройству" +
                    "\nперейдите в меню \"Учетные записи\"" +
                    "\nи нажмите \"Сменить устройство\"";

            //подготовим очередь возврата, используя уже существующий стек вызововов MainActivity и добавив к нему целевой интент
            Intent intent = new Intent(context, SignInActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(FirstScreenActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //подготовим уведомление, используя очередь возврата в pendingIntent
            Notification.Builder builder = new Notification.Builder(context)
                    .setTicker(header)
                    .setContentTitle(header)
                    .setContentText(msg)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_taxi);

            Notification notification = new Notification.BigTextStyle(builder).bigText(msg).build();
            notification.sound = Uri.parse("android.resource://tt.richTaxist/raw/notification");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);

            return false;
        }
        Log.d(LOG_TAG, "IMEI check passed");
        //пользователь авторизован, почта подтверждена, есть подписка, IMEI не пустой и проверен---


        Toast.makeText(context, "Здравствуйте, " + user.getUsername() + "\nПриятной работы", Toast.LENGTH_LONG).show();
        Log.d(LOG_TAG, "user has access!!");
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Util.openQuitDialog(this);
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
