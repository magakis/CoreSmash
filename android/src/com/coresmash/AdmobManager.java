package com.coresmash;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public final class AdmobManager implements AdManager {
    private final int ADSHOW = 1;
    private final int ADHIDE = 0;
    private final String admobId;
    private final String TEST_DEVICE = "Ad Your Test Device id here";// get from log of android studio

    private AdView adView;
    private Handler handler;

    public AdmobManager(String id) {
        this.admobId = id;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ADSHOW:
                        adView.setVisibility(View.VISIBLE);
                        break;
                    case ADHIDE:
                        adView.setVisibility(View.GONE);
                        break;
                }
            }
        };
    }

    public void init(Context context, RelativeLayout layout) {
//        MobileAds.initialize(context, admobId);

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);// Shows Ads on Center Bottom
        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        AdRequest.Builder requestBuilder = new AdRequest.Builder();
//        requestBuilder.addTestDevice(TEST_DEVICE);
        adView.loadAd(requestBuilder.build());

        layout.addView(adView, adParams);
    }

    @Override
    public void show() {
        handler.sendEmptyMessage(ADSHOW);
    }

    @Override
    public void toggle() {
        if (adView.isShown()) {
            handler.sendEmptyMessage(ADHIDE);
        } else {
            handler.sendEmptyMessage(ADSHOW);
        }
    }

    @Override
    public void hide() {
        handler.sendEmptyMessage(ADHIDE);
    }
}

