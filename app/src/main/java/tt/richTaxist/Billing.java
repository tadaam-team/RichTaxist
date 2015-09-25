package tt.richTaxist;

/**
 * Created by Tau on 25.09.2015.
 */
public class Billing {
    int billingID;
    String billingName;
    float commission;

    public Billing(int billingID, String billingName, float commission) {
        this.billingID = billingID;
        this.billingName = billingName;
        this.commission = commission;
    }

    public static int getNextBillingID(){
        int id = 0;
        if (MainActivity.billings != null) {
            for (Billing billing : MainActivity.billings)
                if (billing.billingID > id) id = billing.billingID;
            id++;
        }
        return id;
    }

    public static Billing getBillingByID(int ID){
        Billing result = null;
        if (MainActivity.billings != null)
            for (Billing billing: MainActivity.billings)
                if (billing.billingID == ID) result = billing;
        return result;
    }

    @Override
    public String toString() {
        return billingName;
//        return String.format("ID: %d, имя: %s, taxoparkID, taxoparkName);
    }
}
