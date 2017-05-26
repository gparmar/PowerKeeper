package in.tranquilsoft.powerkeeper.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;

import in.tranquilsoft.powerkeeper.service.CreateReportService;

/**
 * Created by gparmar on 24/05/17.
 */

public class JobUtil {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context, long scheduleTime, String time){
        PersistableBundle args = new PersistableBundle();
        args.putString("time", time);
        long currentTime = System.currentTimeMillis();
        if (scheduleTime > currentTime) {
            ComponentName serviceComponent = new ComponentName(context, CreateReportService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            builder.setMinimumLatency(scheduleTime-currentTime); // wait at least
            builder.setOverrideDeadline(3 * 1000); // maximum delay
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setExtras(args);

            //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
            //builder.setRequiresDeviceIdle(true); // device should be idle
            //builder.setRequiresCharging(false); // we don't care if the device is charging or not
            JobScheduler jobScheduler =
                    (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }
    }
}
