package in.tranquilsoft.powerkeeper;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;

import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.util.CommonUtils;
import in.tranquilsoft.powerkeeper.util.Constants;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private PowerKeeperDao mPowerDao;
    private DataAdapter mAdapter;
    private FloatingActionButton fab;
    private ConstraintLayout constraintLayout;
    private int xEtchingWidth;
    private DisplayMetrics metrics;
    private boolean powerSupplyState;
    private BroadcastReceiver dataChangeRcvr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor cursor = mPowerDao.queryAll();
            mAdapter.setCursor(cursor);
        }
    };
    private View.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String email = prefs.getString("email", null);
            if (email == null || email.isEmpty()) {
                Toast.makeText(MainActivity.this, "First, please set your email in Preferences"
                        , Toast.LENGTH_LONG).show();
                return;
            }
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
// ...Irrelevant code for customizing the buttons and title
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.email_options, null);
            dialogBuilder.setMessage("Select the period of report to export");
            dialogBuilder.setView(dialogView);
            final RadioButton pastDay = (RadioButton) dialogView.findViewById(R.id.past_day);
            final RadioButton pastWeek = (RadioButton) dialogView.findViewById(R.id.past_week);
            final RadioButton pastMonth = (RadioButton) dialogView.findViewById(R.id.past_month);
            dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if (pastDay.isChecked()) {
                        Toast.makeText(MainActivity.this, "Past Day", Toast.LENGTH_SHORT).show();
                    } else if (pastWeek.isChecked()) {
                        Toast.makeText(MainActivity.this, "Past Week", Toast.LENGTH_SHORT).show();
                    } else if (pastMonth.isChecked()) {
                        Toast.makeText(MainActivity.this, "Past Month", Toast.LENGTH_SHORT).show();
                    }

                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
//                View options = getLayoutInflater().inflate(R.layout.email_options, null, false);
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder
//                        //.setMessage("Select the period of report")
//                        .setView(view);

//                AlertDialog dialog = builder.create();
//                dialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_layout);
        fab.setOnClickListener(fabOnClickListener);

        mPowerDao = PowerKeeperDao.getInstance(this);

        Cursor cursor = mPowerDao.queryAll();
        mAdapter = new DataAdapter(this, cursor);
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                Log.d(TAG, "delete the id:" + ((DataAdapter.MyViewHolder) viewHolder).id);
                PowerKeeperDao.getInstance(MainActivity.this).delete(((DataAdapter.MyViewHolder) viewHolder).id);
                mAdapter.setCursor(PowerKeeperDao.getInstance(MainActivity.this).queryAll());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String email = prefs.getString("email", "");
        Log.d(TAG, "email:" + email);

        //Setup the chart
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float density = getResources().getDisplayMetrics().density;
        int dpHeight = (int) (metrics.heightPixels / density);
        int dpWidth = (int) (metrics.widthPixels / density);
        xEtchingWidth = (dpWidth - 30) / 24;
        for (int i = 0; i <= 24; i++) {
            View xetching = getXAxisEtching();
            TextView reading = (TextView) xetching.findViewById(R.id.x_reading);
            reading.setText(i + "");
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            lp.leftToRight = R.id.constraint_layout;
            lp.topToBottom = R.id.x_axis;
            lp.leftMargin = CommonUtils.getDPI(28 + i * xEtchingWidth, metrics);

            constraintLayout.addView(xetching, lp);
        }

        //Put some dummy data into the DB
//        ContentValues cv = new ContentValues();
//        cv.put(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN, Constants.STOP_MESSAGE);
//        PowerKeeperDao.getInstance(this).insert(cv);
//        cv.put(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN, Constants.START_MESSAGE);
//        PowerKeeperDao.getInstance(this).insert(cv);
        renderChart();
    }

    private void renderChart(){
        Cursor cursor = PowerKeeperDao.getInstance(this).queryForToday();
        double accumulatedDiffInHours = 0;
        if (cursor.getCount() > 0) {
            powerSupplyState = true;
            long currentTime = CommonUtils.midnightOfToday().getTime();
            int count = 0;
            cursor.moveToFirst();
            do {
                String desc = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN));
                String ts = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN));
                Timestamp timestamp = Timestamp.valueOf(ts);
                if (count==0) {
                    if (Constants.START_MESSAGE.equals(desc)) {
                        powerSupplyState = false;
                    } else {
                        powerSupplyState = true;
                    }
                }
                long timediff = timestamp.getTime() - currentTime;
                double diffInHours = (double) timediff/3600000;
                accumulatedDiffInHours = renderBar(accumulatedDiffInHours,diffInHours);

                currentTime = timestamp.getTime();
                count++;
            } while (cursor.moveToNext());
            long timediff = System.currentTimeMillis() - currentTime;
            double diffInHours = (double)timediff/3600000;
            accumulatedDiffInHours = renderBar(accumulatedDiffInHours,diffInHours);
        }
    }

    private double renderBar(double accumulatedDiffInHours,double diffInHours) {
        View bar = new View(this);
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(CommonUtils.getDPI(diffInHours*xEtchingWidth, metrics),
                CommonUtils.getDPI(20, metrics));
        if (!powerSupplyState) {
            bar.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            bar.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark  ));
        }
        lp.leftToRight = R.id.constraint_layout;
        lp.bottomToTop = R.id.x_axis;
        //lp.rightToRight = R.id.constraint_layout;
        //lp.bottomMargin = CommonUtils.getDPI(30, metrics);
        //lp.rightMargin = CommonUtils.getDPI(100, metrics);
        lp.leftMargin = CommonUtils.getDPI(30+accumulatedDiffInHours*xEtchingWidth
                ,metrics);
        constraintLayout.addView(bar, lp);
        if (powerSupplyState){
            powerSupplyState = false;
        } else if (!powerSupplyState){
            powerSupplyState = true;
        }
        return accumulatedDiffInHours+diffInHours;
    }

    private View getXAxisEtching() {
        View etching = getLayoutInflater().inflate(R.layout.x_axis_etching, null, false);
        return etching;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("DATA_CHANGED");
        registerReceiver(dataChangeRcvr, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dataChangeRcvr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.schedule_job) {
            Intent intent = new Intent(this, ScheduleJobActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.delete_all) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning")
                    .setMessage("This will clear all the previous history.")
                    .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PowerKeeperDao.getInstance(MainActivity.this)
                                    .deleteAll();
                            mAdapter.setCursor(PowerKeeperDao.getInstance(MainActivity.this).queryAll());
                            mAdapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}
