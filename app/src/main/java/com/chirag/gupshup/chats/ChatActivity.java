package com.chirag.gupshup.chats;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.chirag.gupshup.R;
import com.chirag.gupshup.common.Constants;
import com.chirag.gupshup.common.NodeNames;
import com.chirag.gupshup.common.Util;
import com.chirag.gupshup.databinding.ActivityChatBinding;
import com.chirag.gupshup.selectFriend.SelectFriendActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static com.chirag.gupshup.common.Constants.IMAGES_FOLDER;
import static com.chirag.gupshup.common.Constants.MESSAGE_IMAGES;
import static com.chirag.gupshup.common.Constants.MESSAGE_TYPE_IMAGE;
import static com.chirag.gupshup.common.Constants.MESSAGE_TYPE_TEXT;
import static com.chirag.gupshup.common.Constants.MESSAGE_TYPE_VIDEO;
import static com.chirag.gupshup.common.Constants.MESSAGE_VIDEOS;
import static com.chirag.gupshup.common.Constants.TYPING_STARTED;
import static com.chirag.gupshup.common.Constants.USER_STATUS_ONLINE;
import static com.chirag.gupshup.common.Constants.USER_STATUS_TYPING;
import static com.chirag.gupshup.common.Extras.MESSAGE;
import static com.chirag.gupshup.common.Extras.MESSAGE_ID;
import static com.chirag.gupshup.common.Extras.MESSAGE_TYPE;
import static com.chirag.gupshup.common.Extras.PHOTO_NAME;
import static com.chirag.gupshup.common.Extras.USER_KEY;
import static com.chirag.gupshup.common.Extras.USER_NAME;
import static com.chirag.gupshup.common.NodeNames.CHATS;
import static com.chirag.gupshup.common.NodeNames.ONLINE_STATUS;
import static com.chirag.gupshup.common.NodeNames.TYPING;
import static com.chirag.gupshup.common.NodeNames.UNREAD_COUNT;
import static com.chirag.gupshup.common.NodeNames.USERS;
import static com.chirag.gupshup.common.Util.sendNotification;
import static com.chirag.gupshup.common.Util.updateChatDetails;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {


    ActivityChatBinding mBinding;
    DatabaseReference mRootRef, databaseReferenceMessages;
    FirebaseAuth firebaseAuth;
    String currentUserId, chatUserId, userName, photoName;

    private MessagesAdapter messagesAdapter;
    private List<MessageModel> messageModelList;

    private int currentPage = 1;
    private static final int RECORD_PER_PAGE = 30;

    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 102;
    private static final int REQUEST_CODE_PICK_VIDEO = 103;
    private static final int REQUEST_CODE_FORWARD = 104;

    ImageView ivProfile;
    TextView tvUserName, tvUserStatus;


    private BottomSheetDialog bottomSheetDialog;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_action_bar, null);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);

            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        }

        ivProfile = findViewById(R.id.ivProfile);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);

        mBinding.ivSend.setOnClickListener(this);
        mBinding.ivAttachment.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        if (getIntent().hasExtra(USER_KEY)) {
            chatUserId = getIntent().getStringExtra(USER_KEY);
        }

        if (getIntent().hasExtra(USER_NAME)) {
            userName = getIntent().getStringExtra(USER_NAME);
        }

        if (getIntent().hasExtra(PHOTO_NAME)) {
            photoName = getIntent().getStringExtra(PHOTO_NAME);
        }

        tvUserName.setText(userName);

        if (!TextUtils.isEmpty(photoName)) {

            StorageReference photoRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER).child(chatUserId + ".jpg");
            photoRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(ChatActivity.this)
                    .load(uri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(ivProfile));
        }

        messageModelList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messageModelList);

        mBinding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvMessages.setAdapter(messagesAdapter);

        loadMessages();

        mRootRef.child(CHATS).child(currentUserId).child(chatUserId).child(UNREAD_COUNT).setValue(0);
        mBinding.rvMessages.scrollToPosition(messageModelList.size() - 1);

        mBinding.srlMessages.setOnRefreshListener(() -> {
            currentPage++;
            loadMessages();
            mBinding.srlMessages.setRefreshing(false);
        });


        bottomSheetDialog = new BottomSheetDialog(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.chat_file_options, null);
        view.findViewById(R.id.llCameraOption).setOnClickListener(this);
        view.findViewById(R.id.llGalleryOption).setOnClickListener(this);
        view.findViewById(R.id.llVideoOption).setOnClickListener(this);
        view.findViewById(R.id.ivClose).setOnClickListener(this);
        bottomSheetDialog.setContentView(view);

        if (getIntent().hasExtra(MESSAGE) && getIntent().hasExtra(MESSAGE_ID) && getIntent().hasExtra(MESSAGE_TYPE)) {
            String message = getIntent().getStringExtra(MESSAGE);
            String messageId = getIntent().getStringExtra(MESSAGE_ID);
            String messageType = getIntent().getStringExtra(MESSAGE_TYPE);

            DatabaseReference messageRef = mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
            String newMessageId = messageRef.getKey();

            if (messageType != null) {
                if (messageType.equals(MESSAGE_TYPE_TEXT)) {
                    sendMessage(message, messageType, newMessageId);
                } else {
                    StorageReference rootRef = FirebaseStorage.getInstance().getReference();
                    String folderName = messageType.equals(MESSAGE_TYPE_VIDEO) ? MESSAGE_VIDEOS : MESSAGE_IMAGES;
                    String oldFileName = messageType.equals(MESSAGE_TYPE_VIDEO) ? messageId + ".mp4" : messageId + ".jpg";
                    String newFileName = messageType.equals(MESSAGE_TYPE_VIDEO) ? newMessageId + ".mp4" : newMessageId + ".jpg";


                    String localFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + oldFileName;
                    File localFile = new File(localFilePath);
                    StorageReference newFileRef = rootRef.child(folderName).child(newFileName);

                    rootRef.child(folderName).child(oldFileName).getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                        UploadTask uploadTask = newFileRef.putFile(Uri.fromFile(localFile));
                        uploadProgress(uploadTask, newFileRef, newMessageId, messageType);
                    });
                }
            }
        }

        DatabaseReference databaseReferenceUsers = mRootRef.child(USERS).child(chatUserId);
        databaseReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = "";
                if (snapshot.child(ONLINE_STATUS).getValue() != null) {
                    status = String.valueOf(snapshot.child(ONLINE_STATUS).getValue());
                }

                tvUserStatus.setText(status.equalsIgnoreCase("true") ? Constants.USER_STATUS_ONLINE : Constants.USER_STATUS_OFFLINE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mBinding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                DatabaseReference currentUserRef = mRootRef.child(CHATS).child(currentUserId).child(chatUserId);
                currentUserRef.child(NodeNames.TYPING).setValue(editable.toString().matches("") ? Constants.TYPING_STOPPED : Constants.TYPING_STARTED);
            }
        });

        DatabaseReference chatUserRef = mRootRef.child(CHATS).child(chatUserId).child(currentUserId);
        chatUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(TYPING).getValue() != null) {
                    String typingStatus = String.valueOf(snapshot.child(TYPING).getValue());
                    tvUserStatus.setText(typingStatus.equals(TYPING_STARTED) ? USER_STATUS_TYPING : USER_STATUS_ONLINE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String msg, String msgType, String pushId) {

        try {
            if (!msg.equals("")) {
                HashMap<String, Object> messageMap = new HashMap<>();
                messageMap.put(NodeNames.MESSAGE_ID, pushId);
                messageMap.put(NodeNames.MESSAGE, msg);
                messageMap.put(NodeNames.MESSAGE_TYPE, msgType);
                messageMap.put(NodeNames.MESSAGE_FROM, currentUserId);
                messageMap.put(NodeNames.MESSAGE_TIME, ServerValue.TIMESTAMP);

                String currentUserRef = NodeNames.MESSAGES + "/" + currentUserId + "/" + chatUserId;
                String chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId;


                HashMap<String, Object> messageUserMap = new HashMap<>();
                messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

                mBinding.etMessage.setText("");

                mRootRef.updateChildren(messageUserMap, (error, ref) -> {
                    if (error != null) {
                        Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message, error.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                    {
                        //Toast.makeText(ChatActivity.this, R.string.message_sent_successfully, Toast.LENGTH_SHORT).show();
                        String title = "";
                        switch (msgType) {
                            case MESSAGE_TYPE_TEXT:
                                title = "New Message";
                                break;
                            case MESSAGE_TYPE_IMAGE:
                                title = "New Image";
                                break;
                            case MESSAGE_TYPE_VIDEO:
                                title = "New Video";
                                break;
                        }
                        sendNotification(ChatActivity.this, title, msg, chatUserId);

                        String lastMessage = !title.equals("New Message") ? title : msg;

                        updateChatDetails(ChatActivity.this, currentUserId, chatUserId, lastMessage);
                        updateChatDetails(ChatActivity.this, chatUserId, currentUserId, lastMessage);

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        messageModelList.clear();
        databaseReferenceMessages = mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId);

        Query messageQuery = databaseReferenceMessages.limitToLast(currentPage * RECORD_PER_PAGE);

        if (childEventListener != null) {
            messageQuery.removeEventListener(childEventListener);
        }
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel messageModel = snapshot.getValue(MessageModel.class);

                messageModelList.add(messageModel);
                messagesAdapter.notifyDataSetChanged();
                mBinding.rvMessages.scrollToPosition(messageModelList.size() - 1);
                mBinding.srlMessages.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                loadMessages();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mBinding.srlMessages.setRefreshing(false);
            }
        };

        messageQuery.addChildEventListener(childEventListener);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivSend:
                if (Util.connectionAvailable(this)) {
                    DatabaseReference userMessagePush = mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
                    String pushId = userMessagePush.getKey();
                    sendMessage(mBinding.etMessage.getText().toString().trim(), Constants.MESSAGE_TYPE_TEXT, pushId);
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ivAttachment:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (bottomSheetDialog != null)
                        bottomSheetDialog.show();


                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null)
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                break;

            case R.id.llCameraOption:
                bottomSheetDialog.dismiss();
                Intent intentCamera = new Intent(ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentCamera, REQUEST_CODE_CAPTURE_IMAGE);
                break;

            case R.id.llGalleryOption:
                bottomSheetDialog.dismiss();
                Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentGallery, REQUEST_CODE_PICK_IMAGE);
                break;

            case R.id.llVideoOption:
                bottomSheetDialog.dismiss();
                Intent intentVideo = new Intent(Intent.ACTION_PICK);
                intentVideo.setType("video/*");
                startActivityForResult(intentVideo, REQUEST_CODE_PICK_VIDEO);
                break;
            case R.id.ivClose:
                bottomSheetDialog.dismiss();
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (bottomSheetDialog != null)
                    bottomSheetDialog.show();
            } else {
                Toast.makeText(this, R.string.permission_file_access, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAPTURE_IMAGE) {     //Camera
                Bitmap bitmap;
                if (data != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    }
                    uploadByte(bytes, MESSAGE_TYPE_IMAGE);
                }

            } else if (requestCode == REQUEST_CODE_PICK_IMAGE) {      //Gallery
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }
                uploadFile(uri, MESSAGE_TYPE_IMAGE);
            } else if (requestCode == REQUEST_CODE_PICK_VIDEO) {     //Video
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }
                uploadFile(uri, MESSAGE_TYPE_VIDEO);
            } else if (requestCode == REQUEST_CODE_FORWARD) {

                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(USER_KEY, data.getStringExtra(USER_KEY));
                intent.putExtra(USER_NAME, data.getStringExtra(USER_NAME));
                intent.putExtra(PHOTO_NAME, data.getStringExtra(PHOTO_NAME));

                intent.putExtra(MESSAGE, data.getStringExtra(MESSAGE));
                intent.putExtra(MESSAGE_ID, data.getStringExtra(MESSAGE_ID));
                intent.putExtra(MESSAGE_TYPE, data.getStringExtra(MESSAGE_TYPE));
                startActivity(intent);
                finish();
            }
        }
    }

    private void uploadFile(Uri uri, String messageType) {

        DatabaseReference databaseReference = mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
        String pushId = databaseReference.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = messageType.equals(MESSAGE_TYPE_VIDEO) ? MESSAGE_VIDEOS : MESSAGE_IMAGES;
        String fileName = messageType.equals(MESSAGE_TYPE_VIDEO) ? pushId + ".mp4" : pushId + ".jpg";

        StorageReference fileRef = storageReference.child(folderName).child(fileName);
        UploadTask uploadTask = fileRef.putFile(uri);

        uploadProgress(uploadTask, fileRef, pushId, messageType);
    }

    private void uploadByte(ByteArrayOutputStream bytes, String messageType) {
        DatabaseReference databaseReference = mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
        String pushId = databaseReference.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = messageType.equals(MESSAGE_TYPE_VIDEO) ? MESSAGE_VIDEOS : MESSAGE_IMAGES;
        String fileName = messageType.equals(MESSAGE_TYPE_VIDEO) ? pushId + ".mp4" : pushId + ".jpg";

        StorageReference fileRef = storageReference.child(folderName).child(fileName);
        UploadTask uploadTask = fileRef.putBytes(bytes.toByteArray());

        uploadProgress(uploadTask, fileRef, pushId, messageType);
    }


    private void uploadProgress(UploadTask task, StorageReference filePath, String pushId, String messageType) {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.file_progress, null);
        ProgressBar pbProgress = view.findViewById(R.id.pbProgress);
        TextView tvFileProgress = view.findViewById(R.id.tvFileProgress);
        ImageView ivPause = view.findViewById(R.id.ivPause);
        ImageView ivPlay = view.findViewById(R.id.ivPlay);
        ImageView ivCancel = view.findViewById(R.id.ivCancel);

        ivPause.setOnClickListener(v -> {
            task.pause();
            ivPlay.setVisibility(View.VISIBLE);
            ivPause.setVisibility(View.GONE);
        });

        ivPlay.setOnClickListener(v -> {
            task.resume();
            ivPlay.setVisibility(View.GONE);
            ivPause.setVisibility(View.VISIBLE);
        });


        ivCancel.setOnClickListener(v -> task.cancel());

        mBinding.llProgress.addView(view);
        tvFileProgress.setText(getString(R.string.upload_progress, messageType, "0"));

        task.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

            pbProgress.setProgress((int) progress);
            tvFileProgress.setText(getString(R.string.upload_progress, messageType, String.valueOf(pbProgress.getProgress())));

        });

        task.addOnCompleteListener(task1 -> {
            mBinding.llProgress.removeView(view);
            if (task1.isSuccessful()) {
                filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    sendMessage(downloadUrl, messageType, pushId);
                });
            }
        });

        task.addOnFailureListener(e -> {
            mBinding.llProgress.removeView(view);
            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_upload, e.getMessage()), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteMessage(String messageId, String messageType) {
        DatabaseReference databaseReference = mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).child(messageId);
        databaseReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DatabaseReference databaseReferenceChatUser = mRootRef.child(NodeNames.MESSAGES).child(chatUserId).child(currentUserId).child(messageId);
                databaseReferenceChatUser.removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, R.string.message_deleted_succesfully, Toast.LENGTH_SHORT).show();
                        if (!messageType.equalsIgnoreCase(MESSAGE_TYPE_TEXT)) {
                            StorageReference rootRef = FirebaseStorage.getInstance().getReference();
                            String folder = messageType.equalsIgnoreCase(MESSAGE_TYPE_VIDEO) ? MESSAGE_VIDEOS : MESSAGE_IMAGES;
                            String file = messageType.equalsIgnoreCase(MESSAGE_TYPE_VIDEO) ? messageId + ".mp4" : messageId + ".jpg";

                            StorageReference fileRef = rootRef.child(folder).child(file);
                            fileRef.delete().addOnCompleteListener(task2 -> {
                                if (!task2.isSuccessful()) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed_to_delete_file, task2.getException()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(ChatActivity.this, getString(R.string.failed_to_delete_message, task1.getException()), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ChatActivity.this, getString(R.string.failed_to_delete_message, task.getException()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void downloadFile(String messageId, String messageType, boolean isShare) {
        if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            String folderName = messageType.equals(MESSAGE_TYPE_VIDEO) ? MESSAGE_VIDEOS : MESSAGE_IMAGES;
            String fileName = messageType.equals(MESSAGE_TYPE_VIDEO) ? messageId + ".mp4" : messageId + ".jpg";

            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(folderName).child(fileName);
            String localFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName;

            File localFile = new File(localFilePath);
            try {
                if (localFile.exists() || localFile.createNewFile()) {
                    FileDownloadTask downloadTask = fileRef.getFile(localFile);

                    @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.file_progress, null);
                    ProgressBar pbProgress = view.findViewById(R.id.pbProgress);
                    TextView tvFileProgress = view.findViewById(R.id.tvFileProgress);
                    ImageView ivPause = view.findViewById(R.id.ivPause);
                    ImageView ivPlay = view.findViewById(R.id.ivPlay);
                    ImageView ivCancel = view.findViewById(R.id.ivCancel);

                    ivPause.setOnClickListener(v -> {
                        downloadTask.pause();
                        ivPlay.setVisibility(View.VISIBLE);
                        ivPause.setVisibility(View.GONE);
                    });

                    ivPlay.setOnClickListener(v -> {
                        downloadTask.resume();
                        ivPlay.setVisibility(View.GONE);
                        ivPause.setVisibility(View.VISIBLE);
                    });


                    ivCancel.setOnClickListener(v -> downloadTask.cancel());

                    mBinding.llProgress.addView(view);
                    tvFileProgress.setText(getString(R.string.download_progress, messageType, "0"));

                    downloadTask.addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        pbProgress.setProgress((int) progress);
                        tvFileProgress.setText(ChatActivity.this.getString(R.string.download_progress, messageType, String.valueOf(pbProgress.getProgress())));

                    });

                    downloadTask.addOnCompleteListener(task1 -> {
                        mBinding.llProgress.removeView(view);
                        if (task1.isSuccessful()) {
                            if (isShare) {
                                Intent intentShare = new Intent();
                                intentShare.setAction(Intent.ACTION_SEND);
                                intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse(localFilePath));
                                if (messageType.equalsIgnoreCase(MESSAGE_TYPE_VIDEO)) {
                                    intentShare.setType("video/mp4");
                                }
                                if (messageType.equalsIgnoreCase(MESSAGE_TYPE_IMAGE)) {
                                    intentShare.setType("image/jpg");
                                }
                                startActivity(Intent.createChooser(intentShare, getString(R.string.share_with)));
                            } else {
                                Snackbar snackbar = Snackbar.make(mBinding.llProgress, getString(R.string.file_downloaded_successfully), Snackbar.LENGTH_INDEFINITE);
                                snackbar.setAction(R.string.view, view1 -> {
                                    Uri uri = Uri.parse(localFilePath);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    if (messageType.equalsIgnoreCase(MESSAGE_TYPE_VIDEO))
                                        intent.setDataAndType(uri, "video/mp4");
                                    else if (messageType.equalsIgnoreCase(MESSAGE_TYPE_IMAGE))
                                        intent.setDataAndType(uri, "image/jpg");

                                    startActivity(intent);
                                });
                                snackbar.show();
                            }

                        }
                    });

                    downloadTask.addOnFailureListener(e -> {
                        mBinding.llProgress.removeView(view);
                        Toast.makeText(ChatActivity.this, getString(R.string.failed_to_download, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });

                } else {
                    Toast.makeText(this, R.string.failed_to_store_file, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ChatActivity.this, getString(R.string.failed_to_download, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void forwardMessage(String selectedMessageId, String selectedMessage, String selectedMessageType) {
        Intent intent = new Intent(this, SelectFriendActivity.class);
        intent.putExtra(MESSAGE, selectedMessage);
        intent.putExtra(MESSAGE_ID, selectedMessageId);
        intent.putExtra(MESSAGE_TYPE, selectedMessageType);
        startActivityForResult(intent, REQUEST_CODE_FORWARD);
    }

    @Override
    public void onBackPressed() {
        mRootRef.child(CHATS).child(currentUserId).child(chatUserId).child(UNREAD_COUNT).setValue(0);
        super.onBackPressed();
    }
}