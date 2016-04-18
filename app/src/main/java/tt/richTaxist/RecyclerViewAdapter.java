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
import tt.richTaxist.Units.Shift;
/**
 * Created by TAU on 18.04.2016.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<Shift> shifts;
    private Listener listener;

    public RecyclerViewAdapter(ArrayList<Shift> shifts) {
        this.shifts = shifts;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //Создание нового представления
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_entry_recycler, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        //Заполнение заданного представления данными
        CardView cardView = holder.cardView;
        Resources res = cardView.getContext().getResources();

        //назначим картинку каждой строке списка
        ImageView entryIcon = (ImageView) cardView.findViewById(R.id.entryIcon);
        if (shifts.get(position).isClosed()) {
            entryIcon.setImageResource(R.drawable.ic_lock_closed_black);
        } else {
            entryIcon.setImageResource(R.drawable.ic_lock_open_black);
        }

        //установим, какие данные из Shift отобразятся в полях списка
        TextView tv_Main = (TextView) cardView.findViewById(R.id.tv_Main);
        Calendar beginShiftCal = Calendar.getInstance();
        beginShiftCal.setTime(shifts.get(position).beginShift);
        tv_Main.setText(String.format(res.getString(R.string.shift)+" %02d.%02d",
                beginShiftCal.get(Calendar.DAY_OF_MONTH), beginShiftCal.get(Calendar.MONTH) + 1));

        TextView tv_Additional = (TextView) cardView.findViewById(R.id.tv_Additional);
        tv_Additional.setText(String.format(res.getString(R.string.salaryOfficialShort) + ": %d,\n" +
                        res.getString(R.string.salaryUnofficialShort) + ": %d",
                shifts.get(position).salaryOfficial, shifts.get(position).salaryUnofficial));

        //установим слушатели
        LinearLayout viewStub = (LinearLayout) cardView.findViewById(R.id.viewStub);
        viewStub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(shifts.get(position));
                }
            }
        });

        ImageView deleteIcon = (ImageView) cardView.findViewById(R.id.deleteIcon);
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClickDelete(shifts.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount(){
        return shifts.size();
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public interface Listener {
        void onClick(Shift selectedShift);
        void onClickDelete(Shift selectedShift);
    }
}
