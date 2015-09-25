package tt.richTaxist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

public class TaxoparksFragment extends ListFragment {
    private static final String LOG_TAG = "TaxoparksFragment";
    private AppCompatActivity mActivity;
    private ArrayAdapter taxoparksAdapter;
    public TaxoparksFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
        taxoparksAdapter = new TaxoparksAdapter(mActivity);
        setListAdapter(taxoparksAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_taxoparks, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        (rootView.findViewById(R.id.addTaxopark)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Taxopark taxopark = new Taxopark(Taxopark.getNextTaxoparkID(), "", false, 0);
                MainActivity.taxoparks.add(taxopark);
                taxoparksAdapter.notifyDataSetChanged();
                if (OrderFragment.spnTaxoparkAdapter != null) OrderFragment.spnTaxoparkAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    class TaxoparksAdapter extends ArrayAdapter<Taxopark> {
        private final Context context;

        public TaxoparksAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, MainActivity.taxoparks);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry_taxoparks, parent, false);
            }
            //TODO: проверить корректность строки ниже
            final Taxopark taxopark = MainActivity.taxoparks.get(position);

            final EditText taxoparkName = (EditText) convertView.findViewById(R.id.taxoparkName);
            taxoparkName.setText(taxopark.taxoparkName);
            //моментами сохранения считаются либо нажатие enter либо потеря фокуса EditText-ом
            taxoparkName.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveTaxoparkName(taxoparkName, taxopark);
                        return true;
                    }
                    return false;
                }
            });
//            taxoparkName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View view, boolean hasFocus) {
//                    if (!hasFocus) saveTaxoparkName(taxoparkName, taxopark);
//                }
//            });

            RadioButton rbDefault = (RadioButton) convertView.findViewById(R.id.rbDefault);
            rbDefault.setChecked(taxopark.isDefault);
            rbDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (Taxopark taxopark : MainActivity.taxoparks) {
                        taxopark.isDefault = false;
                    }
                    taxopark.isDefault = true;
                    notifyDataSetChanged();
                    if (OrderFragment.spnTaxoparkAdapter != null) OrderFragment.spnTaxoparkAdapter.notifyDataSetChanged();
                }
            });

            (convertView.findViewById(R.id.delTaxopark)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.taxoparks.remove(taxopark);
                    notifyDataSetChanged();
                    if (OrderFragment.spnTaxoparkAdapter != null) OrderFragment.spnTaxoparkAdapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    private void saveTaxoparkName(EditText taxoparkName, Taxopark currentTaxopark){
        String newName = taxoparkName.getText().toString();
        boolean isInTheList = false;
        for (Taxopark taxopark : MainActivity.taxoparks) {
            if (newName.equals(taxopark.taxoparkName) && currentTaxopark.taxoparkID != taxopark.taxoparkID) isInTheList = true;
        }
        if (isInTheList) {
            Toast.makeText(mActivity, "таксопарк " + String.valueOf(newName) + " уже есть в списке", Toast.LENGTH_SHORT).show();
            taxoparkName.setText("");
        }
        else currentTaxopark.taxoparkName = newName;
    }
}
