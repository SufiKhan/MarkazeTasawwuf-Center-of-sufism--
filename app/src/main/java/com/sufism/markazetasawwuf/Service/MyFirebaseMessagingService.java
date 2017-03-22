package com.sufism.markazetasawwuf.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sufism.markazetasawwuf.Constants;
import com.sufism.markazetasawwuf.MainActivity;
import com.sufism.markazetasawwuf.MyApplication;
import com.sufism.markazetasawwuf.R;

import java.util.Map;



public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final int NOTIFICATION_ID = 237;
    private static int value = 0;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            sendNotification(remoteMessage.getData().get(Constants.MESSAGE));
            if(remoteMessage.getData().containsKey(Constants.CATEGORY)) {
                parseMessageData(remoteMessage.getData());
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    static void updateMyActivity(Context context, String category) {

        Intent intent = new Intent(Constants.NEW_PUSH_ALERT);
        //put whatever data you want to send, if any
        intent.putExtra(Constants.CATEGORY, category);
        //send broadcast
        context.sendBroadcast(intent);
    }

    private  void  parseMessageData(Map<String,String> data){
        String ids = data.get(Constants.IDS);
        switch (data.get(Constants.CATEGORY)){
            case Constants.VIDEO_CAT:
                addListToTinyDB(ids,Constants.VIDEO_CAT);
                break;
            case Constants.TALEEM_CAT:
                addListToTinyDB(ids,Constants.TALEEM_CAT);
                break;
            case Constants.AUDIO_CAT:
                addListToTinyDB(ids,Constants.AUDIO_CAT);
                break;
            case Constants.IMAGE_CAT:
                addListToTinyDB(ids,Constants.IMAGE_CAT);
                break;
            case Constants.BOOKS_CAT:
                addListToTinyDB(ids,Constants.BOOKS_CAT);
                break;
            case Constants.UPDATE_CAT:
                MyApplication.getContext().getTinyDb().putBoolean(Constants.UPDATE_CAT,true);
                break;
        }
        updateMyActivity(getApplicationContext(),data.get(Constants.CATEGORY));
    }

    private void addListToTinyDB(String ids, String categoryType){
        if(MyApplication.getContext().getTinyDb().getString(categoryType) != ""){
            String savedids = MyApplication.getContext().getTinyDb().getString(categoryType);
            String newIds = savedids+","+ids;
            MyApplication.getContext().getTinyDb().putString(categoryType,newIds);
        }else{
            MyApplication.getContext().getTinyDb().putString(categoryType,ids);
        }
    }
    // [END receive_message]
    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Style inboxStyle = new NotificationCompat.InboxStyle();
        value ++;
        String _message = messageBody;
        if(value > 1){
            _message = "You have " +value+ " new message.";
        }
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(Constants.APP_NAME)
                .setContentText(_message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setGroupSummary(true).setGroup("test").setStyle(inboxStyle);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
    }
}
