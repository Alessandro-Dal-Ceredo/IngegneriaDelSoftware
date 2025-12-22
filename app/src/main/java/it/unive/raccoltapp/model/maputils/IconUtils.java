package it.unive.raccoltapp.model.maputils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class IconUtils {

    public static BitmapDescriptor getScaledIcon(Context context,
                                                 int drawableRes,
                                                 int sizeDp) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        if (drawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        float density = context.getResources()
                .getDisplayMetrics().density;
        int sizePx = (int) (sizeDp * density);

        Bitmap bitmap = Bitmap.createBitmap(
                sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, sizePx, sizePx);
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
