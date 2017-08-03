package in.tranquilsoft.powerkeeper.model;

import android.database.Cursor;

import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;

/**
 * Created by gparmar on 03/08/17.
 */

public class Timekeeper {
    private int id;
    private String description;
    private String timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static Timekeeper fromCursor(Cursor cursor){
        if (cursor != null) {
            Timekeeper tk = new Timekeeper();
            tk.setDescription(cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.DESCRIPTION_COLUMN)));
            tk.setTimestamp(cursor.getString(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry.TIMESTAMP_COLUMN)));
            tk.setId(cursor.getInt(cursor.getColumnIndex(PowerKeeperContract.TimekeeperEntry._ID)));
            return tk;
        }
        return null;
    }
}
