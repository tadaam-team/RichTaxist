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

        //applying saved settings state to controls when opening screen
        setToggleButtonState(R.id.tbListsSortOrder, Util.youngIsOnTop);
        setToggleButtonState(R.id.tbTimePickClicks, Util.twoTapTimePick);
        setToggleButtonState(R.id.tbShowTaxometer, Util.showTaxometer);
    }

    //it is important to remember that default of ToggleButton.isChecked() is false. text of defaulf is textOff
    private void setToggleButtonState(int tbID, boolean state){
        ToggleButton tb = ((ToggleButton) findViewById(tbID));
        if (tb != null) {
            tb.setChecked(state);
        }
    }

    public void onTBListsSortOrderClick(View view) {
        Util.youngIsOnTop = ((ToggleButton) view).isChecked();
        Util.saveSettingsToCloud();
    }

    public void onTBTimePickClicksClick(View view) {
        Util.twoTapTimePick = ((ToggleButton) view).isChecked();
        Util.saveSettingsToCloud();
    }

    public void onTBShowTaxometerClick(View view) {
        Util.showTaxometer = ((ToggleButton) view).isChecked();
        Util.saveSettingsToCloud();
    }

    public void onParksAndBillingsClick(View p1) {
        startActivity(new Intent(this, Settings4ParksAndBillingsActivity.class));
    }
}
