package com.chirag.gupshup.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.chirag.gupshup.MainActivity;
import com.chirag.gupshup.R;
import com.chirag.gupshup.common.MessageActivity;
import com.chirag.gupshup.common.Util;
import com.chirag.gupshup.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.chirag.gupshup.common.Util.updateDeviceToken;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding mBinding;

    private View progressBar;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {

            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> updateDeviceToken(LoginActivity.this, instanceIdResult.getToken()));

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        progressBar = findViewById(R.id.custom_progressbar);
    }

    public void btnSignUpClick(View v) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }

    public void btnLoginClick(View v) {
        if (mBinding.etEmail.getText().toString().isEmpty()) {
            mBinding.etEmail.setError(getString(R.string.enter_email));
        } else if (mBinding.etPassword.getText().toString().isEmpty()) {
            mBinding.etPassword.setError(getString(R.string.enter_password));
        } else {
            if (Util.connectionAvailable(this)) {

                progressBar.setVisibility(View.VISIBLE);

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signInWithEmailAndPassword(mBinding.etEmail.getText().toString(), mBinding.etPassword.getText().toString())
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                                if (firebaseUser != null) {

                                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> updateDeviceToken(LoginActivity.this, instanceIdResult.getToken()));

                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Login Failed: - " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                startActivity(new Intent(this, MessageActivity.class));
            }
        }
    }

    public void tvResetPasswordClick(View view) {
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }
}