package in.tranquilsoft.powerkeeper;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import in.tranquilsoft.powerkeeper.util.RedGreenBarRenderer;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    @BindView(R.id.list_view)
    RecyclerView recyclerView;
    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.no_data)
    TextView noData;
    @BindView(R.id.progressBar)
    View progressBar;
    @BindView(R.id.progress_msg)
    TextView progressBarMsg;

    private DateCursorAdapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private LinearLayoutManager layoutManager;
    private int verticalScrollPosition;
    private int selectedPosition;
    private Date selectedDateForExport;
    private String exportAsPictureFilename;
    private int mYear;
    private int mMonth;
    private int mDay;
    private String[] eventTypes = {Constants.START_MESSAGE,Constants.STOP_MESSAGE};

    private Cursor datesCursor;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("DATA_CHANGED")) {
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
        if (datesCursor == null || datesCursor.getCount() == 0) {
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
            //menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.database).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            refreshCursor();
        }
        else if (item.getItemId() == R.id.add_data) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please select the date and time you wish to add:");
            View view = getLayoutInflater().inflate(R.layout.date_time_dialog, null, false);
            final EditText dateET = (EditText) view.findViewById(R.id.date_val);
            final EditText timeET = (EditText) view.findViewById(R.id.time_val);
            ((Button)view.findViewById(R.id.pickDateBtn)).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Calendar c = Calendar.getInstance();
                            mYear = c.get(Calendar.YEAR);
                            mMonth = c.get(Calendar.MONTH);
                            mDay = c.get(Calendar.DAY_OF_MONTH);
                            DatePickerDialog datePickerDialog =
                                    new DatePickerDialog(HomeActivity.this,
                                            new DatePickerDialog.OnDateSetListener() {
                                                @Override
                                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                                    dateET.setText(
                                                            CommonUtils.getZeroBufferedInt(day)+
                                                                    "-"+
                                                                    CommonUtils.getZeroBufferedInt(month+1)+"-"+year);
                                                }
                                            },mYear,mMonth,mDay);
                            datePickerDialog.show();
                        }
                    }
            );
            ((Button)view.findViewById(R.id.pickTimeBtn)).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Calendar c = Calendar.getInstance();

                            TimePickerDialog timePickerDialog =
                                    new TimePickerDialog(HomeActivity.this,
                                            new TimePickerDialog.OnTimeSetListener(){

                                                @Override
                                                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                                    timeET.setText(
                                                            CommonUtils.getZeroBufferedInt(hour)+":"+
                                                                    CommonUtils.getZeroBufferedInt(minute));
                                                }
                                            },c.get(Calendar.HOUR),c.get(Calendar.MINUTE),true);
                            timePickerDialog.show();
                        }
                    }
            );
            final Spinner spinner = (Spinner)view.findViewById(R.id.event_type);
            spinner.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,eventTypes));
            builder.setView(view);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (TextUtils.isEmpty(dateET.getText().toString())||
                            TextUtils.isEmpty(timeET.getText().toString())) {
                        Toast.makeText(HomeActivity.this,
                                R.string.date_and_time_rqd,Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "Time:" + dateET.getText() + " " + timeET.getText() + ", Event:" + spinner.getSelectedItem());
                        Date pickedDatetime = null;
                        try {
                            pickedDatetime = Constants.LONG_FORMAT_MINUS_SECONDS.parse(dateET.getText().toString()
                                    + " " + timeET.getText().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        //First check if this date is present in the dates table
                        if (pickedDatetime != null) {
                            Cursor cursor = PowerKeeperDao.getInstance(HomeActivity.this)
                                    .getDatesTableRowByDate(Constants.DB_SHORT_FORMAT.format(pickedDatetime));
                            if (cursor != null && cursor.moveToNext()) {
                                //dates row is present

                            } else {
                                ContentValues cv = new ContentValues();
                                cv.put(PowerKeeperContract.DateEntry.DATE_COLUMN,
                                        Constants.DB_SHORT_FORMAT.format(pickedDatetime));
                                PowerKeeperDao.getInstance(HomeActivity.this).insertDatekeeper(cv);
                            }
                            ContentValues cv = new ContentValues();
                            cv.put(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN,
                                    Constants.DB_LONG_FORMAT.format(pickedDatetime));
                            cv.put(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN, spinner.getSelectedItem().toString());

                            PowerKeeperDao.getInstance(HomeActivity.this).insertTimekeeper(cv);
                        }
                    }
                }
            }).show();

        }
//        else if (item.getItemId() == R.id.settings) {
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivity(intent);
//        }
        else if (item.getItemId() == R.id.delete_all) {
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
            progressBarMsg.setText(R.string.exporting_data_progress_msg);
            progressBar.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Void>() {
                private String filename;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        filename = CommonUtils.exportData(HomeActivity.this);
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progressBar.setVisibility(View.GONE);
                    if (TextUtils.isEmpty(filename)) {
                        new AlertDialog.Builder(HomeActivity.this).setTitle(R.string.exported_title)
                                .setMessage(R.string.not_exported_msg)
                                .setPositiveButton(R.string.ok_lbl, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    } else {
                        new AlertDialog.Builder(HomeActivity.this)
                                .setMessage(HomeActivity.this.getString(R.string.exported_msg,
                                        "<InternalStorage of your mobile>/" + Constants.EXPORT_FOLDER + "/" +
                                                filename))
                                .setPositiveButton(R.string.ok_lbl, null)
                                .show();
                    }
                }
            }.execute();


        } else if (item.getItemId() == R.id.import_data) {
            Intent intent = new Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
        } else if (item.getItemId() == R.id.export_as_picture) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Ask for WRITE_EXTERNAL_STORAGE permission from the user, if not present
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Constants.EXPORT_AS_PICTURE_REQ_CODE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                    return true;
                }
            }


            beginExportingAsPicture();


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Granted permission to write to local folders");
        if (requestCode == Constants.WRITE_EXTERNAL_STORAGE_REQ_CODE) {
            progressBarMsg.setText(R.string.exporting_data_progress_msg);
            progressBar.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Void>() {
                private String filename;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        filename = CommonUtils.exportData(HomeActivity.this);
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progressBar.setVisibility(View.GONE);
                    if (TextUtils.isEmpty(filename)) {
                        new AlertDialog.Builder(HomeActivity.this).setTitle(R.string.exported_title)
                                .setMessage(R.string.not_exported_msg)
                                .setPositiveButton(R.string.ok_lbl, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    } else {
                        new AlertDialog.Builder(HomeActivity.this)
                                .setMessage(HomeActivity.this.getString(R.string.exported_msg,
                                        "<InternalStorage of your mobile>/" + Constants.EXPORT_FOLDER + "/" +
                                                filename))
                                .setPositiveButton(R.string.ok_lbl, null)
                                .show();
                    }
                }
            }.execute();

        } else if (requestCode == Constants.EXPORT_AS_PICTURE_REQ_CODE) {

            beginExportingAsPicture();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            progressBarMsg.setText(R.string.importing_data_msg);
            progressBar.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {

                    try {
                        Uri selectedfile = data.getData(); //The uri with the location of the file
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

                                PowerKeeperDao.getInstance(HomeActivity.this).insertTimekeeper(cv);

                                String date = Constants.DB_SHORT_FORMAT.format(Constants.DB_LONG_FORMAT.parse(tkns[0]));
                                if (!dates.contains(date)) {
                                    cv = new ContentValues();
                                    dates.add(date);
                                    cv.put(PowerKeeperContract.DateEntry.DATE_COLUMN,
                                            date);
                                    PowerKeeperDao.getInstance(HomeActivity.this).insertDatekeeper(cv);
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    refreshCursor();
                    progressBar.setVisibility(View.GONE);
                }
            }.execute();
        }
    }

    private void beginExportingAsPicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("Select the month which you wanted to export.")
                .setPositiveButton("Export", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (selectedDateForExport == null) {
                            Toast.makeText(HomeActivity.this,
                                    R.string.no_date_selected_msg,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        progressBarMsg.setText(R.string.export_picture_progress_msg);
                        progressBar.setVisibility(View.VISIBLE);
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                exportAsPicture();

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                progressBar.setVisibility(View.GONE);
                                new AlertDialog.Builder(HomeActivity.this)
                                        .setMessage(getString(R.string.pic_created, "<InternalStorage of your mobile>/" + Constants.EXPORT_FOLDER + "/" +
                                                exportAsPictureFilename))
                                        .setPositiveButton("Ok", null)
                                        .show();

                            }
                        }.execute();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        View spinnerContainer = getLayoutInflater().inflate(R.layout.ask_month_dialog, null, false);
        Cursor cursor = PowerKeeperDao.getInstance(this).getAllDates();
        final List<String> dates = new ArrayList<>();
        if (cursor != null) {

            while (cursor.moveToNext()) {
                try {
                    String date = Constants.MONTH_YEAR_FORMAT.format(
                            Constants.DB_SHORT_FORMAT.parse(
                                    cursor.getString(cursor.getColumnIndex(
                                            PowerKeeperContract.DateEntry.DATE_COLUMN))));
                    if (!dates.contains(date)) {
                        dates.add(date);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }
        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dates));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String selectedMonth = dates.get(position);
                    selectedDateForExport = Constants.DAY_MONTH_YEAR_FORMAT.parse(
                            "01 " + selectedMonth);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        builder.setView(spinnerContainer);
        builder.show();
    }

    private void exportAsPicture() {

        LinearLayout linearLayout = new LinearLayout(this);

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        View view = recyclerView.getChildAt(0);
        Log.d(TAG, "ll width:" + linearLayout.getWidth() + ", height:" + linearLayout.getHeight());
        RedGreenBarRenderer redGreenBarRenderer = new RedGreenBarRenderer(this);
        try {

            Date monthStart = CommonUtils.startOfMonth(selectedDateForExport);
            Date endOfToday = CommonUtils.endOfDay(new Date());
            Calendar cal = Calendar.getInstance();
            cal.setTime(monthStart);
            int month = cal.get(Calendar.MONTH);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM_yyyy");
            int newMonth = month;
            double accumulatedRediff = 0;
            while (newMonth == month && cal.getTimeInMillis() < endOfToday.getTime()) {
                ConstraintLayout constraintLayout = (ConstraintLayout)
                        getLayoutInflater().inflate(R.layout.date_item, linearLayout, false);
                accumulatedRediff += redGreenBarRenderer.renderRedAndGreens(null, constraintLayout,
                        accumulatedRediff, Constants.DB_SHORT_FORMAT.format(cal.getTime()));
                linearLayout.addView(constraintLayout);
                cal.add(Calendar.DATE, 1);
                newMonth = cal.get(Calendar.MONTH);
            }
            View summaryView = getLayoutInflater().inflate(R.layout.summary_item, linearLayout, false);
            ((TextView) summaryView.findViewById(R.id.total_downtime)).setText(getString(R.string.hours,
                    new DecimalFormat("#.##").format(accumulatedRediff)));
            linearLayout.addView(summaryView);
            Bitmap bmp = CommonUtils.createBitmapFromView(this, linearLayout);
            FileOutputStream out = null;
            File file = null;
            try {
                File exportDir = new File(Environment.getExternalStorageDirectory() +
                        File.separator + Constants.EXPORT_FOLDER);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                file = new File(exportDir, sdf.format(monthStart) + ".png");
                out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                exportAsPictureFilename = file.getName();
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "", e);
            Toast.makeText(this, "There was some error while creating a picture of the current month.",
                    Toast.LENGTH_LONG).show();
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
        CommonUtils.putSharedPref(this, "verticalScrollPosition", layoutManager.findFirstVisibleItemPosition() + "");
        CommonUtils.putSharedPref(this, "selectedPosition", selectedPosition + "");
    }

    private void refreshCursor() {
        Cursor datesCursor = PowerKeeperDao.getInstance(HomeActivity.this).getAllDates();
        if (datesCursor == null || datesCursor.getCount() == 0) {
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
