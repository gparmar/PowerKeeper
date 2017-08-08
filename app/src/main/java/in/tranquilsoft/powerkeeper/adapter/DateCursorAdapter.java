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
import in.tranquilsoft.powerkeeper.util.RedGreenBarRenderer;

/**
 * Created by gparmar on 03/08/17.
 */

public class DateCursorAdapter extends RecyclerView.Adapter<DateCursorAdapter.MyViewHolder> {
    private static final String TAG = "DateCursorAdapter";

    private Context context;
    private double redDiff;
    private Cursor cursor;
    private int selectedPosition = -1;
    RedGreenBarRenderer redGreenBarRenderer;

    public DateCursorAdapter(Context context, Cursor c) {
        this.cursor = c;
        this.context = context;
        redGreenBarRenderer = new RedGreenBarRenderer(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.date_item, parent, false);

        return new MyViewHolder(constraintLayout);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final ConstraintLayout constraintLayout = (ConstraintLayout) holder.itemView;

        cursor.moveToPosition(position);
        final String date = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.DateEntry.DATE_COLUMN));

        Log.d(TAG, "The const layout has " + constraintLayout.getChildCount() + " children for date:" + date);
        for (View view : holder.viewsToRemove) {
            Log.d(TAG, "Removing view");
            constraintLayout.removeView(view);
        }
        try {
            redGreenBarRenderer.renderRedAndGreens(holder, ((ConstraintLayout) holder.itemView), 0, date);
        } catch (ParseException e) {
            e.printStackTrace();
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
                FirebaseAnalytics.getInstance(context).logEvent("Detail_Opened", null);
                selectedPosition = position;
                ((HomeActivity) context).setSelectedPosition(position);
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

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public List<View> viewsToRemove = new ArrayList<>();

        public MyViewHolder(View itemView) {
            super(itemView);
        }

    }
}
