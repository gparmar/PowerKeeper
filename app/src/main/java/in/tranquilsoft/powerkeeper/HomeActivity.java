package in.tranquilsoft.powerkeeper;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import in.tranquilsoft.powerkeeper.util.Constants;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    @BindView(R.id.list_view)
    RecyclerView recyclerView;
    @BindView(R.id.adView)
    AdView mAdView;

    private DateCursorAdapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    private Cursor datesCursor;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("DATA_CHANGED")){
                Log.d(TAG, "in onReceive. Setting new cursor and notifying dataset changed.");
                adapter.setCursor(PowerKeeperDao.getInstance(HomeActivity.this).getAllDates());
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);
        datesCursor = PowerKeeperDao.getInstance(this).getAllDates();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DateCursorAdapter(this, datesCursor);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

            }
        });
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (!BuildConfig.DEBUG) {
            menu.findItem(R.id.settings).setEnabled(false);
            menu.findItem(R.id.database).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            adapter.notifyDataSetChanged();
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
                            adapter.setCursor(PowerKeeperDao.getInstance(HomeActivity.this).getAllDates());
                            adapter.notifyDataSetChanged();
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
            Cursor cursor = PowerKeeperDao.getInstance(this).queryAll();
            if (cursor != null) {
                File exportDir = new File(Environment.getExternalStorageDirectory() + File.separator + ".PowerKeeper");
                long freeBytesInternal = new File(getApplicationContext().getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
                long megAvailable = freeBytesInternal / 1048576;
                boolean memoryErr = false;
                if (megAvailable < 0.1) {
                    Toast.makeText(this, "There is no storage. Storage present" + megAvailable,
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

                            out.write(Constants.LONG_FORMAT.format(new Date(timestamp.getTime())) + "," + desc + "\n");
                        }
                        out.flush();

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
//            progressBar.setVisibility(View.GONE);
        } else if (item.getItemId() == R.id.import_data) {
            Intent intent = new Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
        }
        return super.onOptionsItemSelected(item);
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
                adapter.setCursor(PowerKeeperDao.getInstance(HomeActivity.this).getAllDates());
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        registerReceiver(receiver, new IntentFilter("DATA_CHANGED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
