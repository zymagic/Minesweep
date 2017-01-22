package lib.config;

import android.content.Context;

/**
 * Created by j-zhangyang5 on 2017/1/16.
 */

public class ConfigManager {

    private static ConfigManager sInstance;

    public static void active(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new ConfigManager(context);
        }
    }

    private Context context;


    private ConfigManager(Context context) {
        this.context = context;
    }

}
