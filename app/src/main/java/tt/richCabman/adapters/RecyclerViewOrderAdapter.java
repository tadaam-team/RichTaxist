package tt.richCabman.adapters;

import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import tt.richCabman.R;
import tt.richCabman.model.TypeOfPayment;
import tt.richCabman.model.Order;
/**
 * Created by TAU on 18.04.2016.
 */
public class RecyclerViewOrderAdapter extends RecyclerView.Adapter<ViewHolder> {
    private ArrayList<Order> orders;
    private Listener listener;

    public RecyclerViewOrderAdapter(ArrayList<Order> orders) {
        this.orders = orders;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_entry_recycler, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position){
        //Заполнение заданного представления данными
        CardView cardView = holder.getCardView();
        Resources res = cardView.getContext().getResources();
        Locale locale = res.getConfiguration().locale;
        final Order order = orders.get(position);

        //назначим картинку каждой строке списка
        ImageView entryIcon = (ImageView) cardView.findViewById(R.id.entryIcon);
        if (order != null) {
            if (TypeOfPayment.CASH.id == order.typeOfPaymentID)  entryIcon.setImageResource(R.drawable.ic_cash);
            if (TypeOfPayment.CARD.id == order.typeOfPaymentID)  entryIcon.setImageResource(R.drawable.ic_card);
            if (TypeOfPayment.TIP.id == order.typeOfPaymentID)   entryIcon.setImageResource(R.drawable.ic_tip);
        }

        //установим, какие данные из Order отобразятся в полях списка
        TextView tv_Main = (TextView) cardView.findViewById(R.id.tv_Main);
        TextView tv_Additional = (TextView) cardView.findViewById(R.id.tv_Additional);
        if (order != null) {
            Calendar arrivalCal = Calendar.getInstance();
            arrivalCal.setTime(order.arrivalDateTime);
            tv_Main.setText(String.format(locale, "%02d:%02d",
                    arrivalCal.get(Calendar.HOUR_OF_DAY),
                    arrivalCal.get(Calendar.MINUTE)));
            tv_Additional.setText(String.format(locale, "%d", order.price));
        }

        //установим слушатели
        LinearLayout viewStub = (LinearLayout) cardView.findViewById(R.id.viewStub);
        viewStub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(orders.get(holder.getAdapterPosition()));
                }
            }
        });

        ImageView moreIcon = (ImageView) cardView.findViewById(R.id.moreIcon);
        moreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClickMore(orders.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount(){
        return orders.size();
    }

    //crucial method, because notifyDataSetChanged() called from outside of adapter magically doesn't work for rv
    public void setOrders(ArrayList<Order> orders){
        this.orders = orders;
        notifyDataSetChanged();
    }

    public void addOrderToList(Order order, int position) {
        orders.add(position, order);
        notifyItemInserted(position);
    }

    public void removeOrderFromList(Order order, int positionInRVList){
        orders.remove(order);
        notifyItemRemoved(positionInRVList);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public interface Listener {
        void onClick(Object selectedObject);
        void onClickMore(Object selectedObject, int positionInRVList);
    }
}
