package tt.richTaxist;

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
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Shift;
/**
 * Created by TAU on 18.04.2016.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<? extends Object> objects;
    private AdapterDataType adapterDataType;
    private Listener listener;

    public RecyclerViewAdapter(ArrayList<? extends Object> objects, AdapterDataType adapterDataType) {
        this.objects = objects;
        this.adapterDataType = adapterDataType;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_entry_recycler, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position){
        //Заполнение заданного представления данными
        CardView cardView = holder.cardView;
        Resources res = cardView.getContext().getResources();
        Locale locale = res.getConfiguration().locale;
        Shift shift = null;
        Order order = null;
        if (adapterDataType == AdapterDataType.SHIFT) {
            shift = (Shift) objects.get(position);
        } else if(adapterDataType == AdapterDataType.ORDER) {
            order = (Order) objects.get(position);
        }

        //назначим картинку каждой строке списка
        ImageView entryIcon = (ImageView) cardView.findViewById(R.id.entryIcon);
        if (shift != null) {
            if (shift.isClosed()) {
                entryIcon.setImageResource(R.drawable.ic_lock_closed_black);
            } else {
                entryIcon.setImageResource(R.drawable.ic_lock_open_black);
            }
        } else if (order != null) {
            if (TypeOfPayment.CASH.id == order.typeOfPaymentID)  entryIcon.setImageResource(R.drawable.ic_cash);
            if (TypeOfPayment.CARD.id == order.typeOfPaymentID)  entryIcon.setImageResource(R.drawable.ic_card);
            if (TypeOfPayment.TIP.id == order.typeOfPaymentID)   entryIcon.setImageResource(R.drawable.ic_tip);
        }

        //установим, какие данные из Shift/Order отобразятся в полях списка
        TextView tv_Main = (TextView) cardView.findViewById(R.id.tv_Main);
        TextView tv_Additional = (TextView) cardView.findViewById(R.id.tv_Additional);
        if (shift != null) {
            Calendar beginShiftCal = Calendar.getInstance();
            beginShiftCal.setTime(shift.beginShift);
            tv_Main.setText(String.format(locale, "%02d.%02d %02d:%02d", beginShiftCal.get(Calendar.DAY_OF_MONTH), beginShiftCal.get(Calendar.MONTH) + 1,
                    beginShiftCal.get(Calendar.HOUR_OF_DAY), beginShiftCal.get(Calendar.MINUTE)));
            tv_Additional.setText(String.format(locale, res.getString(R.string.salaryUnofficialFormatter), shift.salaryUnofficial));
        } else if(order != null) {
            Calendar arrivalCal = Calendar.getInstance();
            arrivalCal.setTime(order.arrivalDateTime);
            tv_Main.setText(String.format(locale, "%02d:%02d", arrivalCal.get(Calendar.HOUR_OF_DAY), arrivalCal.get(Calendar.MINUTE)));
            tv_Additional.setText(String.format(locale, "%d", order.price));
        }

        //установим слушатели
        LinearLayout viewStub = (LinearLayout) cardView.findViewById(R.id.viewStub);
        viewStub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(objects.get(holder.getAdapterPosition()));
                }
            }
        });

        ImageView moreIcon = (ImageView) cardView.findViewById(R.id.moreIcon);
        moreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClickMore(objects.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount(){
        return objects.size();
    }

    //crucial method, because notifyDataSetChanged() called from outside of adapter magically doesn't work for rv
    public void setObjects(ArrayList<? extends Object> objects){
        this.objects = objects;
        notifyDataSetChanged();
    }

    public void removeObject(Object object, int positionInRVList){
        objects.remove(object);
        notifyItemRemoved(positionInRVList);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public interface Listener {
        void onClick(Object selectedObject);
        void onClickMore(Object selectedObject, int positionInRVList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public enum AdapterDataType {SHIFT, ORDER}
}
