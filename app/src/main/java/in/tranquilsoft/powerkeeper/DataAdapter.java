package in.tranquilsoft.powerkeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import in.tranquilsoft.powerkeeper.data.PowerKeeperContract.*;
import in.tranquilsoft.powerkeeper.util.Constants;

/**
 * Created by gparmar on 24/05/17.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MyViewHolder>{
    private Cursor mCursor;
    private Context mContext;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");


    public DataAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View container = LayoutInflater.from(mContext).inflate(R.layout.data_item, parent, false);
        return new MyViewHolder(container);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String desc = mCursor.getString(mCursor.getColumnIndex(TimekeeperEntry.DESCRIPTION_COLUMN));
        String ts = mCursor.getString(mCursor.getColumnIndex(TimekeeperEntry.TIMESTAMP_COLUMN));
        Timestamp timestamp = Timestamp.valueOf(ts);
        if (Constants.START_MESSAGE.equals(desc)) {
            holder.started.setVisibility(View.VISIBLE);
            holder.stopped.setVisibility(View.GONE);
        } else {
            holder.started.setVisibility(View.GONE);
            holder.stopped.setVisibility(View.VISIBLE);
        }
        holder.timestamp.setText(sdf.format(new Date(timestamp.getTime())));
        holder.id = mCursor.getLong(mCursor.getColumnIndex(TimekeeperEntry._ID));
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void setCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        long id;
        View stopped;
        View started;
        TextView timestamp;
        public MyViewHolder(View itemView) {
            super(itemView);
            started = itemView.findViewById(R.id.power_started);
            stopped = itemView.findViewById(R.id.power_stopped);
            timestamp = (TextView) itemView.findViewById(R.id.time_stamp);
        }
    }
}
