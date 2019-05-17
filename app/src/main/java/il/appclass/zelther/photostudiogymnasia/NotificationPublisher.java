package il.appclass.zelther.photostudiogymnasia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class is used as a BroadcastReceiver for notifications usage. When the user lends item, the system starts an alarm to active this broadcast receiver a week ahead.
 * The class publishes the notification which has been given on intent.
 * @author Itai Zelther
 * @see LendActivity
 */
public class NotificationPublisher extends BroadcastReceiver {

    public static final String NOTIFICATION_ID = "notification-id";
    public static final String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        createChannel(context);
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);

        notificationManager.notify(id, notification);

    }

    /**
     * Creates notifications channel for newer versions of Android.
     * @param context The context given by the receiver.
     */
    private void createChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel rChannel = new NotificationChannel(LendActivity.CHANNEL_ID, "Retrieve Items Reminder Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(rChannel);
        }
    }

}
