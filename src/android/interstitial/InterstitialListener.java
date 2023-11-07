package name.ratson.cordova.admob.interstitial;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;

import org.json.JSONException;
import org.json.JSONObject;

import name.ratson.cordova.admob.AbstractExecutor;

class InterstitialListener extends FullScreenContentCallback {
    private final InterstitialExecutor executor;

    InterstitialListener(InterstitialExecutor executor) {
        this.executor = executor;
    }

    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
        JSONObject data = new JSONObject();
        int errorCode = loadAdError.getCode();
        try {
            data.put("error", errorCode);
            data.put("reason", AbstractExecutor.getErrorReason(errorCode));
            data.put("adType", executor.getAdType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        executor.fireAdEvent("admob.interstitial.events.LOAD_FAIL", data);
        executor.fireAdEvent("onFailedToReceiveAd", data);
    }

    public void onAdLoaded() {
        Log.w("AdMob", "InterstitialAdLoaded");
        executor.fireAdEvent("admob.interstitial.events.LOAD");
        executor.fireAdEvent("onReceiveInterstitialAd");

        if (executor.shouldAutoShow()) {
            executor.showAd(true, null);
        }
    }

    @Override
    public void onAdClicked() {
        JSONObject data = new JSONObject();
        try {
            data.put("adType", executor.getAdType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        executor.fireAdEvent("admob.interstitial.events.EXIT_APP", data);
        executor.fireAdEvent("onLeaveToAd", data);
    }

    @Override
    public void onAdShowedFullScreenContent() {
        executor.fireAdEvent("admob.interstitial.events.OPEN");
        executor.fireAdEvent("onPresentInterstitialAd");
    }

    @Override
    public void onAdDismissedFullScreenContent() {
        executor.fireAdEvent("admob.interstitial.events.CLOSE");
        executor.fireAdEvent("onDismissInterstitialAd");
        executor.destroy();
    }
}
