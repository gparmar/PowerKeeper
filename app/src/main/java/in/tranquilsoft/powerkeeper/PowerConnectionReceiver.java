package in.tranquilsoft.powerkeeper;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import in.tranquilsoft.powerkeeper.model.Timekeeper;
import in.tranquilsoft.powerkeeper.service.SaveEventService;
import in.tranquilsoft.powerkeeper.util.Constants;

/**
 * Created by gparmar on 24/05/17.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {
    private static String TAG = "PowerConnectionReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Came into onReceive");
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            Intent saveEventServiceIntent = new Intent(context, SaveEventService.class);
            saveEventServiceIntent.putExtra(Constants.EVENT_TIME, System.currentTimeMillis());
            if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
                saveEventServiceIntent.putExtra(Constants.EVENT_TYPE, Constants.EVENT_TYPE_STARTED_CHARGING);
            } else if (chargePlug == 0) {
                saveEventServiceIntent.putExtra(Constants.EVENT_TYPE, Constants.EVENT_TYPE_STOPPED_CHARGING);
            }
            Log.d(TAG, "Starting service SaveEventService with "+saveEventServiceIntent.getStringExtra(Constants.EVENT_TYPE));
            context.startService(saveEventServiceIntent);
        }catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.logcat(Log.ERROR, TAG, "Excn caught while performing onReceive");
            FirebaseCrash.report(e);
        } catch (Error er) {
            er.printStackTrace();
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error caught while performing onReceive");
            FirebaseCrash.report(er);
        }
    }
}
