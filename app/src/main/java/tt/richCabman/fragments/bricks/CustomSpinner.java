package tt.richCabman.fragments.bricks;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import tt.richCabman.database.DataSource;
import tt.richCabman.R;
import tt.richCabman.model.Billing;
import tt.richCabman.model.Taxopark;
/**
 * Created by TAU on 27.04.2016.
 */
public class CustomSpinner extends Spinner {
    public long taxoparkID = -1;
    public long billingID = -1;
    public long monthID = -1;
    private ArrayAdapter<Taxopark> spnTaxoparkAdapter;
    private ArrayAdapter<Billing> spnBillingAdapter;
    private DataSource dataSource;

    public CustomSpinner(Context context) {
        super(context);
        dataSource = new DataSource(context.getApplicationContext());
    }
    //inflate a view from XML
    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        dataSource = new DataSource(context.getApplicationContext());
    }
    //inflate a view from XML and apply a class-specific base style
    public CustomSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dataSource = new DataSource(context.getApplicationContext());
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
                monthID = getSelectedItemId();
                break;
        }
    }

    public void setPositionOfSpinner(TypeOfSpinner typeOfSpinner, long id){
        switch (typeOfSpinner){
            case TAXOPARK:
                Taxopark taxopark;
                if (id == -2) {
                    //если получена команда обнулить состояние спиннера, возвращаем не просто первый по списку, а умолчание
                    taxopark = dataSource.getTaxoparksSource().getDefaultTaxopark();
                } else {
                    taxopark = dataSource.getTaxoparksSource().getTaxoparkByID(id);
                }
//                Logger.d("setPositionOfSpinner. taxopark: " + String.valueOf(taxopark));
                int tIndexInSpinner = 0;
                if (taxopark != null){
                    tIndexInSpinner = spnTaxoparkAdapter.getPosition(taxopark);
                }
                setSelection(tIndexInSpinner);
                break;

            case BILLING:
                Billing billing = dataSource.getBillingsSource().getBillingByID(id);
                int bIndexInSpinner = 0;
                if (billing != null){
                    bIndexInSpinner = spnBillingAdapter.getPosition(billing);
                }
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
                    Taxopark blankTaxopark = new Taxopark("- - -", false, 0);
                    blankTaxopark.taxoparkID = 0;
                    listOfTaxoparks.add(blankTaxopark);
                }
                listOfTaxoparks.addAll(dataSource.getTaxoparksSource().getAllTaxoparks());
                spnTaxoparkAdapter = new ArrayAdapter<>(getContext(), R.layout.list_entry_spinner, listOfTaxoparks);
                setAdapter(spnTaxoparkAdapter);
                if (!addBlankListEntry) {
                    setPositionOfSpinner(TypeOfSpinner.TAXOPARK, -2);
                }
                break;

            case BILLING:
                ArrayList<Billing> listOfBillings = new ArrayList<>();
                listOfBillings.addAll(dataSource.getBillingsSource().getAllBillings());
                spnBillingAdapter = new ArrayAdapter<>(getContext(), R.layout.list_entry_spinner, listOfBillings);
                setAdapter(spnBillingAdapter);
                setPositionOfSpinner(TypeOfSpinner.BILLING, 0);
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
