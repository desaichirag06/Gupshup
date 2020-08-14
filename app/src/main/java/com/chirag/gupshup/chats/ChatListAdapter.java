package com.chirag.gupshup.chats;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chirag.gupshup.ChatActivity;
import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.ChatListLayoutBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.chirag.gupshup.common.Constants.IMAGES_FOLDER;
import static com.chirag.gupshup.common.Extras.USER_KEY;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    Context context;
    private List<ChatListModel> chatListModelList;

    public ChatListAdapter(Context context, List<ChatListModel> chatListModelList) {
        this.context = context;
        this.chatListModelList = chatListModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatListLayoutBinding mBinder = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.chat_list_layout, parent, false);
        return new ViewHolder(mBinder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatListModel chatListModel = chatListModelList.get(position);
        ChatListLayoutBinding binding = ((ViewHolder) holder).binding;
        binding.tvFullName.setText(chatListModel.getUserName());


        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER + "/" + chatListModel.getUserId());

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(binding.ivProfile));

        binding.llChatList.setOnClickListener(v -> context.startActivity(new Intent(context, ChatActivity.class)
                .putExtra(USER_KEY, chatListModel.getUserId())));
    }

    @Override
    public int getItemCount() {
        return chatListModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ChatListLayoutBinding binding;

        ViewHolder(ChatListLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
