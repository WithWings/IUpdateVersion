package sample.withwings.iupdateversion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import sample.withwings.updateversion.UpdateVersion;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!UpdateVersion.checkLocal(MainActivity.this)) {
                    UpdateVersion.downApk(MainActivity.this, "");
                }
            }
        });
    }
}
