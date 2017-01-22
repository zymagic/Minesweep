package game.minesweep;

import java.util.ArrayList;

/**
 * Created by j-zhangyang5 on 2017/1/20.
 */

public interface GameControl {
    void dig(int x, int y);
    void mark(int x, int y);
    void sweep(int x, int y);
    ArrayList<Core.Grid> getData();
}
