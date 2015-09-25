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
import android.widget.Toast;

public class BillingsFragment extends ListFragment {
    private static final String LOG_TAG = "BillingsFragment";
    private AppCompatActivity mActivity;
    private ArrayAdapter billingsAdapter;
    public BillingsFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
        billingsAdapter = new BillingsAdapter(mActivity);
        setListAdapter(billingsAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_billings, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        (rootView.findViewById(R.id.addBilling)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Billing billing = new Billing(Billing.getNextBillingID(), "", 0.0f);
                MainActivity.billings.add(billing);
                billingsAdapter.notifyDataSetChanged();
                if (OrderFragment.spnBillingAdapter != null) OrderFragment.spnBillingAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    class BillingsAdapter extends ArrayAdapter<Billing> {
        private final Context context;

        public BillingsAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, MainActivity.billings);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry_billings, parent, false);
            }
            //TODO: проверить корректность строки ниже
            final Billing billing = MainActivity.billings.get(position);

            final EditText billingName = (EditText) convertView.findViewById(R.id.billingName);
            billingName.setText(billing.billingName);
            //моментами сохранения считаются либо нажатие enter либо потеря фокуса EditText-ом
            billingName.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveBillingName(billingName, billing);
                        return true;
                    }
                    return false;
                }
            });
//            BillingName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View view, boolean hasFocus) {
//                    if (!hasFocus) saveBillingName(BillingName, Billing);
//                }
//            });
            final EditText etCommission = (EditText) convertView.findViewById(R.id.etCommission);
            etCommission.setText(String.valueOf(billing.commission));
            //моментами сохранения считаются либо нажатие enter либо потеря фокуса EditText-ом
            etCommission.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        billing.commission = Float.valueOf(etCommission.getText().toString());
                        return true;
                    }
                    return false;
                }
            });

            (convertView.findViewById(R.id.delBilling)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.billings.remove(billing);
                    notifyDataSetChanged();
                    if (OrderFragment.spnBillingAdapter != null) OrderFragment.spnBillingAdapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    private void saveBillingName(EditText billingName, Billing currentBilling){
        String newName = billingName.getText().toString();
        boolean isInTheList = false;
        for (Billing billing : MainActivity.billings) {
            if (newName.equals(billing.billingName) && currentBilling.billingID != billing.billingID) isInTheList = true;
        }
        if (isInTheList) {
            Toast.makeText(mActivity, "система расчетов " + String.valueOf(newName) + " уже есть в списке", Toast.LENGTH_SHORT).show();
            billingName.setText("");
        }
        else currentBilling.billingName = newName;
    }
}
