package game.minesweep;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by j-zhangyang5 on 2017/1/22.
 */

public class GameRecord {

    public static final String RECORD_PREFERENCE = "preference_record";
    private static final String PREF_TIME_RECORD = "pref_time_record_";

    public static boolean newRecord(Context context, int seconds, String key) {
        SharedPreferences sf = context.getSharedPreferences(RECORD_PREFERENCE, Context.MODE_PRIVATE);
        int record = sf.getInt(PREF_TIME_RECORD + key, -1);
        boolean newRecord = record > 0 && seconds < record;
        if (record < 0 || seconds < record) {
            sf.edit().putInt(PREF_TIME_RECORD + key, seconds).apply();
        }
        return newRecord;
    }

    public static String formatTime(int seconds) {
        int h = seconds / 3600;
        int m = (seconds - h * 3600) / 60;
        int s = seconds - h * 3600 - m * 60;
        return formatTime(h, m, s);
    }

    public static String formatTime(int h, int m, int s) {
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }
}
