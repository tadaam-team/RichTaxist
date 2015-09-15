package tt.richTaxist;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Tau on 27.06.2015.
 */
public class SwipeDetector implements View.OnTouchListener {
    public enum Action {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP,
        None // no action detected
    }

    private static final String LOG_TAG = "SwipeDetector";
    private static final int MIN_DISTANCE = 50;
    private float downX, downY, upX, upY;
    private Action mSwipeDetected = Action.None;

    public boolean swipeDetected() {
        return mSwipeDetected != Action.None;
    }
    public Action getAction() {
        return mSwipeDetected;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://нажали
                downX = event.getX();
                downY = event.getY();
                mSwipeDetected = Action.None;
                return false; // allow other events like Click to be processed

            case MotionEvent.ACTION_UP://отпустили
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                // horizontal swipe detection
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0) {
                        mSwipeDetected = Action.LEFT_TO_RIGHT;
                        return false;
                    }
                    if (deltaX > 0) {
                        mSwipeDetected = Action.RIGHT_TO_LEFT;
                        return false;
                    }
                }
                else if (Math.abs(deltaY) > MIN_DISTANCE) {
                    // vertical swipe detection up or down
                    if (deltaY < 0) {
                        mSwipeDetected = Action.TOP_TO_BOTTOM;
                        return false;
                    }
                    if (deltaY > 0) {
                        mSwipeDetected = Action.BOTTOM_TO_TOP;
                        return false;
                    }
                }
                else {
                    //Смещение меньше MIN_DISTANCE по обеим осям. Предполагаем, что это тап
                    Log.d(LOG_TAG, "callOnClick");
                }
                return false;
        }
        return false;
    }
}
