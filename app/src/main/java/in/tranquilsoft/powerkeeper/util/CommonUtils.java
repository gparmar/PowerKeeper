package in.tranquilsoft.powerkeeper.util;

import android.util.DisplayMetrics;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gparmar on 31/05/17.
 */

public class CommonUtils {

    public static int getDPI(double size, DisplayMetrics metrics){
        return (int)((size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static Date midnightOfToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY,0);
        today.set(Calendar.MINUTE,0);
        today.set(Calendar.SECOND,1);
        return today.getTime();
    }
}
