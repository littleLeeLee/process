package com.kintex.check.recevier;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.elvishew.xlog.XLog;


public class DeviceAdministrator extends DeviceAdminReceiver {

    @Override
    public void onDisabled(Context context, Intent intent) {

        super.onDisabled(context, intent);
        XLog.d("Device admin has been Disabled.");

    }

    @Override
    public void onEnabled(Context context, Intent intent) {

        super.onEnabled(context, intent);
        XLog.d("Device admin has been Enabled.");

    }
}
