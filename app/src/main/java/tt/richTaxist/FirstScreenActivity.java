package tt.richTaxist;

import android.app.Notification;
import android.app.PendingIntent;
import android.net.Uri;
import android.os.Build;
import android.app.NotificationManager;
import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
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
import tt.richTaxist.ChatClient.ChatLoginActivity;
import tt.richTaxist.DB.ShiftsStorage;
import tt.richTaxist.Enums.ActivityState;
import tt.richTaxist.gps.GPSHelper;
import tt.richTaxist.gps.RouteActivity;

public class FirstScreenActivity extends AppCompatActivity implements
        FirstScreenFragment.OnFirstScreenFragmentInteractionListener{
    static AppCompatActivity activity;
    static Context context;
    private static final String LOG_TAG = "FirstScreenActivity";
    public static ArrayAdapter shiftAdapterMA;
    private FragmentManager fragmentManager;
    private FirstScreenFragment fragment1;
    private ShiftsListFragment fragment2;
    private ActivityState activityState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
        activity = FirstScreenActivity.this;
        context = getApplicationContext();
//        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.fragment1));
        //TODO: выяснить, зачем такое назначение ссылки
        MainActivity.context = context;

        GPSHelper.startService(MainActivity.context);

        MainActivity.shiftsStorage.clear();
        MainActivity.shiftsStorage.addAll(ShiftsStorage.getShiftsForList());
        Storage.init(this);
        Storage.deviceIsInLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        try {
            Parse.initialize(this, "PF47pDUAtRLyPmuFEh607NmCOA4NxMHKAODTsAqy", "kax79lUpsVC0S3BN0rBPqvvkqPce4rVtBvNy8d0D");
            ParseAnalytics.trackAppOpened(getIntent());
            ParseUser.enableAutomaticUser();
        }catch (RuntimeException e){Log.d(LOG_TAG, "Parse already launched");}

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Storage.deviceIMEI = tm.getDeviceId();
        Log.d(LOG_TAG, "IMEI: " + Storage.deviceIMEI);

        if (Storage.currentUser == null) {
            // Отправляем данные на Parse.com для проверки только если юзер еще не авторизован
            Log.d(LOG_TAG, "username: " + Storage.username + "password: " + Storage.password);
            authorize(this);
        }

        //фрагментная логика
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragment2 = new ShiftsListFragment();
            fragment1 = new FirstScreenFragment();
            FragmentTransaction transactionInitial = fragmentManager.beginTransaction();
            transactionInitial.add(R.id.container_first_screen, fragment2, "fragment2");
            transactionInitial.add(R.id.container_first_screen, fragment1, "fragment1");
            transactionInitial.commit();
//            fragmentManager.executePendingTransactions();
        }
        else {
            int activityStateID = savedInstanceState.getInt("activityState", ActivityState.LAND_2_1.id);
            activityState = ActivityState.getById(activityStateID);
            fragment1 = (FirstScreenFragment) fragmentManager.findFragmentByTag("fragment1");
            fragment2 = (ShiftsListFragment) fragmentManager.findFragmentByTag("fragment2");
        }
        activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
    }


    @Override
    public void onButtonSelected(int buttonIndex){
        switch (buttonIndex){
            case R.id.btnOpenLastShift:
                if (ShiftsStorage.getLastShift() == null)
                    Toast.makeText(activity, "сохраненных смен не найдено", Toast.LENGTH_SHORT).show();
                else {
                    MainActivity.currentShift = ShiftsStorage.getLastShift();
                    MainActivity.ordersStorage.fillOrdersByShift(MainActivity.currentShift);
                    Intent intent = new Intent(activity, ShiftTotalsActivity.class);
                    intent.putExtra("author", "FirstScreenActivity");
                    startActivity(intent);
                    Log.d(LOG_TAG, "открываю последнюю сохраненную смену");
                    finish();
                }
                break;

            case R.id.btnNewShift:
                MainActivity.currentShift = new Shift();
                MainActivity.ordersStorage.clear(false);
                startActivity(new Intent(activity, MainActivity.class));
                Log.d(LOG_TAG, "открываю новую смену");
                finish();
                break;

            case R.id.btnOpenShift:
                //Обработчик нажатия кнопки "Список смен"
                if (ShiftsStorage.getLastShift() == null)
                    Toast.makeText(activity, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                else {
                    if (Storage.deviceIsInLandscape) activityState = ActivityState.LAND_2;
                    else activityState = ActivityState.PORT_2;
                    activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
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

            case R.id.btnChat:
                startActivity(new Intent(activity, ChatLoginActivity.class));
                Log.d(LOG_TAG, "открываю чат");
                break;

            case R.id.btnRoute:
                if (MainActivity.currentShift == null){
                    MainActivity.currentShift = ShiftsStorage.getLastShift();
                    if (MainActivity.currentShift == null) {
                        Toast.makeText(activity, "сохраненных смен не найдено", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MainActivity.ordersStorage.fillOrdersByShift(MainActivity.currentShift);
                }
                startActivity(new Intent(activity, RouteActivity.class));
                Log.d(LOG_TAG, "открываю карту маршрута смены");
                break;

            case R.id.btnGrandTotals:
                startActivity(new Intent(activity, GrandTotalsActivity.class));
                Log.d(LOG_TAG, "открываю итоги по зарплате");
                break;

            case R.id.btnExit:
                onBackPressed();
                break;

            default:
                Toast.makeText(getApplicationContext(), "кнопка не определена", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("activityState", activityState.id);
    }

    //пока не могу найти способ не задваивать метод авторизации
    //если выносить его в сторож, происходит рассинхронизация обработки результатов метода done,
    //что приводит к не штатному вызову SignInActivity.showLogInORLogOut()
    public void authorize(final Context context){
        // Отправляем данные на Parse.com для проверки
        LogInCallback logInCallback = new LogInCallback() {
            public void done(ParseUser user, ParseException error) {
                if (user != null) {
                    Storage.userHasAccess = verifyUser(user);
                } else {
                    Log.d(LOG_TAG, "error code " + error.getCode());//всегда 101
                    Toast.makeText(context, "Ошибка логина или пароля", Toast.LENGTH_SHORT).show();
                }
            }
        };
        if (Storage.username != null && !Storage.username.equals(""))
            ParseUser.logInInBackground(Storage.username, Storage.password, logInCallback);
    }


    private boolean verifyUser(ParseUser user) {
        //пользователь авторизован. загрузим его сохраненные настройки из облака
        Log.d(LOG_TAG, "user logged in");
        Storage.currentUser     = user;
        Storage.premiumUser     = user.getBoolean("premiumUser");
        Storage.emailVerified   = user.getBoolean("emailVerified");
        Storage.showListHint    = user.getBoolean("showListHint");
        Storage.youngIsOnTop    = user.getBoolean("youngIsOnTop");
        Storage.twoTapTimePick  = user.getBoolean("twoTapTimePick");
        Storage.hideTaxometer   = user.getBoolean("hideTaxometer");

        //TODO: если письмо с подтверждением не пришло, то оно не может быть запрошено повторно, т.к. юзер уже в базе
        if (!Storage.emailVerified) {
            //для бесплатного доступа вообще то необязательно подтверждать почту или сверять IMEI,
            // но для дисциплины напомним юзеру, что надо подтвердить
            Toast.makeText(context, "Здравствуйте, " + user.getUsername() +
                    "\nВаш email еще не подтвержден", Toast.LENGTH_LONG).show();
            return false;
        }
        Log.d(LOG_TAG, "email verified");
        //пользователь авторизован и почта подтверждена--------------------------------------------


        if (!Storage.premiumUser){
            //если премиум доступа нет, то и нет смысла проверять IMEI
            Toast.makeText(context, "Здравствуйте, " + user.getUsername() +
                    "\nПриятной работы", Toast.LENGTH_LONG).show();
            return false;
        }
        Log.d(LOG_TAG, "user is premium");
        //пользователь авторизован, почта подтверждена и есть подписка-----------------------------


        if (user.getString("IMEI") == null || user.getString("IMEI").equals("detached")) {
            //IMEI еще не привязан или отвязан по запросу
            Storage.currentUser.put("IMEI", Storage.deviceIMEI);
            Storage.currentUser.saveInBackground();
            Toast.makeText(context, "Вы привязали это устройство" +
                    "\nк своей учетной записи." +
                    "\nПриятной работы", Toast.LENGTH_LONG).show();

            Log.d(LOG_TAG, "IMEI saved");
            return true;
        }
        Log.d(LOG_TAG, "IMEI not null");
        //пользователь авторизован, почта подтверждена, есть подписка и IMEI не пустой-------------


        if (!Storage.deviceIMEI.equals(user.getString("IMEI"))) {
            String header = "Здравствуйте, " + user.getUsername();
            String msg = "Вы вошли в систему с другого устройства." +
                    "\nСейчас платные опции недоступны." +
                    "\nЧтобы привязать логин к новому устройству" +
                    "\nперейдите в меню \"Учетные записи\"" +
                    "\nи нажмите \"Сменить устройство\"";

            //для новых апи показываем длинное сообщение в виде уведомления, для старых апи показываем тост
            if (Build.VERSION.SDK_INT >= 16){
                Intent intent = new Intent(context, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

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
            } else
                Toast.makeText(getApplicationContext(), header + msg, Toast.LENGTH_LONG).show();

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
        switch (activityState){
            case LAND_2:
                activityState = ActivityState.LAND_2_1;
                activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
                break;
            case PORT_2:
                activityState = ActivityState.PORT_1;
                activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
                break;
            default:
                Storage.openQuitDialog(this);
        }
    }
}
