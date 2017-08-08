package in.tranquilsoft.powerkeeper.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;

import in.tranquilsoft.powerkeeper.R;
import in.tranquilsoft.powerkeeper.adapter.DateCursorAdapter;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.model.Timekeeper;

/**
 * Created by gparmar on 08/08/17.
 */

public class RedGreenBarRenderer {
    private Context context;
    private double redDiff;
    private DisplayMetrics metrics;
    private int xEtchingWidth = -1;

    public RedGreenBarRenderer(Context context) {
        this.context = context;
        if (xEtchingWidth == -1) {
            metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float density = context.getResources().getDisplayMetrics().density;
            int dpHeight = (int) (metrics.heightPixels / density);
            int dpWidth = (int) (metrics.widthPixels / density);
            xEtchingWidth = (dpWidth - 20) / 24;
        }
    }

    public double renderRedAndGreens(DateCursorAdapter.MyViewHolder vh, ConstraintLayout constraintLayout,
                                    double accumulatedRediff, String date) throws ParseException {
        TextView dateVal = (TextView) constraintLayout.findViewById(R.id.date_val);
        dateVal.setText(Constants.SHORT_FORMAT.format(Constants.DB_SHORT_FORMAT.parse(date)));
        Cursor cursor = PowerKeeperDao.getInstance(context).getValuesForDate(date, true);
        Date rowDate = Constants.DB_SHORT_FORMAT.parse(date);
        if (cursor != null) {
            double accumulatedDiffInHours = 0;
            redDiff = 0;
            long currentTime = CommonUtils.startOfDay(rowDate).getTime();
            //int count = 0;
            boolean powerSupplyState = false;
            while (cursor.moveToNext()) {
                Timekeeper tk = Timekeeper.fromCursor(cursor);
                String desc = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN));
                String ts = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN));
                Timestamp timestamp = Timestamp.valueOf(ts);
                if (Constants.START_MESSAGE.equals(desc)) {
                    powerSupplyState = false;
                } else {
                    powerSupplyState = true;
                }
                long timediff = timestamp.getTime() - currentTime;
                double diffInHours = (double) timediff / 3600000;
                accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours,
                        powerSupplyState, constraintLayout, vh);

                currentTime = timestamp.getTime();
                //count++;
            }
            long endTime = 0;
            Date today = new Date();
            if (today.getYear() == rowDate.getYear() &&
                    today.getMonth() == rowDate.getMonth() &&
                    today.getDate() == rowDate.getDate()) {
                endTime = today.getTime();
            } else {
                endTime = CommonUtils.endOfDay(Constants.DB_SHORT_FORMAT.parse(date)).getTime();
            }
            double diffInHours = (double) (endTime - currentTime) / 3600000;
            accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours,
                    !powerSupplyState, constraintLayout, vh);
            TextView downtimeTV = (TextView) constraintLayout.findViewById(R.id.total_downtime);
            downtimeTV.setText(context.getString(R.string.total_downtime, new DecimalFormat("#.##").format(redDiff)));
            if (vh == null) {
                constraintLayout.removeView(constraintLayout.findViewById(R.id.right_arrow));
            }
        }
        for (int i = 0; i <= 24; i++) {
            View xetching = getXAxisEtching();
            TextView reading = (TextView) xetching.findViewById(R.id.x_reading);
            reading.setText(i + "");
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            lp.leftToLeft = R.id.events_container;
            lp.topToTop = R.id.events_container;
            lp.leftMargin = CommonUtils.getDPI(5 + i * xEtchingWidth, metrics);

            constraintLayout.addView(xetching, lp);
        }
        return redDiff;
    }

    private double renderBar(double accumulatedDiffInHours, double diffInHours,
                             boolean powerSupplyState, ConstraintLayout constraintLayout,
                             DateCursorAdapter.MyViewHolder vh
    ) {

        if (!powerSupplyState) {
            View bar = new View(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bar.setTranslationZ(0f);
            }
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(CommonUtils.getDPI(diffInHours * xEtchingWidth, metrics),
                    ConstraintLayout.LayoutParams.MATCH_PARENT);
            bar.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
            lp.leftToLeft = R.id.events_container;
            lp.topToTop = R.id.events_container;
            lp.bottomToBottom = R.id.events_container;
            lp.leftMargin = CommonUtils.getDPI(7 + accumulatedDiffInHours * xEtchingWidth
                    , metrics);
            constraintLayout.addView(bar, lp);
            if (vh != null) {
                vh.viewsToRemove.add(bar);
            }
            redDiff += diffInHours;
        } else {
            //bar.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }


        return accumulatedDiffInHours + diffInHours;
    }

    private View getXAxisEtching() {
        View etching = LayoutInflater.from(context).inflate(R.layout.x_axis_etching, null, false);
        return etching;
    }
}
