package tt.richTaxist;

import android.content.Context;

public enum TypeOfPayment {
    CASH(0, R.string.payTypeCash),
    CARD(1, R.string.payTypeCard),
    TIP(2, R.string.payTypeTip);

    public final int id;
    private final int captionId;

    TypeOfPayment(int id, int captionId) {
        this.id = id;
        this.captionId = captionId;
    }

    public static TypeOfPayment getById(int id) {
        for (TypeOfPayment x : TypeOfPayment.values()) {
            if (x.id == id) return x;
        }
        throw new IllegalArgumentException();
    }

    public String getDescription(Context context) {
        return context.getString(captionId);
    }
}
