package in.tranquilsoft.powerkeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.tranquilsoft.powerkeeper.adapter.PowerDetailAdapter;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.util.Constants;

public class PowerDetailsActivity extends AppCompatActivity {
    private static final String TAG = "PowerDetailsActivity";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.adView)
    AdView adView;

    private PowerDetailAdapter adapter;
    private String date;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("DATA_CHANGED")){
                Log.d(TAG, "in onReceive. Setting new cursor and notifying dataset changed.");
                adapter.setCursor(PowerKeeperDao.getInstance(PowerDetailsActivity.this).getAllDates());
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_details);
        ButterKnife.bind(this);
        setTitle(getString(R.string.details_title));

        date = getIntent().getStringExtra(Constants.DETAIL_SELECTED_DATE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PowerDetailAdapter(this, PowerKeeperDao.getInstance(this).getValuesForDate(date, false));
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                PowerKeeperDao.getInstance(PowerDetailsActivity.this).delete(((PowerDetailAdapter.PowerDetailViewHolder) viewHolder).id);
                adapter.setCursor(PowerKeeperDao.getInstance(PowerDetailsActivity.this).getValuesForDate(date, false));
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        registerReceiver(receiver, new IntentFilter("DATA_CHANGED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
