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
import android.widget.EditText;
import android.widget.GridLayout;
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
    private GridLayout gla;
    private TextView tvUsername;
    private TextView tvPassword;
    private TextView tvEmail;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etEmail;
    private Button buttonLogIn;
    private Button buttonSignUp;
    private Button buttonClaim;
    private TextView tvWelcome;
    private Button buttonLogOut;
    private View mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();

        gla = (GridLayout) findViewById(R.id.gla);
        tvUsername = (TextView) findViewById(R.id.textUserName);
        tvPassword = (TextView) findViewById(R.id.textPassword);
        tvEmail = (TextView) findViewById(R.id.textEmail);
        etUsername = (EditText) findViewById(R.id.editUserName);
        etPassword = (EditText) findViewById(R.id.editPassword);
        etEmail = (EditText) findViewById(R.id.editEmail);
        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        buttonLogIn = (Button) findViewById(R.id.buttonLogIn);
        buttonClaim = (Button) findViewById(R.id.buttonClaimChangeDevice);
        tvWelcome = (TextView) findViewById(R.id.textViewUser_Secret);
        buttonLogOut = (Button) findViewById(R.id.buttonLogOut);
        mProgress = findViewById(R.id.view_progress);

        mProgress.setVisibility(View.INVISIBLE);
        if (Storage.currentUser != null) showLogInORLogOut(false, true);
        else showLogInORLogOut(true, false);
    }

    public void onSignUpClick(View v) {
        Storage.userName = etUsername.getText().toString();
        Storage.password = etPassword.getText().toString();
        String mEmail = etEmail.getText().toString();
        //TODO нужна капча, ну или сайт...
        //TODO нужна верификация емейла

        if ("".equals(Storage.userName) || "".equals(Storage.password) || "".equals(mEmail)) {
            Toast.makeText(context, "Заполните логин, пароль и почту", Toast.LENGTH_SHORT).show();
        } else {
            showProgress(true);
            // Сохраняем данные нового пользователя на Parse.com
            ParseUser user = new ParseUser();
            user.setUsername(Storage.userName);
            user.setPassword(Storage.password);
            user.setEmail(mEmail);
            user.put("IMEI", Storage.deviceIMEI);

            SignUpCallback signUpCallback = new SignUpCallback() {
                public void done(ParseException error) {
                    if (error == null) {
                        Log.d(LOG_TAG, "SignUp success");
                        Toast.makeText(context, "Вы зарегистрированы", Toast.LENGTH_LONG).show();
                        authorize(context);
                    } else {
                        Log.d(LOG_TAG, "SignUp error code " + error.getCode());
                        showProgress(false);
                        String errorMsg;
                        switch (error.getCode()) {
                            case 203: errorMsg = "Пользователь с такой почтой уже зарегистрирован"; break;
                            case 202: errorMsg = "Логин занят"; break;
                            case 125: errorMsg = "Неверный формат почты"; break;
                            default: errorMsg = "Ошибка при регистрации. Попробуйте ещё раз"; break;
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                        showLogInORLogOut(true, false);
                    }
                }
            };
            user.signUpInBackground(signUpCallback);
        }
    }


    public void onLoginClick(View v) {
        Storage.userName = etUsername.getText().toString();
        Storage.password = etPassword.getText().toString();
        if ("".equals(Storage.userName) || "".equals(Storage.password)) {
            Toast.makeText(context, "Для входа введите логин и пароль", Toast.LENGTH_SHORT).show();
        } else {
            showProgress(true);
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
        Storage.userName = "";
        Storage.password = "";
        Storage.userHasPaidAccess = false;
        showLogInORLogOut(true, false);
    }


    public void onClaimChangeDeviceClick(View v) {
        showProgress(true);
        Log.d(LOG_TAG, "IMEI before detach: " + Storage.currentUser.getString("IMEI"));
        Storage.currentUser.put("IMEI", "detached");
        Storage.currentUser.saveInBackground();
        showProgress(false);
        Toast.makeText(context, "Запрос принят. Заново войдите чтобы привязать это устройство к учетной записи", Toast.LENGTH_LONG).show();
        showLogInORLogOut(true, false);
    }


    private void showProgress(boolean show) {
        for (View view : gla.getTouchables()) {
            view.setEnabled(!show);
        }
        mProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }


    private void showLogInORLogOut(boolean showLogIn, boolean showLogOut) {
        tvUsername.setEnabled(showLogIn);
        tvPassword.setEnabled(showLogIn);
        tvEmail.setEnabled(showLogIn);
        etUsername.setEnabled(showLogIn);
        etPassword.setEnabled(showLogIn);
        etEmail.setEnabled(showLogIn);
        buttonLogIn.setEnabled(showLogIn);
        buttonSignUp.setEnabled(showLogIn);

        if (showLogOut)
            tvWelcome.setText("Здравствуйте\nВы вошли как\n" + Storage.currentUser.getUsername());
        else tvWelcome.setText("Пользователь\nне авторизован");
        buttonLogOut.setEnabled(showLogOut);
        buttonClaim.setEnabled(showLogOut);
    }

    //пока не могу найти способ не задваивать метод авторизации
    //если выносить его в сторож, происходит рассинхронизация обработки результатов метода done,
    //что приводит к не штатному вызову SignInActivity.showLogInORLogOut()
    public void authorize(final Context context){
        // Отправляем данные на Parse.com для проверки
        LogInCallback logInCallback = new LogInCallback() {
            public void done(ParseUser user, ParseException error) {
                if (user != null) {
                    //пользователь авторизован. загрузим его сохраненные настройки из облака
                    Storage.currentUser = user;
                    Storage.typeOfDateInput = Storage.stringToTypeOfInput(user.getString("typeOfDateInput"), context);
//                    Storage.typeOfTimeInput = Storage.stringToTypeOfInput(user.getString("typeOfTimeInput"), context);
//                    Storage.timePickerStep = user.getInt("timePickerStep");
                    Storage.showListHint = user.getBoolean("showListHint");
                    Storage.youngIsOnTop = user.getBoolean("youngIsOnTop");
                    Storage.singleTapTimePick = user.getBoolean("singleTapTimePick");

                    showProgress(false);
                    showLogInORLogOut(false, true);

                    if (user.getString("IMEI") != null && !user.getString("IMEI").equals("detached")) {
                        //пользователь авторизован и имеет привязанный IMEI. проверим его соответствие
                        Log.d(LOG_TAG, "IMEI OK? " + String.valueOf(Storage.deviceIMEI.equals(user.getString("IMEI"))));
                        if (Storage.deviceIMEI.equals(user.getString("IMEI"))) {
                            //пользователь авторизован, имеет привязанный IMEI и IMEI соответствует текущему устройству
                            Toast.makeText(context, "Здравствуйте, " + user.getUsername() + "\nПриятной работы", Toast.LENGTH_LONG).show();
                            Storage.userHasPaidAccess = true;
                        } else {
                            Toast.makeText(context,
                                    "Здравствуйте, " + user.getUsername() +
                                            "\nВы вошли в систему с другого устройства." +
                                            "\nСейчас платные опции недоступны." +
                                            "\nЧтобы привязать логин к новому устройству" +
                                            "\nперейдите в меню \"Учетные записи\"", Toast.LENGTH_LONG).show();
                            Storage.userHasPaidAccess = false;
                        }
                    } else {
                        //пользователь авторизован, но IMEI еще не привязан или отвязан по запросу
                        Storage.currentUser.put("IMEI", Storage.deviceIMEI);
                        Storage.currentUser.saveInBackground();
                        Toast.makeText(context, "Вы привязали это устройство" +
                                "\nк своей учетной записи." +
                                "\nПриятной работы", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(LOG_TAG, "error code " + error.getCode());//всегда 101
                    Toast.makeText(context, "Ошибка логина или пароля", Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    showLogInORLogOut(true, false);
                }
            }
        };
        ParseUser.logInInBackground(Storage.userName, Storage.password, logInCallback);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Storage.instance.saveSettings(context);
    }
}