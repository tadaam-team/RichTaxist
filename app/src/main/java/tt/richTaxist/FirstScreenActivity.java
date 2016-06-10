package tt.richTaxist;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import tt.richTaxist.DB.DataSource;
import tt.richTaxist.Fragments.ShiftsListFragment;
import tt.richTaxist.SharedPreferences.SharedPrefEntry;
import tt.richTaxist.SharedPreferences.SharedPrefsHelper;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Fragments.FirstScreenFragment;
import tt.richTaxist.gps.GPSHelper;
import tt.richTaxist.gps.RouteActivity;

public class FirstScreenActivity extends AppCompatActivity implements
        FirstScreenFragment.FirstScreenInterface,
        ShiftsListFragment.ShiftsListFragmentInterface{
    private DataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
//        GPSHelper.startService(MainActivity.context);

        dataSource = new DataSource(getApplicationContext());
        loadSharedPrefs();

        try {
            Parse.initialize(this, "PF47pDUAtRLyPmuFEh607NmCOA4NxMHKAODTsAqy", "kax79lUpsVC0S3BN0rBPqvvkqPce4rVtBvNy8d0D");
            ParseAnalytics.trackAppOpened(getIntent());
            ParseUser.enableAutomaticUser();
        } catch (RuntimeException e){
            Log.d(Constants.LOG_TAG, "Parse already launched");
        }

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Util.deviceIMEI = tm.getDeviceId();
//        Log.d(Constants.LOG_TAG, "IMEI: " + Util.deviceIMEI);

        if (Util.currentUser == null) {
            // Отправляем данные на Parse.com для проверки только если юзер еще не авторизован
            Log.d(Constants.LOG_TAG, "username: " + Util.username + ", password: " + Util.password);
            authorize();
        }

        //фрагментная логика
        if (getResources().getBoolean(R.bool.screenWiderThan450)){
            //if screenWiderThan450 then FirstScreenFragment is statically added
            addShiftsListFragment(false);
        } else {
            addFirstScreenFragment();
        }
    }

    private void loadSharedPrefs(){
        SharedPrefsHelper sharedPrefsHelper = new SharedPrefsHelper(PreferenceManager.getDefaultSharedPreferences(this));
        SharedPrefEntry sharedPrefEntry = sharedPrefsHelper.getPersonalInfo();
        Util.username = sharedPrefEntry.getName();
        Util.password = sharedPrefEntry.getPassword();
    }

    private void addShiftsListFragment(boolean isListSingleVisible){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ShiftsListFragment fragment = new ShiftsListFragment();
        fragment.setSoloView(isListSingleVisible);
        if (isListSingleVisible){
            ft.replace(R.id.container_first_screen, fragment, ShiftsListFragment.TAG);
            //TODO: эта запись в стэке заставляет пользователя второй раз нажимать назад
            // если из списка смен повернуть устройство в ландшафт и попытаться выйти
            ft.addToBackStack("ShiftsListFragmentTransaction");
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            ft.replace(R.id.container_shifts_list, fragment, ShiftsListFragment.TAG);
        }
        ft.commit();
    }

    private void addFirstScreenFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FirstScreenFragment fragment = new FirstScreenFragment();
        ft.replace(R.id.container_first_screen, fragment);
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: this should be done in callback handler from settings action
        if (getResources().getBoolean(R.bool.screenWiderThan450)) {
            addShiftsListFragment(false);
        }
    }

    @Override
    public void onButtonSelected(int buttonIndex){
        Shift lastShift = dataSource.getShiftsSource().getLastShift();
        switch (buttonIndex){
            case R.id.btnOpenLastShift:
                if (lastShift != null){
                    editShift(lastShift);
                } else {
                    Toast.makeText(this, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnNewShift:
                startActivity(new Intent(this, MainActivity.class));
                Log.d(Constants.LOG_TAG, "открываю новую смену");
                break;

            case R.id.btnOpenShiftsList:
                //Обработчик нажатия кнопки "Список смен"
                if (!getResources().getBoolean(R.bool.screenWiderThan450)) {
                    if (lastShift != null){
                        addShiftsListFragment(true);
                    } else {
                        Toast.makeText(this, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.allowedOnlyInPortraitMSG, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                Log.d(Constants.LOG_TAG, "открываю настройки");
                break;

            case R.id.btnSignIn:
                startActivity(new Intent(this, SignInActivity.class));
                Log.d(Constants.LOG_TAG, "открываю экран учетных записей");
                break;

            case R.id.btnRoute:
                //TODO: запуск RouteActivity нужно вынести в ShiftsListFragment чтобы маршрут был доступен для любой смены
                if (lastShift != null) {
                    Intent intent3 = new Intent(this, RouteActivity.class);
                    intent3.putExtra(Constants.SHIFT_ID_EXTRA, lastShift.shiftID);
                    startActivity(intent3);
                    Log.d(Constants.LOG_TAG, "открываю карту маршрута смены");
                } else {
                    Toast.makeText(this, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnGrandTotals:
                if (lastShift != null){
                    startActivity(new Intent(this, GrandTotalsActivity.class));
                    Log.d(Constants.LOG_TAG, "открываю итоги по зарплате");
                } else {
                    Toast.makeText(this, R.string.noShiftsMSG, Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public void requestDeleteShift (final Shift selectedShift, final int positionInRVList) {
        if (dataSource.getOrdersSource().getOrdersList(selectedShift.shiftID, 0).size() == 0) {
            deleteShift(selectedShift, positionInRVList);
        } else {
            openShiftDeleteDialog(selectedShift, positionInRVList);
        }
    }

    @Override
    public void editShift(Shift selectedShift) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.SHIFT_ID_EXTRA, selectedShift.shiftID);
        startActivity(intent);
    }

    private void openShiftDeleteDialog(final Shift shift, final int positionInRVList) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle(R.string.shiftNotEmptyMSG);
        quitDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataSource.getOrdersSource().deleteOrdersByShift(shift);
                deleteShift(shift, positionInRVList);
            }
        });
        quitDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {/*NOP*/}
        });
        quitDialog.show();
    }

    private void deleteShift(Shift shift, int positionInRVList){
        dataSource.getShiftsSource().remove(shift);
        ShiftsListFragment shiftsListFragment = (ShiftsListFragment) getSupportFragmentManager()
                .findFragmentByTag(ShiftsListFragment.TAG);
        shiftsListFragment.removeShiftFromList(shift, positionInRVList);
        Toast.makeText(this, R.string.shiftDeletedMSG, Toast.LENGTH_SHORT).show();
    }

    public void authorize(){
        //опишем, что делать после того, как мы получим ответ от Parse.com
        LogInCallback logInCallback = new LogInCallback() {
            public void done(ParseUser user, ParseException error) {
                if (user != null) {
                    Util.userHasAccess = Util.verifyUser(user, getApplicationContext());
                    Util.saveSettingsToCloud();
                } else {
                    Log.d(Constants.LOG_TAG, "error code " + error.getCode());
                    String msg;
                    switch (error.getCode()){
                        case 100: msg = getResources().getString(R.string.noInternetMSG); break;
                        case 101: msg = getResources().getString(R.string.usernameOrPasswordErrMSG); break;
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
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Util.openQuitDialog(this);
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
