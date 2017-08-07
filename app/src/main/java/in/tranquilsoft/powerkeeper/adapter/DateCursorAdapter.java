package in.tranquilsoft.powerkeeper.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.tranquilsoft.powerkeeper.HomeActivity;
import in.tranquilsoft.powerkeeper.PowerDetailsActivity;
import in.tranquilsoft.powerkeeper.R;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.model.Timekeeper;
import in.tranquilsoft.powerkeeper.util.CommonUtils;
import in.tranquilsoft.powerkeeper.util.Constants;

/**
 * Created by gparmar on 03/08/17.
 */

public class DateCursorAdapter extends RecyclerView.Adapter<DateCursorAdapter.MyViewHolder> {
    private static final String TAG = "DateCursorAdapter";
    private DisplayMetrics metrics;
    private int xEtchingWidth = -1;
    private Context context;
    private double redDiff;
    private Cursor cursor;
    private int selectedPosition = -1;

    public DateCursorAdapter(Context context, Cursor c) {
        this.cursor = c;
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

    private View getXAxisEtching() {
        View etching = LayoutInflater.from(context).inflate(R.layout.x_axis_etching, null, false);
        return etching;
    }

    private void renderRedAndGreens(MyViewHolder vh, ConstraintLayout constraintLayout,
                                    String date) throws ParseException {
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
            if (today.getYear()==rowDate.getYear() &&
                    today.getMonth()==rowDate.getMonth() &&
                    today.getDate()==rowDate.getDate()) {
                endTime = today.getTime();
            } else {
                endTime = CommonUtils.endOfDay(Constants.DB_SHORT_FORMAT.parse(date)).getTime();
            }
            double diffInHours = (double) (endTime - currentTime) / 3600000;
            accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours,
                    !powerSupplyState, constraintLayout, vh);
            TextView downtimeTV = (TextView) constraintLayout.findViewById(R.id.total_downtime);
            downtimeTV.setText(context.getString(R.string.total_downtime, new DecimalFormat("#.##").format(redDiff)));

        }
    }

    private double renderBar(double accumulatedDiffInHours, double diffInHours,
                             boolean powerSupplyState, ConstraintLayout constraintLayout,
                             MyViewHolder vh
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
            vh.viewsToRemove.add(bar);
            redDiff += diffInHours;
        } else {
            //bar.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }


        return accumulatedDiffInHours + diffInHours;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //String date = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.DateEntry.DATE_COLUMN));
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.date_item, parent, false);
        //TextView dateVal = (TextView) constraintLayout.findViewById(R.id.date_val);

//        try {
//            dateVal.setText(Constants.SHORT_FORMAT.format(Constants.DB_SHORT_FORMAT.parse(date)));
//            renderRedAndGreens(constraintLayout, date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        for (int i = 0; i <= 24; i++) {
//            View xetching = getXAxisEtching();
//            TextView reading = (TextView) xetching.findViewById(R.id.x_reading);
//            reading.setText(i + "");
//            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT);
//
//            lp.leftToLeft = R.id.events_container;
//            lp.topToTop = R.id.events_container;
//            lp.leftMargin = CommonUtils.getDPI(10 + i * xEtchingWidth, metrics);
//
//            constraintLayout.addView(xetching, lp);
//        }
        return new MyViewHolder(constraintLayout);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final ConstraintLayout constraintLayout = (ConstraintLayout) holder.itemView;

        cursor.moveToPosition(position);
        final String date = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.DateEntry.DATE_COLUMN));
        TextView dateVal = (TextView) holder.itemView.findViewById(R.id.date_val);
        Log.d(TAG, "The const layout has " + constraintLayout.getChildCount() + " children for date:"+date);
        for (View view : holder.viewsToRemove) {
            Log.d(TAG, "Removing view");
            constraintLayout.removeView(view);
        }
        try {
            dateVal.setText(Constants.SHORT_FORMAT.format(Constants.DB_SHORT_FORMAT.parse(date)));
            renderRedAndGreens(holder, ((ConstraintLayout) holder.itemView), date);
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
            lp.leftMargin = CommonUtils.getDPI(5 + i * xEtchingWidth, metrics);

            ((ConstraintLayout) holder.itemView).addView(xetching, lp);
        }
        if (selectedPosition > -1 && selectedPosition == position) {
            holder.itemView.setPressed(true);
        } else {
            holder.itemView.setPressed(false);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "date:" + date);
                Intent intent = new Intent(context, PowerDetailsActivity.class);
                intent.putExtra(Constants.DETAIL_SELECTED_DATE, date);
                context.startActivity(intent);
                FirebaseAnalytics.getInstance(context).logEvent("Detail_Opened",null);
                selectedPosition = position;
                ((HomeActivity)context).setSelectedPosition(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        List<View> viewsToRemove = new ArrayList<>();

        public MyViewHolder(View itemView) {
            super(itemView);
        }

    }
}
