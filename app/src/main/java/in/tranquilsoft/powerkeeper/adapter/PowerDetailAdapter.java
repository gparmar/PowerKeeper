package in.tranquilsoft.powerkeeper.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import in.tranquilsoft.powerkeeper.DataAdapter;
import in.tranquilsoft.powerkeeper.R;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.util.Constants;

/**
 * Created by gparmar on 04/08/17.
 */

public class PowerDetailAdapter extends RecyclerView.Adapter<PowerDetailAdapter.PowerDetailViewHolder> {
    private Context context;
    private Cursor cursor;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public PowerDetailAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public PowerDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View container = LayoutInflater.from(context).inflate(R.layout.data_item, parent, false);
        return new PowerDetailViewHolder(container);
    }

    @Override
    public void onBindViewHolder(PowerDetailViewHolder holder, int position) {
        cursor.moveToPosition(position);
        String desc = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN));
        String ts = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN));
        Timestamp timestamp = Timestamp.valueOf(ts);
        if (Constants.START_MESSAGE.equals(desc)) {
            holder.started.setVisibility(View.VISIBLE);
            holder.stopped.setVisibility(View.GONE);
        } else {
            holder.started.setVisibility(View.GONE);
            holder.stopped.setVisibility(View.VISIBLE);
        }
        holder.timestamp.setText(sdf.format(new Date(timestamp.getTime())));
        holder.id = cursor.getLong(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry._ID));
    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }

    public class PowerDetailViewHolder extends RecyclerView.ViewHolder {
        public long id;
        View stopped;
        View started;
        TextView timestamp;
        public PowerDetailViewHolder(View itemView) {
            super(itemView);
            started = itemView.findViewById(R.id.power_started);
            stopped = itemView.findViewById(R.id.power_stopped);
            timestamp = (TextView) itemView.findViewById(R.id.time_stamp);
        }
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }
}
