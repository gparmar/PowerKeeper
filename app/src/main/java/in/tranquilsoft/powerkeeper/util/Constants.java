package in.tranquilsoft.powerkeeper.util;

import java.text.SimpleDateFormat;

/**
 * Created by gparmar on 31/05/17.
 */

public interface Constants {
    String START_MESSAGE = "Started";
    String STOP_MESSAGE = "Stopped";
    String SWIPE_ALERT_SHOWN = "SWIPE_ALERT_SHOWN";
    String FIRST_EVENT_RECORDED="FIRST_EVENT_RECORDED";
    SimpleDateFormat LONG_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat DB_LONG_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat DB_SHORT_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat SHORT_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("MMM yyyy");
    SimpleDateFormat DAY_MONTH_YEAR_FORMAT = new SimpleDateFormat("dd MMM yyyy");
    String EVENT_TYPE = "EVENT_TYPE";
    String EVENT_TIME = "EVENT_TIME";
    String EVENT_TYPE_STARTED_CHARGING = "EVENT_TYPE_STARTED_CHARGING";
    String EVENT_TYPE_STOPPED_CHARGING = "EVENT_TYPE_STOPPED_CHARGING";

    String DETAIL_SELECTED_DATE = "DETAIL_SELECTED_DATE";
    int WRITE_EXTERNAL_STORAGE_REQ_CODE = 10;
    int EXPORT_AS_PICTURE_REQ_CODE = 11;
    String EXPORT_FOLDER = "PowerSupplyTracker";
}
