package it.unive.raccoltapp.model.maputils;import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent; // Importa ViewParent
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
        // Ottieni il genitore
        ViewParent parent = getParent();

        // Controlla che il genitore non sia null prima di usarlo
        if (parent != null) {
            // "Dici" al NestedScrollView di NON intercettare gli eventi di tocco
            parent.requestDisallowInterceptTouchEvent(true);
        }

        return super.dispatchTouchEvent(ev);
    }
}
