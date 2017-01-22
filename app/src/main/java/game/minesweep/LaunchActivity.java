package game.minesweep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import tapjoy.test.R;

/**
 * Created by j-zhangyang5 on 2017/1/22.
 */

public class LaunchActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_activity);
        findViewById(R.id.easy).setOnClickListener(this);
        findViewById(R.id.medium).setOnClickListener(this);
        findViewById(R.id.expert).setOnClickListener(this);
        findViewById(R.id.custom).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.easy:
                GameActivity.launch(this, GameSettings.getEasySettings());
                break;
            case R.id.medium:
                GameActivity.launch(this, GameSettings.getMediumSettings());
                break;
            case R.id.expert:
                GameActivity.launch(this, GameSettings.getExpertSettings());
                break;
            case R.id.custom:
                startActivity(new Intent(this, CustomActivity.class));
                break;
        }
    }
}
