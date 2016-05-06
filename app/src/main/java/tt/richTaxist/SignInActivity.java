package tt.richTaxist;

/**
 * Created by Tau on 18.07.2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import tt.richTaxist.Enums.InputStyle;

public class SignInActivity extends AppCompatActivity {
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;
    private Context context;
    int contributeCount = 0;

    private TextView tvUsername, tvPassword, tvEmail;
    private EditText etUsername, etPassword, etEmail;
    private Button buttonSignUp, buttonLogIn, buttonClaim, buttonContribute, buttonLogOut;
    private View mProgress;
    private TextView tvWelcome;
    private CheckBox cbUserActive, cbEmailVerified, cbIMEICorrect, cbPremiumUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        context = getApplicationContext();
        Util.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_sign_in));

        initiateWidgets();
        mProgress.setVisibility(View.INVISIBLE);
        if (Util.currentUser != null) showLogInORLogOut(false, true);
        else showLogInORLogOut(true, false);
        //TODO: убрать клавиатуру при входе на экран
//        View focused = getCurrentFocus();
//        if (focused != null) {
//            focused.clearFocus();
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(null, InputMethodManager.HIDE_NOT_ALWAYS);
//        }
    }

    private void initiateWidgets() {
        tvUsername      = (TextView)    findViewById(R.id.tvUsername);
        tvPassword      = (TextView)    findViewById(R.id.tvPassword);
        tvEmail         = (TextView)    findViewById(R.id.tvEmail);
        etUsername      = (EditText)    findViewById(R.id.editUserName);
        etPassword      = (EditText)    findViewById(R.id.editPassword);
        etEmail         = (EditText)    findViewById(R.id.editEmail);
        buttonSignUp    = (Button)      findViewById(R.id.buttonSignUp);
        buttonLogIn     = (Button)      findViewById(R.id.buttonLogIn);
        buttonClaim     = (Button)      findViewById(R.id.buttonClaimChangeDevice);
        buttonContribute = (Button)     findViewById(R.id.buttonContribute);
        buttonLogOut    = (Button)      findViewById(R.id.buttonLogOut);
        mProgress       =               findViewById(R.id.view_progress);
        tvWelcome       = (TextView)    findViewById(R.id.textViewUser_Secret);
        cbUserActive    = (CheckBox)    findViewById(R.id.cbUserActive);
        cbEmailVerified = (CheckBox)    findViewById(R.id.cbEmailVerified);
        cbIMEICorrect   = (CheckBox)    findViewById(R.id.cbIMEICorrect);
        cbPremiumUser   = (CheckBox)    findViewById(R.id.cbPremiumUser);
    }

    public void onSignUpClick(View v) {
        Util.username = etUsername.getText().toString();
        Util.password = etPassword.getText().toString();
        String mEmail = etEmail.getText().toString();
        final Resources res = getResources();

        if ("".equals(Util.username) || "".equals(Util.password) || "".equals(mEmail)) {
            Toast.makeText(context, res.getString(R.string.blankFieldsSignUpError), Toast.LENGTH_SHORT).show();
        } else {
            showProgress(true);
            // Сохраняем данные нового пользователя на Parse.com
            ParseUser user = new ParseUser();
            user.setUsername(Util.username);
            user.setPassword(Util.password);
            user.setEmail(mEmail);
            user.put("IMEI", Util.deviceIMEI);

            SignUpCallback signUpCallback = new SignUpCallback() {
                public void done(ParseException error) {
                    if (error == null) {
                        Log.d(LOG_TAG, "SignUp success. Waiting for email confirmation");
                        Toast.makeText(context, res.getString(R.string.confirmEmail), Toast.LENGTH_LONG).show();
                        tvWelcome.setText(res.getString(R.string.confirmEmail));
                        showProgress(false);
                        //showLogInORLogOut(false, true); отсутствие строки здесь не ошибка
                    } else {
                        Log.d(LOG_TAG, "SignUp error code " + error.getCode());
                        String errorMsg;
                        switch (error.getCode()) {
                            case 203: errorMsg = res.getString(R.string.emailError); break;
                            case 202: errorMsg = res.getString(R.string.loginError); break;
                            case 125: errorMsg = res.getString(R.string.emailFormatError); break;
                            default: errorMsg = res.getString(R.string.irregularErrMSG); break;
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                        showProgress(false);
                        showLogInORLogOut(true, false);
                    }
                }
            };
            user.signUpInBackground(signUpCallback);
        }
    }

    public void onLoginClick(View v) {
        showProgress(true);
        Util.username = etUsername.getText().toString();
        Util.password = etPassword.getText().toString();
        if ("".equals(Util.username) || "".equals(Util.password)) {
            showProgress(false);
            showLogInORLogOut(true, false);
            Toast.makeText(context, getResources().getString(R.string.blankFieldsSignInError), Toast.LENGTH_SHORT).show();
        } else {
            authorize(context);
        }
    }

    public void onContributeClick(View p1) {
        String msg;
        switch (++contributeCount) {
            case 1: msg = "спасибо и вам за Ваш выбор!"; break;
            case 2: msg = "нам очень приятно!"; break;
            case 3: msg = "в самом деле, не стоит благодарности!"; break;
            case 4: msg = "СПАСИБО!!"; break;
            case 5: msg = "можете погладить с обратной стороны телефона"; break;
            case 6:
                if (Util.currentUser != null && Util.emailVerified) {
                    if (!Util.premiumUser){
                        Util.premiumUser = true;
                        Util.userHasAccess = true;

                        showLogInORLogOut(false, true);//по сути это костыль вместо refreshWidgets()
                        msg = "премиум подписка подключена";
                    } else msg = "у Вас уже есть премиум";
                } else msg = "Вы не авторизованы или почта не подтверждена.\nНе могу подключить премиум подписку";
                break;
            default: msg = "хватит баловаться"; break;
        }
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void onLogoutClick(View v) {
        ParseUser.logOut();
        etUsername.setText("");
        etPassword.setText("");
        etEmail.setText("");

        Util.currentUser = null;
        Util.username = "";
        Util.password = "";
        Util.resetSettings();
        Util.saveSettings(context);
        showLogInORLogOut(true, false);
        //onLogoutClick влияет только на наличие подписки и настройки. база на устройстве доступна любому пользователю
    }


    public void onClaimChangeDeviceClick(View v) {
        showProgress(true);
        Log.d(LOG_TAG, "IMEI before detach: " + Util.currentUser.getString("IMEI"));
        Util.currentUser.put("IMEI", "detached");
        Util.currentUser.saveInBackground();
        showProgress(false);
        showLogInORLogOut(true, false);
        Toast.makeText(context, "Запрос принят. Заново войдите чтобы привязать это устройство к учетной записи", Toast.LENGTH_LONG).show();
    }


    private void showProgress(boolean show) {
        tvUsername  .setEnabled(!show);
        tvPassword  .setEnabled(!show);
        tvEmail     .setEnabled(!show);
        etUsername  .setEnabled(!show);
        etPassword  .setEnabled(!show);
        etEmail     .setEnabled(!show);
        buttonSignUp.setEnabled(!show);
        buttonLogIn .setEnabled(!show);

        buttonClaim.setEnabled(!show);
        buttonContribute.setEnabled(!show);
        buttonLogOut.setEnabled(!show);
        mProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }


    private void showLogInORLogOut(boolean showLogIn, boolean showLogOut) {
//рад бы, но тогда прога не правильно скрывает/отображает кнопки. видимо нужно выделять логин/логаут в отдельный процесс
//        for (View view : laNotSigned.getTouchables())  view.setEnabled(showLogIn);
//        for (View view : laSigned.getTouchables())     view.setEnabled(showLogOut);
        tvUsername  .setEnabled(showLogIn);
        tvPassword  .setEnabled(showLogIn);
        tvEmail     .setEnabled(showLogIn);
        etUsername  .setEnabled(showLogIn);
        etPassword  .setEnabled(showLogIn);
        etEmail     .setEnabled(showLogIn);
        buttonSignUp.setEnabled(showLogIn);
        buttonLogIn .setEnabled(showLogIn);

        buttonClaim.setEnabled(showLogOut);
        buttonContribute.setEnabled(showLogOut);
        buttonLogOut.setEnabled(showLogOut);
        if (Util.currentUser != null){
            tvWelcome.setText(String.format(getResources().getString(R.string.hello), Util.currentUser.getUsername()));
            cbUserActive    .setChecked(Util.currentUser.isAuthenticated());
            cbEmailVerified .setChecked(Util.currentUser.getBoolean("emailVerified"));
            cbIMEICorrect   .setChecked(Util.deviceIMEI.equals(Util.currentUser.getString("IMEI")));
            cbPremiumUser   .setChecked(Util.premiumUser);
        }
        else {
            tvWelcome.setText("");
            cbUserActive    .setChecked(false);
            cbEmailVerified .setChecked(false);
            cbIMEICorrect   .setChecked(false);
            cbPremiumUser   .setChecked(false);
        }
    }

    //пока не могу найти способ не задваивать метод авторизации
    //если выносить его в сторож, происходит рассинхронизация обработки результатов метода done,
    //что приводит к не штатному вызову SignInActivity.showLogInORLogOut()
    public void authorize(final Context context){
        showProgress(true);
        // Отправляем данные на Parse.com для проверки
        LogInCallback logInCallback = new LogInCallback() {
            public void done(ParseUser user, ParseException error) {
                if (user != null) {
                    Util.userHasAccess = verifyUser(user);
                } else {
                    Log.d(LOG_TAG, "error code " + error.getCode());
                    String msg;
                    switch (error.getCode()){
                        case 100: msg = "Проверьте интернет-подключение"; break;
                        case 101: msg = "Ошибка логина или пароля"; break;
                        default:  msg = "Необычная ошибка " + error.getCode(); break;
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    showLogInORLogOut(true, false);
                }
            }
        };
        if (Util.username != null && !Util.username.equals(""))
            ParseUser.logInInBackground(Util.username, Util.password, logInCallback);
    }


    private boolean verifyUser(ParseUser user) {
        //пользователь авторизован. загрузим его сохраненные настройки из облака
        cbUserActive.setChecked(true);
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
            showProgress(false);//костыль чтобы открыть доступ ко всем кнопкам сразу
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
            showProgress(false);
            showLogInORLogOut(false, true);
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

            showProgress(false);
            showLogInORLogOut(false, true);
            return false;
        }
        Log.d(LOG_TAG, "IMEI check passed");
        //пользователь авторизован, почта подтверждена, есть подписка, IMEI не пустой и проверен---


        Toast.makeText(context, "Здравствуйте, " + user.getUsername() + "\nПриятной работы", Toast.LENGTH_LONG).show();
        showProgress(false);
        showLogInORLogOut(false, true);
        Log.d(LOG_TAG, "user has access!!");
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Util.saveSettings(context);
    }
}