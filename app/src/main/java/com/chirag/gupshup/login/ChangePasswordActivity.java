package com.chirag.gupshup.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    ActivityChangePasswordBinding mBinding;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_change_password);
        progressBar = findViewById(R.id.custom_progressbar);
    }

    public void btnChangePasswordClick(View view) {
        if (mBinding.etPassword.getText().toString().isEmpty()) {
            mBinding.etPassword.setError(getString(R.string.enter_password));
        } else if (mBinding.etConfirmPassword.getText().toString().isEmpty()) {
            mBinding.etConfirmPassword.setError(getString(R.string.confirm_password));
        } else if (!mBinding.etPassword.getText().toString().equals(mBinding.etConfirmPassword.getText().toString())) {
            mBinding.etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            progressBar.setVisibility(View.VISIBLE);
            if (firebaseUser != null) {
                firebaseUser.updatePassword(mBinding.etPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this, R.string.password_changed_successfully, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, getString(R.string.something_went_wrong, task.getException()), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}