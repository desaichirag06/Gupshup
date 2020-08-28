package com.chirag.gupshup.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.chirag.gupshup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.chirag.gupshup.common.Constants.FIREBASE_KEY;
import static com.chirag.gupshup.common.Constants.NOTIFICATION_DATA;
import static com.chirag.gupshup.common.Constants.NOTIFICATION_MESSAGE;
import static com.chirag.gupshup.common.Constants.NOTIFICATION_TITLE;
import static com.chirag.gupshup.common.Constants.NOTIFICATION_TO;
import static com.chirag.gupshup.common.Constants.SENDER_ID;
import static com.chirag.gupshup.common.NodeNames.CHATS;
import static com.chirag.gupshup.common.NodeNames.DEVICE_TOKEN;
import static com.chirag.gupshup.common.NodeNames.LAST_MESSAGE;
import static com.chirag.gupshup.common.NodeNames.LAST_MESSAGE_TIME;
import static com.chirag.gupshup.common.NodeNames.TIME_STAMP;
import static com.chirag.gupshup.common.NodeNames.UNREAD_COUNT;

/**
 * Created by Chirag Desai on 25-07-2020.
 */
public class Util {
    public static boolean connectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null) {
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        } else {
            return false;
        }
    }

    public static void updateDeviceToken(Context context, String token) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference rootRefChild = rootRef.child(NodeNames.TOKENS).child(currentUser.getUid());

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(DEVICE_TOKEN, token);

            rootRefChild.setValue(hashMap).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(context, context.getString(R.string.failed_to_store_token, task.getException()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void sendNotification(Context context, String title, String message, String userId) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = rootRef.child(NodeNames.TOKENS).child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(DEVICE_TOKEN).getValue() != null) {
                    String deviceToken = snapshot.child(DEVICE_TOKEN).getValue().toString();


                    JSONObject notification = new JSONObject();
                    JSONObject notificationData = new JSONObject();

                    try {
                        notificationData.put(NOTIFICATION_TITLE, title);
                        notificationData.put(NOTIFICATION_MESSAGE, message);

                        notification.put(NOTIFICATION_TO, deviceToken);
                        notification.put(NOTIFICATION_DATA, notificationData);

                        String fcmApiUrl = "https://fcm.googleapis.com/fcm/send";
                        String contentType = "application/json";

                        Response.Listener<JSONObject> successListener = response -> Toast.makeText(context, "Notification sent", Toast.LENGTH_SHORT).show();

                        Response.ErrorListener errorListener = response -> Toast.makeText(context, context.getString(R.string.failed_to_send_notification, response.getMessage()), Toast.LENGTH_SHORT).show();

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(fcmApiUrl, notification,
                                successListener, errorListener) {
                            @Override
                            public Map<String, String> getHeaders() {

                                Map<String, String> params = new HashMap<>();

                                params.put("Authorization", "key=" + FIREBASE_KEY);
                                params.put("Sender", "id=" + SENDER_ID);
                                params.put("Content-Type", contentType);

                                return params;
                            }
                        };

                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(jsonObjectRequest);


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, context.getString(R.string.failed_to_send_notification, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, context.getString(R.string.failed_to_send_notification, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static void updateChatDetails(Context context, String currentUserId, String chatUserId, String lastMessage) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatRef = rootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUserId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentCount = "0";
                if (snapshot.child(NodeNames.UNREAD_COUNT).getValue() != null) {
                    currentCount = String.valueOf(snapshot.child(UNREAD_COUNT).getValue());

                    Map<String, Object> chatMap = new HashMap<>();
                    chatMap.put(TIME_STAMP, ServerValue.TIMESTAMP);
                    chatMap.put(UNREAD_COUNT, Integer.parseInt(currentCount) + 1);
                    chatMap.put(LAST_MESSAGE, lastMessage);
                    chatMap.put(LAST_MESSAGE_TIME, ServerValue.TIMESTAMP);

                    Map<String, Object> chatUserMap = new HashMap<>();
                    chatUserMap.put(CHATS + "/" + chatUserId + "/" + currentUserId, chatMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(context, context.getString(R.string.something_went_wrong, error.getMessage()), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, context.getString(R.string.something_went_wrong, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getTimeAgo(long time) {

        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 24 * HOUR_MILLIS;

        //time *= 1000;

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "";
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Just Now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 59 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " min ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hr ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }

    }
}
