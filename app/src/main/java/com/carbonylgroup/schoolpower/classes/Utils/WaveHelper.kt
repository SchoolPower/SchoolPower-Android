/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.gelitenight.waveview.library.WaveView
import java.util.ArrayList


class WaveHelper(private val mWaveView: WaveView, private val lastLevelRatio: Float = 0f, private var lastShiftRatio: Float = 0f) {

    private var mAnimatorSet: AnimatorSet? = null

    init {
        initAnimation()
    }

    fun start() {

        mWaveView.isShowWave = true
        if (mAnimatorSet != null) mAnimatorSet!!.start()
    }

    private fun initAnimation() {

        val animators = ArrayList<Animator>()

        // horizontal animation.
        // wave waves infinitely.
        val waveShiftAnim1 = ObjectAnimator.ofFloat(
                mWaveView, "waveShiftRatio", lastShiftRatio, 1f)
        waveShiftAnim1.repeatCount = 1
        waveShiftAnim1.duration = (1000f * (1f - lastShiftRatio)).toLong()
        waveShiftAnim1.interpolator = LinearInterpolator()
        animators.add(waveShiftAnim1)

        val waveShiftAnim2 = ObjectAnimator.ofFloat(
                mWaveView, "waveShiftRatio", 0f, 1f)
        waveShiftAnim2.repeatCount = ValueAnimator.INFINITE
        waveShiftAnim2.duration = 1000
        waveShiftAnim2.startDelay = waveShiftAnim1.duration
        waveShiftAnim2.interpolator = LinearInterpolator()
        animators.add(waveShiftAnim2)

        // vertical animation.
        // water level increases from 0 to center of WaveView
        val waterLevelAnim = ObjectAnimator.ofFloat(
                mWaveView, "waterLevelRatio", lastLevelRatio, mWaveView.waterLevelRatio)
        waterLevelAnim.duration = 1000
        waterLevelAnim.interpolator = DecelerateInterpolator()
        animators.add(waterLevelAnim)

        // amplitude animation.
        // wave grows big then grows small, repeatedly
        val amplitudeAnim = ObjectAnimator.ofFloat(
                mWaveView, "amplitudeRatio", 0.03f, 0.03f)
        amplitudeAnim.repeatCount = ValueAnimator.INFINITE
        amplitudeAnim.repeatMode = ValueAnimator.REVERSE
        amplitudeAnim.duration = 1000
        amplitudeAnim.interpolator = LinearInterpolator()
        animators.add(amplitudeAnim)

        mAnimatorSet = AnimatorSet()
        mAnimatorSet!!.playTogether(animators)
    }

    fun cancel() {
        if (mAnimatorSet != null) mAnimatorSet!!.end()
    }
}
