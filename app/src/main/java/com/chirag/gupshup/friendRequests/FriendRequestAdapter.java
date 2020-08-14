package com.chirag.gupshup.friendRequests;

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
import com.chirag.gupshup.databinding.FriendRequestLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.chirag.gupshup.common.Constants.IMAGES_FOLDER;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private Context context;

    DatabaseReference databaseReferenceFriendRequest, databaseReferenceChats;
    FirebaseUser currentUser;
    private List<FriendRequestModel> requestModelList;

    public FriendRequestAdapter(Context context, List<FriendRequestModel> requestModelList) {
        this.context = context;
        this.requestModelList = requestModelList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FriendRequestLayoutBinding mBinder = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.friend_request_layout, parent, false);
        return new ViewHolder(mBinder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequestModel requestModel = requestModelList.get(position);
        FriendRequestLayoutBinding binding = ((ViewHolder) holder).binding;
        binding.tvFullName.setText(requestModel.getUserName());

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER + "/" + requestModel.getUserId());

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(binding.ivProfile));


        databaseReferenceFriendRequest = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        binding.btnDenyRequest.setOnClickListener(v -> {
            binding.pbDecision.setVisibility(View.VISIBLE);
            binding.btnDenyRequest.setVisibility(View.GONE);
            binding.btnAcceptRequest.setVisibility(View.GONE);

            databaseReferenceFriendRequest.child(currentUser.getUid()).child(requestModel.getUserId())
                    .child(NodeNames.REQUEST_TYPE).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        databaseReferenceFriendRequest.child(requestModel.getUserId()).child(currentUser.getUid())
                                .child(NodeNames.REQUEST_TYPE).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    binding.pbDecision.setVisibility(View.GONE);
                                    binding.btnDenyRequest.setVisibility(View.VISIBLE);
                                    binding.btnAcceptRequest.setVisibility(View.VISIBLE);
                                    Toast.makeText(context, R.string.request_denied_successfully, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, context.getString(R.string.failed_to_deny_request, task.getException()), Toast.LENGTH_SHORT).show();
                                    binding.pbDecision.setVisibility(View.GONE);
                                    binding.btnDenyRequest.setVisibility(View.VISIBLE);
                                    binding.btnAcceptRequest.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(context, context.getString(R.string.failed_to_deny_request, task.getException()), Toast.LENGTH_SHORT).show();
                        binding.pbDecision.setVisibility(View.GONE);
                        binding.btnDenyRequest.setVisibility(View.VISIBLE);
                        binding.btnAcceptRequest.setVisibility(View.VISIBLE);
                    }
                }
            });

        });

        binding.btnAcceptRequest.setOnClickListener(v -> {
            binding.pbDecision.setVisibility(View.VISIBLE);
            binding.btnDenyRequest.setVisibility(View.GONE);
            binding.btnAcceptRequest.setVisibility(View.GONE);

            databaseReferenceChats.child(currentUser.getUid()).child(requestModel.getUserId())
                    .child(NodeNames.TIME_STAMP).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    databaseReferenceChats.child(requestModel.getUserId()).child(currentUser.getUid())
                            .child(NodeNames.TIME_STAMP).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            databaseReferenceFriendRequest.child(currentUser.getUid()).child(requestModel.getUserId()).child(NodeNames.REQUEST_TYPE)
                                    .setValue(Constants.REQUEST_STATUS_ACCEPTED).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    databaseReferenceFriendRequest.child(requestModel.getUserId()).child(currentUser.getUid())
                                            .child(NodeNames.REQUEST_TYPE).setValue(Constants.REQUEST_STATUS_ACCEPTED).addOnCompleteListener(task3 -> {
                                        if (task3.isSuccessful()) {
                                            binding.pbDecision.setVisibility(View.GONE);
                                            binding.btnDenyRequest.setVisibility(View.VISIBLE);
                                            binding.btnAcceptRequest.setVisibility(View.VISIBLE);
                                            Toast.makeText(context, R.string.request_accepted_successfully, Toast.LENGTH_SHORT).show();
                                        } else {
                                            handleException(binding, task3.getException());
                                        }
                                    });
                                } else {
                                    handleException(binding, task1.getException());
                                }
                            });

                        } else {
                            handleException(binding, task1.getException());
                        }
                    });
                } else {
                    handleException(binding, task.getException());

                }
            });


        });

    }

    private void handleException(FriendRequestLayoutBinding binding, Exception exception) {
        binding.pbDecision.setVisibility(View.GONE);
        binding.btnDenyRequest.setVisibility(View.VISIBLE);
        binding.btnAcceptRequest.setVisibility(View.VISIBLE);
        Toast.makeText(context, context.getString(R.string.failed_to_accept_request, exception), Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return requestModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FriendRequestLayoutBinding binding;

        ViewHolder(FriendRequestLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
