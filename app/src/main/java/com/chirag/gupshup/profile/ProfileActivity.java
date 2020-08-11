package com.chirag.gupshup.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.ActivityProfileBinding;
import com.chirag.gupshup.login.ChangePasswordActivity;
import com.chirag.gupshup.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding mBinding;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri localFileUri, serverFileUri;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            mBinding.etName.setText(firebaseUser.getDisplayName());
            mBinding.etEmail.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if (serverFileUri != null) {
                Glide.with(this)
                    .load(serverFileUri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(mBinding.ivProfile);
            }
        }
    }

    private void updateNameAndPhoto() {
        String strFileName = firebaseUser.getUid() + ".jpg";

        final StorageReference fileRef = storageReference.child("images/" + strFileName);

        fileRef.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
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
                                        params.put(EMAIL, firebaseUser.getEmail());
                                        params.put(ONLINE_STATUS, "true");
                                        params.put(PHOTO_URL, serverFileUri.getPath());

                                        databaseReference.child(userID).setValue(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    finish();
                                                }
                                            }
                                        });

                                    } else {
                                        Toast.makeText(ProfileActivity.this, getString(R.string.failed_to_update_user, task.getException()), Toast.LENGTH_SHORT).show();
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
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
            .setDisplayName(mBinding.etName.getText().toString()).build();


        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS);

                    HashMap<String, String> params = new HashMap<>();
                    params.put(NAME, mBinding.etName.getText().toString().trim());

                    databaseReference.child(userID).setValue(params).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                finish();
                            }
                        }
                    });

                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.failed_to_update_user, task.getException()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void btnLogoutClick(View view) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }

    public void btnSaveClick(View view) {
        if (mBinding.etName.getText().toString().isEmpty()) {
            mBinding.etName.setError(getString(R.string.enter_name));
        } else {
            if (localFileUri != null) {
                updateNameAndPhoto();
            } else
                updateOnlyName();
        }
    }

    public void changeImage(View v) {
        if (serverFileUri == null) {
            pickImage();
        } else {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_picture, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();

                    if (id == R.id.mnuChangePicture) {
                        pickImage();
                    } else if (id == R.id.mnuRemovePicture) {
                        removePhoto();
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    public void pickImage() {
        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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

    private void removePhoto() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
            .setDisplayName(mBinding.etName.getText().toString())
            .setPhotoUri(null)
            .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS);

                    HashMap<String, String> params = new HashMap<>();
                    params.put(PHOTO_URL, "");

                    databaseReference.child(userID).setValue(params)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    finish();
                                }
                            }
                        });

                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.failed_to_update_user, task.getException()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void btnChangePasswordClick(View view) {
        startActivity(new Intent(this, ChangePasswordActivity.class));
    }
}