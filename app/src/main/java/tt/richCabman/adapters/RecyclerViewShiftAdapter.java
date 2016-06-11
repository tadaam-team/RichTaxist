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
import tt.richCabman.model.Shift;
/**
 * Created by TAU on 18.04.2016.
 */
public class RecyclerViewShiftAdapter extends RecyclerView.Adapter<ViewHolder> {
    private ArrayList<Shift> shifts;
    private Listener listener;

    public RecyclerViewShiftAdapter(ArrayList<Shift> shifts) {
        this.shifts = shifts;
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
        Shift shift = shifts.get(position);

        //назначим картинку каждой строке списка
        ImageView entryIcon = (ImageView) cardView.findViewById(R.id.entryIcon);
        if (shift != null) {
            if (shift.isClosed()) {
                entryIcon.setImageResource(R.drawable.ic_lock_closed_black);
            } else {
                entryIcon.setImageResource(R.drawable.ic_lock_open_black);
            }
        }

        //установим, какие данные из Shift/Order отобразятся в полях списка
        TextView tv_Main = (TextView) cardView.findViewById(R.id.tv_Main);
        TextView tv_Additional = (TextView) cardView.findViewById(R.id.tv_Additional);
        if (shift != null) {
            Calendar beginShiftCal = Calendar.getInstance();
            beginShiftCal.setTime(shift.beginShift);
            tv_Main.setText(String.format(locale, "%02d.%02d %02d:%02d",
                    beginShiftCal.get(Calendar.DAY_OF_MONTH),
                    beginShiftCal.get(Calendar.MONTH) + 1,
                    beginShiftCal.get(Calendar.HOUR_OF_DAY),
                    beginShiftCal.get(Calendar.MINUTE)));
            tv_Additional.setText(String.format(locale, res.getString(R.string.salaryUnofficialFormatter), shift.salaryUnofficial));
        }

        //установим слушатели
        LinearLayout viewStub = (LinearLayout) cardView.findViewById(R.id.viewStub);
        viewStub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(shifts.get(holder.getAdapterPosition()));
                }
            }
        });

        ImageView moreIcon = (ImageView) cardView.findViewById(R.id.moreIcon);
        moreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClickMore(shifts.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount(){
        return shifts.size();
    }

    //crucial method, because notifyDataSetChanged() called from outside of adapter magically doesn't work for rv
    public void setShifts(ArrayList<Shift> shifts){
        this.shifts = shifts;
        notifyDataSetChanged();
    }

    public void removeShiftFromList(Shift shift, int positionInRVList){
        shifts.remove(shift);
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
