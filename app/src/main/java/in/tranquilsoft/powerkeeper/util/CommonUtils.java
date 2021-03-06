package in.tranquilsoft.powerkeeper.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import in.tranquilsoft.powerkeeper.R;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;

/**
 * Created by gparmar on 31/05/17.
 */

public class CommonUtils {
    private static final String TAG = "CommonUtils";

    public static int getDPI(double size, DisplayMetrics metrics) {
        int result = (int) ((size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT);

        if (result == 0) {
            //This is because if it is a width then if it uses 0 it occupies the whole parent.
            return 1;
        } else {
            return result;
        }
    }

    public static void putSharedPref(Context context, String name, Object object) {
        SharedPreferences prefs = context.getSharedPreferences("PowerKeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(name, object.toString());
        editor.apply();
    }

    public static String getSharedPref(Context context, String name, String defaultVal) {
        SharedPreferences prefs = context.getSharedPreferences("PowerKeeper", Context.MODE_PRIVATE);
        return prefs.getString(name, defaultVal);
    }

    public static Date startOfToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        return today.getTime();
    }

    public static Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    public static Date startOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static Date startOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static Date getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    public static boolean isDatesSame(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public static String exportData(Context context) {
        Cursor cursor = PowerKeeperDao.getInstance(context).queryAll();
        if (cursor != null) {
            File exportDir = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.EXPORT_FOLDER);
            long freeBytesInternal = new File(context.getApplicationContext().getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
            long megAvailable = freeBytesInternal / 1048576;
            boolean memoryErr = false;
            if (megAvailable < 0.1) {
                Toast.makeText(context, "There is no storage. Storage present" + megAvailable,
                        Toast.LENGTH_LONG).show();
                memoryErr = true;
            } else {
                String exportDirStr = exportDir.toString();// to show in dialogbox
                Log.v(TAG, "exportDir path::" + exportDir);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH_mm_ss");
                File exportFile = new File(exportDir, "powerkeeper" +
                        (sdf.format(new Date())) + ".csv");
                FileWriter out = null;
                try {
                    out = new FileWriter(exportFile);
                    out.write("Time,Description\n");
                    while (cursor.moveToNext()) {
                        String desc = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN));
                        String ts = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN));
                        Timestamp timestamp = Timestamp.valueOf(ts);

                        out.write(Constants.DB_LONG_FORMAT.format(new Date(timestamp.getTime())) + "," + desc + "\n");
                    }
                    out.flush();
                    return exportFile.getName();


                } catch (IOException e) {
                    Log.e(TAG, "", e);



                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
        return null;
    }

    public static Bitmap createBitmapFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public static String getZeroBufferedInt(int i) {
        if (i < 10) {
            return "0"+i;
        }
        else {
            return i+"";
        }
    }
}
