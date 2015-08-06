package tt.richTaxist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.parse.ParseUser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

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
    public static Boolean singleTapTimePick = true;

    public static ParseUser currentUser;
    public static String userName = "pro";
    public static String password = "pro";
    public static String deviceIMEI = "";
    //TODO привязать платные участки проги к булевому userHasPaidAccess
    public static Boolean userHasPaidAccess = false;
    public static String IP = "";
    //ниже пока не используемая информация
    public static String phoneModel = "";
    public static String androidVersion = "";
    public static String operatorName = "";
    public static double screenWidth = 0.0;
    public static double screenHeight = 0.0;
    public static double screenDiagonal = 0.0;
    public static double batteryCapacity = 0.0;
    public static int batteryLevel = 0;

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

//два метода ниже нужны для отправки в Parse и получения из него, т.к. enum он не понимает
    public static String typeOfInputToString(TypeOfInput typeOfInput, Context context) {
        switch (typeOfInput){
            case BUTTON: return context.getString(R.string.button);
            case SPINNER: return context.getString(R.string.spinner);
            case TEXT_INPUT: return context.getString(R.string.textInput);
            default:
                Log.d(LOG_TAG, "ошибка перевода enum в String");
                return context.getString(R.string.button);
        }
    }

    public static TypeOfInput stringToTypeOfInput(String string, Context context) {
        if      (context.getString(R.string.button).equals(string))    return TypeOfInput.BUTTON;
        else if (context.getString(R.string.spinner).equals(string))   return TypeOfInput.SPINNER;
        else if (context.getString(R.string.textInput).equals(string)) return TypeOfInput.TEXT_INPUT;
        else Log.d(LOG_TAG, "ошибка перевода String в enum");           return TypeOfInput.BUTTON;
    }

    public void saveSettings(Context context){//to cloud and file
        Log.d(LOG_TAG, "saving settings to cloud");
        if (currentUser != null) {
            currentUser.put("typeOfDateInput", typeOfInputToString(typeOfDateInput, context));
            currentUser.put("typeOfTimeInput", typeOfInputToString(typeOfTimeInput, context));
            currentUser.put("timePickerStep", timePickerStep);
            currentUser.put("showListHint", showListHint);
            currentUser.put("userHasPaidAccess", userHasPaidAccess);
            //userHasPaidAccess отправляется в облако только как индикатор для нас. из облака в прогу оно не подгружается!
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
            prop.setProperty("userName", userName);
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
            userName         = prop.getProperty("userName");//дефолта быть не должно, т.к. это приведет к ошибке сличения IMEI
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
