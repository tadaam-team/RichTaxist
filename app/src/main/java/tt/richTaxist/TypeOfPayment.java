package tt.richTaxist;

/**
 * Created by AlexShredder on 01.07.2015.
 */
public enum TypeOfPayment {
    CASH (0,R.string.payTypeCash),
    CARD (1,R.string.payTypeCard),
    BONUS (2,R.string.payTypeBonus);

    public final int id;
    private final int captionId;

    TypeOfPayment(int id, int captionId) {
        this.id = id;
        this.captionId = captionId;
    }

    public static TypeOfPayment getById(int id){
        for (TypeOfPayment x: TypeOfPayment.values()){
            if (x.id==id) return x;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return MainActivity.context.getString(captionId);
    }
}
