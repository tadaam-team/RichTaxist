package tt.richCabman.fragments.bricks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import tt.richCabman.R;

public class DF_ChooseFromList extends DialogFragment {
    private ListInputDialogListener mListener;

    public DF_ChooseFromList() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try { mListener = (ListInputDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] step = {"1", "2", "5", "10", "15"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.chooseNumber);
        builder.setItems(step, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mListener.onFinishInputDialog(Integer.parseInt(step[which]));
            }
        });
        return builder.create();
    }

    public interface ListInputDialogListener {
        void onFinishInputDialog(int inputNumber);
    }
}