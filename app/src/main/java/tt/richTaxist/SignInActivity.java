package tt.richTaxist;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import tt.richTaxist.SharedPreferences.SharedPrefEntry;
import tt.richTaxist.SharedPreferences.SharedPrefsHelper;
/**
 * Created by Tau on 18.07.2015.
 */
public class SignInActivity extends AppCompatActivity {
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;
    private EmailValidator emailValidator;
    private SharedPrefsHelper sharedPrefsHelper;
    private int contributeCount = 0;

    private ViewGroup laNotSigned, laSigned;
    private EditText etUsername, etPassword, etEmail;
    private View mProgress;
    private TextView tvWelcome;
    private CheckBox cbUserActive, cbEmailVerified, cbIMEICorrect, cbPremiumUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Util.measureScreenWidth(getApplicationContext(), (ViewGroup) findViewById(R.id.activity_sign_in));

        initiateWidgets();
        emailValidator = new EmailValidator();
        etEmail.addTextChangedListener(emailValidator);

        sharedPrefsHelper = new SharedPrefsHelper(PreferenceManager.getDefaultSharedPreferences(this));
        populateUi();

        mProgress.setVisibility(View.INVISIBLE);
        if (Util.currentUser != null) {
            showLogInORLogOut(false, true);
        } else {
            showLogInORLogOut(true, false);
        }
        //TODO: убрать клавиатуру при входе на экран
//        View focused = getCurrentFocus();
//        if (focused != null) {
//            focused.clearFocus();
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(null, InputMethodManager.HIDE_NOT_ALWAYS);
//        }
    }

    private void initiateWidgets() {
        laNotSigned = (ViewGroup) findViewById(R.id.laNotSigned);
        laSigned = (ViewGroup) findViewById(R.id.laSigned);
        etUsername = (EditText) findViewById(R.id.editUserName);
        etPassword = (EditText) findViewById(R.id.editPassword);
        etEmail = (EditText) findViewById(R.id.editEmail);
        mProgress = findViewById(R.id.view_progress);
        tvWelcome = (TextView) findViewById(R.id.textViewUser_Secret);
        cbUserActive = (CheckBox) findViewById(R.id.cbUserActive);
        cbEmailVerified = (CheckBox) findViewById(R.id.cbEmailVerified);
        cbIMEICorrect = (CheckBox) findViewById(R.id.cbIMEICorrect);
        cbPremiumUser = (CheckBox) findViewById(R.id.cbPremiumUser);
    }

    private void populateUi() {
        SharedPrefEntry sharedPrefEntry = sharedPrefsHelper.getPersonalInfo();
        etUsername.setText(sharedPrefEntry.getName());
        etPassword.setText(sharedPrefEntry.getPassword());
    }

    public void onSignUpClick(View v) {
        //получим данные из полей ввода
        final String username = etUsername.getText().toString();
        final String password = etPassword.getText().toString();
        String email = etEmail.getText().toString();
        final Resources res = getResources();

        //проверим поля ввода. прервем выполнение метода, если встретим ошибку
        if ("".equals(username) || "".equals(password)) {
            Toast.makeText(getApplicationContext(), res.getString(R.string.blankFieldsSignUpError), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!emailValidator.isValid()) {
            Toast.makeText(getApplicationContext(), R.string.emailFormatError, Toast.LENGTH_SHORT).show();
            return;
        }

        //проверки пройдены. сохраним данные нового пользователя на Parse.com
        mProgress.setVisibility(View.VISIBLE);
        showLogInORLogOut(false, false);
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("IMEI", Util.deviceIMEI);
        user.put("premiumUser", Util.premiumUser);
        user.put("showListHint", Util.showListHint);
        user.put("youngIsOnTop", Util.youngIsOnTop);
        user.put("twoTapTimePick", Util.twoTapTimePick);
        user.put("hideTaxometer", Util.hideTaxometer);
        user.put("userHasAccess", Util.userHasAccess);

        SignUpCallback signUpCallback = new SignUpCallback() {
            public void done(ParseException error) {
                if (error == null) {
                    Log.d(LOG_TAG, "SignUp success. Waiting for email confirmation");
                    Toast.makeText(getApplicationContext(), res.getString(R.string.confirmEmail), Toast.LENGTH_LONG).show();
                    tvWelcome.setText(res.getString(R.string.confirmEmail));
                    //сохранять данные пользователя локально в SharedPreferences логично только после подтверждения от Parse.com
                    saveLocalSharedPrefs(username, password);
                    showLogInORLogOut(false, true);
                } else {
                    Log.d(LOG_TAG, "SignUp error code " + error.getCode());
                    String errorMsg;
                    switch (error.getCode()) {
                        case 203: errorMsg = res.getString(R.string.emailError); break;
                        case 202: errorMsg = res.getString(R.string.loginError); break;
                        case 125: errorMsg = res.getString(R.string.emailFormatError); break;
                        default:  errorMsg = res.getString(R.string.irregularErrMSG); break;
                    }
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    showLogInORLogOut(true, false);
                }
                mProgress.setVisibility(View.INVISIBLE);
            }
        };
        user.signUpInBackground(signUpCallback);
    }

    private void saveLocalSharedPrefs(String username, String password){
        SharedPrefEntry entry = new SharedPrefEntry(username, password);
        boolean isSuccess = sharedPrefsHelper.savePersonalInfo(entry);
        if (isSuccess) {
            Log.d(LOG_TAG, "Personal information saved");
        } else {
            Log.d(LOG_TAG, "Failed to write personal information to SharedPreferences");
        }
    }

    public void onLoginClick(View v) {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if ("".equals(username) || "".equals(password)) {
            showLogInORLogOut(true, false);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.blankFieldsSignInError), Toast.LENGTH_SHORT).show();
            return;
        }
        authorize(username, password, getApplicationContext());
    }

    public void onContributeClick(View p1) {
        String msg;
        switch (++contributeCount) {
            case 1: msg = "спасибо и вам за Ваш выбор!"; break;
            case 2: msg = "нам очень приятно!"; break;
            case 3: msg = "в самом деле, не стоит благодарности!"; break;
            case 4:
                if (Util.currentUser != null && Util.emailVerified) {
                    if (!Util.premiumUser){
                        Util.premiumUser = true;
                        Util.userHasAccess = true;

                        showLogInORLogOut(false, true);
                        msg = "премиум подписка подключена";
                    } else {
                        msg = "у Вас уже есть премиум";
                    }
                } else {
                    msg = "Вы не авторизованы или почта не подтверждена.\nНе могу подключить премиум подписку";
                }
                break;
            default: msg = "хватит баловаться"; break;
        }
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void onLogoutClick(View v) {
        etUsername.setText("");
        etPassword.setText("");
        etEmail.setText("");
        saveLocalSharedPrefs("", "");
        Util.resetSettings();
        Util.saveSettingsToCloud();
        ParseUser.logOut();
        showLogInORLogOut(true, false);
        //TODO: onLogoutClick влияет только на наличие подписки и настройки. база на устройстве доступна любому пользователю
        //dropDataBase с подтверждением
    }

    public void onClaimChangeDeviceClick(View v) {
        Log.d(LOG_TAG, "IMEI before detach: " + Util.currentUser.getString("IMEI"));
        Util.currentUser.put("IMEI", "detached");
        Util.currentUser.saveInBackground();
        showLogInORLogOut(true, false);
        Toast.makeText(getApplicationContext(), "Запрос принят. Заново войдите чтобы привязать это устройство к учетной записи", Toast.LENGTH_LONG).show();
    }

    private void showLogInORLogOut(boolean showLogIn, boolean showLogOut) {
        Log.d(LOG_TAG, "showLogIn: " + String.valueOf(showLogIn));
        Log.d(LOG_TAG, "showLogOut: " + String.valueOf(showLogOut));
        recursiveLoopChildren(laNotSigned, showLogIn);
        recursiveLoopChildren(laSigned, showLogOut);

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
    private void recursiveLoopChildren(ViewGroup parent, boolean show) {
        //getTouchables() не возвращает список деактивированных кнопок
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                recursiveLoopChildren((ViewGroup) child, show);
            } else {
                if (child != null) {
                    child.setEnabled(show);
                }
            }
        }
    }

    private void authorize(final String username, final String password, final Context context){
        if (username == null || "".equals(username) || password == null || "".equals(password)) {
            return;
        }
        //опишем, что делать после того, как мы получим ответ от Parse.com
        LogInCallback logInCallback = new LogInCallback() {
            public void done(ParseUser user, ParseException error) {
                if (user != null) {
                    cbUserActive.setChecked(true);
                    Log.d(LOG_TAG, "user logged in");
                    Util.userHasAccess = Util.verifyUser(user, context);
                    saveLocalSharedPrefs(username, password);
                    Util.saveSettingsToCloud();
                    showLogInORLogOut(false, true);
                } else {
                    Log.d(LOG_TAG, "error code " + error.getCode());
                    String msg;
                    switch (error.getCode()){
                        case 100: msg = "Проверьте интернет-подключение"; break;
                        case 101: msg = "Ошибка логина или пароля"; break;
                        default:  msg = "Необычная ошибка " + error.getCode(); break;
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    showLogInORLogOut(true, false);
                }
                mProgress.setVisibility(View.INVISIBLE);
            }
        };
        // Отправляем данные на Parse.com для проверки
        mProgress.setVisibility(View.VISIBLE);
        showLogInORLogOut(false, false);
        ParseUser.logInInBackground(username, password, logInCallback);
    }
}