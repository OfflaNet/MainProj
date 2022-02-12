package com.android.mainproj.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.mainproj.activity.BroadCastReceiverActivity;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, BroadCastReceiverActivity.class);
        
        // Activity 가 아닌 다른곳에서 화면을 띄우려고 할 때 NEW_TASK 옵션으로
        // 새 창을 실행
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(i);
    }
}
