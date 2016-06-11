package tt.richCabman.fragments.bricks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import tt.richCabman.util.Constants;
import tt.richCabman.R;
/**
 * Created by TAU on 21.05.2016.
 */
public class SingleChoiceListDF extends DialogFragment {
    private SingleChoiceListDFInterface listener;
    private long selectedObjectID;
    private int positionInRVList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //проверим, реализован ли нужный интерфейс родительским фрагментом или активностью
            listener = (SingleChoiceListDFInterface) getParentFragment();
            if (listener == null) {
                listener = (SingleChoiceListDFInterface) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SingleChoiceListDFInterface");
        }

        selectedObjectID = getArguments().getLong(Constants.OBJECT_ID_EXTRA, -1);
        positionInRVList = getArguments().getInt(Constants.POSITION_EXTRA, -1);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.chooseShiftOrOrderAction)
                .setItems(R.array.shiftOrOrderAction, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        listener.processListItem(selectedObjectID, which, positionInRVList);
                    }
                });
        return builder.create();
    }

    public interface SingleChoiceListDFInterface{
        void processListItem(long selectedObjectID, int selectedActionID, int positionInRVList);
    }
}