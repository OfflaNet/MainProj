package com.android.mainproj.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mainproj.R;
import com.android.mainproj.dialog.PermissionDialog;
import com.android.mainproj.log.LogService;
import com.android.mainproj.receiver.BatteryReceiver;
import com.android.mainproj.receiver.BootReceiver;
import com.android.mainproj.receiver.SMSReceiver;
import com.android.mainproj.receiver.SMSSendReceiver;

/*
브로드캐스트 리시버는 안드로이드에서 발생하는 여러 브로드캐스트(이벤트)를 감지한다.
예를 들어서 부팅이 완료가 되었을 때, 날짜가 변경이 되었을 때 등을 알수있다.
브로드 캐스트의 종류는 다음과 같다.
ACTION_BOOT_COMPLETED : 부팅이 완료되었을 때 발생
ACTION_CAMERA_BUTTON : 카메라 버튼을 클릭했을 때 발생
ACTION_DATE_CHANGED : 날짜가 변경되었을 떄 발생
ACTION_TIME_CHANGED : 시간이 변경되었을 때 발생
ACTION_MEDIA_BUTTON : 미디어 버튼이 클릭되었을 때 발생
ACTION_MEDIA_MOUNTED : 외부 저장 미디어를 추가하였을 때 발생
ACTION_MEDIA_UNMOUNTED : 외부 저장 미디어를 제거하였을 때 발생
ACTION_SCREEN_ON : 화면이 켜졌을 떄 발생
ACTION_SCREEN_OFF : 와면이 꺼졌을 때 발생
ACTION_TIMEZONE_CHANGED : 시간대가 변경되었을 때 발생
브로드 캐스트 리시버는 10초 이내의 작업만을 보장하므로 오랜 시간 동작하는 작업은
별도의 서비스나 스레드로 구현하여야 한다.
*/
public class BroadCastReceiverActivity extends AppCompatActivity
{
    private final int REQUEST_SEND_SMS = 1005;

    private Activity activity;

    private TextView tv_charging_method;

    private TextView tv_charging_status;

    private TextView tv_charging_level;

    private ImageButton btn_receiver_back;

    private Button btn_charging_info;

    private Button btn_boot_auto_run;

    private Button btn_send_message;

    private EditText et_receiver_number;

    private EditText et_send_message;

    private BatteryReceiver batteryReceiver;

    private ActivityResultLauncher<Intent> resultLauncher;

    private SMSSendReceiver smsSendReceiver;

    private SMSReceiver smsReceiver;

    // PendingIntent : Intent를 가지고 있는 클래스로, 기본 목적은 다른 어플리케이션의 권한을 허가하여 가지고 있는
    // Intent를 마치 본인 앱의 프로세스에서 실행하는 것처럼 사용하게 하는 것
    private PendingIntent sendIntent;

    private PendingIntent deliveryIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_broadcast_receiver);

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

        btn_receiver_back = findViewById(R.id.btn_receiver_back);

        tv_charging_method = findViewById(R.id.tv_charging_method);

        tv_charging_status = findViewById(R.id.tv_charging_status);

        tv_charging_level = findViewById(R.id.tv_charging_level);

        btn_charging_info = findViewById(R.id.btn_charging_info);

        btn_boot_auto_run = findViewById(R.id.btn_boot_auto_run);

        et_receiver_number = findViewById(R.id.et_receiver_number);

        et_send_message = findViewById(R.id.et_send_message);

        btn_send_message = findViewById(R.id.btn_send_message);

        batteryReceiver = new BatteryReceiver();

        smsSendReceiver = new SMSSendReceiver();

        smsReceiver = new SMSReceiver();
    }

    private void setting()
    {
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResultCallback);

        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        sendIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMSSendReceiver.ACTION_SEND_COMPLETE), 0);

        deliveryIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMSSendReceiver.ACTION_DELIVERY_COMPLETE), 0);

        registerReceiver(smsSendReceiver, new IntentFilter(SMSSendReceiver.ACTION_SEND_COMPLETE));

        registerReceiver(smsSendReceiver, new IntentFilter(SMSSendReceiver.ACTION_DELIVERY_COMPLETE));

        //registerReceiver(smsReceiver, new IntentFilter(SMSReceiver.ACTION_RECEIVE_COMPLETE));
    }

    private void addListener()
    {
        btn_receiver_back.setOnClickListener(listener_back_click);

        btn_charging_info.setOnClickListener(listener_charging_info);

        btn_boot_auto_run.setOnClickListener(listener_boot_auto_run);

        btn_send_message.setOnClickListener(listener_send_message);
    }

    private View.OnClickListener listener_back_click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            finish();
        }
    };

    private View.OnClickListener listener_charging_info = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            tv_charging_method.setText(batteryReceiver.getPlugged());

            tv_charging_status.setText(batteryReceiver.getStatus());

            tv_charging_level.setText(batteryReceiver.getLevel());
        }
    };

    private View.OnClickListener listener_boot_auto_run = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // SYSTEM_ALERT_WINDOW
            if(Settings.canDrawOverlays(activity) == false)
            {
                PermissionDialog dialog = new PermissionDialog(activity, "다른 앱 위에 그리기");
                dialog.setDialogOnClickListener(new PermissionDialog.OnDialogClickListener()
                {
                    @Override
                    public void onYesClick()
                    {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));

                        resultLauncher.launch(intent);
                    }

                    @Override
                    public void onNoClick()
                    {

                    }
                });

                dialog.show();
            }
            else
            {
                Toast.makeText(activity, "이미 부탕 시 앱 자동 실행 설정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener listener_send_message = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            try
            {
                if
                (
                    checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                )
                {
                    String[] permissions = {Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS};

                    requestPermissions(permissions, REQUEST_SEND_SMS);
                }
                else
                {
                    sendMessage();
                }
            }
            catch (Exception ex)
            {
                LogService.error(activity, ex.getMessage(), ex);
            }

        }
    };

    // SMS 전송 함수
    private void sendMessage()
    {
        String receiver_number = et_receiver_number.getText().toString();

        String message = et_send_message.getText().toString();

        SmsManager sms = SmsManager.getDefault();

        sms.sendTextMessage(receiver_number, null, message, sendIntent, deliveryIntent);
    }

    private ActivityResultCallback<ActivityResult> activityResultCallback = new ActivityResultCallback<ActivityResult>()
    {
        @Override
        public void onActivityResult(ActivityResult result)
        {
            if(Settings.canDrawOverlays(activity) == true)
            {
                Toast.makeText(activity, "부팅시 앱 자동 실행 설정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(activity, "부팅시 앱 자동 실행 설정이 취소되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        BroadcastReceiver[] receivers = {batteryReceiver, smsSendReceiver};

        try
        {
            for(int i = 0; i < receivers.length; i++)
            {
                if (receivers[i] != null)
                {
                    unregisterReceiver(receivers[i]);
                }
            }

        }
        catch (RuntimeException rEx)
        {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_SEND_SMS)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                sendMessage();
            }
            else
            {
                PermissionDialog dialog = new PermissionDialog(activity, "SMS송신");
                dialog.setDialogOnClickListener(new PermissionDialog.OnDialogClickListener() {
                    @Override
                    public void onYesClick()
                    {
                        Intent appDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));

                        appDetail.addCategory(Intent.CATEGORY_DEFAULT);

                        appDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(appDetail);
                    }

                    @Override
                    public void onNoClick()
                    {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }
}