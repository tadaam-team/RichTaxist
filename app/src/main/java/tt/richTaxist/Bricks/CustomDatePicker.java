package tt.richTaxist.Bricks;

/**
 * Created by Tau on 12.07.2015.
 */

import android.content.Context;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CustomDatePicker extends DatePicker {
    //no proxy unlike CustomTimePicker
    public CustomDatePicker(Context context) {
        super(context);
        try {
            Class<?> classForId = Class.forName("com.android.internal.R$id");
            Field field = classForId.getField("year");

            NumberPicker yearSpinner = (NumberPicker) this.findViewById(field.getInt(null));
            yearSpinner.setMinValue(115);//start year is 1900
            yearSpinner.setMaxValue(124);//start year is 1900
            List<String> displayedValues = new ArrayList<>();
            for (int i = 2015; i < 2025; i ++)//2015-2024
                displayedValues.add(String.format("%02d", i));
            yearSpinner.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}