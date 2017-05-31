package in.tranquilsoft.powerkeeper.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import in.tranquilsoft.powerkeeper.util.CommonUtils;

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

    public void insert(ContentValues cv) {
        dbHelper.getWritableDatabase().insert(PowerKeeperContract.TimekeeperEntry.TABLE_NAME,
                null, cv);
    }

    public Cursor queryAll() {
        return dbHelper.getReadableDatabase().query(PowerKeeperContract.TimekeeperEntry.TABLE_NAME,
                null, null, null, null, null, PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN + " DESC");
    }

    public Cursor queryForToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = sdf.format(CommonUtils.midnightOfToday());
        return dbHelper.getReadableDatabase()
                .query(PowerKeeperContract.TimekeeperEntry.TABLE_NAME
                        , null, PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN+">?"
                        , new String[]{datetime}, null, null,
                        PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN);
    }
    public Cursor queryForOneBeforeToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = sdf.format(CommonUtils.midnightOfToday());
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
}
