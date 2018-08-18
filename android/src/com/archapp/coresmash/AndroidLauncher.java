package com.archapp.coresmash;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useWakelock = true;
        RelativeLayout layout = new RelativeLayout(this);

        AdmobManager adManager = new AdmobManager("ca-app-pub-3940256099942544/6300978111");
        View gameView = initializeForView(new CoreSmash(adManager), config);
        layout.addView(gameView);
        adManager.init(this, layout);
        setContentView(layout);

    }
}
