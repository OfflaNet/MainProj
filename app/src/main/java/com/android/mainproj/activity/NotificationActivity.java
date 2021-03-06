package com.android.mainproj.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.mainproj.R;
import com.android.mainproj.log.LogService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationActivity extends AppCompatActivity
{
    private Activity activity;

    private NotificationManager notificationManager;

    private ImageButton btn_noti_back;

    private EditText et_noti_tel;

    private Button btn_noti_normal, btn_noti_extend, btn_noti_inbox, btn_noti_dialog;

    private Button btn_fcm_token;

    private static final int NOTIFICATION_ID = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_noti);

            init();

            setting();

            addListener();
        }
        catch (Exception ex)
        {
            LogService.error(this, ex.getMessage(), ex);
        }
    }

    private void init()
    {
        activity = this;

        btn_noti_back = findViewById(R.id.btn_noti_back);

        et_noti_tel = findViewById(R.id.et_noti_tel);

        btn_noti_normal = findViewById(R.id.btn_noti_normal);

        btn_noti_extend = findViewById(R.id.btn_noti_extend);

        btn_noti_inbox = findViewById(R.id.btn_noti_inbox);

        btn_noti_dialog = findViewById(R.id.btn_noti_dialog);

        btn_fcm_token = findViewById(R.id.btn_fcm_token);
    }

    private void setting()
    {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void addListener()
    {
        btn_noti_back.setOnClickListener(listener_back_click);

        btn_noti_normal.setOnClickListener(listener_notification_normal);

        btn_noti_extend.setOnClickListener(listener_notification_extend);

        btn_noti_inbox.setOnClickListener(listener_notification_inbox);

        btn_noti_dialog.setOnClickListener(listener_notification_dialog);

        btn_fcm_token.setOnClickListener(listener_fcm_token);
    }

    private View.OnClickListener listener_back_click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            finish();
        }
    };

    private View.OnClickListener listener_notification_normal = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            NotificationCompat.Builder builder = getDefaultBuilder();

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    };

    private View.OnClickListener listener_notification_extend = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            NotificationCompat.Builder builder = getDefaultBuilder();

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.steak);
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle(builder);
            bigPictureStyle.setBigContentTitle("????????? ???????????????.");
            bigPictureStyle.setSummaryText("?????? ????????? ??????????????????.");
            bigPictureStyle.bigPicture(bitmap);
            builder.setStyle(bigPictureStyle);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    };

    private View.OnClickListener listener_notification_inbox = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            NotificationCompat.Builder builder = getDefaultBuilder();

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);

            inboxStyle.setSummaryText("??? ??????");
            inboxStyle.addLine("????????????");
            inboxStyle.addLine("?????????");
            inboxStyle.addLine("?????????????????????.");

            builder.setStyle(inboxStyle);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    };

    private View.OnClickListener listener_notification_dialog = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            NotificationCompat.Builder builder = getDefaultBuilder();

            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + et_noti_tel.getText().toString()));
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Intent cancel = new Intent("android.intent.action.NOTIFICATION_CANCEL");
            cancel.setPackage("com.android.mainproj");
            cancel.putExtra("NOTIFICATION_ID", NOTIFICATION_ID);
            PendingIntent cancelIntent = PendingIntent.getBroadcast(activity, 0, cancel, PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentIntent(null);

            builder.addAction(android.R.drawable.star_on, "??????", pendingIntent);
            builder.addAction(android.R.drawable.star_off, "??????", cancelIntent);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    };

    private View.OnClickListener listener_fcm_token = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>()
            {
                @Override
                public void onSuccess(String token)
                {
                    Toast.makeText(activity, token, Toast.LENGTH_SHORT).show();

                    LogService.info(activity, "token : " + token);
                }
            });
        }
    };

    private NotificationCompat.Builder getDefaultBuilder()
    {
        String channelID = "tel_notification_channel";

        // API 26 ?????? ??????????????? ?????? ????????? ????????? ????????? ?????? ??? ?????? ????????? ??????????????? ??????.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if(notificationManager != null && notificationManager.getNotificationChannel(channelID) == null)
            {
                NotificationChannel notificationChannel = new NotificationChannel
                        (
                                channelID,
                                "Tel Notification Channel",
                                NotificationManager.IMPORTANCE_HIGH
                        );

                notificationChannel.setDescription("?????? ?????? ??????");

                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + et_noti_tel.getText().toString()));

        // ?????? ???????????? ?????? ????????? ????????? ??? ?????? ????????? ?????? ????????? ??????
        // PendingIntent??? ?????? ???????????? ?????? ???????????? ??????
        // FLAG_CANCEL_CURRENT : ????????? ?????? ???????????? ????????? ??????????????? ?????? ?????????
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder =  new NotificationCompat.Builder(activity, channelID);

        builder.setSmallIcon(R.mipmap.ic_launcher);

        builder.setContentTitle("??????");

        builder.setDefaults(NotificationCompat.DEFAULT_ALL);

        builder.setContentText("?????? ??? ???????????????.");

        builder.setContentIntent(pendingIntent);

        // ?????? ????????? ?????? ??????
        builder.setAutoCancel(false);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        return builder;
    }
}