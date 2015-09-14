package tt.richTaxist.Enums;

import tt.richTaxist.MainActivity;
import tt.richTaxist.R;

/**
 * Created by Tau on 14.09.2015.
 */
public enum ActivityState {
    LAND_2_1    (0, R.string.activityStateLAND_2_1),
    LAND_2      (1, R.string.activityStateLAND_2),
    PORT_1      (2, R.string.activityStatePORT_1),
    PORT_2      (3, R.string.activityStatePORT_2);

    public final int id;
    private final int captionId;

    ActivityState(int id, int captionId) {
        this.id = id;
        this.captionId = captionId;
    }

    public static ActivityState getById(int id){
        for (ActivityState x: ActivityState.values()){
            if (x.id == id) return x;
        }
        throw new IllegalArgumentException();
    }

//    @Override
//    public String toString() {
//        return MainActivity.context.getString(captionId);
//    }
}
