package com.example.chatopia.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chats";
    public static final String KEY_SENDER_ID = "sender_id";
    public static final String KEY_RECIVER_ID = "reciver_id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIME = "time";
    public static final String KEY_COLLECTIONS_CONVERSATIONS  = "conversations";
    public static final String KEY_RECEIVER_NAME  = "receiver_name";
    public static final String KEY_SENDER_NAME  = "sender_name";
    public static final String KEY_RECEIVER_IMAGE  = "receiver_image";
    public static final String KEY_SENDER_IMAGE  = "sender_image";
    public static final String KEY_AVAILABILITY  = "availability";
    public static final String KEY_LAST_MESSAGE  = "last_message";
    public static final String REMOTE_MSG_AUTHORIZATION  = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE  = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static HashMap<String , String> remoteMsgHeader = null;
    public static HashMap<String ,String> getRemoteMsgHeader(){
        if(remoteMsgHeader == null){
            remoteMsgHeader = new HashMap<>();
            remoteMsgHeader.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAOqXUJFM:APA91bF3wua-g9lXeCua-kbVCQebQ5Lw0ASZfbYJL1_VzqdkFIylFAUn71u9xW0gb7186GxLKcyxT_3xZnjDb_1CbYKQiIJoqrqnPwWnPWOR4NZSIqaaOLRrzOPiIUSRIb_nyaa-RLuh"
            );
            remoteMsgHeader.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeader;
    }


}
