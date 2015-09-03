package tt.richTaxist.Bricks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import tt.richTaxist.R;

public class DF_NumberInput extends DialogFragment implements TextView.OnEditorActionListener {
    private Activity superActivity;//ссылка на контейнер класса Activity, в котором сейчас находится этот фрагмент
    private EditText mEditText;
    public DF_NumberInput() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_number_input, container);
        superActivity = getActivity();
        mEditText = (EditText) rootView.findViewById(R.id.editTextName);
//        getDialog().setTitle("Введите фактический\nчек за бензин");

        rootView.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditorAction(null, EditorInfo.IME_ACTION_DONE, null);
            }
        });

        rootView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        // Show soft keyboard automatically
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditText.setOnEditorActionListener(this);

        return rootView;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            EditNameDialogListener activity = (EditNameDialogListener) superActivity;
            try { activity.onFinishEditDialog(Integer.parseInt(mEditText.getText().toString()));
            } catch (NumberFormatException e){
                Toast.makeText(superActivity, "только целое число, пожалуйста", Toast.LENGTH_SHORT).show();
            }
            getDialog().dismiss();
            return true;
        }
        return false;
    }

    public interface EditNameDialogListener {
        void onFinishEditDialog(int inputNumber);
    }
}