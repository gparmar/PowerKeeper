package in.tranquilsoft.powerkeeper.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by gparmar on 24/05/17.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CreateReportService extends JobService {
    public static final String TAG = "CreateReportService";
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Object object = jobParameters.getExtras().get("time");
        //Do bunch of tasks to send the report by email
        if (object != null) {
            Log.d(TAG, "in the onStartJob method. Executing..."+object);
        } else
        Log.d(TAG, "in the onStartJob method. Executing...");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "in the onStopJob method. Executing...");
        return true;
    }
}
