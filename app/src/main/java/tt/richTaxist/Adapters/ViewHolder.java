package tt.richTaxist.Adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
/**
 * Created by TAU on 07.06.2016.
 */
public class ViewHolder  extends RecyclerView.ViewHolder {
    private CardView cardView;
    public ViewHolder(CardView v) {
        super(v);
        cardView = v;
    }

    public CardView getCardView() {
        return cardView;
    }
}
