package game.minesweep;

/**
 * Created by j-zhangyang5 on 2017/1/20.
 */

public interface GameView {
    void setTime(int h, int m, int s);
    void setMines(int mines);
    void update(int[] dirty);
    void newGame();
    void endGame(int state, int time);
    void setGameControl(GameControl control);
}
