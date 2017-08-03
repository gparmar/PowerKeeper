package in.tranquilsoft.powerkeeper.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;

import in.tranquilsoft.powerkeeper.R;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.model.Timekeeper;
import in.tranquilsoft.powerkeeper.util.CommonUtils;
import in.tranquilsoft.powerkeeper.util.Constants;

/**
 * Created by gparmar on 03/08/17.
 */

public class DateCursorAdapter extends CursorAdapter {
    private DisplayMetrics metrics;
    private int xEtchingWidth = -1;
    private Context context;
    private double redDiff;

    public DateCursorAdapter(Context context, Cursor c) {
        super(context, c, false);
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

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        String date = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.DateEntry.DATE_COLUMN));
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.date_item, parent, false);
        TextView dateVal = (TextView) constraintLayout.findViewById(R.id.date_val);
        dateVal.setText(date);

        try {
            renderRedAndGreens(constraintLayout, date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int i = 0; i <= 24; i++) {
            View xetching = getXAxisEtching();
            TextView reading = (TextView) xetching.findViewById(R.id.x_reading);
            reading.setText(i + "");
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            lp.leftToLeft = R.id.events_container;
            lp.topToTop = R.id.events_container;
            lp.leftMargin = CommonUtils.getDPI(10 + i * xEtchingWidth, metrics);

            constraintLayout.addView(xetching, lp);
        }
        return constraintLayout;
    }

    private View getXAxisEtching() {
        View etching = LayoutInflater.from(context).inflate(R.layout.x_axis_etching, null, false);
        return etching;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String date = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.DateEntry.DATE_COLUMN));
        TextView dateVal = (TextView) view.findViewById(R.id.date_val);
        dateVal.setText(date);
        try {
            renderRedAndGreens(((ConstraintLayout) view) , date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int i = 0; i <= 24; i++) {
            View xetching = getXAxisEtching();
            TextView reading = (TextView) xetching.findViewById(R.id.x_reading);
            reading.setText(i + "");
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            lp.leftToLeft = R.id.events_container;
            lp.topToTop = R.id.events_container;
            lp.leftMargin = CommonUtils.getDPI(10 + i * xEtchingWidth, metrics);

            ((ConstraintLayout) view).addView(xetching, lp);
        }
    }

    private void renderRedAndGreens(ConstraintLayout constraintLayout,String date) throws ParseException {
        Cursor cursor = PowerKeeperDao.getInstance(context).getValuesForDate(date);
        if (cursor != null) {
            double accumulatedDiffInHours = 0;
            redDiff = 0;
            long currentTime = CommonUtils.startOfDay(Constants.SHORT_FORMAT.parse(date)).getTime();
            //int count = 0;
            boolean powerSupplyState = true;
            while (cursor.moveToNext()) {
                Timekeeper tk = Timekeeper.fromCursor(cursor);
                String desc = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN));
                String ts = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN));
                Timestamp timestamp = Timestamp.valueOf(ts);
//                if (count == 0) {
                    if (Constants.START_MESSAGE.equals(desc)) {
                        powerSupplyState = false;
                    } else {
                        powerSupplyState = true;
                    }
//                }
                long timediff = timestamp.getTime() - currentTime;
                double diffInHours = (double) timediff / 3600000;
                accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours,
                        powerSupplyState, constraintLayout);

                currentTime = timestamp.getTime();
                //count++;
            }
            long endTime = CommonUtils.endOfDay(Constants.SHORT_FORMAT.parse(date)).getTime();
            double diffInHours = (double) (endTime-currentTime) / 3600000;
            accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours,
                    !powerSupplyState, constraintLayout);
            TextView downtimeTV = (TextView)constraintLayout.findViewById(R.id.total_downtime);
            downtimeTV.setText(context.getString(R.string.total_downtime,new DecimalFormat("#.##").format(redDiff)));

        }
    }

    private double renderBar(double accumulatedDiffInHours, double diffInHours,
                             boolean powerSupplyState, ConstraintLayout constraintLayout
    ) {
        View bar = new View(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bar.setTranslationZ(0f);
        }
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(CommonUtils.getDPI(diffInHours * xEtchingWidth, metrics),
                ConstraintLayout.LayoutParams.MATCH_PARENT);
        if (!powerSupplyState) {
            bar.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
            redDiff += diffInHours;
        } else {
            bar.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }
        lp.leftToLeft = R.id.events_container;
        lp.topToTop = R.id.events_container;
        lp.bottomToBottom = R.id.events_container;
        //lp.rightToRight = R.id.constraint_layout;
        //lp.bottomMargin = CommonUtils.getDPI(30, metrics);
        //lp.rightMargin = CommonUtils.getDPI(100, metrics);
        lp.leftMargin = CommonUtils.getDPI(12 + accumulatedDiffInHours * xEtchingWidth
                , metrics);
        constraintLayout.addView(bar, lp);

        return accumulatedDiffInHours + diffInHours;
    }
}
