package game.minesweep;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import tapjoy.test.R;

/**
 * Created by j-zhangyang5 on 2017/1/20.
 */

public class GameActivity extends Activity implements GameView, View.OnClickListener{

    private Game game;
    public  static final String EXTRA_GAME_SETTINGS = "extra_settings";

    private TextView mines;
    private TextView time;
    private View newGame;
    private View restart;
    private View digMode;
    private MineGrid grid;

    private boolean isDigMode = true;

    public static void launch(Activity activity, GameSettings settings) {
        Intent intent = new Intent(activity, GameActivity.class);
        intent.putExtra(EXTRA_GAME_SETTINGS, settings);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameSettings settings = getIntent().getParcelableExtra(EXTRA_GAME_SETTINGS);
        if (settings == null) {
            finish();
            return;
        }
        setContentView(R.layout.minesweep);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mines = (TextView) findViewById(R.id.mines);
        time = (TextView) findViewById(R.id.time);
        newGame = findViewById(R.id.new_game);
        restart = findViewById(R.id.restart_game);
        digMode = findViewById(R.id.dig);
        grid = (MineGrid) findViewById(R.id.grid);

        newGame.setOnClickListener(this);
        restart.setOnClickListener(this);
        digMode.setOnClickListener(this);
        grid.setOnClickListener(this);

        grid.setGameSettings(settings.width, settings.height);

        game = new Game(this, this, settings.width, settings.height, settings.mines);
        game.newGame();
    }

    @Override
    protected void onStart() {
        super.onStart();
        game.resumeGame();
    }

    @Override
    protected void onStop() {
        super.onStop();
        game.pauseGame();
    }

    @Override
    public void setTime(int h, int m, int s) {
        if (h > 0) {
            time.setTextSize(14);
            time.setText(String.format("%d:%02d:%02d", h, m, s));
        } else {
            time.setTextSize(18);
            time.setText(String.format("%02d:%02d", m, s));
        }
    }

    @Override
    public void setMines(int mines) {
        this.mines.setText("¤ x\n" + mines);
    }

    @Override
    public void update(int[] dirty) {
        grid.update(dirty);
    }

    @Override
    public void newGame() {
        isDigMode = true;
        digMode.setBackgroundColor(0xffff0000);
        time.setText("00:00");
        mines.setText("¤ x\n" + game.getMines());
        grid.newGame();
    }

    @Override
    public void endGame(int win, int time) {
        grid.endGame(win, time);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(win >= 0 ? "Win" : "Game Over");
        builder.setMessage(win < 0 ? "Bad Luck ..." : win == 0 ? "You Win ! Yes !" : ("New Record: " + GameRecord.formatTime(time) + " !"));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        game.newGame();
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        game.restartGame();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        finish();
                }
            }
        };
        builder.setPositiveButton("New Game", listener);
        builder.setNeutralButton("Restart", listener);
        builder.setNegativeButton("Quit", listener);

        builder.create().show();
    }

    @Override
    public void setGameControl(GameControl control) {
        grid.setGameControl(control);
    }

    @Override
    public void onClick(View view) {
        if (view == newGame) {
            game.newGame();
        } else if (view == restart) {
            game.restartGame();
        } else if (view == digMode) {
            isDigMode = !isDigMode;
            grid.setDigMode(isDigMode);
            if (isDigMode) {
                digMode.setBackgroundColor(0xffff0000);
            } else {
                digMode.setBackgroundColor(0xff00a1e7);
            }
        }
    }
}
