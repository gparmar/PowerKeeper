package in.tranquilsoft.powerkeeper;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.tranquilsoft.powerkeeper.adapter.DateCursorAdapter;
import in.tranquilsoft.powerkeeper.data.AndroidDatabaseManager;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.util.CommonUtils;
import in.tranquilsoft.powerkeeper.util.Constants;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    @BindView(R.id.list_view)
    RecyclerView recyclerView;
    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.no_data)
    TextView noData;

    private DateCursorAdapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private LinearLayoutManager layoutManager;
    private int verticalScrollPosition;
    private int selectedPosition;

    private Cursor datesCursor;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("DATA_CHANGED")){
                Log.d(TAG, "in onReceive. Setting new cursor and notifying dataset changed.");
                refreshCursor();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);
        datesCursor = PowerKeeperDao.getInstance(this).getAllDates();
        if (datesCursor ==null || datesCursor.getCount() == 0) {
            noData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DateCursorAdapter(this, datesCursor);
        recyclerView.setAdapter(adapter);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

        if (BuildConfig.DEBUG) {
            new AdRequest.Builder().addTestDevice("D4CA5E10AAA16CDA5581DE7AF8AB3946");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (!BuildConfig.DEBUG) {
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.database).setVisible(false);
            menu.findItem(R.id.export_as_picture).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            refreshCursor();
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
                            PowerKeeperDao.getInstance(HomeActivity.this)
                                    .deleteAll();
                            refreshCursor();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        } else if (item.getItemId() == R.id.database) {
            Intent intent = new Intent(this, AndroidDatabaseManager.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.export_data) {
//            progressBar.setVisibility(View.VISIBLE);
            //Cehck write permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Ask for WRITE_EXTERNAL_STORAGE permission from the user, if not present
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Constants.WRITE_EXTERNAL_STORAGE_REQ_CODE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                    return true;
                }
            }
            CommonUtils.exportData(this);
//            progressBar.setVisibility(View.GONE);
        } else if (item.getItemId() == R.id.import_data) {
            Intent intent = new Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
        } else if (item.getItemId() == R.id.export_as_picture) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            View view = recyclerView.getChildAt(0);

            Log.d(TAG, "date:"+((TextView)view.findViewById(R.id.date_val)).getText());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Granted permission to write to local folders");
        if (requestCode == Constants.WRITE_EXTERNAL_STORAGE_REQ_CODE) {
            String filepath = CommonUtils.exportData(this);
            if (filepath != null) {
                new AlertDialog.Builder(this).setTitle(R.string.exported_title)
                        .setMessage(getString(R.string.exported_msg, filepath))
                        .setPositiveButton(R.string.ok_lbl, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            } else {
                new AlertDialog.Builder(this).setTitle(R.string.exported_title)
                        .setMessage(R.string.not_exported_msg)
                        .setPositiveButton(R.string.ok_lbl, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        getContentResolver().openInputStream(selectedfile)));
                String line;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                List<String> dates = new ArrayList<>();
                while ((line = in.readLine()) != null) {
                    if (line.contains("Time,Description")) {
                        continue;
                    }
                    String tkns[] = line.split(",");
                    if (tkns.length == 2) {
                        ContentValues cv = new ContentValues();

                        cv.put(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN,
                                tkns[0]);
                        cv.put(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN, tkns[1]);

                        PowerKeeperDao.getInstance(this).insertTimekeeper(cv);

                        String date = Constants.DB_SHORT_FORMAT.format(Constants.DB_LONG_FORMAT.parse(tkns[0]));
                        if (!dates.contains(date)) {
                            cv = new ContentValues();
                            dates.add(date);
                            cv.put(PowerKeeperContract.DateEntry.DATE_COLUMN,
                                    date);
                            PowerKeeperDao.getInstance(this).insertDatekeeper(cv);
                        }
                    }
                }
                refreshCursor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCursor();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        registerReceiver(receiver, new IntentFilter("DATA_CHANGED"));
        verticalScrollPosition =
                Integer.parseInt(CommonUtils.getSharedPref(this, "verticalScrollPosition", "0"));

        if (verticalScrollPosition > 0) {
            recyclerView.scrollToPosition(verticalScrollPosition);
        }
        selectedPosition =
                Integer.parseInt(CommonUtils.getSharedPref(this, "selectedPosition", "0"));
        if (selectedPosition > 0) {
            adapter.setSelectedPosition(selectedPosition);
            //recyclerView.scrollToPosition(selectedPosition);
            //recyclerView.getChildAt(0).setSelected(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        CommonUtils.putSharedPref(this, "verticalScrollPosition", layoutManager.findFirstVisibleItemPosition()+"");
        CommonUtils.putSharedPref(this, "selectedPosition", selectedPosition+"");
    }

    private void refreshCursor(){
        Cursor datesCursor = PowerKeeperDao.getInstance(HomeActivity.this).getAllDates();
        if (datesCursor ==null || datesCursor.getCount() == 0) {
            noData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.setCursor(datesCursor);
        adapter.notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }
}
