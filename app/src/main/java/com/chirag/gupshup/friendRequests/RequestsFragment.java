package com.chirag.gupshup.friendRequests;

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
import com.chirag.gupshup.databinding.FragmentRequestsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends Fragment {

    FragmentRequestsBinding mBinding;

    FriendRequestAdapter friendRequestAdapter;
    private List<FriendRequestModel> friendRequestModelList;

    private DatabaseReference databaseReferenceRequests, databaseReferenceUsers;
    private FirebaseUser currentUser;

    private View progressBar;


    public RequestsFragment() {
        // Required empty public constructor
    }

    public static RequestsFragment newInstance(String param1, String param2) {
        return new RequestsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_requests, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);

        friendRequestModelList = new ArrayList<>();

        friendRequestAdapter = new FriendRequestAdapter(getContext(), friendRequestModelList);
        mBinding.rvRequests.setAdapter(friendRequestAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

        databaseReferenceRequests = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS).child(currentUser.getUid());

        mBinding.tvEmptyRequests.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        databaseReferenceRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                friendRequestModelList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        String requestType = ds.child(NodeNames.REQUEST_TYPE).getValue().toString();
                        if (requestType.equals(Constants.REQUEST_STATUS_RECEIVED)) {
                            String userId = ds.getKey();

                            databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String userName = snapshot.child(NodeNames.NAME).getValue().toString();
                                    String photoName = "";

                                    if (snapshot.child(NodeNames.PHOTO_URL).getValue() != null) {
                                        photoName = snapshot.child(NodeNames.PHOTO_URL).getValue().toString();
                                    }

                                    FriendRequestModel requestModel = new FriendRequestModel(userId, userName, photoName);
                                    friendRequestModelList.add(requestModel);

                                    friendRequestAdapter.notifyDataSetChanged();
                                    mBinding.tvEmptyRequests.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), getString(R.string.failed_to_fetch_friend_requests, error.getMessage()), Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    } else {
                        mBinding.tvEmptyRequests.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.failed_to_fetch_friend_requests, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

    }
}