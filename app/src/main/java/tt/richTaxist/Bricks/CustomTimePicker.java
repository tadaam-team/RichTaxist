package tt.richTaxist.Bricks;

/**
 * Created by Tau on 12.07.2015.
 */

import android.content.Context;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import tt.richTaxist.Storage;

public class CustomTimePicker extends TimePicker {
    private OnTimeChangedListener timeChangedListener;

    public CustomTimePicker(Context context) {
        super(context);
        try {
            Class<?> classForId = Class.forName("com.android.internal.R$id");
            Field field = classForId.getField("minute");

            NumberPicker minuteSpinner = (NumberPicker) this.findViewById(field.getInt(null));
            minuteSpinner.setMaxValue((60 / Storage.timePickerStep) - 1);
            List<String> displayedValues = new ArrayList<>();
            for (int i = 0; i < 60; i += Storage.timePickerStep)
                displayedValues.add(String.format("%02d", i));
            minuteSpinner.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int maxMinuteIndex() {
        return (60 / Storage.timePickerStep) - 1;
    }

    @Override
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        super.setOnTimeChangedListener(internalTimeChangedListener);
        this.timeChangedListener = onTimeChangedListener;
    }

    @Override
    public Integer getCurrentMinute() {
        return super.getCurrentMinute() * Storage.timePickerStep;
    }

    @Override
    public void setCurrentMinute(Integer currentMinute) {
        int cleanMinute = currentMinute / Storage.timePickerStep;
        if(currentMinute % Storage.timePickerStep > 0) {
            if(cleanMinute == maxMinuteIndex()) {
                cleanMinute = 0;
                setCurrentHour(getCurrentHour()+1);
            } else {
                cleanMinute++;
            }
        }
        super.setCurrentMinute(cleanMinute);
    }

    // We want to proxy all the calls to our member variable OnTimeChangedListener with our own
    // internal listener in order to make sure our overridden getCurrentMinute is called. Without
    // this some versions of android return the underlying minute index.
    private OnTimeChangedListener internalTimeChangedListener = new OnTimeChangedListener() {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            timeChangedListener.onTimeChanged(view, getCurrentHour(), getCurrentMinute());
        }
    };
}