package tt.richTaxist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;
import tt.richTaxist.Bricks.DF_ChooseFromList;
import tt.richTaxist.Enums.InputStyle;


public class SettingsActivity extends AppCompatActivity implements DF_ChooseFromList.ListInputDialogListener {
    Context context;
    protected Button btnTimePickerInterval;
    String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = getApplicationContext();
        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_settings));

        //транслируем сохраненное состояние настроек в виджеты при открытии
        ((ToggleButton) findViewById(R.id.tbShowListHint))  .setChecked(!Storage.showListHint);
        ((ToggleButton) findViewById(R.id.tbListsSortOrder)).setChecked(!Storage.youngIsOnTop);
        ((ToggleButton) findViewById(R.id.tbTimePickClicks)).setChecked(!Storage.twoTapTimePick);
        ((ToggleButton) findViewById(R.id.tbHideTaxometer)) .setChecked(!Storage.hideTaxometer);
        ((ToggleButton) findViewById(R.id.tbInputStyle))    .setChecked(Storage.inputStyle.id != InputStyle.SPINNER.id);

        btnTimePickerInterval = (Button) findViewById(R.id.btnTimePickerInterval);
        if (Storage.inputStyle.id == InputStyle.SPINNER.id)
            btnTimePickerInterval.setEnabled(true);
        else btnTimePickerInterval.setEnabled(false);
        btnTimePickerInterval.setText(String.valueOf(Storage.timePickerStep));
        btnTimePickerInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DF_ChooseFromList().show(getSupportFragmentManager(), "DF_ChooseFromList");
                //DF_ChooseFromList тег - идентификатор диалога в виде строковой константы, по которому можно идентифицировать диалоговое окно, если их будет много в нашем проекте
            }
        });

    }

    @Override
    public void onFinishInputDialog(int inputNumber) {
        Storage.timePickerStep = inputNumber;
        btnTimePickerInterval.setText(String.valueOf(inputNumber));
    }

    //важно помнить, что дефолт ToggleButton.isChecked() это false. текст false это textOff
    //имена переменных же, как и их предпочтительные значения наоборот предполагают true
    //также сбивает с толку предпросмотр XML, показывающий textOff
    //чтобы устранить это противоречие к опросу ToggleButton и его инициализации добавлен !
    public void onTBListHintClick(View view)       { Storage.showListHint     = !((ToggleButton) view).isChecked(); }
    public void onTBListsSortOrderClick(View view) { Storage.youngIsOnTop     = !((ToggleButton) view).isChecked(); }
    public void onTBTimePickClicksClick(View view) { Storage.twoTapTimePick   = !((ToggleButton) view).isChecked(); }
    public void onTBHideTaxometerClick(View view)  { Storage.hideTaxometer    = !((ToggleButton) view).isChecked(); }

    public void onTBInputStyleClick(View view) {
        if (!((ToggleButton) view).isChecked()) Storage.inputStyle = InputStyle.SPINNER;
        else Storage.inputStyle = InputStyle.BUTTON;
        btnTimePickerInterval.setEnabled(!((ToggleButton) view).isChecked());
    }

    public void onParksAndBillingsClick(View p1) {
        startActivity(new Intent(this, Settings4ParksAndBillingsActivity.class));
    }
//    public void onExportImportShiftsClick(View p1) {
//        Toast.makeText(this, "когда-нибудь это будет открывать окно экспорта", Toast.LENGTH_SHORT).show();
//    }
//    public void onSocialsClick(View p1) {
//        Toast.makeText(this, "когда-нибудь это будет вести к ссылкам на наши странички", Toast.LENGTH_SHORT).show();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Storage.saveSettings(context);
        if (MainActivity.currentShift != null) {
            MainActivity.sortOrdersStorage();
            MainActivity.sortShiftsStorage();
            if (FirstScreenActivity.shiftAdapterMA != null) FirstScreenActivity.shiftAdapterMA.notifyDataSetChanged();
//            изменения типа ввода кнопка/спиннер обрабатываются в OrderFragment.onResume()
        }
    }
}
