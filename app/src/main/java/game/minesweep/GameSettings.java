package game.minesweep;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by j-zhangyang5 on 2017/1/20.
 */

public class GameSettings implements Parcelable {

    public int width, height, mines;

    public GameSettings(int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.mines = mines;
    }

    protected GameSettings(Parcel in) {
        width = in.readInt();
        height = in.readInt();
        mines = in.readInt();
    }

    public static final Creator<GameSettings> CREATOR = new Creator<GameSettings>() {
        @Override
        public GameSettings createFromParcel(Parcel in) {
            return new GameSettings(in);
        }

        @Override
        public GameSettings[] newArray(int size) {
            return new GameSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(width);
        parcel.writeInt(height);
        parcel.writeInt(mines);
    }

    private static int maxCountX = 30, maxCountY = 24, maxMines = 668;
    private static int minCountX = 9, minCountY = 9, minMines = 10;

    private static int expertCountX = 30, expertCountY = 16, expertMines = 99;
    private static int mediumCountX = 16, mediumCountY = 16, mediumMines = 40;
    private static int easyCountX = 9, easyCountY = 9, easyMines = 10;

    private static final String PREF_CUSTOM_X = "pref_count_x";
    private static final String PREF_CUSTOM_Y = "pref_count_y";
    private static final String PREF_CUSTOM_MINES = "pref_mines";

    public static void evaluate(Context context) {
        Point point = Utils.getScreenRealDimension(context);
        int screenWidth = point.x;
        int screenHeight = point.y;
        if (screenWidth < screenHeight) {
            screenWidth ^= screenHeight;
            screenHeight ^= screenWidth;
            screenWidth ^= screenHeight;
        }
        float density = context.getResources().getDisplayMetrics().density;
        screenWidth /= density;
        screenHeight /= density;

        int spaceX = screenWidth - 72 - 24;
        int spaceY = screenHeight - 24;

        int minGrid = 24;

        int maxX = spaceX / minGrid;
        int maxY = spaceY / minGrid;

        if (maxX > maxCountX) {
            maxCountX = maxX;
            maxMines = Math.max(maxMines, maxCountX * maxCountY * 8 / 9);
        }
        if (maxY > maxCountY) {
            maxCountY = maxY;
            maxMines = Math.max(maxMines, maxCountX * maxCountY * 8 / 9);
        }
    }

    public static GameSettings getEasySettings() {
        return new GameSettings(easyCountX, easyCountY, easyMines);
    }

    public static GameSettings getMediumSettings() {
        return new GameSettings(mediumCountX, mediumCountY, mediumMines);
    }

    public static GameSettings getExpertSettings() {
        return new GameSettings(expertCountX, expertCountX, expertMines);
    }

    public static GameSettings getCustomSettings(Context context) {
        SharedPreferences sf = context.getSharedPreferences(GameRecord.RECORD_PREFERENCE, Context.MODE_PRIVATE);
        int w = sf.getInt(PREF_CUSTOM_X, mediumCountX);
        w = Utils.clamp(w, minCountX, maxCountX);
        int h = sf.getInt(PREF_CUSTOM_Y, mediumCountY);
        h = Utils.clamp(h, minCountY, maxCountY);
        int m = sf.getInt(PREF_CUSTOM_MINES, mediumMines);
        m = Utils.clamp(m, minMines, maxMines);
        return new GameSettings(w, h, m);
    }

    public static GameSettings getMaxSettings() {
        return new GameSettings(maxCountX, maxCountY, maxMines);
    }

    public static GameSettings getMinSettings() {
        return new GameSettings(minCountX, minCountY, minMines);
    }

    public static void saveCustomSettings(Context context, int w, int h, int m) {
        w = Utils.clamp(w, minCountX, maxCountX);
        h = Utils.clamp(h, minCountY, maxCountY);
        m = Utils.clamp(m, minMines, maxMines);
        SharedPreferences sf = context.getSharedPreferences(GameRecord.RECORD_PREFERENCE, Context.MODE_PRIVATE);
        sf.edit().putInt(PREF_CUSTOM_X, w).putInt(PREF_CUSTOM_Y, h).putInt(PREF_CUSTOM_MINES, m).apply();
    }

    public static String getKey(int w, int h, int m) {
        if (w == easyCountX && h == easyCountY && m == easyMines) {
            return "easy";
        }
        if (w == mediumCountX && h == mediumCountY && m == mediumMines) {
            return "medium";
        }
        if (w == expertCountX && h == expertCountY && m == expertMines) {
            return "expert";
        }
        return w + "_" + h + "_" + m;
    }
}
