package tt.richTaxist;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import com.parse.ParseUser;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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
    public static Boolean youngIsOnTop = true;
    public static Boolean twoTapTimePick = true;
    public static Boolean showTaxometer = false;

    public static String deviceIMEI = "";
    //TODO привязать платные участки проги к булевому userHasAccess
    public static Boolean userHasAccess = false;
    //TODO: проверять каждые 6 часов, что с пользователем все хорошо.
    // Если плохо, сбрасывать доступ, но так чтобы это отразилось на следующем опросе состояния, а не выкидывать его из платного процесса
//    public static long sessionLength = 0;

    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);


    public static boolean verifyUser(ParseUser user, Context context) {
        //пользователь авторизован. загрузим его сохраненные настройки из облака
        context = context.getApplicationContext();
        Util.currentUser     = user;
        Util.premiumUser     = user.getBoolean("premiumUser");
        Util.emailVerified   = user.getBoolean("emailVerified");
        Util.youngIsOnTop    = user.getBoolean("youngIsOnTop");
        Util.twoTapTimePick  = user.getBoolean("twoTapTimePick");
        Util.showTaxometer = user.getBoolean("showTaxometer");

        //если письмо с подтверждением не пришло, то оно не может быть запрошено повторно, т.к. юзер уже в базе
        if (!Util.emailVerified) {
            //для бесплатного доступа вообще то необязательно подтверждать почту или сверять IMEI,
            // но для дисциплины напомним юзеру, что надо подтвердить
            Toast.makeText(context, "Здравствуйте, " + user.getUsername() +
                    "\nВаш email еще не подтвержден", Toast.LENGTH_LONG).show();
            return false;
        }
//        Log.d(LOG_TAG, "email verified");
        //пользователь авторизован и почта подтверждена--------------------------------------------


        if (!Util.premiumUser){
            //если премиум доступа нет, то и нет смысла проверять IMEI
            Toast.makeText(context, "Здравствуйте, " + user.getUsername() +
                    "\nПриятной работы", Toast.LENGTH_LONG).show();
            return false;
        }
//        Log.d(LOG_TAG, "user is premium");
        //пользователь авторизован, почта подтверждена и есть подписка-----------------------------


        if (user.getString("IMEI") == null || user.getString("IMEI").equals("detached")) {
            //IMEI еще не привязан или отвязан по запросу
            Util.currentUser.put("IMEI", Util.deviceIMEI);
            Util.currentUser.saveInBackground();
            Toast.makeText(context, "Вы привязали это устройство" +
                    "\nк своей учетной записи." +
                    "\nПриятной работы", Toast.LENGTH_LONG).show();

//            Log.d(LOG_TAG, "IMEI saved");
            return true;
        }
//        Log.d(LOG_TAG, "IMEI not null");
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
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(FirstScreenActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

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
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
            return false;
        }
//        Log.d(LOG_TAG, "IMEI check passed");
        //пользователь авторизован, почта подтверждена, есть подписка, IMEI не пустой и проверен---


        Toast.makeText(context, "Здравствуйте, " + user.getUsername() + "\nПриятной работы", Toast.LENGTH_LONG).show();

//        Log.d(LOG_TAG, "user has access");
        return true;
    }


    //обрежет используемую область экрана до 720pix если текущая ширина экрана больше 6,5 см
    public static void measureScreenWidth(Context context, ViewGroup layout){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double screenWidthInches = metrics.widthPixels / metrics.xdpi;
        screenWidthInches = RoundResult(screenWidthInches, 3);
        double screenWidthSm = RoundResult(screenWidthInches * 2.54, 3);
        if (screenWidthSm > 6.5 && layout != null) {// 720/320*2.54=5.715
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            params.width = (int) Math.round(6.5 / 2.54 * metrics.xdpi);
        }
    }

    public static void logDate(String dateName, Calendar dateToLog, Context context){
        Locale locale = context.getResources().getConfiguration().locale;
        String log = String.format(locale, "%02d.%02d.%04d %02d:%02d:%02d", dateToLog.get(Calendar.DAY_OF_MONTH),
                dateToLog.get(Calendar.MONTH) + 1, dateToLog.get(Calendar.YEAR), dateToLog.get(Calendar.HOUR_OF_DAY),
                dateToLog.get(Calendar.MINUTE), dateToLog.get(Calendar.SECOND));
        if (dateName.length() >= 20) {
            Log.d(LOG_TAG, dateName + log);
        } else {
            while (dateName.length() < 20) dateName += '.';
            Log.d(LOG_TAG, dateName + log);
        }
    }

    public static void saveSettingsToCloud(){
        if (currentUser != null) {
            currentUser.put("premiumUser", premiumUser);
            //emailVerified не отправляем в облако, т.к. эта информация генерируется там
            currentUser.put("youngIsOnTop", youngIsOnTop);
            currentUser.put("twoTapTimePick", twoTapTimePick);
            currentUser.put("showTaxometer", showTaxometer);
            currentUser.put("userHasAccess", userHasAccess);
            //userHasAccess отправляется в облако только как индикатор для нас. из облака в прогу оно не подгружается!
            currentUser.saveInBackground();
        }
    }

    public static void resetSettings() {
        currentUser = null;
        username = "";
        password = "";
        premiumUser     = false;
        emailVerified   = false;
        youngIsOnTop    = true;
        twoTapTimePick  = true;
        showTaxometer = false;
        userHasAccess   = false;
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

    public static String getStringDateFromCal(Calendar date, Context context){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        Locale locale = context.getResources().getConfiguration().locale;
        return String.format(locale, "%02d.%02d.%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR) % 100);
    }

    public static String getStringTimeFromCal(Calendar date, Context context){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        Locale locale = context.getResources().getConfiguration().locale;
        return String.format(locale, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    static void openQuitDialog(final AppCompatActivity currentActivity) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(currentActivity);
        quitDialog.setTitle("Выход: Вы уверены?");
        quitDialog.setNegativeButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GPSHelper.stopService(currentActivity.getApplicationContext());
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
