package tt.richTaxist.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import java.util.ArrayList;
import tt.richTaxist.Units.Taxopark;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.R;

public class TaxoparksFragment extends ListFragment {
    private static final String LOG_TAG = "TaxoparksFragment";
    ArrayList<Taxopark> taxoparks = new ArrayList<>();
    private Context context;
    private ArrayAdapter taxoparksAdapter;
    public TaxoparksFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        taxoparks.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        taxoparksAdapter = new TaxoparksAdapter(context);
        setListAdapter(taxoparksAdapter);

        View rootView = inflater.inflate(R.layout.fragment_taxoparks, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        (rootView.findViewById(R.id.addTaxopark)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Taxopark taxopark = new Taxopark("", false, 0);
                taxoparks.add(taxopark);
                TaxoparksSQLHelper.dbOpenHelper.create(taxopark);
                taxoparksAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }


    class TaxoparksAdapter extends ArrayAdapter<Taxopark> {
        public TaxoparksAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, taxoparks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry_taxoparks_editable, parent, false);
            }
            final Taxopark taxopark = getItem(position);

            final EditText etTaxoparkName = (EditText) convertView.findViewById(R.id.etTaxoparkName);
            etTaxoparkName.setText(taxopark.taxoparkName);
            //моментом сохранения считается нажатие enter
            etTaxoparkName.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveTaxoparkName(taxopark, etTaxoparkName);
                        return true;
                    }
                    return false;
                }
            });

            //если таксопарк по умолчанию не назначен ни для одного члена списка, назначим текущий парк парком по умолчанию
            boolean defaultTaxoparkDefined = false;
            for (Taxopark taxoparkIter : TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks())
                if (taxoparkIter.isDefault) defaultTaxoparkDefined = true;
            if (!defaultTaxoparkDefined) taxopark.isDefault = true;

            RadioButton rbDefault = (RadioButton) convertView.findViewById(R.id.rbDefault);
            rbDefault.setChecked(taxopark.isDefault);
            rbDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //сбрасываем все таксопарки в недефолтные и устанавливаем дефолтным текущий
                    for (Taxopark taxoparkIter : taxoparks) {
                        taxoparkIter.isDefault = false;
                        TaxoparksSQLHelper.dbOpenHelper.update(taxoparkIter);
                    }
                    taxopark.isDefault = true;
                    TaxoparksSQLHelper.dbOpenHelper.update(taxopark);
                    notifyDataSetChanged();
                }
            });

            (convertView.findViewById(R.id.delTaxopark)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    taxoparks.remove(taxopark);
                    TaxoparksSQLHelper.dbOpenHelper.remove(taxopark);
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
        
        private void saveTaxoparkName(Taxopark currentTaxopark, EditText taxoparkName){
            String newName = taxoparkName.getText().toString();
            boolean isInTheList = false;
            for (Taxopark taxoparkIter : TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks()) {
                if (newName.equals(taxoparkIter.taxoparkName) && currentTaxopark.taxoparkID != taxoparkIter.taxoparkID) isInTheList = true;
            }
            if (isInTheList) {
                Toast.makeText(context, getResources().getString(R.string.taxopark) + " " + String.valueOf(newName) + " " +
                        getResources().getString(R.string.isInTheList), Toast.LENGTH_SHORT).show();
                taxoparkName.setText("");
            }
            else {
                currentTaxopark.taxoparkName = newName;
                TaxoparksSQLHelper.dbOpenHelper.update(currentTaxopark);
            }
        }
    }
}
