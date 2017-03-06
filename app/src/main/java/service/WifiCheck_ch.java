package service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.b140414.njupt.checkins.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bmob_table.Checkin_table;
import bmob_table.Leave_table;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.SaveListener;

public class WifiCheck_ch extends Service {

    private WifiManager wifiM;
    private WifiCheckBinder wificheckbinder = new WifiCheckBinder();
    private static final int NOTIFICATION_FLAG = 1;
    private String account;
    private String name;
    private String BSSID;
    private String Key;



        //检测服务的Binder
        public class WifiCheckBinder extends Binder{
            //控制符，用来停止线程
            public boolean quit = false;
            //标识符，用来表示是否扫描到指定的wifi
            public boolean hasscanresult = false;
            //扫描签到检测
            public void ScanCheck(){
                //实例化一个通知
            Context context = getApplicationContext();
            final NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent();
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);
            final Notification notify3 = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.duidui)
                    .setTicker("怼怼签到提示:" + "您的Wifi不见啦！")
                    .setContentTitle("怼怼签到提示")
                    .setContentText("无法扫描到指定Wifi,请检查网络,请勿离场!")
                    .setContentIntent(contentIntent).setNumber(1).build();
            notify3.flags |= Notification.FLAG_AUTO_CANCEL; // FLAG_AUTO_CANCEL表明当通知被用户点击时，通知将被清除。
            //运行一个线程
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    while (!quit) {
                        //扫描附近的wifi
                        wifiM = (WifiManager) getSystemService(WIFI_SERVICE);
                        wifiM.startScan();
                        List<ScanResult> mData= wifiM.getScanResults();
                        //循环查找是否有符合的wifi
                        for (ScanResult a : mData) {
                            if(a.BSSID.equals(BSSID)){
                                hasscanresult = true;
                                break;
                            }
                        }
                        //判断是否离场
                        if (hasscanresult) {

                        } else {
                            //执行一个通知
                            notificationManager.notify(1, notify3);
                            //填写离场信息元组
                            Date date=new Date();
                            SimpleDateFormat sdf=new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                            SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd");
                            final String ltime=sdf.format(date);
                            final String ltime2=sdf2.format(date);
                            Leave_table leave = new Leave_table();
                            leave.setAccount(account);
                            leave.setRealName(name);
                            leave.setLeaveTime(ltime);
                            leave.setLeaveType("中途离场"+ltime2);
                            leave.setBSSID(BSSID);
                            leave.setKey(Key);
                            //上传离场信息到服务器
                            leave.save(WifiCheck_ch.this, new SaveListener(){
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(WifiCheck_ch.this, "中途离场信息已被记录！\n 姓名:"+name+"\n账号:"+account+"\n" +
                                            "时间："+ ltime, Toast.LENGTH_LONG).show();

                                }
                                @Override
                                public void onFailure(int code, String arg0) {
                                    Toast.makeText(WifiCheck_ch.this, "中途离场信息记录失败!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        try {
                            //每次检测完后，标示符重置
                            hasscanresult = false;
                            Thread.sleep(1000*10);    //每1分钟检查一次，为了方面演示，先改为10秒检测一次
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.start();


        }

    }




    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Bundle bundle = (Bundle)intent.getExtras();
        account=bundle.getString("account");
        name = bundle.getString("name");
        BSSID = bundle.getString("BSSID");
        Key = bundle.getString("Key");
        return wificheckbinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }
}
