package name.ratson.cordova.admob.interstitial;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;

import name.ratson.cordova.admob.AbstractExecutor;
import name.ratson.cordova.admob.AdMob;
import name.ratson.cordova.admob.AdMobConfig;

public class InterstitialExecutor extends AbstractExecutor {
    /**
     * The interstitial ad to display to the user.
     */
    private InterstitialAd mInterstitialAd;

    public InterstitialExecutor(AdMob plugin) {
        super(plugin);
    }

    @Override
    public String getAdType() {
        return "interstitial";
    }

    public PluginResult prepareAd(JSONObject options, CallbackContext callbackContext) {
        AdMobConfig config = plugin.config;
        CordovaInterface cordova = plugin.cordova;
        config.setInterstitialOptions(options);

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdMobConfig config = plugin.config;
                CordovaInterface cordova = plugin.cordova;

                destroy();
                Log.i("interstitial", config.getInterstitialAdUnitId());
                InterstitialListener listener = new InterstitialListener(InterstitialExecutor.this);
                InterstitialAd.load(cordova.getActivity(), config.getInterstitialAdUnitId(), plugin.buildAdRequest(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(listener);
                        listener.onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        listener.onAdFailedToLoad(loadAdError);
                    }
                });
                delayCallback.success();
            }
        });
        return null;
    }

    @Override
    public void destroy() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(null);
            mInterstitialAd = null;
        }
    }

    public PluginResult showAd(final boolean show, final CallbackContext callbackContext) {
        if (mInterstitialAd == null) {
            return new PluginResult(PluginResult.Status.ERROR, "interstitialAd is null, call createInterstitialView first.");
        }
        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd == null) {
                    return;
                }
                AdMobConfig config = plugin.config;

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(cordova.getActivity());
                    if (callbackContext != null) {
                        callbackContext.success();
                    }
                } else if (!config.autoShowInterstitial) {
                    if (callbackContext != null) {
                        callbackContext.error("Interstital not ready yet");
                    }
                }
            }
        });

        return null;
    }

    public PluginResult isReady(final CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd != null) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                }
            }
        });

        return null;
    }

    boolean shouldAutoShow() {
        return plugin.config.autoShowInterstitial;
    }
}
