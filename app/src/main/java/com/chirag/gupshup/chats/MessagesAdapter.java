package com.chirag.gupshup.chats;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chirag.gupshup.R;
import com.chirag.gupshup.databinding.MessageLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.chirag.gupshup.common.Constants.MESSAGE_TYPE_IMAGE;
import static com.chirag.gupshup.common.Constants.MESSAGE_TYPE_TEXT;
import static com.chirag.gupshup.common.Constants.MESSAGE_TYPE_VIDEO;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private Context context;
    private List<MessageModel> messageModelList;
    private FirebaseAuth firebaseAuth;

    private ActionMode actionMode;

    private ConstraintLayout selectedView;


    public MessagesAdapter(Context context, List<MessageModel> messageModelList) {
        this.context = context;
        this.messageModelList = messageModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageLayoutBinding mBinder = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.message_layout, parent, false);
        return new ViewHolder(mBinder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel messageModel = messageModelList.get(position);
        MessageLayoutBinding binding = holder.binding;
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String fromUserId = messageModel.getMessageFrom();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
        String dateTime = sdf.format(new Date(messageModel.getMessageTime()));
        String[] splitString = dateTime.split(" ");
        String messageTime = splitString[1];

        if (fromUserId.equals(currentUserId)) {
            if (messageModel.getMessageType().equals(MESSAGE_TYPE_TEXT)) {
                binding.llSent.setVisibility(View.VISIBLE);
                binding.llSentImage.setVisibility(View.GONE);
            } else {
                binding.llSent.setVisibility(View.GONE);
                binding.llSentImage.setVisibility(View.VISIBLE);
            }
            binding.llReceived.setVisibility(View.GONE);
            binding.llReceivedImage.setVisibility(View.GONE);

            binding.tvSentMessage.setText(messageModel.getMessage());
            binding.tvSentMessageTime.setText(messageTime);
            binding.tvSentImageTime.setText(messageTime);
            Glide.with(context)
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(binding.ivSentImage);
        } else {
            if (messageModel.getMessageType().equals(MESSAGE_TYPE_TEXT)) {
                binding.llReceived.setVisibility(View.VISIBLE);
                binding.llReceivedImage.setVisibility(View.GONE);
            } else {
                binding.llReceived.setVisibility(View.GONE);
                binding.llReceivedImage.setVisibility(View.VISIBLE);
            }
            binding.llSent.setVisibility(View.GONE);
            binding.llSentImage.setVisibility(View.GONE);
            binding.tvReceivedMessage.setText(messageModel.getMessage());
            binding.tvReceivedMessageTime.setText(messageTime);
            binding.tvReceivedImageTime.setText(messageTime);
            Glide.with(context)
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(binding.ivReceivedImage);
        }

        binding.clMessage.setTag(R.id.TAG_MESSAGE, messageModel.getMessage());
        binding.clMessage.setTag(R.id.TAG_MESSAGE_ID, messageModel.getMessageId());
        binding.clMessage.setTag(R.id.TAG_MESSAGE_TYPE, messageModel.getMessageType());

        binding.clMessage.setOnClickListener(view -> {
            String messageType = view.getTag(R.id.TAG_MESSAGE_TYPE).toString();
            Uri uri = Uri.parse(view.getTag(R.id.TAG_MESSAGE).toString());
            if (messageType.equals(MESSAGE_TYPE_VIDEO)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setDataAndType(uri, "video/mp4");
                context.startActivity(intent);
            } else if (messageType.equals(MESSAGE_TYPE_IMAGE)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setDataAndType(uri, "image/jpg");
                context.startActivity(intent);
            }
        });

        binding.clMessage.setOnLongClickListener(v -> {
            if (actionMode != null)
                return false;

            selectedView = binding.clMessage;

            actionMode = ((AppCompatActivity) context).startSupportActionMode(actionModeCallBack);
            binding.clMessage.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));

            return true;
        });

    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MessageLayoutBinding binding;

        ViewHolder(MessageLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public ActionMode.Callback actionModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_chat_options, menu);

            String selectedMessageType = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_TYPE));
            if (selectedMessageType.equalsIgnoreCase(MESSAGE_TYPE_TEXT)) {
                MenuItem itemDownload = menu.findItem(R.id.mnuDownload);
                itemDownload.setVisible(false);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            String selectedMessageId = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_ID));
            String selectedMessage = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE));
            String selectedMessageType = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_TYPE));

            switch (item.getItemId()) {
                case R.id.mnuDelete:
                    if (context instanceof ChatActivity) {
                        ((ChatActivity) context).deleteMessage(selectedMessageId, selectedMessageType);
                    }
                    mode.finish();
                    break;
                case R.id.mnuDownload:
                    if (context instanceof ChatActivity) {
                        ((ChatActivity) context).downloadFile(selectedMessageId, selectedMessageType, false);
                    }
                    mode.finish();
                    break;
                case R.id.mnuForward:
                    if (context instanceof ChatActivity) {
                        ((ChatActivity) context).forwardMessage(selectedMessageId, selectedMessage, selectedMessageType);
                    }
                    mode.finish();
                    break;
                case R.id.mnuShare:
                    if (selectedMessageType.equalsIgnoreCase(MESSAGE_TYPE_TEXT)) {
                        Intent intentShare = new Intent();
                        intentShare.setAction(Intent.ACTION_SEND);
                        intentShare.putExtra(Intent.EXTRA_TEXT, selectedMessage);
                        intentShare.setType("text/plain");
                        context.startActivity(intentShare);
                    } else {
                        if (context instanceof ChatActivity) {
                            ((ChatActivity) context).downloadFile(selectedMessageId, selectedMessageType, true);
                        }
                    }

                    mode.finish();
                    break;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedView.setBackgroundColor(context.getResources().getColor(R.color.chat_background));
        }
    };
}
