package com.chirag.gupshup.findFriends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.chirag.gupshup.R;
import com.chirag.gupshup.common.Constants;
import com.chirag.gupshup.common.NodeNames;
import com.chirag.gupshup.databinding.FragmentFindFriendsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindFriendsFragment extends Fragment {

    FragmentFindFriendsBinding mBinding;

    FindFriendAdapter findFriendAdapter;
    private List<FindFriendModel> findFriendModelList;
    private DatabaseReference databaseReferenceFriendRequests;
    FirebaseUser currentUser;
    private View progressBar;

    public FindFriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_friends, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.FRIEND_REQUESTS).child(currentUser.getUid());

        findFriendModelList = new ArrayList<>();
        findFriendAdapter = new FindFriendAdapter(getContext(), findFriendModelList);
        mBinding.rvFindFriends.setAdapter(findFriendAdapter);

        mBinding.tvEmptyFriendsList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Query query = databaseReference.orderByChild(NodeNames.NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                findFriendModelList.clear();


                for (DataSnapshot ds : snapshot.getChildren()) {
                    String userID = ds.getKey();
                    if (userID != null && !userID.equals(currentUser.getUid()) && ds.child(NodeNames.NAME).getValue() != null) {
                        String fullName = ds.child(NodeNames.NAME).getValue().toString();

                        String photoName = "";

                        if (snapshot.child(NodeNames.PHOTO_URL).getValue() != null) {
                            photoName = snapshot.child(NodeNames.PHOTO_URL).getValue().toString();
                        }

                        String finalPhotoName = photoName;
                        databaseReferenceFriendRequests.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String requestType = snapshot.child(NodeNames.REQUEST_TYPE).getValue().toString();
                                    if (requestType.equals(Constants.REQUEST_STATUS_SENT)) {
                                        findFriendModelList.add(new FindFriendModel(fullName, finalPhotoName, userID, true));
                                        findFriendAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    findFriendModelList.add(new FindFriendModel(fullName, finalPhotoName, userID, false));
                                    findFriendAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });


                        mBinding.tvEmptyFriendsList.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                mBinding.tvEmptyFriendsList.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.failed_to_fetch_friends, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}