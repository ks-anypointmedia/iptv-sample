package tv.anypoint.lineartv.sample;

import android.app.Application;
import android.util.Log;
import tv.anypoint.api.AnypointSdk;
import tv.anypoint.impl.common.SdkModuleLifecycleListener;

public class TVApplication extends Application {
    public static final String TAG = "ANYPOINT_SDK_SAMPLE";

    @Override
    public void onCreate() {
        super.onCreate();

        AnypointSdk.setDebugMode(true);
        AnypointSdk.initialize(getApplicationContext());

        boolean isInitialized = AnypointSdk.isInitialized();
        if (!isInitialized) {
        }

        AnypointSdk.addModuleLifecycleListener(new SdkModuleLifecycleListener() {
            @Override
            public void initialized(String module) {
                if (module.equals("multicast")) {
                    Log.d(TAG, "initialized: ");
                }
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        AnypointSdk.destroy();
    }
}
