package tt.richTaxist.Bricks;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DF_ListInput extends DialogFragment {
    public DF_ListInput() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] catNamesArray = {"1", "2", "5", "10", "15"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Выберите число");
        builder.setItems(catNamesArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ListInputDialogListener activity = (ListInputDialogListener) getActivity();
                activity.onFinishInputDialog(Integer.parseInt(catNamesArray[which]));
            }
        });
        return builder.create();
    }

    public interface ListInputDialogListener {
        void onFinishInputDialog(int inputNumber);
    }
}