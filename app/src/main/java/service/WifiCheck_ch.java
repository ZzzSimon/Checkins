package service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;

import com.b140414.njupt.checkins.R;

public class WifiCheck_ch extends Service {
    private boolean quit = false;
    private WifiManager wifiM;
    private WifiCheckBinder wificheckbinder = new WifiCheckBinder();
    private static final int NOTIFICATION_FLAG = 1;

    public class WifiCheckBinder extends Binder{
        public void startCheck(){
            Context context = getApplicationContext();
            final NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent();
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);
           final Notification notify3 = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.duidui)
                    .setTicker("怼怼签到提示:" + "您的Wifi不见啦！")
                    .setContentTitle("怼怼签到提示")
                    .setContentText("Wifi连接已中断,请检查网络,请勿离场!")
                    .setContentIntent(contentIntent).setNumber(1).build();
            notify3.flags |= Notification.FLAG_AUTO_CANCEL; // FLAG_AUTO_CANCEL表明当通知被用户点击时，通知将被清除。
            notificationManager.notify(NOTIFICATION_FLAG, notify3);// 步骤4：通过通知管理器来发起通知。如果id不同，则每click，在status哪里增加一个提示
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    while (!quit) {
                        try {
                            Thread.sleep(1000*60*1);    //每5分钟检查一次
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        wifiM = (WifiManager) getSystemService(WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiM.getConnectionInfo();
                        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
                        if (wifiM.isWifiEnabled() && ipAddress != 0) {

                        } else {
                            notificationManager.notify(1, notify3);
                        }
                    }
                }
            }.start();
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return wificheckbinder;

    }
}
