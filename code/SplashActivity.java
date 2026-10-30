package com.example.viragram.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.OvershootInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.example.viragram.MainActivity;
import com.example.viragram.databinding.ActivitySplashBinding;
import com.example.viragram.firebase.FirebaseAuthManager;

/**
 * Startup splash activity displaying animated Viragram branding and routing based on auth state.
 */
public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        playSplashAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (FirebaseAuthManager.getInstance().isUserSignedIn() && FirebaseAuthManager.getInstance().isEmailVerified()) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2200); // 2200ms holds the completed splash long enough to see the animation finish
    }

    private void playSplashAnimation() {
        // 1. Logo scales and fades in
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(binding.ivSplashLogo, "scaleX", 0.75f, 1.0f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(binding.ivSplashLogo, "scaleY", 0.75f, 1.0f);
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(binding.ivSplashLogo, "alpha", 0f, 1f);
        
        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(logoScaleX, logoScaleY, logoAlpha);
        logoSet.setDuration(600);
        logoSet.setInterpolator(new OvershootInterpolator(1.2f));

        // 2. "Viragram" title fades and slides up slightly
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(binding.tvSplashTitle, "alpha", 0f, 1f);
        ObjectAnimator titleSlide = ObjectAnimator.ofFloat(binding.tvSplashTitle, "translationY", 30f, 0f);
        
        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(titleAlpha, titleSlide);
        titleSet.setDuration(500);
        titleSet.setInterpolator(new DecelerateInterpolator());

        // 3. Subtitle fades in
        ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(binding.tvSplashSubtitle, "alpha", 0f, 1f);
        subtitleAlpha.setDuration(400);

        // 4. Footer fades in
        ObjectAnimator footerAlpha = ObjectAnimator.ofFloat(binding.tvSplashFooter, "alpha", 0f, 1f);
        footerAlpha.setDuration(400);

        // Choreograph the sequence
        AnimatorSet masterSequence = new AnimatorSet();
        masterSequence.playSequentially(
                logoSet,
                titleSet,
                subtitleAlpha
        );
        masterSequence.playTogether(subtitleAlpha, footerAlpha);
        masterSequence.start();
    }
}