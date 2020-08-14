package com.chirag.gupshup.findFriends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chirag.gupshup.R;
import com.chirag.gupshup.common.Constants;
import com.chirag.gupshup.common.NodeNames;
import com.chirag.gupshup.databinding.FindFriendsLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.chirag.gupshup.common.Constants.IMAGES_FOLDER;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.ViewHolder> {

    Context context;
    List<FindFriendModel> findFriendModelList;

    private DatabaseReference friendRequestReference;
    private FirebaseUser currentUser;
    private String userId;

    public FindFriendAdapter(Context context, List<FindFriendModel> findFriendModelList) {
        this.context = context;
        this.findFriendModelList = findFriendModelList;
    }


    @NonNull
    @Override
    public FindFriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FindFriendsLayoutBinding mBinder = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.find_friends_layout, parent, false);
        return new ViewHolder(mBinder);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendAdapter.ViewHolder holder, int position) {
        FindFriendModel friendModel = findFriendModelList.get(position);
        FindFriendsLayoutBinding binding = ((ViewHolder) holder).binding;
        binding.tvFullName.setText(friendModel.getUserName());

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER + "/" + friendModel.getPhotoName());
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(binding.ivProfile));

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (friendModel.isRequestSent()) {
            binding.btnSendRequest.setVisibility(View.GONE);
            binding.btnCancelRequest.setVisibility(View.VISIBLE);
        } else {
            binding.btnSendRequest.setVisibility(View.VISIBLE);
            binding.btnCancelRequest.setVisibility(View.GONE);
        }

        binding.btnSendRequest.setOnClickListener(v -> {
            binding.btnSendRequest.setVisibility(View.GONE);
            binding.pbRequest.setVisibility(View.VISIBLE);

            userId = friendModel.getUserId();

            friendRequestReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE)
                    .setValue(Constants.REQUEST_STATUS_SENT).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    friendRequestReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                            .setValue(Constants.REQUEST_STATUS_RECEIVED).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(context, R.string.request_sent_successfully, Toast.LENGTH_SHORT).show();

                            binding.btnSendRequest.setVisibility(View.GONE);
                            binding.pbRequest.setVisibility(View.GONE);
                            binding.btnCancelRequest.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(context, context.getString(R.string.failed_to_send_friend_request, task1.getException()), Toast.LENGTH_SHORT).show();
                            binding.btnSendRequest.setVisibility(View.VISIBLE);
                            binding.pbRequest.setVisibility(View.GONE);
                            binding.btnCancelRequest.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_to_send_friend_request, task.getException()), Toast.LENGTH_SHORT).show();
                    binding.btnSendRequest.setVisibility(View.VISIBLE);
                    binding.pbRequest.setVisibility(View.GONE);
                    binding.btnCancelRequest.setVisibility(View.GONE);
                }
            });
        });

        binding.btnCancelRequest.setOnClickListener(v1 -> {
            binding.btnCancelRequest.setVisibility(View.GONE);
            binding.pbRequest.setVisibility(View.VISIBLE);

            userId = friendModel.getUserId();

            friendRequestReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE)
                    .removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    friendRequestReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                            .removeValue().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(context, R.string.request_cancelled_successfully, Toast.LENGTH_SHORT).show();

                            binding.btnSendRequest.setVisibility(View.VISIBLE);
                            binding.pbRequest.setVisibility(View.GONE);
                            binding.btnCancelRequest.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(context, context.getString(R.string.failed_to_cancel_friend_request, task1.getException()), Toast.LENGTH_SHORT).show();
                            binding.btnSendRequest.setVisibility(View.GONE);
                            binding.pbRequest.setVisibility(View.GONE);
                            binding.btnCancelRequest.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_to_cancel_friend_request, task.getException()), Toast.LENGTH_SHORT).show();
                    binding.btnSendRequest.setVisibility(View.GONE);
                    binding.pbRequest.setVisibility(View.GONE);
                    binding.btnCancelRequest.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return findFriendModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FindFriendsLayoutBinding binding;

        ViewHolder(FindFriendsLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
