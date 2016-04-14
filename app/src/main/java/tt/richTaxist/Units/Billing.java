package tt.richTaxist.Units;

import tt.richTaxist.DB.BillingsSQLHelper;

/**
 * Created by Tau on 25.09.2015.
 */
public class Billing {
    public int billingID;
    public String billingName;
    public float commission;

    public Billing(String billingName, float commission) {
        Billing lastBilling = BillingsSQLHelper.dbOpenHelper.getLastBilling();
        if (lastBilling == null) billingID = 0;
        else billingID = lastBilling.billingID + 1;

        this.billingName = billingName;
        this.commission = commission;
    }

    public Billing(int billingID) {
        this.billingID = billingID;
        billingName = "";
        commission = 0f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Billing that = (Billing) o;
        return billingID == that.billingID;
    }

    @Override
    public int hashCode() {
        return 31 * billingID;
    }

    @Override
    public String toString() {
        return billingName;
//        return String.format("ID: %d, имя: %s, комиссия: %f", billingID, billingName, commission);
    }
}
