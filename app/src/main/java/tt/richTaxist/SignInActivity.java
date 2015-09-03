package tt.richTaxist;

/**
 * Created by Tau on 18.07.2015.
 */

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignInActivity extends AppCompatActivity {
    private static final String LOG_TAG = "SignInActivity";
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();

        initiateWidgets();
        mProgress.setVisibility(View.INVISIBLE);
        if (Storage.currentUser != null) showLogInORLogOut(false, true);
        else showLogInORLogOut(true, false);
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
        Storage.username = etUsername.getText().toString();
        Storage.password = etPassword.getText().toString();
        String mEmail = etEmail.getText().toString();

        if ("".equals(Storage.username) || "".equals(Storage.password) || "".equals(mEmail)) {
            Toast.makeText(context, "Заполните логин, пароль и почту", Toast.LENGTH_SHORT).show();
        } else {
            showProgress(true);
            // Сохраняем данные нового пользователя на Parse.com
            ParseUser user = new ParseUser();
            user.setUsername(Storage.username);
            user.setPassword(Storage.password);
            user.setEmail(mEmail);
            user.put("IMEI", Storage.deviceIMEI);

            SignUpCallback signUpCallback = new SignUpCallback() {
                public void done(ParseException error) {
                    if (error == null) {
                        Log.d(LOG_TAG, "SignUp success. Waiting for email confirmation");
                        tvWelcome.setText("Подтвердите почту\nи нажмите \"Войти\"");
                        showProgress(false);
                        //showLogInORLogOut(false, true); отсутствие строки здесь не ошибка
                    } else {
                        Log.d(LOG_TAG, "SignUp error code " + error.getCode());
                        String errorMsg;
                        switch (error.getCode()) {
                            case 203: errorMsg = "Пользователь с такой почтой уже зарегистрирован"; break;
                            case 202: errorMsg = "Логин занят"; break;
                            case 125: errorMsg = "Неверный формат почты"; break;
                            default: errorMsg = "Ошибка при регистрации. Попробуйте ещё раз"; break;
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
        Storage.username = etUsername.getText().toString();
        Storage.password = etPassword.getText().toString();
        if ("".equals(Storage.username) || "".equals(Storage.password)) {
            showProgress(false);
            showLogInORLogOut(true, false);
            Toast.makeText(context, "Для входа введите логин и пароль", Toast.LENGTH_SHORT).show();
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
                if (Storage.currentUser != null && Storage.emailVerified) {
                    if (!Storage.premiumUser){
                        Storage.premiumUser = true;
                        Storage.userHasAccess = true;

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

        Storage.currentUser = null;
        Storage.username = "";
        Storage.password = "";
        Storage.resetSettings();
        Storage.saveSettings(context);
        showLogInORLogOut(true, false);
        //TODO: база не должна быть доступна, если залогинился другой юзер
    }


    public void onClaimChangeDeviceClick(View v) {
        showProgress(true);
        Log.d(LOG_TAG, "IMEI before detach: " + Storage.currentUser.getString("IMEI"));
        Storage.currentUser.put("IMEI", "detached");
        Storage.currentUser.saveInBackground();
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
        if (Storage.currentUser != null){
            tvWelcome.setText("Здравствуйте, " + Storage.currentUser.getUsername());
            cbUserActive    .setChecked(Storage.currentUser.isAuthenticated());
            cbEmailVerified .setChecked(Storage.currentUser.getBoolean("emailVerified"));
            cbIMEICorrect   .setChecked(Storage.deviceIMEI.equals(Storage.currentUser.getString("IMEI")));
            cbPremiumUser   .setChecked(Storage.premiumUser);
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
                    Storage.userHasAccess = verifyUser(user);
                } else {
                    Log.d(LOG_TAG, "error code " + error.getCode());//всегда 101
                    Toast.makeText(context, "Ошибка логина или пароля", Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    showLogInORLogOut(true, false);
                }
            }
        };
        if (Storage.username != null) ParseUser.logInInBackground(Storage.username, Storage.password, logInCallback);
    }


    private boolean verifyUser(ParseUser user) {
        //пользователь авторизован. загрузим его сохраненные настройки из облака
        cbUserActive.setChecked(true);
        Log.d(LOG_TAG, "user logged in");


        Storage.currentUser         = user;
        Storage.showListHint        = user.getBoolean("showListHint");
        Storage.youngIsOnTop        = user.getBoolean("youngIsOnTop");
        Storage.singleTapTimePick   = user.getBoolean("singleTapTimePick");
        Storage.premiumUser         = user.getBoolean("premiumUser");
        Storage.emailVerified       = user.getBoolean("emailVerified");

        //TODO: если письмо с подтверждением не пришло, то оно не может быть запрошено повторно, т.к. юзер уже в базе
        if (!Storage.emailVerified) {
            //для бесплатного доступа вообще то необязательно подтверждать почту или сверять IMEI,
            // но для дисциплины напомним юзеру, что надо подтвердить
            Toast.makeText(context, "Здравствуйте, " + user.getUsername() +
                    "\nВаш email еще не подтвержден", Toast.LENGTH_LONG).show();
            showProgress(false);//костыль чтобы открыть доступ ко всем кнопкам сразу
            return false;
        }
        Log.d(LOG_TAG, "email verified");
        //пользователь авторизован и почта подтверждена--------------------------------------------


        if (!Storage.premiumUser){
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
            showProgress(false);
            showLogInORLogOut(false, true);
            Toast.makeText(context,  "Здравствуйте, " + user.getUsername() +
                    "\nВы привязали это устройство" +
                    "\nк своей учетной записи." +
                    "\nПриятной работы", Toast.LENGTH_LONG).show();

            Log.d(LOG_TAG, "IMEI saved");
            return true;
        }
        Log.d(LOG_TAG, "IMEI not null");
        //пользователь авторизован, почта подтверждена, есть подписка и IMEI не пустой-------------


        if (!Storage.deviceIMEI.equals(user.getString("IMEI"))) {
            Toast.makeText(context, "Здравствуйте, " + user.getUsername() +
                    "\nВы вошли в систему с другого устройства." +
                    "\nСейчас платные опции недоступны." +
                    "\nЧтобы привязать логин к новому устройству" +
                    "\nперейдите в меню \"Учетные записи\"" +
                    "\nи нажмите \"Сменить устройство\"", Toast.LENGTH_LONG).show();
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
        Storage.saveSettings(context);
    }
}