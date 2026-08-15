package com.example.data.ad

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AdStatus {
    object Idle : AdStatus()
    object Loading : AdStatus()
    object Ready : AdStatus()
    data class Error(val message: String) : AdStatus()
}

class RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
    private var isShowingAd: Boolean = false

    private val _adStatus = MutableStateFlow<AdStatus>(AdStatus.Idle)
    val adStatus: StateFlow<AdStatus> = _adStatus.asStateFlow()

    companion object {
        // Official Google Test Rewarded Ad Unit ID for Android
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    /**
     * Preloads a Rewarded Ad if not already loading or loaded.
     */
    fun loadAd(context: Context) {
        if (rewardedAd != null) {
            _adStatus.value = AdStatus.Ready
            return
        }
        if (_adStatus.value is AdStatus.Loading) {
            return
        }

        _adStatus.value = AdStatus.Loading

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            TEST_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _adStatus.value = AdStatus.Ready
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    _adStatus.value = AdStatus.Error(loadAdError.message.ifBlank { "Ad Not Available" })
                }
            }
        )
    }

    /**
     * Shows the Rewarded Ad if loaded.
     * Prevents multiple ad presentations via rapid button clicks.
     */
    fun showAd(
        context: Context,
        onUserEarnedReward: () -> Unit,
        onAdClosedWithoutReward: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        // Prevent rapid button clicks from opening multiple ads
        if (isShowingAd) return

        val activity = context.findActivity()
        if (activity == null) {
            onAdFailedToShow("Activity not available")
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            _adStatus.value = AdStatus.Error("Ad Not Available")
            onAdFailedToShow("Ad Not Available")
            // Attempt to preload for next retry
            loadAd(context)
            return
        }

        isShowingAd = true
        var isRewardEarned = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // Ad was displayed; clear reference so it won't be reused
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                isShowingAd = false
                _adStatus.value = AdStatus.Error(adError.message.ifBlank { "Failed to show ad" })
                onAdFailedToShow(adError.message)
                // Preload the next rewarded ad
                loadAd(context)
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                isShowingAd = false
                if (!isRewardEarned) {
                    onAdClosedWithoutReward()
                }
                // Preload the next rewarded ad after ad is dismissed
                loadAd(context)
            }
        }

        ad.show(activity) { _ ->
            // User completed watching the ad and earned the reward
            isRewardEarned = true
            onUserEarnedReward()
        }
    }

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
