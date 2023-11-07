package name.ratson.cordova.admob.rewardvideo;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;

import name.ratson.cordova.admob.AbstractExecutor;
import name.ratson.cordova.admob.AdMob;

public class RewardVideoExecutor extends AbstractExecutor {
    /**
     * RewardVideo
     */
    private RewardedAd mRewardedVideoAd;
    boolean isRewardedVideoLoading = false;
    final Object rewardedVideoLock = new Object();

    public RewardVideoExecutor(AdMob plugin) {
        super(plugin);
    }

    @Override
    public String getAdType() {
        return "rewardvideo";
    }

    public PluginResult prepareAd(JSONObject options, CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;
        plugin.config.setRewardVideoOptions(options);

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CordovaInterface cordova = plugin.cordova;
                clearAd();

                Log.w("rewardedvideo", plugin.config.getRewardedVideoAdUnitId());
                RewardVideoListener listener = new RewardVideoListener(RewardVideoExecutor.this);

                synchronized (rewardedVideoLock) {
                    if (!isRewardedVideoLoading) {
                        isRewardedVideoLoading = true;
                        Bundle extras = new Bundle();
                        extras.putBoolean("_noRefresh", true);
                        AdRequest adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
                        RewardedAd.load(cordova.getContext(), plugin.config.getRewardedVideoAdUnitId(), adRequest, new RewardedAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                mRewardedVideoAd = rewardedAd;
                                mRewardedVideoAd.setFullScreenContentCallback(listener);
                                listener.onAdLoaded();
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                mRewardedVideoAd = null;
                                listener.onAdFailedToLoad(loadAdError);
                            }
                        });
                        delayCallback.success();
                    }
                }
            }
        });
        return null;
    }

    public void clearAd() {
        if (mRewardedVideoAd == null) {
            return;
        }
        mRewardedVideoAd.setFullScreenContentCallback(null);
        mRewardedVideoAd = null;
    }

    public PluginResult showAd(final boolean show, final CallbackContext callbackContext) {
        if (mRewardedVideoAd == null) {
            return new PluginResult(PluginResult.Status.ERROR, "rewardedVideoAd is null, call createRewardVideo first.");
        }
        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mRewardedVideoAd != null) {
                    mRewardedVideoAd.show(cordova.getActivity(), rewardItem -> {
                        RewardVideoListener listener = new RewardVideoListener(RewardVideoExecutor.this);
                        listener.onRewarded(rewardItem);
                    });
                }

                if (callbackContext != null) {
                    callbackContext.success();
                }
            }
        });

        return null;
    }

    @Override
    public void destroy() {
        this.clearAd();
    }

    public PluginResult isReady(final CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRewardedVideoAd != null) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                }
            }
        });

        return null;
    }

    boolean shouldAutoShow() {
        return plugin.config.autoShowRewardVideo;
    }
}
