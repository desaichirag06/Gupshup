package com.chirag.gupshup.selectFriend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.SelectFriendLayoutBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.chirag.gupshup.common.Constants.IMAGES_FOLDER;

public class SelectFriendAdapter extends RecyclerView.Adapter<SelectFriendAdapter.ViewHolder> {

    Context context;
    private List<SelectFriendModel> selectFriendList;

    public SelectFriendAdapter(Context context, List<SelectFriendModel> selectFriendList) {
        this.context = context;
        this.selectFriendList = selectFriendList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SelectFriendLayoutBinding mBinder = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.select_friend_layout, parent, false);
        return new ViewHolder(mBinder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SelectFriendModel selectFriendModel = selectFriendList.get(position);
        SelectFriendLayoutBinding binding = holder.binding;

        binding.tvFullName.setText(selectFriendModel.getUserName());

        StorageReference photoRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER + "/" + selectFriendModel.getPhotoName());

        photoRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(binding.ivProfile));

        binding.llSelectFriend.setOnClickListener(v -> {
            if (context instanceof SelectFriendActivity) {
                ((SelectFriendActivity) context).returnSelectedFriend(selectFriendModel.getUserId(), selectFriendModel.getUserName(),
                        selectFriendModel.getUserId() + ".jpg");
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectFriendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SelectFriendLayoutBinding binding;

        ViewHolder(SelectFriendLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
