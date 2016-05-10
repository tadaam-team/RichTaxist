package tt.richTaxist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Util.measureScreenWidth(getApplicationContext(), (ViewGroup) findViewById(R.id.activity_settings));

        //транслируем сохраненное состояние настроек в виджеты при открытии
        ((ToggleButton) findViewById(R.id.tbShowListHint))  .setChecked(!Util.showListHint);
        ((ToggleButton) findViewById(R.id.tbListsSortOrder)).setChecked(!Util.youngIsOnTop);
        ((ToggleButton) findViewById(R.id.tbTimePickClicks)).setChecked(!Util.twoTapTimePick);
        ((ToggleButton) findViewById(R.id.tbHideTaxometer)) .setChecked(!Util.hideTaxometer);
    }

    //важно помнить, что дефолт ToggleButton.isChecked() это false. текст false это textOff
    //имена переменных же, как и их предпочтительные значения наоборот предполагают true
    //также сбивает с толку предпросмотр XML, показывающий textOff
    //чтобы устранить это противоречие к опросу ToggleButton и его инициализации добавлен !
    public void onTBListHintClick(View view) {
        Util.showListHint = !((ToggleButton) view).isChecked();
        Util.saveSettingsToCloud();
    }

    public void onTBListsSortOrderClick(View view) {
        Util.youngIsOnTop = !((ToggleButton) view).isChecked();
        Util.saveSettingsToCloud();
    }

    public void onTBTimePickClicksClick(View view) {
        Util.twoTapTimePick = !((ToggleButton) view).isChecked();
        Util.saveSettingsToCloud();
    }

    public void onTBHideTaxometerClick(View view) {
        Util.hideTaxometer = !((ToggleButton) view).isChecked();
        Util.saveSettingsToCloud();
    }

    public void onParksAndBillingsClick(View p1) {
        startActivity(new Intent(this, Settings4ParksAndBillingsActivity.class));
    }
}
