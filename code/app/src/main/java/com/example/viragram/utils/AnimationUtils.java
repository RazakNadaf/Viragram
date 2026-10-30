package com.example.viragram.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class AnimationUtils {

    public static void applyPressScale(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
        scaleUpX.setDuration(150);
        scaleUpY.setDuration(150);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.play(scaleUpX).with(scaleUpY);
        scaleUp.setStartDelay(100);

        AnimatorSet finalSet = new AnimatorSet();
        finalSet.play(scaleDown).before(scaleUp);
        finalSet.start();
    }

    public static void applyPulseAnimation(View view) {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.25f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.25f);
        scaleUpX.setDuration(150);
        scaleUpY.setDuration(150);
        scaleUpX.setInterpolator(new OvershootInterpolator());
        scaleUpY.setInterpolator(new OvershootInterpolator());

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.25f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.25f, 1f);
        scaleDownX.setDuration(150);
        scaleDownY.setDuration(150);
        scaleDownX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleDownY.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet pulse = new AnimatorSet();
        pulse.play(scaleUpX).with(scaleUpY);
        
        AnimatorSet shrink = new AnimatorSet();
        shrink.play(scaleDownX).with(scaleDownY);
        shrink.setStartDelay(150);

        AnimatorSet finalSet = new AnimatorSet();
        finalSet.play(pulse).before(shrink);
        finalSet.start();
    }

    public static void applyFadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1f).setDuration(300).start();
    }
    
    public static void applySlideUpFadeIn(View view) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1f).translationY(0f).setDuration(400).setInterpolator(new OvershootInterpolator()).start();
    }
    
    public static void applySlideRightFadeIn(View view) {
        view.setAlpha(0f);
        view.setTranslationX(-50f);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1f).translationX(0f).setDuration(350).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }
    
    public static void applyStaggeredFadeIn(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(30f);
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(position * 50L)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator())
            .start();
    }
}