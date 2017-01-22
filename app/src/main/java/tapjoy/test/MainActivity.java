package tapjoy.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import game.minesweep.LaunchActivity;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, LaunchActivity.class));
        if (true) {
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        findViewById(R.id.txt).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Toast.makeText(App.getApp(), "test toast", Toast.LENGTH_LONG).show();
    }
}
