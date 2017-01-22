package game.minesweep;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import tapjoy.test.R;

/**
 * Created by j-zhangyang5 on 2017/1/22.
 */

public class CustomActivity extends Activity implements View.OnClickListener {

    GameSettings min, max;

    private EditText widthEdit, heightEdit, minesEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_activity);

        GameSettings custom = GameSettings.getCustomSettings(this);
        min = GameSettings.getMinSettings();
        max = GameSettings.getMaxSettings();

        TextView width = (TextView) findViewById(R.id.width);
        width.setText(String.format("WIDTH (%d - %d)", min.width, max.width));
        TextView height = (TextView) findViewById(R.id.height);
        height.setText(String.format("HEIGHT (%d - %d)", min.height, max.height));
        TextView mines = (TextView) findViewById(R.id.mines);
        mines.setText(String.format("MINES (%d - %d)", min.mines, max.mines));

        widthEdit = (EditText) findViewById(R.id.width_edit);
        widthEdit.setText(Integer.toString(custom.width));
        heightEdit = (EditText) findViewById(R.id.height_edit);
        heightEdit.setText(Integer.toString(custom.height));
        minesEdit = (EditText) findViewById(R.id.mines_edit);
        minesEdit.setText(Integer.toString(custom.mines));

        findViewById(R.id.ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ok) {
            try {
                int w = Integer.parseInt(widthEdit.getText().toString());
                int h = Integer.parseInt(heightEdit.getText().toString());
                int m = Integer.parseInt(minesEdit.getText().toString());
                GameSettings.saveCustomSettings(this, w, h, m);
            } catch (Exception e) {
                // ignore
            }
            GameActivity.launch(this, GameSettings.getCustomSettings(this));
            finish();
        }
    }
}
