package tt.richTaxist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.Fragments.ShiftsListFragment;
import tt.richTaxist.SharedPreferences.SharedPrefEntry;
import tt.richTaxist.SharedPreferences.SharedPrefsHelper;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Fragments.FirstScreenFragment;
import tt.richTaxist.gps.GPSHelper;
import tt.richTaxist.gps.RouteActivity;

public class FirstScreenActivity extends AppCompatActivity implements
        FirstScreenFragment.FirstScreenInterface {
    static AppCompatActivity activity;
    static Context context;
    public static final String LOG_TAG = "MY_LOG";
    private boolean deviceIsInLandscape;
    private ShiftsSource shiftsSource;
    private OrdersSource ordersSource;
    private SharedPrefsHelper sharedPrefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
        activity = FirstScreenActivity.this;
        context = getApplicationContext();
//        Util.measureScreenWidth(context, (ViewGroup) findViewById(R.id.container_first_screen));
//        GPSHelper.startService(MainActivity.context);

        shiftsSource = new ShiftsSource(getApplicationContext());
        ordersSource = new OrdersSource(getApplicationContext());
        MainActivity.currentShift = shiftsSource.getLastShift();
        loadSharedPrefs();

        try {
            Parse.initialize(this, "PF47pDUAtRLyPmuFEh607NmCOA4NxMHKAODTsAqy", "kax79lUpsVC0S3BN0rBPqvvkqPce4rVtBvNy8d0D");
            ParseAnalytics.trackAppOpened(getIntent());
            ParseUser.enableAutomaticUser();
        } catch (RuntimeException e){
            Log.d(LOG_TAG, "Parse already launched");
        }

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Util.deviceIMEI = tm.getDeviceId();
//        Log.d(LOG_TAG, "IMEI: " + Util.deviceIMEI);

        if (Util.currentUser == null) {
            // Отправляем данные на Parse.com для проверки только если юзер еще не авторизован
            Log.d(LOG_TAG, "username: " + Util.username + "password: " + Util.password);
            authorize();
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

    private void loadSharedPrefs(){
        sharedPrefsHelper = new SharedPrefsHelper(PreferenceManager.getDefaultSharedPreferences(this));
        SharedPrefEntry sharedPrefEntry = sharedPrefsHelper.getPersonalInfo();
        Util.username = sharedPrefEntry.getName();
        Util.password = sharedPrefEntry.getPassword();
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
                GPSHelper.stopService(getApplicationContext());
                finish();
                break;

            default:
                Toast.makeText(getApplicationContext(), "кнопка не определена", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void authorize(){
        //опишем, что делать после того, как мы получим ответ от Parse.com
        LogInCallback logInCallback = new LogInCallback() {
            public void done(ParseUser user, ParseException error) {
                if (user != null) {
                    Util.userHasAccess = Util.verifyUser(user, getApplicationContext());
                    Util.saveSettingsToCloud();
                } else {
                    Log.d(LOG_TAG, "error code " + error.getCode());
                    String msg;
                    switch (error.getCode()){
                        case 100: msg = getResources().getString(R.string.noInternetMSG); break;
                        case 101: msg = getResources().getString(R.string.loginOrPasswordErrMSG); break;
                        default:  msg = getResources().getString(R.string.irregularErrMSG) + " " + error.getCode(); break;
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        };
        if (Util.username != null && !Util.username.equals("")) {
            // Отправляем данные на Parse.com для проверки
            ParseUser.logInInBackground(Util.username, Util.password, logInCallback);
        }
    }

    @Override
    protected void onDestroy() {
        Util.resetSettings();
        super.onDestroy();
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
