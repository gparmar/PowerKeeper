package in.tranquilsoft.powerkeeper.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;

import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.util.CommonUtils;
import in.tranquilsoft.powerkeeper.util.Constants;

/**
 * Created by gparmar on 03/08/17.
 */

public class SaveEventService extends IntentService {
    private static final String TAG = "SaveEventService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SaveEventService() {
        super("SaveEventService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String eventType = intent.getStringExtra(Constants.EVENT_TYPE);
        long eventTime = intent.getLongExtra(Constants.EVENT_TIME, -1);
        Log.d(TAG, "SaveEventService.onHandleIntent. eventType:"+eventType+", eventTime:"+eventTime);
        ContentValues cv = new ContentValues();
        Date date = null;
        try {
            date = new Date(eventTime);
        } catch (Exception e) {
            date = new Date();
        }
        boolean firstEventRecorded =
                Boolean.parseBoolean(CommonUtils.getSharedPref(this, Constants.FIRST_EVENT_RECORDED, "false"));
        if (Constants.EVENT_TYPE_STARTED_CHARGING.equals(eventType)) {
            if (!firstEventRecorded) {
                CommonUtils.putSharedPref(this, Constants.FIRST_EVENT_RECORDED, "true");
                return;
            }
            cv.put(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN,
                    Constants.START_MESSAGE);


            cv.put(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN, Constants.DB_LONG_FORMAT.format(date));
            PowerKeeperDao.getInstance(this).insertTimekeeper(cv);
        } else if (Constants.EVENT_TYPE_STOPPED_CHARGING.equals(eventType)) {
            if (!firstEventRecorded) {
                CommonUtils.putSharedPref(this, Constants.FIRST_EVENT_RECORDED, "true");
            }
            cv.put(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN,
                    Constants.STOP_MESSAGE);

            cv.put(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN, Constants.DB_LONG_FORMAT.format(date));
            PowerKeeperDao.getInstance(this).insertTimekeeper(cv);
            Log.d(TAG, "inserted timekeeper row");
        }

        Cursor cursor =
                PowerKeeperDao.getInstance(this).getDatesTableRowByDate(Constants.DB_SHORT_FORMAT.format(date));
        String dateFromDb = null;
        if (cursor != null && cursor.moveToNext()) {
            dateFromDb = cursor.getString(cursor.getColumnIndex(PowerKeeperContract.DateEntry.DATE_COLUMN));

        }
        if (dateFromDb != null && dateFromDb.length() > 0) {
            //do nothing because the date is already there.
            Log.d(TAG, "Did not insert dateentry row");
        } else {
            cv = new ContentValues();
            cv.put(PowerKeeperContract.DateEntry.DATE_COLUMN, Constants.DB_SHORT_FORMAT.format(date));
            PowerKeeperDao.getInstance(this).insertDatekeeper(cv);
            Log.d(TAG, "inserted dateentry row");
        }

        Intent dataChangedBroadcast = new Intent("DATA_CHANGED");
        sendBroadcast(dataChangedBroadcast);
        Log.d(TAG, "Sent broadcast with action DATA_CHANGED");
    }
}
