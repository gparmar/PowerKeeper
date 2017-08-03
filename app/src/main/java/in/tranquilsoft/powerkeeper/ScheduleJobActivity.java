package in.tranquilsoft.powerkeeper;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

import in.tranquilsoft.powerkeeper.util.JobUtil;

public class ScheduleJobActivity extends AppCompatActivity {
    private EditText scheduleTimeHour;
    private EditText scheduleTimeMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_job);

        scheduleTimeHour = (EditText) findViewById(R.id.schedule_time_hour);
        scheduleTimeMin = (EditText) findViewById(R.id.schedule_time_min);
        Button btn = (Button) findViewById(R.id.schedule_job_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR, Integer.parseInt(scheduleTimeHour.getText().toString()));
                cal.set(Calendar.MINUTE, Integer.parseInt(scheduleTimeMin.getText().toString()));
                JobUtil.scheduleJob(ScheduleJobActivity.this, cal.getTimeInMillis(),
                        scheduleTimeHour.getText().toString()+":"+scheduleTimeMin.getText().toString());
            }
        });
    }
}
