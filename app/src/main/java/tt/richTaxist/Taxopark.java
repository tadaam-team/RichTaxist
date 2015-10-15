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
        this.taxoparkID     = getNextID();
        this.taxoparkName   = taxoparkName;
        this.isDefault      = isDefault;
        this.defaultBilling = defaultBilling;
    }

    public Taxopark(int taxoparkID, String taxoparkName, boolean isDefault, int defaultBilling) {
        this.taxoparkID     = taxoparkID;
        this.taxoparkName   = taxoparkName;
        this.isDefault      = isDefault;
        this.defaultBilling = defaultBilling;
    }

    private int getNextID() {
        Taxopark lastTaxopark = TaxoparksSQLHelper.dbOpenHelper.getLastTaxopark();
        return (lastTaxopark == null) ? 1 : lastTaxopark.taxoparkID + 1;
        //младший id = 1 гарантирует свободность 0-индекса для списков с "- - -"
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
