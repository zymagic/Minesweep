package game.minesweep;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by j-zhangyang5 on 2017/1/20.
 */

public class Game {

    Activity context;
    private GameView gameView;
    private int[] dirty = new int[4];
    private Core.State state = new Core.State();
    private Core core;
    private int width;
    private int height;
    private int mines;

    private GameTimer timer = new GameTimer();

    public Game(Activity context, GameView gameView, int width, int height, int mines) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.mines = mines;
        this.gameView = gameView;
        gameView.setGameControl(gameControl);
    }

    public void newGame() {
        core = new Core(width, height, mines);
        timer.restart();
        gameView.newGame();
    }

    public void restartGame() {
        if (core != null) {
            core.restart();
            timer.restart();
            gameView.newGame();
        }
    }

    public void resumeGame() {
        timer.resume();
    }

    public void pauseGame() {
        timer.pause();
    }

    public int getMines() {
        return mines;
    }

    private GameControl gameControl = new GameControl() {
        @Override
        public void dig(int x, int y) {
            dirty[0] = dirty[2] = x;
            dirty[1] = dirty[3] = y;
            if (core != null) {
                core.dig(x, y, dirty);
                update();
            }
        }

        @Override
        public void mark(int x, int y) {
            dirty[0] = dirty[2] = x;
            dirty[1] = dirty[3] = y;
            if (core != null) {
                core.mark(x, y, dirty);
                update();
            }
        }

        @Override
        public void sweep(int x, int y) {
            dirty[0] = dirty[2] = x;
            dirty[1] = dirty[3] = y;
            if (core != null) {
                core.sweep(x, y, dirty);
                update();
            }
        }

        void update() {
            core.getState(state);
            if (state.state == -1) {
                timer.stop();
                gameView.endGame(false);
            } else if (state.state == 1) {
                timer.stop();
                gameView.endGame(true);
            } else {
                if (dirty[0] <= dirty[2] && dirty[1] <= dirty[3]) {
                    gameView.update(dirty);
                }
                gameView.setMines(state.mines);
            }
        }

        @Override
        public ArrayList<Core.Grid> getData() {
            return core == null ? null : core.getData();
        }
    };

    private class GameTimer implements Runnable {

        int seconds;
        long startTime = -1;
        Handler handler = new Handler(Looper.getMainLooper());

        void start() {
            if (startTime != -1) {
                return;
            }
            startTime = System.currentTimeMillis();
            seconds = 0;
            handler.postDelayed(this, 1000);
        }

        void pause() {
            if (startTime == -1) {
                return;
            }
            handler.removeCallbacks(this);
        }

        void resume() {
            if (startTime == -1) {
                return;
            }
            handler.post(this);
        }

        void stop() {
            handler.removeCallbacks(this);
        }

        void restart() {
            startTime = -1;
            seconds = 0;
            start();
        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            long passed = now - startTime;
            seconds = (int) (passed / 1000);
            long next = startTime + seconds * 1000 - now + 1000;
            handler.postDelayed(this, next);
            int h = seconds / 3600;
            int m = (seconds - h * 3600) / 60;
            int s = seconds - h * 3600 - m * 60;
            gameView.setTime(h, m, s);
        }
    }
}
