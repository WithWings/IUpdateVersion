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
                    UpdateVersion.downApk(MainActivity.this, "https://github.com/WithWings/MarkdownUseSrc/blob/master/%E6%96%B0%E6%B5%AA_V1.4.0_RELEASE_140_1_vivo_sign.apk?raw=true");
                }
            }
        });
    }
}
