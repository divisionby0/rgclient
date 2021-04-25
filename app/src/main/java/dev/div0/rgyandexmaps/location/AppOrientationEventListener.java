package dev.div0.rgyandexmaps.location;

import android.content.Context;
import android.view.OrientationEventListener;

public class AppOrientationEventListener extends OrientationEventListener {

    public AppOrientationEventListener(Context context, int rate) {
        super(context, rate);

    }

    @Override
    public void onOrientationChanged(int i) {

    }
}
