package tt.richTaxist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import com.parse.ParseUser;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import tt.richTaxist.Enums.ActivityState;
import tt.richTaxist.Enums.TypeOfInput;
import tt.richTaxist.gps.GPSHelper;

/**
 * Created by Tau on 09.07.2015.
 */
public class Storage {
    public static TypeOfInput typeOfDateInput = TypeOfInput.BUTTON;
    public static TypeOfInput typeOfTimeInput = TypeOfInput.BUTTON;
    public static int timePickerStep = 10;
    public static Boolean showListHint = true;
    public static Boolean youngIsOnTop = true;
    public static Boolean singleTapTimePick = false;

    public static ParseUser currentUser;
    public static String username = "";
    public static String password = "";
    public static String deviceIMEI = "";
    public static Boolean emailVerified = false;
    public static Boolean premiumUser = false;
    //TODO привязать платные участки проги к булевому userHasAccess
    public static Boolean userHasAccess = false;
    //TODO: проверять каждые 6 часов, что с пользователем все хорошо.
    // Если плохо, сбрасывать доступ, но так чтобы это отразилось на следующем опросе состояния, а не выкидывать его из платного процесса
    public static long sessionLength = 0;

    public static String IP = "";
    //ниже пока не используемая информация
    public static String phoneModel = "";
    public static String androidVersion = "";
    public static String operatorName = "";
    public static double batteryCapacity = 0.0;
    public static int batteryLevel = 0;
    public static boolean deviceIsInLandscape;

    private static final String LOG_TAG = "Storage";
    public static Storage instance;
    private Context context;


    public static Storage init(Context context){
        if (instance == null) instance = new Storage(context);
        return instance;
    }

    private Storage(Context context) {
        this.context = context;
        Log.d(LOG_TAG, "Constructor");
        loadSettingsFromFile();
        Log.d(LOG_TAG, "Created");
    }

    //обрежет используемую область экрана до 720pix если текущая ширина экрана больше 8 см
    //временная мера чтобы не писать отдельные лэйауты для планшетов
    public static void measureScreenWidth(Context context, ViewGroup layout){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double screenWidthInches = metrics.widthPixels / metrics.xdpi;
        screenWidthInches = RoundResult(screenWidthInches, 3);
        double screenWidthSm = RoundResult(screenWidthInches * 2.54, 3);
        Log.d(LOG_TAG, "layout: " + String.valueOf(layout));
        if (screenWidthSm > 6.5 && layout != null) {// 720/320*2.54=5.715
//            Toast.makeText(context, "screenWidthSm: " + String.valueOf(screenWidthSm), Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "metrics.xdpi: " + String.valueOf(metrics.xdpi), Toast.LENGTH_SHORT).show();
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            int maxWidth = (int) Math.round(6.5 / 2.54 * metrics.xdpi);
            Log.d(LOG_TAG, "maxWidth: " + String.valueOf(maxWidth));
            params.width = maxWidth;
        }
    }


    public static ActivityState manageFragments(FragmentManager fragmentManager, ActivityState activityState,
                                                Fragment fragment1, Fragment fragment2){
        if (Storage.deviceIsInLandscape) {
            if (activityState == null) activityState = ActivityState.LAND_2_1;//точка входа ландшафт
            else if (activityState == ActivityState.PORT_1) activityState = ActivityState.LAND_2_1;
        }
        else {
            if (activityState == null) activityState = ActivityState.PORT_1;//точка входа портрет
            else if (activityState == ActivityState.LAND_2_1) activityState = ActivityState.PORT_1;
        }

        FragmentTransaction transaction;

        //очистим экран от возможно содержащихся фрагментов
//        boolean f1IsHere = fragmentManager.findFragmentByTag("fragment1") != null;
//        boolean f2IsHere = fragmentManager.findFragmentByTag("fragment2") != null;
        transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .hide(fragment1)
                .hide(fragment2)
                .commit();

        Log.d(LOG_TAG, "activityState: " + String.valueOf(activityState));
        switch (activityState){
            case LAND_2_1:
                //на входе сюда гарантированно нет ни одного подключенного фрагмента
                transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .show(fragment2)
                        .show(fragment1)
                        .commit();
                break;

            case LAND_2:
                transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .show(fragment2)
                        .commit();
                break;

            case PORT_1:
                transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .show(fragment1)
                        .commit();
                break;

            case PORT_2:
                transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .show(fragment2)
                        .commit();
                break;
        }
        return activityState;
    }

    //TODO: это должно быть внутри класса enum
    //два метода ниже нужны для отправки в Parse и получения из него, т.к. enum он не понимает
    public static String typeOfInputToString(TypeOfInput typeOfInput, Context context) {
        switch (typeOfInput){
            case BUTTON: return context.getString(R.string.button);
            case SPINNER: return context.getString(R.string.spinner);
            default:
                Log.d(LOG_TAG, "ошибка перевода enum в String");
                return context.getString(R.string.button);
        }
    }

    public static TypeOfInput stringToTypeOfInput(String string, Context context) {
        if      (context.getString(R.string.button).equals(string))    return TypeOfInput.BUTTON;
        else if (context.getString(R.string.spinner).equals(string))   return TypeOfInput.SPINNER;
        else Log.d(LOG_TAG, "ошибка перевода String в enum"); return TypeOfInput.BUTTON;
    }

    public static void saveSettings(Context context){//to cloud and file
        Log.d(LOG_TAG, "saving settings to cloud");
        if (currentUser != null) {
            currentUser.put("typeOfDateInput", typeOfInputToString(typeOfDateInput, context));
            currentUser.put("typeOfTimeInput", typeOfInputToString(typeOfTimeInput, context));
            currentUser.put("timePickerStep", timePickerStep);
            currentUser.put("showListHint", showListHint);
            currentUser.put("premiumUser", premiumUser);
            currentUser.put("userHasAccess", userHasAccess);
            //userHasAccess отправляется в облако только как индикатор для нас. из облака в прогу оно не подгружается!
            currentUser.put("youngIsOnTop", youngIsOnTop);
            currentUser.put("singleTapTimePick", singleTapTimePick);
            currentUser.saveInBackground();
        }

        Log.d(LOG_TAG, "saving settings to file");
        String path = context.getFilesDir() + "/config.properties";
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            output = new FileOutputStream(path);
            prop.setProperty("username", username);
            prop.setProperty("password", password);
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(LOG_TAG, "Error saving prop file!");
                }
            }
        }
    }


    private void loadSettingsFromFile(){
        Log.d(LOG_TAG, "Load settings from file");
        String path = context.getFilesDir() + "/config.properties";
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(path);
            prop.load(input);
            username = prop.getProperty("username");//дефолта быть не должно, т.к. это приведет к ошибке сличения IMEI
            password         = prop.getProperty("password");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(LOG_TAG, "Error opening prop file!");
                }
            }
        }
    }

    public static void resetSettings() {
        emailVerified = false;
        premiumUser = false;
        userHasAccess = false;

        typeOfDateInput = TypeOfInput.BUTTON;
        typeOfTimeInput = TypeOfInput.BUTTON;
        timePickerStep = 10;
        showListHint = true;
        youngIsOnTop = true;
        singleTapTimePick = false;
    }

    //работает только с числом десятичных знаков 0-5
    public static double RoundResult (double value, int decimalSigns) {
        if (decimalSigns < 0 || decimalSigns > 5) {
            Log.d(LOG_TAG, "decimalSigns meant to be bw 0-5. Request is: " + String.valueOf(decimalSigns));
            if (decimalSigns < 0) decimalSigns = 0;
            if (decimalSigns > 5) decimalSigns = 5;
        }
        double multiplier = Math.pow(10.0, (double) decimalSigns);//всегда .0
        long numerator  = Math.round(value * multiplier);
        return numerator / multiplier;
    }

    static void openQuitDialog(final AppCompatActivity currentActivity) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(currentActivity);
        quitDialog.setTitle("Выход: Вы уверены?");
        quitDialog.setNegativeButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GPSHelper.stopService(MainActivity.context);
                currentActivity.finish();
            }
        });
        quitDialog.setPositiveButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {/*NOP*/}
        });
        quitDialog.show();
    }
}
