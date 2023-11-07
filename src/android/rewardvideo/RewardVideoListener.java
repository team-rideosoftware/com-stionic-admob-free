package name.ratson.cordova.admob.rewardvideo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;

import org.json.JSONException;
import org.json.JSONObject;

import name.ratson.cordova.admob.AbstractExecutor;

class RewardVideoListener extends FullScreenContentCallback {
    private final RewardVideoExecutor executor;

    RewardVideoListener(RewardVideoExecutor executor) {
        this.executor = executor;
    }

    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
        synchronized (executor.rewardedVideoLock) {
            executor.isRewardedVideoLoading = false;
        }

        JSONObject data = new JSONObject();
        int errorCode = loadAdError.getCode();
        try {
            data.put("error", errorCode);
            data.put("reason", AbstractExecutor.getErrorReason(errorCode));
            data.put("adType", executor.getAdType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        executor.fireAdEvent("admob.rewardvideo.events.LOAD_FAIL", data);
    }

    public void onAdLoaded() {
        synchronized (executor.rewardedVideoLock) {
            executor.isRewardedVideoLoading = false;
        }
        Log.w("AdMob", "RewardedVideoAdLoaded");
        executor.fireAdEvent("admob.rewardvideo.events.LOAD");

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
        executor.fireAdEvent("admob.rewardvideo.events.EXIT_APP", data);
    }

    @Override
    public void onAdShowedFullScreenContent() {
        executor.fireAdEvent("admob.rewardvideo.events.OPEN");
    }

    @Override
    public void onAdDismissedFullScreenContent() {
        executor.fireAdEvent("admob.rewardvideo.events.CLOSE");
        executor.clearAd();
    }

    public void onRewarded(RewardItem reward) {
        JSONObject data = new JSONObject();
        try {
            data.put("adType", executor.getAdType());
            data.put("rewardType", reward.getType());
            data.put("rewardAmount", reward.getAmount());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        executor.fireAdEvent("admob.rewardvideo.events.REWARD", data);
    }
}
