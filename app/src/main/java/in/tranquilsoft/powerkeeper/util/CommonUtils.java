package in.tranquilsoft.powerkeeper.util;

import android.util.DisplayMetrics;

/**
 * Created by gparmar on 31/05/17.
 */

public class CommonUtils {
    public static int getDPI(double size, DisplayMetrics metrics){
        return (int)((size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT);
    }
}
