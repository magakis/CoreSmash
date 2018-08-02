package com.coresmash;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public final class AdmobManager implements AdManager {
    private final int ADSHOW = 1;
    private final int ADHIDE = 0;
    private final String admobId;
    private final String TEST_DEVICE = "Ad Your Test Device id here";// get from log of android studio

    private AdView adView;
    private ProgressDialog dialog;
    private RewardedVideoAd rewardedVideoAd;
    private Handler handler;
    private AndroidApplication application;

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

    public void init(final AndroidApplication context, final RelativeLayout layout) {
        MobileAds.initialize(context, "ca-app-pub-3940256099942544~3347511713");
        application = context;
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);

        dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading...");
        dialog.setCanceledOnTouchOutside(false);

        adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.invalidate();
                adView.setVisibility(View.VISIBLE);
                adView.requestLayout();
                Toast.makeText(context, "AdLoaded!", Toast.LENGTH_SHORT).show();
            }
        });

        AdRequest.Builder requestBuilder = new AdRequest.Builder();
//        requestBuilder.addTestDevice(TEST_DEVICE);
        adView.loadAd(requestBuilder.build());

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);// Shows Ads on Center Bottom
        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        if (adView.getParent() == null) {
//                    layout.removeView(adView);
            layout.addView(adView, adParams);
        }

        adView.setVisibility(View.VISIBLE);

    }

    @Override
    public void show() {
        handler.sendEmptyMessage(ADSHOW);
    }

    @Override
    public void showAdForReward(final AdRewardListener listener) {
        application.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rewardedVideoAd.isLoaded()) {
                    rewardedVideoAd.show();
                } else {
                    dialog.show();
                    rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
                    rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                        @Override
                        public void onRewardedVideoAdLoaded() {
                            if (dialog.isShowing()) {
                                dialog.hide();
                                rewardedVideoAd.show();
                            }
                        }

                        @Override
                        public void onRewardedVideoAdOpened() {

                        }

                        @Override
                        public void onRewardedVideoStarted() {

                        }

                        @Override
                        public void onRewardedVideoAdClosed() {

                        }

                        @Override
                        public void onRewarded(RewardItem item) {
                            listener.reward(item.getType(), item.getAmount());
                        }

                        @Override
                        public void onRewardedVideoAdLeftApplication() {

                        }

                        @Override
                        public void onRewardedVideoAdFailedToLoad(int i) {

                        }

                        @Override
                        public void onRewardedVideoCompleted() {
                        }
                    });
                }
            }
        });
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