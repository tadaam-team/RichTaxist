package tt.richTaxist.Bricks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import tt.richTaxist.Constants;
import tt.richTaxist.R;
/**
 * Created by TAU on 21.05.2016.
 */
public class SingleChoiceListDF extends DialogFragment {
    private SingleChoiceListDFInterface mListener;
    private long selectedObjectID;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try { mListener = (SingleChoiceListDFInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OrdersListInterface");
        }
        selectedObjectID = getArguments().getLong(Constants.OBJECT_ID_EXTRA, -1);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.chooseShiftOrOrderAction)
                .setItems(R.array.shiftOrOrderAction, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        mListener.getSelectedAction(selectedObjectID, which);
                    }
                });
        return builder.create();
    }

    public interface SingleChoiceListDFInterface{
        void getSelectedAction(long selectedObjectID, int selectedActionID);
    }
}