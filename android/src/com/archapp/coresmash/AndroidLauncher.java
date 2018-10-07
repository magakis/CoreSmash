package com.archapp.coresmash;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.archapp.coresmash.platform.FeedbackMailHandler;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    GoogleGamesAndroid googleGamesAndroid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;

        if (CoreSmash.DEV_MODE) {
            ensurePermissionsGranted();
        }

        googleGamesAndroid = new GoogleGamesAndroid(this);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Log.i("DPI", String.valueOf(metrics.densityDpi));
        Log.i("scaledDPI", String.valueOf(metrics.scaledDensity));
        Log.i("Density", String.valueOf(metrics.density));

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useWakelock = true;
        final RelativeLayout layout = new RelativeLayout(this);

        AdmobManager adManager = new AdmobManager();

        PlatformSpecificManager platformSpecificManager = new PlatformSpecificManager();
        platformSpecificManager.googleGames = googleGamesAndroid;
        platformSpecificManager.adManager = adManager;
        platformSpecificManager.feedbackMailHandler = new FeedbackMailHandler() {
            @Override
            public void createFeedbackMail() {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"archapp.contact@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                i.putExtra(Intent.EXTRA_TEXT, "Hey what's up?\n\nI played your game and I have to say the following:\n\n");
                try {
                    startActivity(Intent.createChooser(i, "Send Feedback with..."));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        View gameView = initializeForView(new CoreSmash(platformSpecificManager), config);
        layout.addView(gameView);
        adManager.init(this, layout);
        setContentView(layout);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.GOOGLE_SIGN_IN) {
            googleGamesAndroid.onActivityResult(data);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void ensurePermissionsGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1); // This 1 is app defined as an "internal" code for handling this specific request
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    exit();
                    return;
                }
            }
        } else {
            exit();
        }
    }

    public static class RequestCode {
        public static final int NULL = 0x0;
        public static final int GOOGLE_SIGN_IN = 0x1;
    }
}
