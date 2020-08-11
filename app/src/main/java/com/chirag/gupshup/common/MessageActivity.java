package com.chirag.gupshup.common;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.ActivityMessageBinding;

public class MessageActivity extends AppCompatActivity {

    ActivityMessageBinding mBinding;

    ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    finish();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    mBinding.tvMessage.setText(R.string.no_internet);
                }
            };

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(), networkCallback);
        }
    }

    public void btnRetryClick(View view) {
        mBinding.pbMessage.setVisibility(View.VISIBLE);
        if (Util.connectionAvailable(this)) {
            finish();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBinding.pbMessage.setVisibility(View.GONE);
                }
            }, 2000);
        }
    }

    public void btnCloseClick(View view) {
        finishAffinity();
    }
}