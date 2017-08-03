package in.tranquilsoft.powerkeeper.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import in.tranquilsoft.powerkeeper.util.CommonUtils;
import in.tranquilsoft.powerkeeper.util.Constants;

/**
 * Created by gparmar on 24/05/17.
 */

public class PowerKeeperDao {
    private static PowerKeeperDao instance;
    private PowerDbHelper dbHelper;

    public static PowerKeeperDao getInstance(Context context) {
        if (instance == null) {
            instance = new PowerKeeperDao(context);
            Log.d("PowerKeeperDao", "Creating a new instance");
        }
        return instance;
    }

    private PowerKeeperDao(Context context) {
        dbHelper = new PowerDbHelper(context);
    }

    public void insertTimekeeper(ContentValues cv) {
        dbHelper.getWritableDatabase().insert(PowerKeeperContract.TimekeeperEntry.TABLE_NAME,
                null, cv);
    }
    public void insertDatekeeper(ContentValues cv) {
        dbHelper.getWritableDatabase().insert(PowerKeeperContract.DateEntry.TABLE_NAME,
                null, cv);
    }

    public Cursor queryAll() {
        return dbHelper.getReadableDatabase().query(PowerKeeperContract.TimekeeperEntry.TABLE_NAME,
                null, null, null, null, null, PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN + " DESC");
    }

    public Cursor queryForFirstDataDate() {
        return dbHelper.getReadableDatabase().query(PowerKeeperContract.TimekeeperEntry.TABLE_NAME,
                null, null, null, null, null, PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN+" ASC LIMIT 1");
    }

    public Cursor queryForDay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = sdf.format(CommonUtils.startOfDay(date));
        return dbHelper.getReadableDatabase()
                .query(PowerKeeperContract.TimekeeperEntry.TABLE_NAME
                        , null, PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN+">?"
                        , new String[]{datetime}, null, null,
                        PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN);
    }
    public Cursor queryForToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = sdf.format(CommonUtils.startOfToday());
        return dbHelper.getReadableDatabase()
                .query(PowerKeeperContract.TimekeeperEntry.TABLE_NAME
                        , null, PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN+">?"
                        , new String[]{datetime}, null, null,
                        PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN);
    }
    public Cursor queryForOneBeforeDay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = sdf.format(CommonUtils.startOfDay(date));
        return dbHelper.getReadableDatabase()
                .query(PowerKeeperContract.TimekeeperEntry.TABLE_NAME
                        , null, PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN+"<?"
                        , new String[]{datetime}, null, null,
                        "1");
    }

    public void deleteAll() {
        dbHelper.getWritableDatabase().delete(PowerKeeperContract.TimekeeperEntry.TABLE_NAME, null, null);
    }

    public void delete(long id) {
        dbHelper.getWritableDatabase().delete(PowerKeeperContract.TimekeeperEntry.TABLE_NAME
                , PowerKeeperContract.TimekeeperEntry._ID + "=?", new String[]{id + ""});
    }

    public Cursor getAllDates(){
        return dbHelper.getReadableDatabase().query(PowerKeeperContract.DateEntry.TABLE_NAME,
                null,null,null,null,null,null);
    }

    public Cursor getValuesForDate(String date) {
        try {
            String dbDateFormat = Constants.DB_SHORT_FORMAT.format(Constants.SHORT_FORMAT.parse(date));
            return dbHelper.getReadableDatabase().query(PowerKeeperContract.TimekeeperEntry.TABLE_NAME,
                    null, PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN+" >=? and "+
                            PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN+" <=? ",
                    new String[]{dbDateFormat+" 00:00:00", dbDateFormat+" 23:59:59"},null,null,
                    PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN+" asc");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
