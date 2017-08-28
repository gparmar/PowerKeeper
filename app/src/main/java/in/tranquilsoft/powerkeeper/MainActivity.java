package in.tranquilsoft.powerkeeper;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import in.tranquilsoft.powerkeeper.data.AndroidDatabaseManager;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.util.CommonUtils;
import in.tranquilsoft.powerkeeper.util.Constants;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String POWERKEEPER_STORAGE_FOLDER = ".PowerKeeper";
    private PowerKeeperDao mPowerDao;
    private DataAdapter mAdapter;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private ConstraintLayout constraintLayout;
    private int xEtchingWidth;
    private DisplayMetrics metrics;
    private boolean powerSupplyState;
    //private CalendarView calendarView;
    private Date selectedDate;
    private List<View> greenAndRedBars = new ArrayList<>();
    private View progressBar;

    private String[] mFileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() +
            "/"+POWERKEEPER_STORAGE_FOLDER+"/");
    private String mChosenFile;
    private static final String FTYPE = ".csv";
    private static final int DIALOG_LOAD_FILE = 1000;

    private int mYear;
    private int mMonth;
    private int mDay;
    private String[] eventTypes = {Constants.EVENT_TYPE_STARTED_CHARGING,Constants.EVENT_TYPE_STOPPED_CHARGING};

    private BroadcastReceiver dataChangeRcvr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor cursor = mPowerDao.queryForDay(selectedDate);
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

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (selectedDate == null) {
            selectedDate = new Date();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_layout);
        progressBar = findViewById(R.id.progressBar);

        fab.setOnClickListener(fabOnClickListener);
        mPowerDao = PowerKeeperDao.getInstance(this);


        //calendarView.setMinDate(CommonUtils.startOfMonth(selectedDate).getTime());
//        new AsyncTask<Void, Void, Cursor>() {
//            @Override
//            protected Cursor doInBackground(Void... voids) {
//                Cursor cursor = PowerKeeperDao.getInstance(MainActivity.this).queryForFirstDataDate();
//                return cursor;
//            }
//
//            @Override
//            protected void onPostExecute(Cursor cursor) {
//                long minTime = 0;
//                if (cursor == null || cursor.getCount() == 0) {
//                    minTime = CommonUtils.startOfDay(selectedDate).getTime();
//                } else {
//                    cursor.moveToFirst();
//                    String ts = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN));
//                    Timestamp timestamp = Timestamp.valueOf(ts);
//                    minTime = CommonUtils.startOfDay(new Date(timestamp.getTime())).getTime();
//                }
//                //calendarView.setMinDate(minTime);
//            }
//        }.execute();


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
                mAdapter.setCursor(PowerKeeperDao.getInstance(MainActivity.this).queryForDay(selectedDate));
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
            //TextView reading = (TextView) xetching.findViewById(R.id.x_reading);
            //reading.setText(i + "");
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            lp.leftToRight = R.id.constraint_layout;
            lp.topToBottom = R.id.x_axis;
            lp.leftMargin = CommonUtils.getDPI(10 + i * xEtchingWidth, metrics);

            constraintLayout.addView(xetching, lp);
        }

        refreshPage();
    }

    private void renderChart(Cursor cursor) {
        //Cursor cursor = PowerKeeperDao.getInstance(this).queryForToday();
        double accumulatedDiffInHours = 0;
        long currentTime = CommonUtils.startOfDay(selectedDate).getTime();
        Iterator<View> views = greenAndRedBars.iterator();
        while (views.hasNext()) {
            View view = views.next();
            constraintLayout.removeView(view);
        }
        greenAndRedBars.clear();
        if (cursor.getCount() > 0) {
            powerSupplyState = true;
            int count = 0;
            cursor.moveToFirst();
            do {
                String desc = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN));
                String ts = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN));
                Timestamp timestamp = Timestamp.valueOf(ts);
                if (count == 0) {
                    if (Constants.START_MESSAGE.equals(desc)) {
                        powerSupplyState = false;
                    } else {
                        powerSupplyState = true;
                    }
                }
                long timediff = timestamp.getTime() - currentTime;
                double diffInHours = (double) timediff / 3600000;
                accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours);

                currentTime = timestamp.getTime();
                count++;
            } while (cursor.moveToNext());
            if (CommonUtils.isDatesSame(selectedDate, new Date())) {
                long timediff = System.currentTimeMillis() - currentTime;
                double diffInHours = (double) timediff / 3600000;
                accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours);
            } else {
                long timediff = CommonUtils.endOfDay(selectedDate).getTime() - currentTime;
                double diffInHours = (double) timediff / 3600000;
                accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours);
            }
        } else {
            Cursor cursor1 = PowerKeeperDao.getInstance(this).queryForOneBeforeDay(selectedDate);
            if (cursor1.getCount() > 0) {
                cursor1.moveToFirst();
                String desc = cursor1.getString(cursor1.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN));
                if (Constants.START_MESSAGE.equals(desc)) {
                    powerSupplyState = true;
                } else {
                    powerSupplyState = false;
                }
            } else {
                powerSupplyState = true;
            }
            long timediff = System.currentTimeMillis() - currentTime;
            double diffInHours = (double) timediff / 3600000;
            accumulatedDiffInHours = renderBar(accumulatedDiffInHours, diffInHours);
        }
    }

    private double renderBar(double accumulatedDiffInHours, double diffInHours) {
        View bar = new View(this);
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(CommonUtils.getDPI(diffInHours * xEtchingWidth, metrics),
                CommonUtils.getDPI(20, metrics));
        if (!powerSupplyState) {
            bar.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            bar.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        lp.leftToRight = R.id.constraint_layout;
        lp.bottomToTop = R.id.x_axis;
        //lp.rightToRight = R.id.constraint_layout;
        //lp.bottomMargin = CommonUtils.getDPI(30, metrics);
        //lp.rightMargin = CommonUtils.getDPI(100, metrics);
        lp.leftMargin = CommonUtils.getDPI(10 + accumulatedDiffInHours * xEtchingWidth
                , metrics);
        constraintLayout.addView(bar, lp);
        greenAndRedBars.add(bar);
        if (powerSupplyState) {
            powerSupplyState = false;
        } else if (!powerSupplyState) {
            powerSupplyState = true;
        }
        return accumulatedDiffInHours + diffInHours;
    }

    private View getXAxisEtching() {
        View etching = getLayoutInflater().inflate(R.layout.x_axis_etching, null, false);
        return etching;
    }

    private void refreshPage() {
        Cursor cursor = mPowerDao.queryForDay(selectedDate);
        if (mAdapter == null) {
            mAdapter = new DataAdapter(this, cursor);
            recyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCursor(cursor);
        }
        renderChart(cursor);
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
        if (item.getItemId() == R.id.refresh) {
            refreshPage();
        } else if (item.getItemId() == R.id.add_data) {
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
                                    new DatePickerDialog(MainActivity.this,
                                            new DatePickerDialog.OnDateSetListener() {
                                                @Override
                                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                                    dateET.setText(day+"-"+(month+1)+"-"+year);
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
                                    new TimePickerDialog(MainActivity.this,
                                            new TimePickerDialog.OnTimeSetListener(){

                                                @Override
                                                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                                    timeET.setText(hour+":"+minute);
                                                }
                                            },c.get(Calendar.HOUR),c.get(Calendar.MINUTE),true);
                            timePickerDialog.show();
                        }
                    }
            );
            final Spinner spinner = (Spinner)view.findViewById(R.id.event_type);
            spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,eventTypes));
            builder.setView(view);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d(TAG,"Time:"+dateET.getText()+" "+timeET.getText()+", Event:"+spinner.getSelectedItem());
                }
            }).show();

        }
        else if (item.getItemId() == R.id.delete_all) {
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
        } else if (item.getItemId() == R.id.database) {
            Intent intent = new Intent(this, AndroidDatabaseManager.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.export_data) {
            progressBar.setVisibility(View.VISIBLE);
            Cursor cursor = PowerKeeperDao.getInstance(this).queryAll();
            if (cursor != null) {
                File exportDir = new File(Environment.getExternalStorageDirectory() + File.separator + ".PowerKeeper");
                long freeBytesInternal = new File(getApplicationContext().getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
                long megAvailable = freeBytesInternal / 1048576;
                boolean memoryErr = false;
                if (megAvailable < 0.1) {
                    Toast.makeText(this,"There is no storage. Storage present" + megAvailable,
                            Toast.LENGTH_LONG).show();
                    memoryErr = true;
                } else {
                    String exportDirStr = exportDir.toString();// to show in dialogbox
                    Log.v(TAG, "exportDir path::" + exportDir);
                    if (!exportDir.exists()) {
                        exportDir.mkdirs();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH_mm_ss");
                    File exportFile = new File(exportDir,"powerkeeper"+
                            (sdf.format(new Date()))+".csv");
                    FileWriter out = null;
                    try {
                        out = new FileWriter(exportFile);
                        out.write("Time,Description\n");
                        while (cursor.moveToNext()) {
                            String desc = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN));
                            String ts = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN));
                            Timestamp timestamp = Timestamp.valueOf(ts);

                            out.write(Constants.LONG_FORMAT.format(new Date(timestamp.getTime()))+","+desc+"\n");
                        }
                        out.flush();

                    } catch (IOException e) {
                        Log.e(TAG,"",e);
                    } finally {
                        if (out!=null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
            progressBar.setVisibility(View.GONE);
        } else if (item.getItemId() == R.id.import_data){
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
        if(requestCode==123 && resultCode==RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        getContentResolver().openInputStream(selectedfile)));
                String line;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                while((line=in.readLine())!= null){
                    if (line.contains("Time,Description")){
                        continue;
                    }
                    String tkns[] = line.split(",");
                    if (tkns.length==2){
                        ContentValues cv = new ContentValues();

                        cv.put(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN,
                                tkns[0]);
                        cv.put(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN, tkns[1]);

                        PowerKeeperDao.getInstance(this).insertTimekeeper(cv);

                        cv = new ContentValues();
                        cv.put(PowerKeeperContract.DateEntry.DATE_COLUMN,
                                Constants.SHORT_FORMAT.format(Constants.DB_LONG_FORMAT.parse(tkns[0])));
                        PowerKeeperDao.getInstance(this).insertDatekeeper(cv);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                if(mFileList == null) {
                    Log.e(TAG, "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                        //you can do stuff with the file here too
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }
    private void loadFileList() {
        try {
            mPath.mkdirs();
        }
        catch(SecurityException e) {
            Log.e(TAG, "unable to write on the sd card " + e.toString());
        }
        if(mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(FTYPE) || sel.isDirectory();
                }

            };
            mFileList = mPath.list(filter);
        }
        else {
            mFileList= new String[0];
        }
    }
}
