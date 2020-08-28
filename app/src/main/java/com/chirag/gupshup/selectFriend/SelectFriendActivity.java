package com.chirag.gupshup.selectFriend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chirag.gupshup.R;
import com.chirag.gupshup.common.NodeNames;
import com.chirag.gupshup.databinding.ActivitySelectFriendBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.chirag.gupshup.common.Extras.MESSAGE;
import static com.chirag.gupshup.common.Extras.MESSAGE_ID;
import static com.chirag.gupshup.common.Extras.MESSAGE_TYPE;
import static com.chirag.gupshup.common.Extras.PHOTO_NAME;
import static com.chirag.gupshup.common.Extras.USER_KEY;
import static com.chirag.gupshup.common.Extras.USER_NAME;

public class SelectFriendActivity extends AppCompatActivity {

    ActivitySelectFriendBinding mBinding;
    private SelectFriendAdapter mSelectFriendAdapter;
    private List<SelectFriendModel> mSelectFriendsList;
    private View progressBar;
    private DatabaseReference databaseReferenceUsers, databaseReferenceChats;

    private FirebaseUser currentUser;
    private ValueEventListener valueEventListener;

    private String selectedMessage, selectedMessageId, selectedMessageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_friend);

        if (getIntent().hasExtra(MESSAGE)) {
            selectedMessage = getIntent().getStringExtra(MESSAGE);
        }
        if (getIntent().hasExtra(MESSAGE_ID)) {
            selectedMessageId = getIntent().getStringExtra(MESSAGE_ID);
        }
        if (getIntent().hasExtra(MESSAGE_TYPE)) {
            selectedMessageType = getIntent().getStringExtra(MESSAGE_TYPE);
        }


        progressBar = findViewById(R.id.progressBar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mBinding.rvSelectFriend.setLayoutManager(linearLayoutManager);

        mSelectFriendsList = new ArrayList<>();
        mSelectFriendAdapter = new SelectFriendAdapter(this, mSelectFriendsList);
        mBinding.rvSelectFriend.setAdapter(mSelectFriendAdapter);

        progressBar.setVisibility(View.VISIBLE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS).child(currentUser.getUid());
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userId = ds.getKey();
                    databaseReferenceUsers.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userName = snapshot.child(NodeNames.NAME).getValue() != null ? snapshot.child(NodeNames.NAME).getValue().toString() : "";

                            SelectFriendModel selectFriendModel = new SelectFriendModel(userId, userName, userId + ".jpg");
                            mSelectFriendsList.add(selectFriendModel);
                            mSelectFriendAdapter.notifyDataSetChanged();

                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SelectFriendActivity.this, getString(R.string.failed_to_fetch_friends_list, error.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectFriendActivity.this, getString(R.string.failed_to_fetch_friends_list, error.getMessage()), Toast.LENGTH_SHORT).show();

            }
        };

        databaseReferenceChats.addValueEventListener(valueEventListener);
    }

    public void returnSelectedFriend(String userId, String userName, String photoName) {
        databaseReferenceChats.removeEventListener(valueEventListener);
        Intent intent = new Intent();

        intent.putExtra(USER_KEY, userId);
        intent.putExtra(USER_NAME, userName);
        intent.putExtra(PHOTO_NAME, photoName);

        intent.putExtra(MESSAGE, selectedMessage);
        intent.putExtra(MESSAGE_ID, selectedMessageId);
        intent.putExtra(MESSAGE_TYPE, selectedMessageType);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}