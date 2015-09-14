package tt.richTaxist;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;

public class FirstScreenFragment extends Fragment implements View.OnClickListener {
    String LOG_TAG = "FirstScreenFragment";
    private OnFirstScreenFragmentInteractionListener mListener;
    private View rootView;

    public FirstScreenFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try { mListener = (OnFirstScreenFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFirstScreenFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //onDestroy() will not be called (but onDetach() still will be, because the fragment is being detached from its current activity).
        //onCreate(Bundle) will not be called since the fragment is not being re-created.
        //onAttach(Activity) and onActivityCreated(Bundle) will still be called.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_first_screen, container, false);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 2.0f);
        rootView.setLayoutParams(layoutParams);
//        Storage.measureScreenWidth(getActivity(), (ViewGroup) rootView);

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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFirstScreenFragmentInteractionListener {
        public void onButtonSelected(int buttonIndex);
    }
}
