package tt.richTaxist;


import tt.richTaxist.DB.TaxoparksSQLHelper;

/**
 * Created by Tau on 23.09.2015.
 */
public class Taxopark {
    public int taxoparkID;
    public String taxoparkName;
    public boolean isDefault;
    public int defaultBilling;

    public Taxopark(String taxoparkName, boolean isDefault, int defaultBilling) {
        Taxopark lastTaxopark = TaxoparksSQLHelper.dbOpenHelper.getLastTaxopark();
        if (lastTaxopark == null) taxoparkID = 0;
        else taxoparkID = lastTaxopark.taxoparkID + 1;
        
        this.taxoparkName   = taxoparkName;
        this.isDefault      = isDefault;
        this.defaultBilling = defaultBilling;
    }

    public Taxopark(int taxoparkID) {
        this.taxoparkID = taxoparkID;
        taxoparkName = "";
        isDefault = false;
        defaultBilling = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Taxopark that = (Taxopark) o;
        return taxoparkID == that.taxoparkID;
    }

    @Override
    public int hashCode() {
        return 31 * taxoparkID;
    }

    @Override
    public String toString() {
        return taxoparkName;
//        return String.format("ID: %d, имя: %s, основной?: %s, способ расчетов: %d", taxoparkID, taxoparkName, isDefault,  defaultBilling);
    }
}
