package game.minesweep;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * Created by j-zhangyang5 on 2017/1/22.
 */

public class Utils {

    @SuppressLint("NewApi")
    public static Point getScreenRealDimension(Context context) {
        Point ret = new Point();
        DisplayMetrics dp = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        try {
            wm.getDefaultDisplay().getRealMetrics(dp);
            ret.set(dp.widthPixels, dp.heightPixels);
            return ret;
        } catch (Throwable e) {
            wm.getDefaultDisplay().getMetrics(dp);
        }
        try {
            Method m = DisplayMetrics.class.getDeclaredMethod("getRealSize", Point.class);
            m.setAccessible(true);
            m.invoke(dp, ret);
            return ret;
        } catch (Exception e) {
            // ignore
        }
        try {
            Method m = DisplayMetrics.class.getDeclaredMethod("getRawWidth");
            m.setAccessible(true);
            int width = (int) m.invoke(dp);
            m = DisplayMetrics.class.getDeclaredMethod("getRawHeight");
            m.setAccessible(true);
            int height = (int) m.invoke(dp);
            ret.set(width, height);
        } catch (Exception e) {
            // ignore
        }
        return ret;
    }

    public static int clamp(int a, int min, int max) {
        if (min > max) {
            max ^= min;
            min ^= max;
            max ^= min;
        }
        if (a < min) {
            return min;
        }
        if (a > max) {
            return max;
        }
        return a;
    }
}
