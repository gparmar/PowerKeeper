package in.tranquilsoft.powerkeeper;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by gparmar on 24/05/17.
 */

public class PowerKeeperApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
