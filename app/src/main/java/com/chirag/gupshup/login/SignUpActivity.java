package com.chirag.gupshup.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static com.chirag.gupshup.common.NodeNames.EMAIL;
import static com.chirag.gupshup.common.NodeNames.NAME;
import static com.chirag.gupshup.common.NodeNames.ONLINE_STATUS;
import static com.chirag.gupshup.common.NodeNames.PHOTO_URL;
import static com.chirag.gupshup.common.NodeNames.USERS;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding mBinding;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri localFileUri, serverFileUri;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);

        storageReference = FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.custom_progressbar);
    }

    public void pickImage(View v) {
        if (ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        } else {
            Toast.makeText(this, R.string.access_permission_required, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            localFileUri = data.getData();
            mBinding.ivProfile.setImageURI(localFileUri);
        }
    }

    private void updateNameAndPhoto() {
        progressBar.setVisibility(View.VISIBLE);
        String strFileName = firebaseUser.getUid() + ".jpg";

        final StorageReference fileRef = storageReference.child("images/" + strFileName);

        fileRef.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverFileUri = uri;

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(mBinding.etName.getText().toString())
                                .setPhotoUri(serverFileUri)
                                .build();

                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        String userID = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS);

                                        HashMap<String, String> params = new HashMap<>();
                                        params.put(NAME, mBinding.etName.getText().toString().trim());
                                        params.put(EMAIL, mBinding.etEmail.getText().toString().trim());
                                        params.put(ONLINE_STATUS, "true");
                                        params.put(PHOTO_URL, serverFileUri.getPath());

                                        databaseReference.child(userID).setValue(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SignUpActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                }
                                            }
                                        });

                                    } else {
                                        Toast.makeText(SignUpActivity.this, getString(R.string.failed_to_update_user, task.getException()), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void updateOnlyName() {
        progressBar.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
            .setDisplayName(mBinding.etName.getText().toString()).build();


        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS);

                    HashMap<String, String> params = new HashMap<>();
                    params.put(NAME, mBinding.etName.getText().toString().trim());
                    params.put(EMAIL, mBinding.etEmail.getText().toString().trim());
                    params.put(ONLINE_STATUS, "true");
                    params.put(PHOTO_URL, "");

                    databaseReference.child(userID).setValue(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            }
                        }
                    });

                } else {
                    Toast.makeText(SignUpActivity.this, getString(R.string.failed_to_update_user, task.getException()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void btnSignUpClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        if (mBinding.etName.getText().toString().isEmpty()) {
            mBinding.etName.setError(getString(R.string.enter_name));
        } else if (mBinding.etEmail.getText().toString().isEmpty()) {
            mBinding.etEmail.setError(getString(R.string.enter_email));
        } else if (mBinding.etPassword.getText().toString().isEmpty()) {
            mBinding.etPassword.setError(getString(R.string.enter_password));
        } else if (mBinding.etConfirmPassword.getText().toString().isEmpty()) {
            mBinding.etConfirmPassword.setError(getString(R.string.confirm_password));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mBinding.etEmail.getText().toString()).matches()) {
            mBinding.etEmail.setError(getString(R.string.enter_correct_email));
        } else if (!mBinding.etPassword.getText().toString().equals(mBinding.etConfirmPassword.getText().toString())) {
            mBinding.etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            firebaseAuth.createUserWithEmailAndPassword(mBinding.etEmail.getText().toString(), mBinding.etPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            firebaseUser = firebaseAuth.getCurrentUser();
                            if (localFileUri != null)
                                updateNameAndPhoto();
                            else
                                updateOnlyName();
                        } else {
                            Toast.makeText(SignUpActivity.this, getString(R.string.signup_failed, task.getException()), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }
}