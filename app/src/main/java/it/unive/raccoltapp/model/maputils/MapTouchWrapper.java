package it.unive.raccoltapp.model.maputils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MapTouchWrapper extends FrameLayout {

    public MapTouchWrapper(Context context) {
        super(context);
    }

    public MapTouchWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Tell NestedScrollView NOT to intercept touch events
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
