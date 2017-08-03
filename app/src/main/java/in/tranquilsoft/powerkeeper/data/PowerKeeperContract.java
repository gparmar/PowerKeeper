package in.tranquilsoft.powerkeeper.data;

import android.provider.BaseColumns;

/**
 * Created by gparmar on 24/05/17.
 */

public class PowerKeeperContract {
    public static final class DateEntry implements BaseColumns {
        public static final String TABLE_NAME = "dates";
        public static final String DATE_COLUMN = "date_col";
    }
    public static final class TimekeeperEntry implements BaseColumns {
        public static final String TABLE_NAME = "timekeeper";
        public static final String DESCRIPTION_COLUMN = "description";
        public static final String TIMESTAMP_COLUMN = "timestamp";
    }
}
