package tt.richTaxist.Bricks;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import tt.richTaxist.DB.BillingsSQLHelper;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.R;
import tt.richTaxist.Units.Billing;
import tt.richTaxist.Units.Taxopark;
/**
 * Created by TAU on 27.04.2016.
 */
public class CustomSpinner extends Spinner {
    private static final String LOG_TAG = "CustomSpinner";
    public int taxoparkID = -1;
    public int billingID = -1;
    public int monthID = -1;
    private ArrayAdapter<Taxopark> spnTaxoparkAdapter;
    private ArrayAdapter<Billing> spnBillingAdapter;

    public CustomSpinner(Context context) {
        super(context);
    }
    //inflate a view from XML
    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    //inflate a view from XML and apply a class-specific base style
    public CustomSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void saveSpinner(TypeOfSpinner typeOfSpinner){
        switch (typeOfSpinner) {
            case TAXOPARK:
                taxoparkID = ((Taxopark) getSelectedItem()).taxoparkID;
                break;

            case BILLING:
                billingID = ((Billing) getSelectedItem()).billingID;
                break;

            case MONTH:
                monthID = (int) getSelectedItemId();
                break;
        }
    }

    public void setPositionOfSpinner(TypeOfSpinner typeOfSpinner, int id){
        switch (typeOfSpinner){
            case TAXOPARK:
                //если получена команда обнулить состояние спиннера, возвращаем не просто первый по списку, а умолчание
                if (id == -1) {
                    for (Taxopark taxoparkIter : TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks()) {
                        if (taxoparkIter.isDefault) {
                            taxoparkID = id = taxoparkIter.taxoparkID;
                        }
                    }
                }
                Taxopark taxopark = TaxoparksSQLHelper.dbOpenHelper.getTaxoparkByID(id);
                int tIndexInSpinner = 0;
                if (taxopark != null){
                    tIndexInSpinner = spnTaxoparkAdapter.getPosition(taxopark);
                }
                Log.d(LOG_TAG, "tIndexInSpinner: " + String.valueOf(tIndexInSpinner));
                setSelection(tIndexInSpinner);
                break;

            case BILLING:
                Billing billing = BillingsSQLHelper.dbOpenHelper.getBillingByID(id);
                int bIndexInSpinner = 0;
                if (billing != null){
                    bIndexInSpinner = spnBillingAdapter.getPosition(billing);
                }
                Log.d(LOG_TAG, "bIndexInSpinner: " + String.valueOf(bIndexInSpinner));
                setSelection(bIndexInSpinner);
                break;

            case MONTH:
                Toast.makeText(getContext(), "setPositionOfSpinner for MONTH is not defined", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void createSpinner(TypeOfSpinner typeOfSpinner, boolean addBlankListEntry){
        switch (typeOfSpinner) {
            case TAXOPARK:
                ArrayList<Taxopark> listOfTaxoparks = new ArrayList<>();
                if (addBlankListEntry){
                    listOfTaxoparks.add(0, new Taxopark(0, "- - -", false, 0));
                }
                listOfTaxoparks.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
                spnTaxoparkAdapter = new ArrayAdapter<>(getContext(), R.layout.list_entry_spinner, listOfTaxoparks);
                setAdapter(spnTaxoparkAdapter);
                setPositionOfSpinner(TypeOfSpinner.TAXOPARK, addBlankListEntry ? 0 : -1);
                break;

            case BILLING:
                ArrayList<Billing> listOfBillings = new ArrayList<>();
                listOfBillings.addAll(BillingsSQLHelper.dbOpenHelper.getAllBillings());
                spnBillingAdapter = new ArrayAdapter<>(getContext(), R.layout.list_entry_spinner, listOfBillings);
                setAdapter(spnBillingAdapter);
                setPositionOfSpinner(TypeOfSpinner.BILLING, addBlankListEntry ? 0 : -1);
                break;

            case MONTH:
                Toast.makeText(getContext(), "createTaxoparkSpinner for MONTH is not defined", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public enum TypeOfSpinner {
        TAXOPARK,
        BILLING,
        MONTH
    }
}
