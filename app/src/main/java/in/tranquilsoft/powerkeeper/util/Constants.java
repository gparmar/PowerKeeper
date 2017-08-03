package in.tranquilsoft.powerkeeper.util;

import java.text.SimpleDateFormat;

/**
 * Created by gparmar on 31/05/17.
 */

public interface Constants {
    String START_MESSAGE = "Started";
    String STOP_MESSAGE = "Stopped";
    SimpleDateFormat LONG_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat DB_LONG_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat DB_SHORT_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat SHORT_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
}
