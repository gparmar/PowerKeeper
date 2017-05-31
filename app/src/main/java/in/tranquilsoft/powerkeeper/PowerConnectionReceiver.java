package in.tranquilsoft.powerkeeper;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import java.sql.Timestamp;
import java.util.Set;

import in.tranquilsoft.powerkeeper.data.PowerKeeperContract;
import in.tranquilsoft.powerkeeper.data.PowerKeeperContract.*;
import in.tranquilsoft.powerkeeper.data.PowerKeeperDao;
import in.tranquilsoft.powerkeeper.util.Constants;

/**
 * Created by gparmar on 24/05/17.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
//            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
//                    status == BatteryManager.BATTERY_STATUS_FULL;

            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
                ContentValues cv = new ContentValues();
                cv.put(TimekeeperEntry.DESCRIPTION_COLUMN, Constants.START_MESSAGE);
//                Timestamp ts = new Timestamp(System.currentTimeMillis());
//                cv.put(TimekeeperEntry.TIMESTAMP_COLUMN, String);
                PowerKeeperDao.getInstance(context).insert(cv);
            } else if (chargePlug == 0) {
                ContentValues cv = new ContentValues();
                cv.put(TimekeeperEntry.DESCRIPTION_COLUMN, Constants.STOP_MESSAGE);
                PowerKeeperDao.getInstance(context).insert(cv);
            }

            Intent dataChangedBroadcast = new Intent("DATA_CHANGED");
            context.sendBroadcast(dataChangedBroadcast);
//            Bundle extas = intent.getExtras();
//            if (extas != null) {
//                Set<String> keys = extas.keySet();
//                for (String key : keys) {
//                    Log.d("PowerConnecnReceiverrrr", "key:" + key + ", value:" + extas.get(key));
//                }
//            }
        }catch (Exception e) {
            e.printStackTrace();
        } catch (Error er) {
            er.printStackTrace();
        }
    }
}
