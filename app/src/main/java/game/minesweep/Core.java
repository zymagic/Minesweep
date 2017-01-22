package game.minesweep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by j-zhangyang5 on 2017/1/20.
 */

public class Core {

    private static Random sRandom = new Random(47);

    private ArrayList<Grid> data;

    private HashMap<Grid, ArrayList<Grid>> groups;

    private int width, height;

    private int mines, total, minesLeft;

    private int state = 0;

    public Core(int width, int height, int mines) {
        this.mines = mines;
        this.width = width;
        this.height = height;
        this.total = width * height;

        data = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            data.add(new Grid(i));
        }

        LinkedList<Grid> hash = new LinkedList<>(data);
        LinkedList<Grid> retain = new LinkedList<>(data);

        for (int i = 0; i < mines; i++) {
            int index = sRandom.nextInt(hash.size());
            Grid grid = hash.get(index);
            grid.value = -1;
            hash.remove(grid);
            retain.remove(grid);
            int x = grid.index % width;
            int y = grid.index / height;
            for (int m = -1; m <= 1; m++) {
                for (int n = -1; n <= 1; n++) {
                    if (x + m < 0 || x + m >= width || y + n < 0 || y + n >= height || m == 0 && n == 0) {
                        continue;
                    }
                    Grid around = data.get((y + n) * width + (x + m));
                    if (around.value >= 0) {
                        around.value++;
                    }
                    retain.remove(around);
                }
            }
        }

        groups = new HashMap<>();

        for (int i = 0; i < retain.size(); i++) {
            Grid grid = retain.get(i);
            if (grid.key == null) {
                grid.key = grid;
            }
            ArrayList<Grid> gp = groups.get(grid.key);
            if (gp == null) {
                gp = new ArrayList<>();
                groups.put(grid.key, gp);
            }
            retain.remove(grid);
            gp.add(grid);
            group(grid, gp, retain);
        }
    }

    private void group(Grid from, ArrayList<Grid> group, LinkedList<Grid> hash) {
        int x = from.index % width;
        int y = from.index / width;
        for (int m = -1; m <= 1; m++) {
            for (int n = -1; n <= 1; n++) {
                if (x + m < 0 || x + m >= width || y + n < 0 || y + n >= height || x == 0 && y == 0) {
                    continue;
                }
                Grid around = data.get((y + n) * width + (x + m));
                if (around.key != null) {
                    continue;
                }
                around.key = from.key;
                group.add(around);
                if (around.value == 0) {
                    hash.remove(around);
                    group(around, group, hash);
                }
            }
        }
    }

    public void restart() {

    }

    public void dig(int x, int y, int[] dirty) {
        if (state != 0) {
            return;
        }
        Grid grid = data.get(y * width + x);
        if (grid.value == -1) {
            state = -1;
            grid.cause = true;
        } else if (grid.value == 0) {
            if (grid.open(true)) {
                markDirty(grid, dirty);
                total--;
            }
            ArrayList<Grid> group = groups.get(grid.key);
            for (Grid g : group) {
                if (g.open(false)) {
                    markDirty(g, dirty);
                    total--;
                }
            }
            if (total == mines) {
                state = 1;
            }
        } else {
            if (grid.state == Grid.STATE_NORMAL) {
                grid.state = Grid.STATE_OPEN;
                markDirty(grid, dirty);
            }
        }
    }

    public void mark(int x, int y, int[] dirty) {
        if (state != 0) {
            return;
        }
        Grid grid = data.get(y * width + x);
        if (grid.nextFlag()) {
            if (grid.state == Grid.STATE_FLAG) {
                minesLeft--;
            } else if (grid.state == Grid.STATE_QUESTION) {
                minesLeft++;
            }
            markDirty(grid, dirty);
        }
    }

    public void sweep(int x, int y, int[] dirty) {
        if (state != 0) {
            return;
        }
        Grid grid = data.get(y * width + x);
        if (grid.value <= 0 || grid.state != Grid.STATE_OPEN) {
            return;
        }
        int sum = 0;
        for (int m = -1; m <= 1; m++) {
            for (int n = -1; n <= 1; n++) {
                if (x + m < 0 || x + m >= width || y + n < 0 || y + n >= width || m == 0 && n == 0) {
                    continue;
                }
                Grid around = data.get((y + n) * width + (x + m));
                if (around.state == Grid.STATE_FLAG) {
                    sum++;
                }
            }
        }
        if (sum < grid.value) {
            return;
        }

        for (int m = -1; m <= 1; m++) {
            for (int n = -1; n <= 1; n++) {
                if (x + m < 0 || x + m >= width || y + n < 0 || y + n >= width || m == 0 && n == 0) {
                    continue;
                }
                Grid around = data.get((y + n) * width + (x + m));
                if (around.state == Grid.STATE_NORMAL || around.state == Grid.STATE_QUESTION) {
                    dig(x + m, y + n, dirty);
                }
            }
        }
    }

    public void getState(State state) {
        state.state = this.state;
        state.mines = this.minesLeft + this.mines;
    }

    public ArrayList<Grid> getData() {
        return data;
    }

    private void markDirty(Grid grid, int[] dirty) {
        if (dirty == null || dirty.length < 4) {
            return;
        }
        int x = grid.index % width;
        int y = grid.index / width;
        if (x < dirty[0]) {
            dirty[0] = x;
        }
        if (y < dirty[1]) {
            dirty[1] = y;
        }
        if (x > dirty[2]) {
            dirty[2] = x;
        }
        if (y > dirty[3]) {
            dirty[3] = y;
        }
    }

    public static class Grid {

        public static final int STATE_NORMAL = 0;
        public static final int STATE_OPEN = 1;
        public static final int STATE_FLAG = 2;
        public static final int STATE_QUESTION = 3;

        int index;

        Grid(int index) {
            this.index = index;
        }

        public int state = STATE_NORMAL;

        public int value;

        public boolean cause;

        Grid key;

        boolean open(boolean force) {
            if (state == STATE_NORMAL || force && state != STATE_OPEN) {
                state = STATE_OPEN;
                return true;
            }
            return false;
        }

        boolean nextFlag() {
            if (state == STATE_OPEN) {
                return false;
            }
            if (state == STATE_NORMAL) {
                state = STATE_FLAG;
            } else if (state == STATE_FLAG) {
                state = STATE_QUESTION;
            } else {
                state = STATE_NORMAL;
            }
            return true;
        }
    }

    public static class State {
        int state;
        int mines;
        int cause;
    }
}
