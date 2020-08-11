package com.chirag.gupshup;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.chirag.gupshup.databinding.ActivitySplashBinding;
import com.chirag.gupshup.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding mBinding;

    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBinding.ivSplash.startAnimation(animation);
        mBinding.tvAppName.startAnimation(animation);
    }
}