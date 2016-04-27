package tt.richTaxist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.parse.ParseUser;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import tt.richTaxist.Units.Billing;
import tt.richTaxist.DB.BillingsSQLHelper;
import tt.richTaxist.Units.Taxopark;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.InputStyle;
import tt.richTaxist.Enums.TypeOfSpinner;
import tt.richTaxist.gps.GPSHelper;

/**
 * Created by Tau on 09.07.2015.
 */
public class Util {
    public static ParseUser currentUser;
    public static String username = "";
    public static String password = "";

    public static Boolean premiumUser = false;
    public static Boolean emailVerified = false;
    public static Boolean showListHint = true;//все булевы, переключаемые через tb должны в дефолте быть true
    public static Boolean youngIsOnTop = true;
    public static Boolean twoTapTimePick = true;
    public static Boolean hideTaxometer = true;
    public static int taxoparkID = -1;
    public static int billingID = -1;
    public static int monthID = -1;

    public static InputStyle inputStyle = InputStyle.BUTTON;
    public static int timePickerStep = 10;

    public static String deviceIMEI = "";
    //TODO привязать платные участки проги к булевому userHasAccess
    public static Boolean userHasAccess = false;
    //TODO: проверять каждые 6 часов, что с пользователем все хорошо.
    // Если плохо, сбрасывать доступ, но так чтобы это отразилось на следующем опросе состояния, а не выкидывать его из платного процесса
//    public static long sessionLength = 0;

    public static boolean deviceIsInLandscape;
    private static final String LOG_TAG = "Util";
    public static Util instance;
    private static Context context;


    public static Util init(Context context){
        if (instance == null) instance = new Util(context);
        return instance;
    }

    private Util(Context context) {
        Util.context = context;
        loadSettingsFromFile();
    }

    //обрежет используемую область экрана до 720pix если текущая ширина экрана больше 6,5 см
    public static void measureScreenWidth(Context context, ViewGroup layout){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double screenWidthInches = metrics.widthPixels / metrics.xdpi;
        screenWidthInches = RoundResult(screenWidthInches, 3);
        double screenWidthSm = RoundResult(screenWidthInches * 2.54, 3);
        Log.d(LOG_TAG, "layout: " + String.valueOf(layout));
        if (screenWidthSm > 6.5 && layout != null) {// 720/320*2.54=5.715
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            params.width = (int) Math.round(6.5 / 2.54 * metrics.xdpi);
        }
    }

    //TODO: extend Spinner and add to it methods from here. also consider creating static field in it
    public static void saveSpinner(TypeOfSpinner typeOfSpinner, Spinner spinner){
        switch (typeOfSpinner) {
            case TAXOPARK:
                try { taxoparkID = ((Taxopark) spinner.getSelectedItem()).taxoparkID;
                } catch (NullPointerException e) {
                    Log.d(LOG_TAG, "taxopark not defined");
                    taxoparkID = -1;
                }
                break;

            case BILLING:
                try { billingID = ((Billing) spinner.getSelectedItem()).billingID;
                } catch (NullPointerException e) {
                    Log.d(LOG_TAG, "billing not defined");
                    billingID = -1;
                }
                break;

            case MONTH:
                try { monthID = (int) spinner.getSelectedItemId();
                } catch (NullPointerException e) {
                    Log.d(LOG_TAG, "month not defined");
                    monthID = -1;
                }
                break;
        }
    }

    public static void setPositionOfSpinner(TypeOfSpinner typeOfSpinner, ArrayAdapter adapter, Spinner spinner, int id){
        switch (typeOfSpinner){
            case TAXOPARK:
                //если получена команда обнулить состояние спиннера, возвращаем не просто первый по списку, а умолчание
                if (id == -1)
                    for (Taxopark taxoparkIter : TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks())
                        if (taxoparkIter.isDefault) taxoparkID = id = taxoparkIter.taxoparkID;

                Taxopark taxopark = TaxoparksSQLHelper.dbOpenHelper.getTaxoparkByID(id);
                int indexInSpinner = adapter.getPosition(taxopark);
                spinner.setSelection(indexInSpinner);
                break;

            case BILLING:
                Billing billing = BillingsSQLHelper.dbOpenHelper.getBillingByID(id);
                indexInSpinner = adapter.getPosition(billing);
                spinner.setSelection(indexInSpinner);
                break;

            case MONTH:
                spinner.setSelection(monthID);
                break;
        }
        adapter.notifyDataSetChanged();
    }

    public static void saveSettings(Context context){//to cloud and file
        Log.d(LOG_TAG, "saving settings to cloud");
        if (currentUser != null) {
            currentUser.put("premiumUser", premiumUser);
            //emailVerified не отправляем в облако, т.к. этот ключ генерируется там
            currentUser.put("showListHint", showListHint);
            currentUser.put("youngIsOnTop", youngIsOnTop);
            currentUser.put("twoTapTimePick", twoTapTimePick);
            currentUser.put("hideTaxometer", hideTaxometer);
            currentUser.put("taxoparkID", taxoparkID);
            currentUser.put("billingID", billingID);
            currentUser.put("monthID", monthID);
            currentUser.put("inputStyle", inputStyle.toString());
            currentUser.put("timePickerStep", timePickerStep);
            currentUser.put("userHasAccess", userHasAccess);
            //userHasAccess отправляется в облако только как индикатор для нас. из облака в прогу оно не подгружается!
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
        Log.d(LOG_TAG, "loading settings from file");
        String path = context.getFilesDir() + "/config.properties";
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(path);
            prop.load(input);
            username = prop.getProperty("username");//дефолта быть не должно, т.к. это приведет к ошибке сличения IMEI
            password = prop.getProperty("password");
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
        premiumUser     = false;
        emailVerified   = false;
        showListHint    = true;
        youngIsOnTop    = true;
        twoTapTimePick  = true;
        hideTaxometer   = true;
        taxoparkID      = 1;
        billingID       = 0;
        monthID         = 0;

        userHasAccess   = false;
        inputStyle = InputStyle.BUTTON;
        timePickerStep  = 10;
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
