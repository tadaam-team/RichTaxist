package tt.richTaxist;


/**
 * Created by Tau on 23.09.2015.
 */
public class Taxopark {
    public int taxoparkID;
    public String taxoparkName;
    public boolean isDefault;
    public int defaultBilling;

    public Taxopark(int taxoparkID, String taxoparkName, boolean isDefault, int defaultBilling) {
        this.taxoparkID = taxoparkID;
        this.taxoparkName = taxoparkName;
        this.isDefault = isDefault;
        this.defaultBilling = defaultBilling;
    }

    public static int getNextTaxoparkID(){
        int id = 0;
        if (MainActivity.taxoparks != null) {
            for (Taxopark taxopark : MainActivity.taxoparks)
                if (taxopark.taxoparkID > id) id = taxopark.taxoparkID;
            id++;
        }
        return id;
    }

    public static Taxopark getTaxoparkByID(int ID){
        Taxopark result = null;
        if (MainActivity.taxoparks != null)
            for (Taxopark taxopark: MainActivity.taxoparks)
                if (taxopark.taxoparkID == ID) result = taxopark;
        return result;
    }

    @Override
    public String toString() {
        return taxoparkName;
//        return String.format("ID: %d, имя: %s, основной?: %s, способ расчетов: %d", taxoparkID, taxoparkName, isDefault,  defaultBilling);
    }
}
