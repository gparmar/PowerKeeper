package in.tranquilsoft.powerkeeper.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract.*;

/**
 * Created by gparmar on 24/05/17.
 */

public class PowerDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="powerkeeper.db";
    public static final int DATABASE_VERSION=1;

    public PowerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold waitlist data
        final String SQL_CREATE_WAITLIST_TABLE = "CREATE TABLE " + TimekeeperEntry.TABLE_NAME + " (" +
                TimekeeperEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TimekeeperEntry.DESCRIPTION_COLUMN + " TEXT NOT NULL, " +
                TimekeeperEntry.TIMESTAMP_COLUMN + " DATETIME DEFAULT (datetime('now','localtime'))" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_WAITLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TimekeeperEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
