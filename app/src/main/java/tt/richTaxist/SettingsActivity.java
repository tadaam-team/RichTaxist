package tt.richTaxist;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;
import tt.richTaxist.Bricks.DF_ListInput;


public class SettingsActivity extends AppCompatActivity implements DF_ListInput.ListInputDialogListener {
    Context context;
    protected Button btnTimePickerInterval;
    String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();
        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_settings));

        //транслируем сохраненное состояние настроек в виджеты при открытии
        final Button buttonDate = (Button) findViewById(R.id.btnDate);
        buttonDate.setText(Storage.typeOfInputToString(Storage.typeOfDateInput, context));
        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Storage.typeOfDateInput == TypeOfInput.BUTTON) {
                    Storage.typeOfDateInput = TypeOfInput.SPINNER;
                    buttonDate.setText(Storage.typeOfInputToString(Storage.typeOfDateInput, context));
                } else
                if (Storage.typeOfDateInput == TypeOfInput.SPINNER) {
                    Storage.typeOfDateInput = TypeOfInput.TEXT_INPUT;
                    buttonDate.setText(Storage.typeOfInputToString(Storage.typeOfDateInput, context));
                } else
                if (Storage.typeOfDateInput == TypeOfInput.TEXT_INPUT) {
                    Storage.typeOfDateInput = TypeOfInput.BUTTON;
                    buttonDate.setText(Storage.typeOfInputToString(Storage.typeOfDateInput, context));
                }
                Log.d(LOG_TAG, "Storage.typeOfDateInput: " + String.valueOf(Storage.typeOfDateInput));
            }
        });

        final Button buttonTime = (Button) findViewById(R.id.btnTime);
        buttonTime.setText(Storage.typeOfInputToString(Storage.typeOfTimeInput, context));
        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Storage.typeOfTimeInput == TypeOfInput.BUTTON) {
                    Storage.typeOfTimeInput = TypeOfInput.SPINNER;
                    buttonTime.setText(Storage.typeOfInputToString(Storage.typeOfTimeInput, context));
                    btnTimePickerInterval.setEnabled(true);
                } else
                if (Storage.typeOfTimeInput == TypeOfInput.SPINNER) {
                    Storage.typeOfTimeInput = TypeOfInput.TEXT_INPUT;
                    buttonTime.setText(Storage.typeOfInputToString(Storage.typeOfTimeInput, context));
                    btnTimePickerInterval.setEnabled(false);
                } else
                if (Storage.typeOfTimeInput == TypeOfInput.TEXT_INPUT) {
                    Storage.typeOfTimeInput = TypeOfInput.BUTTON;
                    buttonTime.setText(Storage.typeOfInputToString(Storage.typeOfTimeInput, context));
                    btnTimePickerInterval.setEnabled(false);
                }
                Log.d(LOG_TAG, "Storage.typeOfTimeInput: " + String.valueOf(Storage.typeOfTimeInput));
            }
        });

        btnTimePickerInterval = (Button) findViewById(R.id.btnTimePickerInterval);
        if (Storage.typeOfTimeInput == TypeOfInput.SPINNER) btnTimePickerInterval.setEnabled(true);
        else btnTimePickerInterval.setEnabled(false);
        btnTimePickerInterval.setText(String.valueOf(Storage.timePickerStep));
        btnTimePickerInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DF_ListInput().show(getSupportFragmentManager(), "DF_ListInput");
                //fragment_list_input тег - идентификатор диалога в виде строковой константы, по которому можно идентифицировать диалоговое окно, если их будет много в нашем проекте
            }
        });

        ((ToggleButton) findViewById(R.id.tbShowListHint))  .setChecked(!Storage.showListHint);
        ((ToggleButton) findViewById(R.id.tbListsSortOrder)).setChecked(!Storage.youngIsOnTop);
        ((ToggleButton) findViewById(R.id.tbTimePickClicks)).setChecked(!Storage.singleTapTimePick);
    }

    @Override
    public void onFinishInputDialog(int inputNumber) {
        Storage.timePickerStep = inputNumber;
        btnTimePickerInterval.setText(String.valueOf(inputNumber));
    }

    //важно помнить, что дефолт ToggleButton.isChecked() это false. текст false это textOff
    //имена переменных же, как и их предпочтительные значения наоборот предполагают true
    //также сбивает с толку предпросмотр XML, показывающий textOff
    //чтобы устранить это противоречие к опросу TB и его инициализации добавлен !
    public void onTBListHintClick(View button)       { Storage.showListHint         = !((ToggleButton) button).isChecked(); }
    public void onTBListsSortOrderClick(View button) { Storage.youngIsOnTop         = !((ToggleButton) button).isChecked(); }
    public void onTBTimePickClicksClick(View button) { Storage.singleTapTimePick    = !((ToggleButton) button).isChecked(); }

    public void onExportImportShiftsClick(View p1) {
        Toast.makeText(this, "когда-нибудь это будет открывать окно экспорта", Toast.LENGTH_SHORT).show();
    }
    public void onSocialsClick(View p1) {
        Toast.makeText(this, "когда-нибудь это будет вести к ссылкам на наши странички", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Storage.saveSettings(context);
        if (MainActivity.currentShift != null) MainActivity.refreshInputStyle();
    }
}
