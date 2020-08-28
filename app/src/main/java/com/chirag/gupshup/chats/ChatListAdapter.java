package com.chirag.gupshup.chats;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.ChatListLayoutBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.chirag.gupshup.common.Constants.IMAGES_FOLDER;
import static com.chirag.gupshup.common.Extras.PHOTO_NAME;
import static com.chirag.gupshup.common.Extras.USER_KEY;
import static com.chirag.gupshup.common.Extras.USER_NAME;
import static com.chirag.gupshup.common.Util.getTimeAgo;

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
        ChatListLayoutBinding binding = holder.binding;
        binding.tvFullName.setText(chatListModel.getUserName());

        String lastMessage = chatListModel.getLastMessage();

        lastMessage = lastMessage.length() > 30 ? lastMessage.substring(0, 30) + "..." : lastMessage;
        binding.tvLastMessage.setText(String.format("%s", lastMessage));

        String lastMessageTime = chatListModel.getLastMessageTime();
        Log.e("lastMessageTime1: ", "==>" + lastMessageTime);
        if (lastMessageTime == null) lastMessageTime = "";
        Log.e("lastMessageTime2: ", "==>" + lastMessageTime);
        if (!TextUtils.isEmpty(lastMessageTime)) {
            binding.tvLastMessageTime.setText(getTimeAgo(Long.parseLong(lastMessageTime)));
        }

        if (!chatListModel.getUnreadCount().equalsIgnoreCase("0")) {
            binding.tvUnreadCount.setVisibility(View.VISIBLE);
            binding.tvUnreadCount.setText(chatListModel.getUnreadCount());
        } else {
            binding.tvUnreadCount.setVisibility(View.GONE);
        }


        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER + "/" + chatListModel.getUserId() + ".jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(binding.ivProfile));

        binding.llChatList.setOnClickListener(v -> context.startActivity(new Intent(context, ChatActivity.class)
                .putExtra(USER_KEY, chatListModel.getUserId())
                .putExtra(USER_NAME, chatListModel.getUserName())
                .putExtra(PHOTO_NAME, chatListModel.getPhotoName())));
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
