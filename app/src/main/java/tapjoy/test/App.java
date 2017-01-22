package tapjoy.test;

import android.app.Application;

import game.minesweep.GameSettings;

/**
 * Created by j-zhangyang5 on 2017/1/7.
 */

public class App extends Application {
    static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        GameSettings.evaluate(this);
    }

    public static App getApp() {
        return sInstance;
    }
}
