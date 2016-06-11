package tt.richCabman.fragments;

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
import android.widget.Toast;
import java.util.ArrayList;
import tt.richCabman.database.DataSource;
import tt.richCabman.model.Billing;
import tt.richCabman.R;

public class BillingsFragment extends ListFragment {
    ArrayList<Billing> billings = new ArrayList<>();
    private Context context;
    private ArrayAdapter billingsAdapter;
    private DataSource dataSource;

    public BillingsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        dataSource = new DataSource(context);
        billings.addAll(dataSource.getBillingsSource().getAllBillings());
        billingsAdapter = new BillingsAdapter(context);
        setListAdapter(billingsAdapter);

        View rootView = inflater.inflate(R.layout.fragment_billings, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        (rootView.findViewById(R.id.addBilling)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Billing billing = new Billing("", 0.0f);
                billing.billingID = dataSource.getBillingsSource().create(billing);
                billings.add(billing);
                billingsAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    class BillingsAdapter extends ArrayAdapter<Billing> {

        public BillingsAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, billings);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry_billings_editable, parent, false);
            }
            final Billing billing = getItem(position);

            final EditText etBillingName = (EditText) convertView.findViewById(R.id.etBillingName);
            etBillingName.setText(billing.billingName);
            final EditText etCommission = (EditText) convertView.findViewById(R.id.etCommission);
            if (billing.commission != 0)
                etCommission.setText(String.valueOf(billing.commission));
            else
                etCommission.setText("");

            //моментом сохранения считается нажатие enter
            etBillingName.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveBilling(billing, etBillingName, etCommission);
                        return true;
                    }
                    return false;
                }
            });
            etCommission.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveBilling(billing, etBillingName, etCommission);
                        return true;
                    }
                    return false;
                }
            });

            (convertView.findViewById(R.id.delBilling)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dataSource.getOrdersSource().canWeDeleteBilling(billing)) {
                        billings.remove(billing);
                        dataSource.getBillingsSource().remove(billing);
                        notifyDataSetChanged();
                    }
                    else Toast.makeText(context, getResources().getString(R.string.billing) + " " +
                            getResources().getString(R.string.cantBeRemoved), Toast.LENGTH_LONG).show();
                }
            });
            return convertView;
        }

        private void saveBilling(Billing currentBilling, EditText billingName, EditText commission){
            String newName = billingName.getText().toString();
            boolean isInTheList = false;
            for (Billing billingIter : dataSource.getBillingsSource().getAllBillings())
                if (newName.equals(billingIter.billingName) && currentBilling.billingID != billingIter.billingID) isInTheList = true;
            if (isInTheList) {
                Toast.makeText(context, getResources().getString(R.string.billing) + " " + String.valueOf(newName) + " " +
                        getResources().getString(R.string.isInTheList), Toast.LENGTH_SHORT).show();
                billingName.setText("");
                commission.setText("");
            }
            else {
                currentBilling.billingName = newName;
                if (!"".equals(commission.getText().toString())) {
                    currentBilling.commission = Float.valueOf(commission.getText().toString());
                }
                dataSource.getBillingsSource().update(currentBilling);
            }
        }
    }
}
