package com.chirag.gupshup.login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.ActivityResetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    ActivityResetPasswordBinding mBinding;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password);
        progressBar = findViewById(R.id.custom_progressbar);
    }

    public void btnResetPasswordClick(View view) {
        if (mBinding.etEmail.getText().toString().isEmpty()) {
            mBinding.etEmail.setError(getString(R.string.enter_email));
        } else {
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.sendPasswordResetEmail(mBinding.etEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mBinding.llResetPassword.setVisibility(View.GONE);
                    mBinding.llMessage.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        mBinding.tvMessage.setText(getString(R.string.reset_password_instructions, mBinding.etEmail.getText().toString()));
                        new CountDownTimer(60000, 1000) {

                            @Override
                            public void onTick(long l) {
                                mBinding.btnRetry.setText(getString(R.string.resend_timer, String.valueOf(l / 1000)));
                                mBinding.btnRetry.setOnClickListener(null);
                            }

                            @Override
                            public void onFinish() {
                                mBinding.btnRetry.setText(getString(R.string.retry));

                                mBinding.btnRetry.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mBinding.llResetPassword.setVisibility(View.VISIBLE);
                                        mBinding.llMessage.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }.start();
                    } else {
                        mBinding.tvMessage.setText(getString(R.string.failed_to_send_email, task.getException()));
                        mBinding.btnRetry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mBinding.llResetPassword.setVisibility(View.VISIBLE);
                                mBinding.llMessage.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        }
    }

    public void btnCloseClick(View view) {
        finish();
    }
}