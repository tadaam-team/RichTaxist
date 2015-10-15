package tt.richTaxist;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;

public class FirstScreenFragment extends Fragment implements View.OnClickListener {
    String LOG_TAG = "FirstScreenFragment";
    private View rootView;

    public FirstScreenFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try { OnFirstScreenFragmentInteractionListener mListener = (OnFirstScreenFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFirstScreenFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_first_screen, container, false);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 2.0f);
        rootView.setLayoutParams(layoutParams);

        (rootView.findViewById(R.id.btnOpenLastShift))  .setOnClickListener(this);
        (rootView.findViewById(R.id.btnNewShift))       .setOnClickListener(this);

        Button btnOpenShift = (Button) rootView.findViewById(R.id.btnOpenShift);
        btnOpenShift.setOnClickListener(this);

        (rootView.findViewById(R.id.btnSettings))       .setOnClickListener(this);
        (rootView.findViewById(R.id.btnSignIn))         .setOnClickListener(this);
        (rootView.findViewById(R.id.btnChat))           .setOnClickListener(this);
        (rootView.findViewById(R.id.btnRoute))          .setOnClickListener(this);
        (rootView.findViewById(R.id.btnGrandTotals))    .setOnClickListener(this);
        (rootView.findViewById(R.id.btnExit))           .setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        ((OnFirstScreenFragmentInteractionListener) getActivity()).onButtonSelected(v.getId());
    }

    public interface OnFirstScreenFragmentInteractionListener {
        public void onButtonSelected(int buttonIndex);
    }
}
